package com.kovu.model;

public record Movimiento(
        Long id,
        Producto producto,
        Deposito ubicacionOrigen,
        Deposito ubicacionDestino,
        Integer cantidad,
        String tipoMovimiento,
        String fecha
) {

   public Movimiento {

        if (id == null) {
            throw new IllegalArgumentException("id no puede ser nulo");
        }

        if (producto == null) {
            throw new IllegalArgumentException("producto no puede ser nulo");
        }

        if (ubicacionOrigen == null) {
            throw new IllegalArgumentException("ubicacionOrigen no puede ser nula");
        }

        if (ubicacionDestino == null) {
            throw new IllegalArgumentException("ubicacionDestino no puede ser nula");
        }

        if (ubicacionOrigen.equals(ubicacionDestino)) {
            throw new IllegalArgumentException("Los depósitos no deben ser iguales");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("cantidad debe ser mayor que cero");
        }

        if (tipoMovimiento == null || tipoMovimiento.isBlank()) {
            throw new IllegalArgumentException("tipoMovimiento no puede ser nulo o vacío");
        }

        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("fecha no puede ser nula o vacía");
        }

    }

}