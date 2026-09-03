package com.kovu.mapper;

import com.kovu.dto.ProductoRequest;
import com.kovu.dto.ProductoResponse;
import com.kovu.model.Producto;

import java.util.List;

/**
 * Traduce entre el mundo del dominio ({@link Producto}) y el mundo del
 * contrato HTTP (DTOs). Es la única clase que sabe que "creadoEn" en el
 * dominio es un {@code Instant} y en la respuesta es un {@code String}.
 */
public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static Producto aModelo(ProductoRequest request) {
        return Producto.nuevo(request.sku(), request.nombre(), request.precioCentavos(), request.stock());
    }

    public static ProductoResponse aRespuesta(Producto producto) {
        return new ProductoResponse(
                producto.id(),
                producto.sku(),
                producto.nombre(),
                producto.precioCentavos(),
                producto.stock(),
                producto.activo(),
                producto.creadoEn().toString()
        );
    }

    public static List<ProductoResponse> aRespuesta(List<Producto> productos) {
        return productos.stream().map(ProductoMapper::aRespuesta).toList();
    }
}
