package com.kovu.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DepositoTest {
    @Test 
    void depositoSinIdDebeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(null, "Am", null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como id un null");
    }

    @Test 
    void depositoSinNombreDebeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(1L, null, null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como id un null");
    }

    @Test 
    void depositoConNombreVacioDebeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(1L, "", null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como id un null");

        assertThrows(IllegalArgumentException.class, () -> {
            new Deposito(1L, " ", null,"Caracas",true);
        }, "Se espera que retorne un error al enviar como id un null");
    }
    //  Deposito deposito = new Deposito(1L, "Am", null,"Caracas",true);
}