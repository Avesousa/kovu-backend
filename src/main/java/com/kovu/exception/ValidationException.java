package com.kovu.exception;

import java.util.Map;

/**
 * Los datos de entrada no son válidos. El ErrorMapper la traduce a HTTP 422
 * e incluye {@link #errores()} (campo -&gt; motivo) en el body de la respuesta.
 */
public final class ValidationException extends AppException {

    private final Map<String, String> errores;

    public ValidationException(Map<String, String> errores) {
        super("Datos inválidos: " + errores);
        this.errores = Map.copyOf(errores);
    }

    public Map<String, String> errores() {
        return errores;
    }
}
