package com.kovu.repository;

import com.kovu.model.Producto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Doble de test de {@link ProductoRepository}: vive en memoria, sin JDBC ni
 * MySQL. Es la pieza clave que demuestra por qué el service depende de la
 * INTERFAZ y no de {@code JdbcProductoRepository} directamente — gracias a
 * eso, {@code ProductoServiceTest} corre en milisegundos sin levantar nada.
 *
 * No implementa concurrencia real (no hace falta: es un test double), pero
 * sí replica la semántica atómica de {@link #descontarStock} para que los
 * tests de esa regla de negocio tengan sentido.
 */
public final class InMemoryProductoRepository implements ProductoRepository {

    private final Map<Long, Producto> datos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    @Override
    public Producto guardar(Producto producto) {
        long id = secuencia.getAndIncrement();
        Producto guardado = producto.conId(id);
        datos.put(id, guardado);
        return guardado;
    }

    @Override
    public Optional<Producto> buscarPorId(long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Optional<Producto> buscarPorSku(String sku) {
        return datos.values().stream().filter(p -> p.sku().equals(sku)).findFirst();
    }

    @Override
    public List<Producto> listar() {
        return List.copyOf(datos.values());
    }

    @Override
    public Producto actualizar(Producto producto) {
        datos.put(producto.id(), producto);
        return producto;
    }

    @Override
    public boolean eliminar(long id) {
        return datos.remove(id) != null;
    }

    @Override
    public boolean descontarStock(long id, int cantidad) {
        Producto actual = datos.get(id);
        if (actual == null || actual.stock() < cantidad) {
            return false;
        }
        datos.put(id, new Producto(actual.id(), actual.sku(), actual.nombre(),
                actual.precioCentavos(), actual.stock() - cantidad, actual.activo(), actual.creadoEn()));
        return true;
    }
}
