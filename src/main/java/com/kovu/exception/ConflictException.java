package com.kovu.exception;

/**
 * La operación choca con el estado actual de los datos (sku duplicado,
 * stock insuficiente, etc). El ErrorMapper la traduce a HTTP 409.
 */
public final class ConflictException extends AppException {

    public ConflictException(String message) {
        super(message);
    }
}
