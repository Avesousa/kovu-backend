package com.kovu.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MovimientoTest {

    @Test
    void movimiento_sinId_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(null, null, null, null, 10, "ENTRADA", Instant.now().toString())
        );
    }

    @Test
    void movimiento_sinProducto_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        10,
                        "ENTRADA",
                        Instant.now().toString()
                )
        );
    }

    @Test
    void movimiento_sinCantidad_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        null,
                        "ENTRADA",
                        Instant.now().toString()
                )
        );
    }

    @Test
    void movimiento_conCantidadCero_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        0,
                        "ENTRADA",
                        Instant.now().toString()
                )
        );
    }

    @Test
    void movimiento_conCantidadNegativa_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        -10,
                        "ENTRADA",
                        Instant.now().toString()
                )
        );
    }

    @Test
    void movimiento_sinTipoMovimiento_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        10,
                        null,
                        Instant.now().toString()
                )
        );
    }

    @Test
    void movimiento_conTipoMovimientoVacio_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        10,
                        "",
                        Instant.now().toString()
                )
        );
    }

    @Test
    void movimiento_sinFecha_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        10,
                        "ENTRADA",
                        null
                )
        );
    }

    @Test
    void movimiento_conFechaVacia_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(
                        1L,
                        null,
                        null,
                        null,
                        10,
                        "ENTRADA",
                        ""
                )
        );
    }
}