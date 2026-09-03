package com.kovu.platform.db;

import java.lang.System.Logger.Level;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Pool de conexiones JDBC escrito a mano: nada de HikariCP. La idea es que
 * quede claro qué problema resuelve un pool (abrir una conexión TCP+auth
 * contra MySQL es caro; reusarlas es barato) y por qué necesita ser
 * thread-safe (muchos hilos virtuales piden conexiones "al mismo tiempo").
 *
 * Estrategia: se abren {@code size} conexiones al construir el pool y se
 * reparten con una {@link BlockingQueue}. Si no hay ninguna libre, {@link
 * #borrow()} espera hasta 5 segundos y después falla explícitamente en vez
 * de bloquear para siempre — mejor un error claro que un servidor colgado.
 */
public final class ConnectionPool implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(ConnectionPool.class.getName());
    private static final long TIMEOUT_SEGUNDOS = 5;

    private final BlockingQueue<Connection> disponibles;
    private final String url;
    private final String user;
    private final String password;

    public ConnectionPool(String url, String user, String password, int size) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.disponibles = new LinkedBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            disponibles.add(crearConexion());
        }
        LOG.log(Level.INFO, "Pool de conexiones inicializado con " + size + " conexiones");
    }

    private Connection crearConexion() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar a la base de datos en " + url, e);
        }
    }

    public Connection borrow() {
        try {
            Connection conn = disponibles.poll(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            if (conn == null) {
                throw new IllegalStateException(
                        "Pool de conexiones agotado: no hubo ninguna libre en " + TIMEOUT_SEGUNDOS + "s");
            }
            if (conn.isClosed()) {
                conn = crearConexion(); // se cayó del otro lado; la reemplazamos
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido esperando una conexión del pool", e);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo validar la conexión", e);
        }
    }

    public void release(Connection conn) {
        if (conn == null) {
            return;
        }
        disponibles.offer(conn);
    }

    @Override
    public void close() {
        disponibles.forEach(conn -> {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // estamos cerrando el pool, no hay mucho más para hacer con este error
            }
        });
    }
}
