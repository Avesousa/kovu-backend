package com.kovu.service;

import com.kovu.exception.ConflictException;
import com.kovu.exception.NotFoundException;
import com.kovu.exception.ValidationException;
import com.kovu.model.Producto;
import com.kovu.repository.ProductoRepository;

import java.util.List;
import java.util.Map;

/**
 * Orquesta los casos de uso sobre {@link Producto}. Acá viven las reglas
 * que involucran MÁS de una operación de repositorio o que combinan
 * dominio con decisiones de negocio (ej: "no se puede crear un sku
 * duplicado"). Reglas que son inherentes al objeto (sku no vacío) ya las
 * valida el propio record {@link Producto}; este service no las repite.
 */
public final class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Producto crear(Producto candidato) {
        repositorio.buscarPorSku(candidato.sku()).ifPresent(existente -> {
            throw new ConflictException("Ya existe un producto con sku=" + candidato.sku());
        });
        return repositorio.guardar(candidato);
    }

    public Producto obtener(long id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> NotFoundException.producto(id));
    }

    public List<Producto> listar() {
        return repositorio.listar();
    }

    public Producto actualizar(long id, Producto cambios) {
        Producto existente = obtener(id); // valida que exista; si no, NotFoundException
        Producto actualizado = new Producto(
                existente.id(),
                existente.sku(),          // el sku no se puede cambiar por este endpoint
                cambios.nombre(),
                cambios.precioCentavos(),
                cambios.stock(),
                existente.activo(),
                existente.creadoEn()
        );
        return repositorio.actualizar(actualizado);
    }

    public void eliminar(long id) {
        obtener(id); // valida que exista antes de intentar borrar
        repositorio.eliminar(id);
    }

    /** Ejemplo de decremento de stock concurrency-safe; ver el repositorio. */
    public Producto venderUnidades(long id, int cantidad) {
        if (cantidad <= 0) {
            throw new ValidationException(Map.of("cantidad", "debe ser mayor a 0"));
        }
        obtener(id); // 404 si el producto no existe
        boolean alcanzoStock = repositorio.descontarStock(id, cantidad);
        if (!alcanzoStock) {
            throw new ConflictException("Stock insuficiente para el producto id=" + id);
        }
        return repositorio.buscarPorId(id).orElseThrow(() -> NotFoundException.producto(id));
    }
}
