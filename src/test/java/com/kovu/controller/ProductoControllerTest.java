package com.kovu.controller;

import com.kovu.platform.http.Router;
import com.kovu.platform.http.Server;
import com.kovu.repository.InMemoryProductoRepository;
import com.kovu.repository.ProductoRepository;
import com.kovu.service.ProductoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test end-to-end de la pila completa HTTP -&gt; controller -&gt; service,
 * pero SIN MySQL: el repositorio es el {@link InMemoryProductoRepository}
 * de test. Por eso no lleva {@code @Tag("integration")} — corre siempre,
 * en {@code mvn test}, junto con el resto de la suite rápida.
 *
 * Compárese con {@code JdbcProductoRepositoryTest}, que sí necesita MySQL
 * levantado y por eso queda excluido del ciclo normal.
 */
class ProductoControllerTest {

    private Server server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        ProductoRepository repositorio = new InMemoryProductoRepository();
        ProductoService servicio = new ProductoService(repositorio);
        ProductoController controller = new ProductoController(servicio);

        Router router = new Router();
        controller.registrarRutas(router);

        server = new Server(0, router); // puerto 0 = el sistema operativo elige uno libre
        server.start();
        baseUrl = "http://localhost:" + server.port();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void crea_y_luego_obtiene_un_producto() throws Exception {
        HttpResponse<String> creado = post("/productos", """
                {"sku":"SKU-1","nombre":"Mate","precioCentavos":500000,"stock":10}
                """);
        assertThat(creado.statusCode()).isEqualTo(201);
        assertThat(creado.body()).contains("SKU-1");

        HttpResponse<String> obtenido = get("/productos/1");
        assertThat(obtenido.statusCode()).isEqualTo(200);
        assertThat(obtenido.body()).contains("Mate");
    }

    @Test
    void crear_con_sku_vacio_devuelve_422_con_detalle_de_validacion() throws Exception {
        HttpResponse<String> response = post("/productos", """
                {"sku":"","nombre":"Mate","precioCentavos":500000,"stock":10}
                """);

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.body()).contains("VALIDATION_ERROR").contains("sku");
    }

    @Test
    void obtener_un_producto_inexistente_devuelve_404() throws Exception {
        HttpResponse<String> response = get("/productos/999");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("NOT_FOUND");
    }

    @Test
    void crear_sku_duplicado_devuelve_409() throws Exception {
        post("/productos", """
                {"sku":"SKU-1","nombre":"Mate","precioCentavos":500000,"stock":10}
                """);

        HttpResponse<String> response = post("/productos", """
                {"sku":"SKU-1","nombre":"Otro","precioCentavos":100,"stock":1}
                """);

        assertThat(response.statusCode()).isEqualTo(409);
    }

    @Test
    void vender_descuenta_stock_via_http() throws Exception {
        post("/productos", """
                {"sku":"SKU-1","nombre":"Mate","precioCentavos":500000,"stock":10}
                """);

        HttpResponse<String> response = post("/productos/1/venta", """
                {"cantidad":4}
                """);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"stock\":6");
    }

    @Test
    void eliminar_un_producto_devuelve_204() throws Exception {
        post("/productos", """
                {"sku":"SKU-1","nombre":"Mate","precioCentavos":500000,"stock":10}
                """);

        HttpResponse<Void> response = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/productos/1")).DELETE().build(),
                HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode()).isEqualTo(204);
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
