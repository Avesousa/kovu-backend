package com.kovu.service;

import com.kovu.exception.ConflictException;
import com.kovu.exception.NotFoundException;
import com.kovu.exception.ValidationException;
import com.kovu.model.Producto;
import com.kovu.repository.InMemoryProductoRepository;
import com.kovu.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests del service usando {@link InMemoryProductoRepository}: ninguno de
 * estos tests toca MySQL. Prueban reglas de negocio (sku duplicado, stock
 * insuficiente), no SQL — eso se prueba en JdbcProductoRepositoryTest.
 */
class ProductoServiceTest {

    private ProductoRepository repositorio;
    private ProductoService servicio;

    @BeforeEach
    void setUp() {
        repositorio = new InMemoryProductoRepository();
        servicio = new ProductoService(repositorio);
    }

    @Test
    void crea_un_producto_nuevo() {
        Producto creado = servicio.crear(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        assertThat(creado.id()).isNotNull();
        assertThat(creado.sku()).isEqualTo("SKU-1");
        assertThat(creado.activo()).isTrue();
    }

    @Test
    void rechaza_un_sku_duplicado_con_conflict() {
        servicio.crear(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        assertThatThrownBy(() -> servicio.crear(Producto.nuevo("SKU-1", "Otro mate", 100, 1)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SKU-1");
    }

    @Test
    void obtener_lanza_not_found_si_no_existe() {
        assertThatThrownBy(() -> servicio.obtener(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void actualizar_conserva_el_sku_original() {
        Producto creado = servicio.crear(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        Producto actualizado = servicio.actualizar(creado.id(),
                Producto.nuevo("SKU-DISTINTO", "Mate imperial", 700000, 5));

        assertThat(actualizado.sku()).isEqualTo("SKU-1");
        assertThat(actualizado.nombre()).isEqualTo("Mate imperial");
        assertThat(actualizado.precioCentavos()).isEqualTo(700000);
    }

    @Test
    void eliminar_lanza_not_found_si_no_existe() {
        assertThatThrownBy(() -> servicio.eliminar(999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void vender_descuenta_stock_cuando_alcanza() {
        Producto creado = servicio.crear(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        Producto actualizado = servicio.venderUnidades(creado.id(), 3);

        assertThat(actualizado.stock()).isEqualTo(7);
    }

    @Test
    void vender_lanza_conflict_si_no_alcanza_el_stock() {
        Producto creado = servicio.crear(Producto.nuevo("SKU-1", "Mate", 500000, 2));

        assertThatThrownBy(() -> servicio.venderUnidades(creado.id(), 5))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void vender_rechaza_cantidad_no_positiva() {
        Producto creado = servicio.crear(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        assertThatThrownBy(() -> servicio.venderUnidades(creado.id(), 0))
                .isInstanceOf(ValidationException.class);
    }
}
