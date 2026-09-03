package com.kovu.dto;

/**
 * Forma del JSON que el cliente envía para crear/actualizar un producto.
 * Deliberadamente separado de {@link com.kovu.model.Producto}: el contrato
 * HTTP puede cambiar (renombrar un campo, aceptar uno nuevo) sin tocar el
 * dominio, y viceversa. No tiene validación propia: eso es responsabilidad
 * del controller antes de mapear a dominio.
 */
public record ProductoRequest(
        String sku,
        String nombre,
        long precioCentavos,
        int stock
) {
}
