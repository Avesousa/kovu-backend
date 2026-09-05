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
 *
 * <p>Al arrancar, reintenta la primera conexión con backoff (ver {@link
 * #ARRANQUE_MAX_INTENTOS}): en Docker Compose, {@code depends_on:
 * condition: service_healthy} garantiza que el healthcheck de MySQL ya
 * pasó, pero no que la red entre contenedores esté 100% propagada en ese
 * mismo instante — sin este reintento, el contenedor de la app puede morir
 * por una carrera de unos pocos milisegundos justo al arrancar.
 */
public final class ConnectionPool implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(ConnectionPool.class.getName());
    private static final long TIMEOUT_SEGUNDOS = 5;
    private static final int ARRANQUE_MAX_INTENTOS = 10;
    private static final long ARRANQUE_ESPERA_MS = 1000;

    private final BlockingQueue<Connection> disponibles;
    private final String url;
    private final String user;
    private final String password;

    public ConnectionPool(String url, String user, String password, int size) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.disponibles = new LinkedBlockingQueue<>(size);
        disponibles.add(crearConexionConReintentos());
        for (int i = 1; i < size; i++) {
            disponibles.add(crearConexion());
        }
        LOG.log(Level.INFO, "Pool de conexiones inicializado con " + size + " conexiones");
    }

    /** Solo se usa para la primera conexión al arrancar; ver el porqué en la doc de la clase. */
    private Connection crearConexionConReintentos() {
        for (int intento = 1; intento <= ARRANQUE_MAX_INTENTOS; intento++) {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                if (intento == ARRANQUE_MAX_INTENTOS) {
                    throw new IllegalStateException(
                            "No se pudo conectar a la base de datos en " + url +
                                    " tras " + ARRANQUE_MAX_INTENTOS + " intentos", e);
                }
                LOG.log(Level.WARNING, "Intento " + intento + "/" + ARRANQUE_MAX_INTENTOS +
                        " de conexión a " + url + " falló, reintentando: " + e.getMessage());
                dormir(ARRANQUE_ESPERA_MS);
            }
        }
        throw new IllegalStateException("No se alcanzó nunca a conectar a " + url); // inalcanzable
    }

    private Connection crearConexion() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar a la base de datos en " + url, e);
        }
    }

    private static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrumpido esperando para reintentar la conexión", e);
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
