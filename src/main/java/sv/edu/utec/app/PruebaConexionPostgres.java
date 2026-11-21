package sv.edu.utec.app;
import sv.edu.utec.datos.ConexionPostgres;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class PruebaConexionPostgres {
    public static void main(String[] args) {
        String sql = "SELECT current_user, current_database(), "
                + "current_setting('server_version')";
        long inicio = System.currentTimeMillis();
        try (Connection cn = ConexionPostgres.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            long ms = System.currentTimeMillis() - inicio;
            System.out.println("Conexion exitosa a PostgreSQL " + rs.getString(3));
            System.out.println(" Usuario : " + rs.getString(1));
            System.out.println(" Base : " + rs.getString(2));
            System.out.println(" Tiempo : " + ms + " ms");
        } catch (SQLException e) {
            System.err.println("Error de conexion [" + e.getSQLState() + "]: "
                    + e.getMessage());
        }
    }
}