package com.kovu.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductoTest {

    @Test
    void Producto_sinSku_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, null, "Producto de prueba", 2000, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar como SKU un null");
    }

    @Test
    void Producto_conSkuVacio_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, "", "Producto de prueba", 2000, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar como SKU un texto vacío");

        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, " ", "Producto de prueba", 2000, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar como SKU solo espacios");
    }

    @Test
    void Producto_sinNombre_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, "SKU001", null, 2000, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar como nombre un null");
    }

    @Test
    void Producto_conNombreVacio_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, "SKU001", "", 2000, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar como nombre un texto vacío");

        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, "SKU001", " ", 2000, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar como nombre solo espacios");
    }

    @Test
    void Producto_conPrecioNegativo_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, "SKU001", "Producto de prueba", -1, 10, true, Instant.now());
        }, "Se espera que retorne un error al enviar un precio negativo");
    }

    @Test
    void Producto_conStockNegativo_debeRetornarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Producto(null, "SKU001", "Producto de prueba", 2000, -1, true, Instant.now());
        }, "Se espera que retorne un error al enviar un stock negativo");
    }

    @Test
    void Producto_debeRetornarProducto() {
        Instant creadoEn = Instant.now();

        Producto producto = new Producto(
                1L,
                "SKU001",
                "Producto de prueba",
                2000,
                10,
                true,
                creadoEn
        );

        assertEquals(1L, producto.id(),
                "Se espera que el producto retorne el ID correcto"
        );

        assertEquals("SKU001", producto.sku(),
                "Se espera que el producto retorne el SKU correcto"
        );

        assertEquals("Producto de prueba", producto.nombre(),
                "Se espera que el producto retorne el nombre correcto"
        );

        assertEquals(2000, producto.precioCentavos(),
                "Se espera que el producto retorne el precio correcto"
        );

        assertEquals(10, producto.stock(),
                "Se espera que el producto retorne el stock correcto"
        );

        assertTrue(producto.activo(),
                "Se espera que el producto esté activo"
        );

        assertEquals(creadoEn, producto.creadoEn(),
                "Se espera que el producto retorne correctamente la fecha de creación"
        );
    }

    @Test
    void Producto_nuevo_debeCrearProductoSinId() {
        Producto producto = Producto.nuevo(
                "SKU001",
                "Producto de prueba",
                2000,
                10
        );

        assertEquals(null, producto.id(),
                "Se espera que un producto nuevo no tenga ID"
        );

        assertEquals("SKU001", producto.sku(),
                "Se espera que el producto nuevo tenga el SKU correcto"
        );

        assertEquals("Producto de prueba", producto.nombre(),
                "Se espera que el producto nuevo tenga el nombre correcto"
        );

        assertEquals(2000, producto.precioCentavos(),
                "Se espera que el producto nuevo tenga el precio correcto"
        );

        assertEquals(10, producto.stock(),
                "Se espera que el producto nuevo tenga el stock correcto"
        );

        assertTrue(producto.activo(),
                "Se espera que un producto nuevo esté activo"
        );
    }

    @Test
    void Producto_conId_debeRetornarProductoConId() {
        Producto producto = Producto.nuevo(
                "SKU001",
                "Producto de prueba",
                2000,
                10
        );

        Producto productoConId = producto.conId(1L);

        assertEquals(1L, productoConId.id(),
                "Se espera que el producto retorne el ID asignado"
        );

        assertEquals(producto.sku(), productoConId.sku(),
                "Se espera que conserve el mismo SKU"
        );

        assertEquals(producto.nombre(), productoConId.nombre(),
                "Se espera que conserve el mismo nombre"
        );

        assertEquals(producto.precioCentavos(), productoConId.precioCentavos(),
                "Se espera que conserve el mismo precio"
        );

        assertEquals(producto.stock(), productoConId.stock(),
                "Se espera que conserve el mismo stock"
        );

        assertEquals(producto.activo(), productoConId.activo(),
                "Se espera que conserve el mismo estado activo"
        );
    }
}