package sv.edu.utec.datos;

import sv.edu.utec.modelo.Producto;
import sv.edu.utec.modelo.ResumenCategoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductoRepositorio implements ProductoRepositorio {

    private static final String DUPLICADO = "23505";     // unique_violation
    private static final String REGLA_VIOLADA = "23514"; // check_violation
    private static final String COLUMNAS =
            "codigo, nombre, categoria, precio, existencia";

    // ------------------------------------------------------------------
    // C: Create
    // ------------------------------------------------------------------
    @Override
    public void insertar(Producto p) {
        String sql = "INSERT INTO producto (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarParametros(ps, p);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw traducir(e, p.getCodigo());
        }
    }

    /** Carga en lote como transacción: todo o nada. */
    @Override
    public int insertarVarios(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) {
            return 0;
        }
        String sql = "INSERT INTO producto (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?)";
        Connection cn = null;
        try {
            cn = ConexionPostgres.obtener();
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                for (Producto p : productos) {
                    asignarParametros(ps, p);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            cn.commit();
            return productos.size();
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw traducir(e, "lote");
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // R: Read
    // ------------------------------------------------------------------
    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        List<Producto> r = consultar(
                "SELECT " + COLUMNAS + " FROM producto WHERE codigo = ?", codigo);
        return r.stream().findFirst();
    }

    @Override
    public List<Producto> listarTodos() {
        return consultar("SELECT " + COLUMNAS + " FROM producto ORDER BY codigo");
    }

    @Override
    public List<Producto> listarPorCategoria(String categoria) {
        return consultar(
                "SELECT " + COLUMNAS + " FROM producto " +
                        "WHERE categoria = ? ORDER BY codigo",
                categoria);
    }

    @Override
    public List<Producto> buscarPorNombre(String texto) {
        String escapado = texto
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return consultar(
                "SELECT " + COLUMNAS + " FROM producto " +
                        "WHERE nombre ILIKE ? ESCAPE '\\' ORDER BY codigo",
                "%" + escapado + "%");
    }

    @Override
    public List<Producto> buscarPorRangoDePrecio(double minimo, double maximo) {
        return consultar(
                "SELECT " + COLUMNAS + " FROM producto " +
                        "WHERE precio BETWEEN ? AND ? ORDER BY precio, codigo",
                minimo, maximo);
    }

    @Override
    public List<ResumenCategoria> resumenPorCategoria() {
        List<ResumenCategoria> resultado = new ArrayList<>();
        String sql = "SELECT categoria, COUNT(*) AS productos, " +
                "SUM(existencia) AS unidades, " +
                "SUM(precio * existencia) AS valor " +
                "FROM producto GROUP BY categoria ORDER BY categoria";
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(new ResumenCategoria(
                        rs.getString("categoria"),
                        rs.getInt("productos"),
                        rs.getInt("unidades"),
                        rs.getDouble("valor")));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error al generar el resumen", e);
        }
        return resultado;
    }

    // ------------------------------------------------------------------
    // U: Update
    // ------------------------------------------------------------------
    @Override
    public boolean actualizar(Producto p) {
        String sql = "UPDATE producto SET nombre = ?, categoria = ?, " +
                "precio = ?, existencia = ?, actualizado_en = now() " +
                "WHERE codigo = ?";
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getExistencia());
            ps.setString(5, p.getCodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducir(e, p.getCodigo());
        }
    }

    @Override
    public boolean ajustarExistencia(String codigo, int cantidad) {
        String sql = "UPDATE producto " +
                "SET existencia = existencia + ?, actualizado_en = now() " +
                "WHERE codigo = ? AND existencia + ? >= 0";
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, codigo);
            ps.setInt(3, cantidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducir(e, codigo);
        }
    }

    // ------------------------------------------------------------------
    // D: Delete
    // ------------------------------------------------------------------
    @Override
    public boolean eliminar(String codigo) {
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducir(e, codigo);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private List<Producto> consultar(String sql, Object... parametros) {
        List<Producto> resultado = new ArrayList<>();
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(aProducto(rs));
                }
            }
            return resultado;

        } catch (SQLException e) {
            throw new RepositorioException("Error al consultar productos", e);
        }
    }

    private void asignarParametros(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getCodigo());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getCategoria());
        ps.setDouble(4, p.getPrecio());
        ps.setInt(5, p.getExistencia());
    }

    private Producto aProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getDouble("precio"),
                rs.getInt("existencia"));
    }

    private RepositorioException traducir(SQLException e, String codigo) {
        String estado = e.getSQLState();
        if (DUPLICADO.equals(estado)) {
            return new RepositorioException(
                    "Ya existe un producto con el codigo " + codigo, e);
        }
        if (REGLA_VIOLADA.equals(estado)) {
            return new RepositorioException(
                    "PostgreSQL rechazó un valor fuera de regla en " + codigo, e);
        }
        return new RepositorioException(
                "Error de base de datos [" + estado + "]: " + e.getMessage(), e);
    }
}