package com.kovu.exception;

/** El recurso pedido no existe. El ErrorMapper la traduce a HTTP 404. */
public final class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException producto(long id) {
        return new NotFoundException("Producto no encontrado: id=" + id);
    }
}
