package com.kovu.platform.http;

/**
 * Representa "el resto de la cadena" desde la perspectiva de un {@link Filter}.
 * Un filtro decide si llama a {@code next(ctx)} (deja pasar la request), y
 * puede ejecutar código antes y después de esa llamada.
 */
@FunctionalInterface
public interface FilterChain {
    void next(RequestContext ctx) throws Exception;
}
