package sv.edu.utec.app;

import sv.edu.utec.datos.JdbcProductoRepositorio;
import sv.edu.utec.datos.ProductoRepositorio;
import sv.edu.utec.datos.RepositorioException;
import sv.edu.utec.modelo.Producto;

import java.util.List;

public class PruebaCrudPostgres {
    public static void main(String[] args) {
        // La UNICA linea distinta a la PruebaCrud de la Guia 4
        ProductoRepositorio repo = new JdbcProductoRepositorio();

        try {
            // 0. Limpieza: la prueba se puede ejecutar varias veces
            for (Producto p : DatosDemo.productos()) {
                repo.eliminar(p.getCodigo());
            }
            repo.eliminar("P-009");
            repo.eliminar("P-011");
            System.out.println("0) Limpieza lista");

            // ---- PARTE C: CREATE y DELETE ----
            int n = repo.insertarVarios(DatosDemo.productos());
            System.out.println("1) Carga inicial: " + n + " productos");
            repo.insertar(new Producto("P-009", "Azucar blanca 1 kg",
                    "Abarrotes", 1.10, 150));
            System.out.println(" Insertado: P-009");
            try {
                repo.insertar(new Producto("P-001", "Duplicado", "Abarrotes", 1.00, 1));
            } catch (RepositorioException e) {
                System.out.println(" Rechazado por PostgreSQL: " + e.getMessage());
            }
            try {
                repo.insertar(new Producto("P-010", "Error", "Abarrotes", -5.00, 1));
            } catch (IllegalArgumentException e) {
                System.out.println(" Rechazado por Java: " + e.getMessage());
            }
            System.out.println("2) P-009 eliminado: " + repo.eliminar("P-009"));
            System.out.println(" P-009 eliminado otra vez: " + repo.eliminar("P-009"));

            // ---- PARTE C: READ ----
            System.out.println("3) Buscar P-004:");
            repo.buscarPorCodigo("P-004").ifPresentOrElse(
                    p -> System.out.println(" " + p),
                    () -> System.out.println(" No existe"));
            System.out.println(" Buscar P-404: "
                    + repo.buscarPorCodigo("P-404").map(Producto::getNombre)
                    .orElse("No existe"));
            System.out.println(" Total de productos: " + repo.listarTodos().size());
            System.out.println(" Categoria Limpieza:");
            repo.listarPorCategoria("Limpieza")
                    .forEach(p -> System.out.println(" " + p));
            System.out.println(" Nombre contiene \"LB\":");
            repo.buscarPorNombre("LB")
                    .forEach(p -> System.out.println(" " + p));
            System.out.println(" Nombre contiene \"%\": "
                    + repo.buscarPorNombre("%").size() + " resultados");
            System.out.println(" Precio entre $1.00 y $2.00:");
            repo.buscarPorRangoDePrecio(1.00, 2.00)
                    .forEach(p -> System.out.println(" " + p));

            // ---- PARTE D: UPDATE ----
            Producto frijol = repo.buscarPorCodigo("P-002").orElseThrow();
            frijol.setPrecio(1.45);
            System.out.println("4) P-002 actualizado: " + repo.actualizar(frijol));
            System.out.println(" P-404 actualizado: " + repo.actualizar(
                    new Producto("P-404", "No existe", "Abarrotes", 1.00, 1)));
            System.out.println(" Entrada de 40 a P-005: "
                    + repo.ajustarExistencia("P-005", 40));
            System.out.println(" Salida de 500 de P-005: "
                    + repo.ajustarExistencia("P-005", -500));
            System.out.println(" Salida de 20 de P-005: "
                    + repo.ajustarExistencia("P-005", -20));
            System.out.println(" --- Inventario final ---");
            repo.listarTodos().forEach(p -> System.out.println(" " + p));

            // ---- PARTE D: REPORTE ----
            System.out.println("5) Resumen por categoria:");
            repo.resumenPorCategoria().forEach(r -> System.out.printf(
                    " %-10s %d productos %4d u. $%9.2f%n",
                    r.categoria(), r.productos(), r.unidades(), r.valorInventario()));
        } catch (RepositorioException e) {
            System.err.println("Error de datos: " + e.getMessage());
        }

        // ---- PARTE E: TRANSACCION (todo o nada) ----
        List<Producto> lote = List.of(
                new Producto("P-011", "Sal yodada 1 kg", "Abarrotes", 0.45, 200),
                new Producto("P-001", "Repetido", "Abarrotes", 9.99, 1));
        try {
            repo.insertarVarios(lote);
        } catch (RepositorioException e) {
            System.out.println("6) " + e.getMessage());
        }
        System.out.println(" P-011 existe: " + repo.buscarPorCodigo("P-011").isPresent());
    }
}
