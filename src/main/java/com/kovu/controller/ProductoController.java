package com.kovu.controller;

import com.kovu.dto.ProductoRequest;
import com.kovu.dto.ProductoResponse;
import com.kovu.dto.VentaRequest;
import com.kovu.exception.ValidationException;
import com.kovu.mapper.ProductoMapper;
import com.kovu.model.Producto;
import com.kovu.platform.http.RequestContext;
import com.kovu.platform.http.Router;
import com.kovu.service.ProductoService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adaptador HTTP para {@link ProductoService}: recibe la request, valida la
 * FORMA de los datos de entrada (que existan, que no sean negativos —
 * validación "sintáctica"), delega la lógica de negocio al service, y
 * mapea el resultado a un DTO de respuesta. No hay SQL ni reglas de
 * negocio acá.
 */
public final class ProductoController {

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    public void registrarRutas(Router router) {
        router.get("/productos", this::listar);
        router.get("/productos/{id}", this::obtener);
        router.post("/productos", this::crear);
        router.put("/productos/{id}", this::actualizar);
        router.delete("/productos/{id}", this::eliminar);
        router.post("/productos/{id}/venta", this::vender);
    }

    private void listar(RequestContext ctx) throws Exception {
        ctx.json(200, ProductoMapper.aRespuesta(servicio.listar()));
    }

    private void obtener(RequestContext ctx) throws Exception {
        Producto producto = servicio.obtener(idDesdePath(ctx));
        ctx.json(200, ProductoMapper.aRespuesta(producto));
    }

    private void crear(RequestContext ctx) throws Exception {
        ProductoRequest cuerpo = ctx.body(ProductoRequest.class);
        validar(cuerpo);
        Producto creado = servicio.crear(ProductoMapper.aModelo(cuerpo));
        ctx.json(201, ProductoMapper.aRespuesta(creado));
    }

    private void actualizar(RequestContext ctx) throws Exception {
        long id = idDesdePath(ctx);
        ProductoRequest cuerpo = ctx.body(ProductoRequest.class);
        validar(cuerpo);
        Producto actualizado = servicio.actualizar(id, ProductoMapper.aModelo(cuerpo));
        ctx.json(200, ProductoMapper.aRespuesta(actualizado));
    }

    private void eliminar(RequestContext ctx) throws Exception {
        servicio.eliminar(idDesdePath(ctx));
        ctx.noContent(204);
    }

    private void vender(RequestContext ctx) throws Exception {
        long id = idDesdePath(ctx);
        VentaRequest cuerpo = ctx.body(VentaRequest.class);
        Producto actualizado = servicio.venderUnidades(id, cuerpo.cantidad());
        ctx.json(200, ProductoMapper.aRespuesta(actualizado));
    }

    private long idDesdePath(RequestContext ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new ValidationException(Map.of("id", "debe ser numérico"));
        }
    }

    private void validar(ProductoRequest r) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (r.sku() == null || r.sku().isBlank()) {
            errores.put("sku", "es obligatorio");
        }
        if (r.nombre() == null || r.nombre().isBlank()) {
            errores.put("nombre", "es obligatorio");
        }
        if (r.precioCentavos() < 0) {
            errores.put("precioCentavos", "no puede ser negativo");
        }
        if (r.stock() < 0) {
            errores.put("stock", "no puede ser negativo");
        }
        if (!errores.isEmpty()) {
            throw new ValidationException(errores);
        }
    }
}
