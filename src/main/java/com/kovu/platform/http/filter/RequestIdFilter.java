package com.kovu.platform.http.filter;

import com.kovu.platform.http.Filter;
import com.kovu.platform.http.FilterChain;
import com.kovu.platform.http.RequestContext;

import java.util.UUID;

/**
 * Genera un id único por request y lo deja disponible como atributo
 * ("requestId") para que otros filtros/handlers lo usen, por ejemplo para
 * correlacionar logs de una misma request. Debe registrarse antes que
 * {@link LoggingFilter}.
 */
public final class RequestIdFilter implements Filter {

    @Override
    public void apply(RequestContext ctx, FilterChain chain) throws Exception {
        ctx.setAttribute("requestId", UUID.randomUUID().toString());
        chain.next(ctx);
    }
}
