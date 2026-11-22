package sv.edu.utec.app;
import sv.edu.utec.datos.JdbcProductoRepositorio;
import sv.edu.utec.datos.ProductoRepositorio;
import sv.edu.utec.datos.RepositorioException;
import java.util.Scanner;
/**
 * Mismo menu de la Guia 4, ahora sobre PostgreSQL.
 * Comparela con App.java: cambia la linea del repositorio y nada mas.
 */
public class AppPostgres {
    public static void main(String[] args) {
        try (Scanner entrada = new Scanner(System.in)) {
            ProductoRepositorio repo = new JdbcProductoRepositorio();
            if (repo.listarTodos().isEmpty()) {
                int n = repo.insertarVarios(DatosDemo.productos());
                System.out.println("Tabla vacia: se cargaron " + n + " productos de ejemplo.");
            }
            new MenuInventario(repo, entrada).iniciar();
        } catch (RepositorioException e) {
            System.err.println("Error de datos: " + e.getMessage());
        }
    }
}