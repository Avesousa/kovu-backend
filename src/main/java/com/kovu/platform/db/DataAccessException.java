package com.kovu.platform.db;

/**
 * Envuelve cualquier {@link java.sql.SQLException} inesperada (la conexión
 * se cayó, un timeout, un error de sintaxis SQL que no debería llegar a
 * producción...). No es una excepción de negocio: el {@code ErrorMapper} no
 * la conoce y por eso cae en el 500 genérico, con el stacktrace completo en
 * el log del servidor para poder diagnosticarla.
 */
public final class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
