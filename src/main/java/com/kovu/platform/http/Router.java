package com.kovu.platform.http;

import com.kovu.exception.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * El corazón de la capa HTTP. Es el único {@link HttpHandler} que se
 * registra en el {@link com.sun.net.httpserver.HttpServer} (en "/"); todo el
 * ruteo real —match de método + path con parámetros, filtros, manejo de
 * errores— pasa por acá.
 *
 * <p>Registrar una ruta: {@code router.get("/productos/{id}", handler)}.
 * El {@code {id}} se captura como path param y se puede leer con
 * {@code ctx.pathParam("id")}.
 */
public final class Router implements HttpHandler {

    private static final Pattern PARAM_TOKEN = Pattern.compile("\\{(\\w+)}");

    private record Route(HttpMethod method, Pattern pattern, Handler handler) {
    }

    private final List<Route> routes = new ArrayList<>();
    private final List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void get(String path, Handler handler) {
        addRoute(HttpMethod.GET, path, handler);
    }

    public void post(String path, Handler handler) {
        addRoute(HttpMethod.POST, path, handler);
    }

    public void put(String path, Handler handler) {
        addRoute(HttpMethod.PUT, path, handler);
    }

    public void patch(String path, Handler handler) {
        addRoute(HttpMethod.PATCH, path, handler);
    }

    public void delete(String path, Handler handler) {
        addRoute(HttpMethod.DELETE, path, handler);
    }

    public void addRoute(HttpMethod method, String pathPattern, Handler handler) {
        routes.add(new Route(method, compile(pathPattern), handler));
    }

    /** Convierte "/productos/{id}" en el regex "^/productos/(?<id>[^/]+)$". */
    private static Pattern compile(String pathPattern) {
        String regex = PARAM_TOKEN.matcher(pathPattern).replaceAll("(?<$1>[^/]+)");
        return Pattern.compile("^" + regex + "$");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        RequestContext ctx = RequestContext.of(exchange);
        try {
            buildChain().next(ctx);
        } catch (Exception e) {
            ErrorMapper.handle(ctx, e);
        } finally {
            exchange.close();
        }
    }

    /** Encadena los filtros (en orden de registro) y termina en el dispatch a la ruta. */
    private FilterChain buildChain() {
        FilterChain chain = this::matchAndDispatch;
        for (int i = filters.size() - 1; i >= 0; i--) {
            Filter filtro = filters.get(i);
            FilterChain siguiente = chain;
            chain = ctx -> filtro.apply(ctx, siguiente);
        }
        return chain;
    }

    private void matchAndDispatch(RequestContext ctx) throws Exception {
        HttpMethod method = HttpMethod.parse(ctx.method());
        String path = ctx.path();

        for (Route route : routes) {
            if (route.method() != method) {
                continue;
            }
            Matcher matcher = route.pattern().matcher(path);
            if (matcher.matches()) {
                ctx.setPathParams(extractParams(matcher));
                route.handler().handle(ctx);
                return;
            }
        }
        throw new NotFoundException("Ruta no encontrada: " + ctx.method() + " " + path);
    }

    private static Map<String, String> extractParams(Matcher matcher) {
        Map<String, String> params = new java.util.HashMap<>();
        for (String nombre : matcher.namedGroups().keySet()) {
            params.put(nombre, matcher.group(nombre));
        }
        return params;
    }
}
