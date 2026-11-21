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


    /** Ejecuta un SELECT con parametros y convierte cada fila en Producto. */
    private List<Producto> consultar(String sql, Object... parametros) {
        List<Producto> resultado = new ArrayList<>();
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]); // los indices empiezan en 1
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

    /** Traduce el SQLState de PostgreSQL al idioma del negocio. */
    private RepositorioException traducir(SQLException e, String codigo) {
        String estado = e.getSQLState();
        if (DUPLICADO.equals(estado)) {
            return new RepositorioException(
                    "Ya existe un producto con el codigo " + codigo, e);
        }
        if (REGLA_VIOLADA.equals(estado)) {
            return new RepositorioException(
                    "PostgreSQL rechazo un valor fuera de regla en " + codigo, e);
        }
        return new RepositorioException(
                "Error de base de datos [" + estado + "]: " + e.getMessage(), e);
    }
}