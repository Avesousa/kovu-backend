package com.kovu.platform.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fachada sobre el {@link ConnectionPool} con dos formas de ejecutar SQL:
 *
 * <ul>
 *   <li>{@link #execute(ConnectionCallback)}: una sola operación, con el
 *   autocommit por defecto del driver (una query, un insert).</li>
 *   <li>{@link #transactional(ConnectionCallback)}: varias operaciones que
 *   tienen que ser todo-o-nada (autocommit apagado, commit al final, rollback
 *   si algo falla). Ver {@link MigrationRunner} para un ejemplo real.</li>
 * </ul>
 *
 * En ambos casos la conexión se toma del pool y se devuelve siempre, pase
 * lo que pase, en el {@code finally}.
 */
public final class Database {

    private final ConnectionPool pool;

    public Database(ConnectionPool pool) {
        this.pool = pool;
    }

    public <T> T execute(ConnectionCallback<T> action) {
        Connection conn = pool.borrow();
        try {
            return action.doWith(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error de acceso a datos", e);
        } finally {
            pool.release(conn);
        }
    }

    public <T> T transactional(ConnectionCallback<T> action) {
        Connection conn = pool.borrow();
        try {
            conn.setAutoCommit(false);
            T resultado = action.doWith(conn);
            conn.commit();
            return resultado;
        } catch (SQLException e) {
            rollbackSilencioso(conn);
            throw new DataAccessException("Error de acceso a datos en transacción", e);
        } catch (RuntimeException e) {
            // una excepción de negocio (ej. ConflictException) lanzada dentro de la
            // transacción también debe hacer rollback antes de propagarse.
            rollbackSilencioso(conn);
            throw e;
        } finally {
            restaurarAutocommitSilencioso(conn);
            pool.release(conn);
        }
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            // ya estamos manejando un error; no tapamos el original por este.
        }
    }

    private static void restaurarAutocommitSilencioso(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}
