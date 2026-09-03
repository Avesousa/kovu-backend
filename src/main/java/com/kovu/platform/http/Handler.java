package com.kovu.platform.http;

/**
 * Un handler atiende una ruta ya resuelta: recibe el contexto de la request
 * (con los path params ya extraídos) y escribe la respuesta.
 *
 * Cualquier excepción que lance (de negocio o inesperada) la captura el
 * {@link Router} y la traduce a una respuesta HTTP a través de {@link ErrorMapper}.
 * El handler nunca necesita hacer try/catch para eso.
 */
@FunctionalInterface
public interface Handler {
    void handle(RequestContext ctx) throws Exception;
}
