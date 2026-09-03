package com.kovu.platform.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración liviano del {@link Router}: levanta un HttpServer de
 * verdad en un puerto efímero y le pega con {@link HttpClient} (del JDK,
 * sin dependencias extra). Cubre exactamente lo que el enunciado pide
 * "escribir a mano": el match de rutas con path params y el mapeo de
 * excepciones a códigos HTTP.
 */
class RouterTest {

    private HttpServer httpServer;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        Router router = new Router();
        router.get("/saludo/{nombre}", ctx -> ctx.json(200, "Hola, " + ctx.pathParam("nombre")));
        router.get("/falla", ctx -> {
            throw com.kovu.exception.NotFoundException.producto(42);
        });

        httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        httpServer.createContext("/", router);
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.start();

        baseUrl = "http://localhost:" + httpServer.getAddress().getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(0);
    }

    @Test
    void resuelve_path_params() throws Exception {
        HttpResponse<String> response = get("/saludo/Ada");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Ada");
    }

    @Test
    void ruta_no_registrada_devuelve_404_con_json_de_error() throws Exception {
        HttpResponse<String> response = get("/no-existe");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("NOT_FOUND");
    }

    @Test
    void excepcion_de_negocio_se_traduce_al_codigo_http_correcto() throws Exception {
        HttpResponse<String> response = get("/falla");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("Producto no encontrado");
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
