package com.kovu.repository.jdbc;

import com.kovu.exception.ConflictException;
import com.kovu.model.Producto;
import com.kovu.platform.db.Database;
import com.kovu.repository.ProductoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC "a mano" del puerto {@link ProductoRepository}: sin
 * ORM, cada método es un SQL explícito con {@code PreparedStatement}
 * (parámetros con {@code ?}, nunca concatenados — eso es lo que previene
 * SQL injection) y su mapeo fila-&gt;objeto manual en {@link #mapearFila}.
 */
public final class JdbcProductoRepository implements ProductoRepository {

    private static final String COLUMNAS =
            "id, sku, nombre, precio_centavos, stock, activo, creado_en";

    private final Database database;

    public JdbcProductoRepository(Database database) {
        this.database = database;
    }

    @Override
    public Producto guardar(Producto producto) {
        String sql = "INSERT INTO productos (sku, nombre, precio_centavos, stock, activo) VALUES (?, ?, ?, ?, ?)";
        return database.execute(conn -> {
            long id;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, producto.sku());
                ps.setString(2, producto.nombre());
                ps.setLong(3, producto.precioCentavos());
                ps.setInt(4, producto.stock());
                ps.setBoolean(5, producto.activo());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getLong(1);
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                // el índice único uq_productos_sku (V1__create_productos.sql) es la
                // última línea de defensa contra un sku duplicado; el chequeo en
                // ProductoService#crear puede perder una carrera, esto no.
                throw new ConflictException("Ya existe un producto con sku=" + producto.sku());
            }
            return buscarPorIdEnConexion(conn, id)
                    .orElseThrow(() -> new IllegalStateException("No se pudo releer el producto insertado: id=" + id));
        });
    }

    @Override
    public Optional<Producto> buscarPorId(long id) {
        return database.execute(conn -> buscarPorIdEnConexion(conn, id));
    }

    private Optional<Producto> buscarPorIdEnConexion(Connection conn, long id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Producto> buscarPorSku(String sku) {
        String sql = "SELECT " + COLUMNAS + " FROM productos WHERE sku = ?";
        return database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sku);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Producto> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM productos ORDER BY id";
        return database.execute(conn -> {
            List<Producto> productos = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearFila(rs));
                }
            }
            return productos;
        });
    }

    @Override
    public Producto actualizar(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, precio_centavos = ?, stock = ?, activo = ? WHERE id = ?";
        return database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, producto.nombre());
                ps.setLong(2, producto.precioCentavos());
                ps.setInt(3, producto.stock());
                ps.setBoolean(4, producto.activo());
                ps.setLong(5, producto.id());
                ps.executeUpdate();
            }
            return buscarPorIdEnConexion(conn, producto.id())
                    .orElseThrow(() -> new IllegalStateException("Producto desapareció durante el UPDATE: id=" + producto.id()));
        });
    }

    @Override
    public boolean eliminar(long id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        return database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean descontarStock(long id, int cantidad) {
        // Un solo UPDATE atómico con la condición en el WHERE, en vez de
        // "SELECT stock -> comparar en Java -> UPDATE stock - cantidad": ese
        // patrón read-then-write tiene una carrera clásica (dos requests leen
        // stock=1 al mismo tiempo, ambas restan 1, el stock queda en -1). Acá
        // MySQL evalúa la condición y actualiza en la misma operación atómica.
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
        return database.execute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, cantidad);
                ps.setLong(2, id);
                ps.setInt(3, cantidad);
                return ps.executeUpdate() > 0;
            }
        });
    }

    private Producto mapearFila(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getLong("id"),
                rs.getString("sku"),
                rs.getString("nombre"),
                rs.getLong("precio_centavos"),
                rs.getInt("stock"),
                rs.getBoolean("activo"),
                rs.getTimestamp("creado_en").toInstant()
        );
    }
}
