package com.kovu;

import com.kovu.config.AppConfig;
import com.kovu.controller.ProductoController;
import com.kovu.platform.db.ConnectionPool;
import com.kovu.platform.db.Database;
import com.kovu.platform.db.MigrationRunner;
import com.kovu.platform.http.Router;
import com.kovu.platform.http.Server;
import com.kovu.platform.http.filter.LoggingFilter;
import com.kovu.platform.http.filter.RequestIdFilter;
import com.kovu.repository.ProductoRepository;
import com.kovu.repository.jdbc.JdbcProductoRepository;
import com.kovu.service.ProductoService;

import java.lang.System.Logger.Level;
import java.util.Map;

/**
 * Punto de entrada: acá y solo acá se "cablea" la aplicación (se
 * construyen las implementaciones concretas y se inyectan a mano —
 * sin ningún framework de DI). Cada capa superior conoce solo interfaces
 * de la capa inferior; Main es el único lugar que conoce las clases
 * concretas de punta a punta.
 */
public final class Main {

    private static final System.Logger LOG = System.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.fromEnv();

        ConnectionPool pool = new ConnectionPool(
                config.dbUrl(), config.dbUser(), config.dbPassword(), config.dbPoolSize());
        Database database = new Database(pool);
        new MigrationRunner(database).run();

        ProductoRepository productoRepositorio = new JdbcProductoRepository(database);
        ProductoService productoServicio = new ProductoService(productoRepositorio);
        ProductoController productoController = new ProductoController(productoServicio);

        Router router = new Router();
        router.addFilter(new RequestIdFilter());
        router.addFilter(new LoggingFilter());
        router.get("/health", ctx -> ctx.json(200, Map.of("status", "ok")));
        productoController.registrarRutas(router);

        Server server = new Server(config.httpPort(), router);
        server.start();
        LOG.log(Level.INFO, "kovu-backend arriba en http://localhost:" + config.httpPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.log(Level.INFO, "Apagando servidor...");
            server.stop(3);
            pool.close();
        }));
    }
}
