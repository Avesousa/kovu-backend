package com.kovu.platform.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Envuelve el {@link HttpExchange} crudo del JDK y le suma lo que un handler
 * necesita a diario: leer el body como objeto, leer query/path params,
 * escribir una respuesta JSON, y guardar datos entre filtros (ej. request id).
 *
 * Instancia única por request, mutable a propósito: el {@link Router} le
 * inyecta los path params una vez que hace el match de la ruta.
 */
public final class RequestContext {

    private final HttpExchange exchange;
    private final Map<String, String> queryParams;
    private final Map<String, Object> attributes = new HashMap<>();
    private Map<String, String> pathParams = Map.of();
    private int statusCode = 0;

    private RequestContext(HttpExchange exchange, Map<String, String> queryParams) {
        this.exchange = exchange;
        this.queryParams = queryParams;
    }

    static RequestContext of(HttpExchange exchange) {
        return new RequestContext(exchange, parseQuery(exchange.getRequestURI().getRawQuery()));
    }

    public String method() {
        return exchange.getRequestMethod();
    }

    public String path() {
        return exchange.getRequestURI().getPath();
    }

    // --- path params (los llena el Router al hacer match de la ruta) ---

    void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }

    public String pathParam(String name) {
        return pathParams.get(name);
    }

    // --- query params: /productos?activo=true ---

    public String queryParam(String name) {
        return queryParams.get(name);
    }

    public String queryParam(String name, String valorPorDefecto) {
        return queryParams.getOrDefault(name, valorPorDefecto);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }
        Map<String, String> params = new HashMap<>();
        for (String par : rawQuery.split("&")) {
            int igual = par.indexOf('=');
            String clave = igual >= 0 ? par.substring(0, igual) : par;
            String valor = igual >= 0 ? par.substring(igual + 1) : "";
            params.put(decode(clave), decode(valor));
        }
        return params;
    }

    private static String decode(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    // --- atributos entre filtros (ej. requestId) ---

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T attribute(String key, Class<T> type) {
        return (T) attributes.get(key);
    }

    // --- body de la request ---

    public <T> T body(Class<T> type) throws IOException {
        return Json.mapper().readValue(exchange.getRequestBody(), type);
    }

    // --- escritura de la respuesta ---

    public void json(int status, Object body) throws IOException {
        byte[] bytes = Json.mapper().writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
        this.statusCode = status;
    }

    public void noContent(int status) {
        try {
            exchange.sendResponseHeaders(status, -1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.statusCode = status;
    }

    /** Código de estado ya escrito en la respuesta (0 si todavía no se escribió nada). */
    public int statusCode() {
        return statusCode;
    }
}
