package sv.edu.utec.app;

import com.mongodb.MongoException;
import sv.edu.utec.datos.ConexionMongo;
import sv.edu.utec.datos.MongoProductoRepositorio;
import sv.edu.utec.datos.ProductoRepositorio;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        try (Scanner entrada = new Scanner(System.in)) {
            ProductoRepositorio repo =
                    new MongoProductoRepositorio(ConexionMongo.getBaseDeDatos());

            if (repo.listarTodos().isEmpty()) {
                int n = repo.insertarVarios(DatosDemo.productos());
                System.out.println("Coleccion vacia: se cargaron " + n + " productos.");
            }

            new MenuInventario(repo, entrada).iniciar();

        } catch (MongoException e) {
            System.err.println("Error de MongoDB: " + e.getMessage());
        } finally {
            ConexionMongo.cerrar();
        }
    }
}