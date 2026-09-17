package com.kovu.model;

// se crea el modelo deposito 
public record Deposito(
        Long id,
        String nombre,
        Deposito depositoPadre,
        String direccion,
        boolean activo
) {

    // se validan las reglas
    public Deposito {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser nulo");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre no puede ser nulo o vacío");
        }
    }
}