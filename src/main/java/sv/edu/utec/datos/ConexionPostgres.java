package sv.edu.utec.datos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * Unica clase que sabe donde esta la base de datos.
 * La URL (con usuario y contrasena) llega por variable de entorno:
 * nunca se escribe en el codigo ni se sube a GitHub.
 */
public final class ConexionPostgres {
    private static final String VARIABLE = "SUPABASE_DB_URL";
    private ConexionPostgres() { } // nadie crea objetos de esta clase
    public static Connection obtener() throws SQLException {
        String url = System.getenv(VARIABLE);
        if (url == null || url.isBlank()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno " + VARIABLE);
        }
        return DriverManager.getConnection(url);
    }
}
