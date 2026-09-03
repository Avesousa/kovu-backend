package com.kovu.dto;

/** Body de POST /productos/{id}/venta. */
public record VentaRequest(int cantidad) {
}
