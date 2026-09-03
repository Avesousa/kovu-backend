package com.kovu.platform.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Punto único de configuración de Jackson. Nótese que las fechas ({@code
 * java.time.Instant}) no se serializan acá: se convierten a String en la
 * capa de mapper (dominio -&gt; DTO) antes de llegar a este ObjectMapper.
 * Así evitamos sumar el módulo jackson-datatype-jsr310 y quede clarísimo
 * cuándo y dónde se hace esa conversión.
 */
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Json() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
