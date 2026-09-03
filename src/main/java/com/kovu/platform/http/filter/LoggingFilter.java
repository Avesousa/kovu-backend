package com.kovu.platform.http.filter;

import com.kovu.platform.http.Filter;
import com.kovu.platform.http.FilterChain;
import com.kovu.platform.http.RequestContext;

import java.lang.System.Logger.Level;

/**
 * Loguea cada request con método, path, status y duración. Corre alrededor
 * de todo el resto de la cadena (incluido el handler), así que el status
 * que loguea es el que efectivamente se escribió en la respuesta.
 */
public final class LoggingFilter implements Filter {

    private static final System.Logger LOG = System.getLogger(LoggingFilter.class.getName());

    @Override
    public void apply(RequestContext ctx, FilterChain chain) throws Exception {
        long inicioNs = System.nanoTime();
        try {
            chain.next(ctx);
        } finally {
            long ms = (System.nanoTime() - inicioNs) / 1_000_000;
            String requestId = ctx.attribute("requestId", String.class);
            LOG.log(Level.INFO, "[%s] %s %s -> %d (%dms)".formatted(
                    requestId, ctx.method(), ctx.path(), ctx.statusCode(), ms));
        }
    }
}
