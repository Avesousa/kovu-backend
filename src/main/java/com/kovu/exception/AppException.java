package com.kovu.exception;

/**
 * Raíz sellada de las excepciones de negocio. "Sellada" significa que la
 * lista de subtipos posibles es fija y conocida acá mismo (permits): quien
 * escribe un {@code switch} pattern-matching sobre este tipo (ver
 * {@link com.kovu.platform.http.ErrorMapper}) puede confiar en que no va a
 * aparecer un cuarto tipo por sorpresa sin que el compilador lo señale.
 *
 * <p>Deliberadamente NO es una excepción checked: en un service o
 * repositorio, forzar a declarar "throws NotFoundException" en cada método
 * ensucia firmas sin agregar seguridad real (nadie recupera de un 404 en
 * tiempo de compilación). El límite entre "expero esto" y "no lo esperaba"
 * lo traza {@link com.kovu.platform.http.ErrorMapper}, no el compilador.
 */
public sealed abstract class AppException extends RuntimeException
        permits NotFoundException, ValidationException, ConflictException {

    protected AppException(String message) {
        super(message);
    }
}
