package com.kovu.repository.jdbc;

import com.kovu.config.AppConfig;
import com.kovu.exception.ConflictException;
import com.kovu.model.Producto;
import com.kovu.platform.db.ConnectionPool;
import com.kovu.platform.db.Database;
import com.kovu.platform.db.MigrationRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración REAL: contra un MySQL de verdad (ver
 * docker-compose.yml). Prueba SQL, no reglas de negocio — el índice único
 * de {@code sku}, el UPDATE atómico de {@link #descontarStock}, el mapeo
 * fila-&gt;objeto.
 *
 * <p>Marcado con {@code @Tag("integration")}: {@code mvn test} lo excluye
 * (ver {@code excludedGroups} en pom.xml). Para correrlo:
 * <pre>
 *   docker compose up -d mysql
 *   mvn test -Dgroups=integration -Dtest=JdbcProductoRepositoryTest
 * </pre>
 */
@Tag("integration")
class JdbcProductoRepositoryTest {

    private static ConnectionPool pool;
    private static Database database;
    private JdbcProductoRepository repositorio;

    @BeforeAll
    static void conectar() {
        AppConfig config = AppConfig.fromEnv();
        pool = new ConnectionPool(config.dbUrl(), config.dbUser(), config.dbPassword(), 2);
        database = new Database(pool);
        new MigrationRunner(database).run();
    }

    @AfterAll
    static void cerrar() {
        pool.close();
    }

    @BeforeEach
    void limpiarTabla() {
        database.execute(conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM productos");
            }
            return null;
        });
        repositorio = new JdbcProductoRepository(database);
    }

    @Test
    void guarda_y_relee_un_producto() {
        Producto guardado = repositorio.guardar(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        Optional<Producto> releido = repositorio.buscarPorId(guardado.id());

        assertThat(releido).isPresent();
        assertThat(releido.get().sku()).isEqualTo("SKU-1");
        assertThat(releido.get().creadoEn()).isNotNull();
    }

    @Test
    void el_indice_unico_de_sku_rechaza_duplicados() {
        repositorio.guardar(Producto.nuevo("SKU-1", "Mate", 500000, 10));

        assertThatThrownBy(() -> repositorio.guardar(Producto.nuevo("SKU-1", "Otro mate", 1, 1)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void descontar_stock_es_atomico_bajo_concurrencia() throws InterruptedException {
        Producto producto = repositorio.guardar(Producto.nuevo("SKU-1", "Mate", 500000, 10));
        int hilos = 10;

        // 10 hilos intentan descontar 1 unidad cada uno "al mismo tiempo" sobre
        // un stock de 10: el resultado final tiene que ser exactamente 0,
        // nunca negativo. Si esto fallara, sería la prueba de que el UPDATE
        // atómico en el WHERE (stock >= cantidad) realmente importa.
        Thread[] threads = new Thread[hilos];
        for (int i = 0; i < hilos; i++) {
            threads[i] = new Thread(() -> repositorio.descontarStock(producto.id(), 1));
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }

        Producto resultado = repositorio.buscarPorId(producto.id()).orElseThrow();
        assertThat(resultado.stock()).isEqualTo(0);
    }
}
