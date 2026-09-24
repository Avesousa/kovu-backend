package com.kovu.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MovimientoTest {
        
@Test
    void movimiento_sinId_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () -> 
                new Movimiento(null, null, null, null, 10, "ENTRADA", Instant.now().toString()),
                "Se espera que retorne un error al enviar como ID un null"
        );
    }

    @Test
    void movimiento_sinProducto_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,10,"ENTRADA",Instant.now().toString()),
                "Se espera que retorne un error al enviar como producto un null"
        );
    }

    @Test
    void movimiento_sinCantidad_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,null,"ENTRADA",Instant.now().toString()),
                "Se espera que retorne un error al enviar como cantidad un null"
        );
    }

    @Test
    void movimiento_conCantidadCero_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,0,"ENTRADA",Instant.now().toString()),
                "Se espera que retorne un error al enviar como cantidad cero"
        );
    }

    @Test
    void movimiento_conCantidadNegativa_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,-10,"ENTRADA",Instant.now().toString()),
                "Se espera que retorne un error al enviar como cantidad un número negativo"
        );
    }

    @Test
    void movimiento_sinTipoMovimiento_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,10,null,Instant.now().toString()),
                "Se espera que retorne un error al enviar como tipo de movimiento un null"
        );
    }

    @Test
    void movimiento_conTipoMovimientoVacio_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,10,"",Instant.now().toString()),
                "Se espera que retorne un error al enviar como tipo de movimiento un texto vacío"
        );
    }

    @Test
    void movimiento_sinFecha_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,10,"ENTRADA",null),
                "Se espera que retorne un error al enviar como fecha un null"
        );
    }

    @Test
    void movimiento_conFechaVacia_debeRetornarError() {

        assertThrows(IllegalArgumentException.class, () ->
                new Movimiento(1L,null,null,null,10,"ENTRADA","" ),
                "Se espera que retorne un error al enviar como fecha un texto vacío"
        );
    }
}