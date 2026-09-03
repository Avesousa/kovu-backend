package com.kovu.dto;

/**
 * Forma del JSON que el servidor devuelve. {@code creadoEn} es String
 * (ISO-8601, ver {@link com.kovu.mapper.ProductoMapper}) y no {@code
 * java.time.Instant}: así Jackson lo serializa sin necesitar el módulo
 * jsr310, que es la única razón por la que se evita acá.
 */
public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        long precioCentavos,
        int stock,
        boolean activo,
        String creadoEn
) {
}
