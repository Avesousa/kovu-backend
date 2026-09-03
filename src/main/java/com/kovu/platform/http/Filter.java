package com.kovu.platform.http;

/**
 * Un filtro se ejecuta alrededor de toda la cadena de ruteo: antes de que
 * cualquier handler corra y después de que termine (incluso si lanzó una
 * excepción, si el filtro usa try/finally). Sirve para comportamiento
 * transversal: logging, request-id, autenticación, medición de tiempos.
 *
 * <p>No confundir con {@code com.sun.net.httpserver.Filter} del JDK: esta es
 * nuestra propia abstracción, más simple, pensada para encadenarse con
 * {@link Router#addFilter(Filter)} en el orden en que se registran.
 */
public interface Filter {
    void apply(RequestContext ctx, FilterChain chain) throws Exception;
}
