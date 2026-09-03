package com.kovu.repository;

import com.kovu.model.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (en el sentido hexagonal, aunque no hagamos todo el hexágono):
 * el service depende de esta interfaz, nunca de la implementación JDBC.
 *
 * <p>Esto es lo que permite, en los tests de {@link com.kovu.service.ProductoService},
 * usar un repositorio en memoria en vez de arrancar MySQL — el service ni
 * se entera de la diferencia.
 */
public interface ProductoRepository {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(long id);

    Optional<Producto> buscarPorSku(String sku);

    List<Producto> listar();

    Producto actualizar(Producto producto);

    boolean eliminar(long id);

    /**
     * Descuenta stock de forma atómica (un solo UPDATE con la condición
     * {@code stock >= cantidad} en el WHERE) y devuelve si alcanzó el stock.
     * Ver {@link com.kovu.repository.jdbc.JdbcProductoRepository#descontarStock}
     * para la razón por la que NO se hace como "leer stock, restar en Java,
     * escribir stock".
     */
    boolean descontarStock(long id, int cantidad);
}
