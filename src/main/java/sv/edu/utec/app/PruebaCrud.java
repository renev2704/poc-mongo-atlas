package sv.edu.utec.app;

import com.mongodb.MongoException;
import sv.edu.utec.datos.ConexionMongo;
import sv.edu.utec.datos.MongoProductoRepositorio;
import sv.edu.utec.datos.ProductoRepositorio;
import sv.edu.utec.datos.RepositorioException;
import sv.edu.utec.modelo.Producto;

public class PruebaCrud {

    public static void main(String[] args) {
        try {
            ProductoRepositorio repo =
                    new MongoProductoRepositorio(ConexionMongo.getBaseDeDatos());

            // 0. Limpieza
            for (Producto p : DatosDemo.productos()) {
                repo.eliminar(p.getCodigo());
            }
            repo.eliminar("P-009");
            repo.eliminar("P-010");
            System.out.println("0) Limpieza lista");

            // --- PARTE B: CREATE y DELETE ---
            int n = repo.insertarVarios(DatosDemo.productos());
            System.out.println("1) Carga inicial: " + n + " productos");

            repo.insertar(new Producto("P-009", "Azucar blanca 1 kg",
                    "Abarrotes", 1.10, 150));
            System.out.println(" Insertado: P-009");

            try {
                repo.insertar(new Producto("P-001", "Duplicado", "Abarrotes", 1.00, 1));
            } catch (RepositorioException e) {
                System.out.println(" Rechazado por MongoDB: " + e.getMessage());
            }

            try {
                repo.insertar(new Producto("P-010", "Error", "Abarrotes", -5.00, 1));
            } catch (IllegalArgumentException e) {
                System.out.println(" Rechazado por Java: " + e.getMessage());
            }

            System.out.println("2) P-009 eliminado: " + repo.eliminar("P-009"));
            System.out.println(" P-009 eliminado otra vez: " + repo.eliminar("P-009"));

            // --- PARTE C: READ ---
            System.out.println("3) Busquedas:");
            repo.buscarPorCodigo("P-002")
                    .ifPresent(p -> System.out.println(" P-002: " + p));
            System.out.println(" Por categoria Abarrotes:");
            repo.listarPorCategoria("Abarrotes")
                    .forEach(p -> System.out.println("  " + p));
            System.out.println(" Por nombre 'lb':");
            repo.buscarPorNombre("lb")
                    .forEach(p -> System.out.println("  " + p));
            System.out.println(" Rango 1.00 - 2.00:");
            repo.buscarPorRangoDePrecio(1.00, 2.00)
                    .forEach(p -> System.out.println("  " + p));

            // --- PARTE D: UPDATE ---
            Producto frijol = repo.buscarPorCodigo("P-002").orElseThrow();
            frijol.setPrecio(1.45);
            System.out.println("4) P-002 actualizado: " + repo.actualizar(frijol));
            System.out.println(" P-404 actualizado: " + repo.actualizar(
                    new Producto("P-404", "No existe", "Abarrotes", 1.00, 1)));
            System.out.println(" Entrada de 40 a P-005: " + repo.ajustarExistencia("P-005", 40));
            System.out.println(" Salida de 500 de P-005: " + repo.ajustarExistencia("P-005", -500));
            System.out.println(" Salida de 20 de P-005: " + repo.ajustarExistencia("P-005", -20));

            System.out.println(" --- Inventario final ---");
            repo.listarTodos().forEach(p -> System.out.println(" " + p));

            // --- PARTE E: REPORTE ---
            System.out.println("5) Resumen por categoria:");
            repo.resumenPorCategoria().forEach(r -> System.out.printf(
                    "   %-10s %d productos %4d u. $%9.2f%n",
                    r.categoria(), r.productos(), r.unidades(), r.valorInventario()));

        } catch (MongoException e) {
            System.err.println("Error de MongoDB: " + e.getMessage());
        } finally {
            ConexionMongo.cerrar();
        }
    }
}