package com.kovu.model;

import java.time.Instant;

/**
 * Entidad de dominio. Es un record a propósito: inmutable (cualquier
 * "cambio" crea una instancia nueva, ver {@link #conId}) y el constructor
 * compacto valida los invariantes básicos ANTES de que exista un objeto
 * inválido — no hace falta un validador separado para reglas que son
 * inherentes al concepto "Producto" (sku vacío nunca es válido, sin
 * importar quién lo construya: el service, un test, la capa JDBC).
 *
 * <p>{@code precioCentavos} en vez de un {@code double}: la plata nunca se
 * modela con punto flotante (errores de redondeo). Se guardan centavos como
 * entero; formatear a "$123.45" es un problema de presentación, no de
 * dominio.
 */
public record Producto(
        Long id,
        String sku,
        String nombre,
        long precioCentavos,
        int stock,
        boolean activo,
        Instant creadoEn
) {

    public Producto {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre no puede estar vacío");
        }
        if (precioCentavos < 0) {
            throw new IllegalArgumentException("precioCentavos no puede ser negativo");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock no puede ser negativo");
        }
    }

    /** Fábrica para un producto que todavía no existe en la base (sin id). */
    public static Producto nuevo(String sku, String nombre, long precioCentavos, int stock) {
        return new Producto(null, sku, nombre, precioCentavos, stock, true, Instant.now());
    }

    /** Devuelve una copia con id asignado, tal como vuelve de un INSERT. */
    public Producto conId(long id) {
        return new Producto(id, sku, nombre, precioCentavos, stock, activo, creadoEn);
    }
}
