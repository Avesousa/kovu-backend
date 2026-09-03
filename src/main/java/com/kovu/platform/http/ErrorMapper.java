package com.kovu.platform.http;

import com.kovu.exception.ConflictException;
import com.kovu.exception.NotFoundException;
import com.kovu.exception.ValidationException;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.Map;

/**
 * Traduce cualquier excepción que se escape de un handler o filtro a una
 * respuesta HTTP con forma consistente. Es el único lugar del código que
 * conoce la relación "tipo de excepción de negocio -&gt; código HTTP".
 *
 * El {@code switch} con pattern matching sobre los subtipos de {@code
 * AppException} (sellada en {@link com.kovu.exception.AppException}) es a
 * propósito: si mañana se agrega un cuarto tipo de excepción de negocio, el
 * compilador obliga a decidir qué código HTTP le corresponde en vez de que
 * caiga silenciosamente en el 500 genérico... salvo que se olvide agregar
 * el {@code case} acá, que es exactamente el punto de discusión en clase.
 */
public final class ErrorMapper {

    private static final System.Logger LOG = System.getLogger(ErrorMapper.class.getName());

    private ErrorMapper() {
    }

    public record ErrorBody(String codigo, String mensaje, Map<String, String> detalles) {
    }

    public static void handle(RequestContext ctx, Throwable ex) {
        int status;
        ErrorBody body;

        switch (ex) {
            case NotFoundException e -> {
                status = 404;
                body = new ErrorBody("NOT_FOUND", e.getMessage(), null);
            }
            case ValidationException e -> {
                status = 422;
                body = new ErrorBody("VALIDATION_ERROR", e.getMessage(), e.errores());
            }
            case ConflictException e -> {
                status = 409;
                body = new ErrorBody("CONFLICT", e.getMessage(), null);
            }
            default -> {
                status = 500;
                body = new ErrorBody("INTERNAL_ERROR", "Ocurrió un error interno", null);
                LOG.log(Level.ERROR, "Error no controlado en " + ctx.method() + " " + ctx.path(), ex);
            }
        }

        if (status != 500) {
            LOG.log(Level.WARNING, "%s %s -> %d: %s".formatted(ctx.method(), ctx.path(), status, ex.getMessage()));
        }

        try {
            ctx.json(status, body);
        } catch (IOException io) {
            LOG.log(Level.ERROR, "No se pudo escribir la respuesta de error", io);
        }
    }
}
