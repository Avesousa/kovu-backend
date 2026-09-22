package com.kovu.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositoTest {
    @Test 
    void Deposito_sinId_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(null, "Am", null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como ID un null");
    }

    @Test 
    void Deposito_sinNombre_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(1L, null, null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como nombre un null");
    }

    @Test 
    void Deposito_conNombreVacio_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(1L, "", null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como nombre un texto vacío");

        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(1L, " ", null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como nombre solo espacios");
    }
    
    @Test
    void Deposito_debeRetornaDeposito() {
        Deposito deposito = new Deposito(1L, "Deposito principal", null,"Caracas",true);
        assertEquals(1L, deposito.id(),
        "Se espera que el depósito retorne el ID correcto"
    );

        assertEquals("Deposito principal", deposito.nombre(),
        "Se espera que el dpósito retorne el nombre correcto"
    );

        assertEquals("Caracas", deposito.direccion(),
        "Se espera que el depósito retorne la dirección correcta"
    );

        assertTrue(deposito.activo(),
        "El deposito debe retornar activo"
    );

        assertNull(deposito.depositoPadre(),
        "Se espera que el depósito principal no tenga un depósito padre"
    );
    }

    @Test
    void Deposito_conOtroDeposito_debeRetornarUnDepositoPadre() {
        Deposito depositoPadre = new Deposito(1L, "Deposito Principal Padre", null,"Caracas",true);
        Deposito depositoHijo = new Deposito(1L, "Deposito secundario hijo", depositoPadre,"Caracas",true);
        assertNotNull(depositoHijo.depositoPadre(),
        "Se espera que el depósito hijo tenga un depósito padre"
    );

        assertEquals(depositoPadre.nombre(), depositoHijo.depositoPadre().nombre(),
        "Se espera que el depósito hijo tenga el mismo nombre que su depósito padre"
    );

        assertEquals(depositoPadre.id(), depositoHijo.depositoPadre().id(),
       "Se espera que el depósito hijo tenga el mismo ID que su depósito padre"
    );
    }
}