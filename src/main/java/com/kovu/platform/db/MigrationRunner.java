package com.kovu.platform.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Corredor de migraciones minimalista (un Flyway/Liquibase casero).
 *
 * <p>Las migraciones viven en {@code src/main/resources/db/migration/} y se
 * listan explícitamente en {@link #MIGRACIONES} en el orden en que deben
 * aplicarse. Se lista a mano (en vez de escanear el classpath) porque
 * escanear un directorio dentro de un jar empaquetado con shade es
 * bastante más complicado que esto, y con dos o tres migraciones por
 * sprint listar a mano no pesa.
 *
 * <p>El estado se guarda en la tabla {@code schema_migrations}: cada
 * migración aplicada queda registrada con su nombre, así que correr {@link
 * #run()} muchas veces (por ejemplo, cada vez que arranca el server) es
 * seguro — las ya aplicadas se saltean.
 */
public final class MigrationRunner {

    private static final System.Logger LOG = System.getLogger(MigrationRunner.class.getName());

    private static final String[] MIGRACIONES = {
            "V1__create_productos.sql",
    };

    private final Database database;

    public MigrationRunner(Database database) {
        this.database = database;
    }

    public void run() {
        database.transactional(conn -> {
            asegurarTablaDeControl(conn);
            Set<String> aplicadas = leerAplicadas(conn);
            for (String archivo : MIGRACIONES) {
                if (aplicadas.contains(archivo)) {
                    continue;
                }
                ejecutarArchivo(conn, archivo);
                registrarAplicada(conn, archivo);
                LOG.log(Level.INFO, "Migración aplicada: " + archivo);
            }
            return null;
        });
    }

    private void asegurarTablaDeControl(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS schema_migrations (
                        version VARCHAR(255) NOT NULL PRIMARY KEY,
                        applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB
                    """);
        }
    }

    private Set<String> leerAplicadas(Connection conn) throws SQLException {
        Set<String> aplicadas = new HashSet<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT version FROM schema_migrations")) {
            while (rs.next()) {
                aplicadas.add(rs.getString("version"));
            }
        }
        return aplicadas;
    }

    private void ejecutarArchivo(Connection conn, String archivo) throws SQLException {
        String contenido = leerRecurso(archivo);
        for (String statement : contenido.split(";")) {
            String sql = statement.strip();
            if (sql.isEmpty()) {
                continue;
            }
            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        }
    }

    private void registrarAplicada(Connection conn, String archivo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO schema_migrations (version) VALUES (?)")) {
            ps.setString(1, archivo);
            ps.executeUpdate();
        }
    }

    private String leerRecurso(String archivo) {
        String ruta = "/db/migration/" + archivo;
        try (InputStream in = MigrationRunner.class.getResourceAsStream(ruta)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró la migración en el classpath: " + ruta);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
