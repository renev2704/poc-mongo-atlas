package sv.edu.utec.app;

import sv.edu.utec.datos.ProductoRepositorio;
import sv.edu.utec.datos.RepositorioException;
import sv.edu.utec.modelo.Producto;
import sv.edu.utec.modelo.ResumenCategoria;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MenuInventario {

    private final ProductoRepositorio repo;
    private final Scanner entrada;

    public MenuInventario(ProductoRepositorio repo, Scanner entrada) {
        this.repo = repo;
        this.entrada = entrada;
    }

    public void iniciar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("====== INVENTARIO DISTRISALVA ======");
            System.out.println(" 1. Listar todos          6. Agregar producto");
            System.out.println(" 2. Buscar codigo         7. Editar producto");
            System.out.println(" 3. Buscar nombre         8. Entrada/salida de existencia");
            System.out.println(" 4. Por categoria         9. Eliminar producto");
            System.out.println(" 5. Rango de precio      10. Reporte por categoria");
            System.out.println(" 0. Salir");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1 -> mostrar(repo.listarTodos());
                    case 2 -> buscarPorCodigo();
                    case 3 -> mostrar(repo.buscarPorNombre(leerTexto("Texto: ")));
                    case 4 -> mostrar(repo.listarPorCategoria(leerTexto("Categoria: ")));
                    case 5 -> mostrar(repo.buscarPorRangoDePrecio(
                            leerDecimal("Precio minimo: "),
                            leerDecimal("Precio maximo: ")));
                    case 6 -> agregar();
                    case 7 -> editar();
                    case 8 -> moverExistencia();
                    case 9 -> eliminar();
                    case 10 -> reporte();
                    case 0 -> System.out.println("Hasta pronto.");
                    default -> System.out.println("Opcion no valida.");
                }
            } catch (IllegalArgumentException | RepositorioException e) {
                System.out.println("No se pudo completar: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void buscarPorCodigo() {
        Optional<Producto> p = repo.buscarPorCodigo(leerTexto("Codigo: "));
        if (p.isEmpty()) {
            System.out.println("(sin resultados)");
        } else {
            System.out.println(p.get());
        }
    }

    private void agregar() {
        Producto p = leerProducto();
        repo.insertar(p);
        System.out.println("Producto agregado: " + p.getCodigo());
    }

    private void editar() {
        String codigo = leerTexto("Codigo a editar: ");
        Optional<Producto> existente = repo.buscarPorCodigo(codigo);
        if (existente.isEmpty()) {
            System.out.println("No existe " + codigo);
            return;
        }
        Producto p = existente.get();
        p.setNombre(leerTexto("Nuevo nombre (" + p.getNombre() + "): "));
        p.setCategoria(leerTexto("Nueva categoria (" + p.getCategoria() + "): "));
        p.setPrecio(leerDecimal("Nuevo precio (" + p.getPrecio() + "): "));
        System.out.println("Actualizado: " + repo.actualizar(p));
    }

    private void moverExistencia() {
        String codigo = leerTexto("Codigo: ");
        int cantidad = leerEntero("Cantidad (+ entrada / - salida): ");
        System.out.println("Ajuste aplicado: " + repo.ajustarExistencia(codigo, cantidad));
    }

    private void eliminar() {
        String codigo = leerTexto("Codigo a eliminar: ");
        String resp = leerTexto("Seguro que desea eliminar " + codigo + "? (s/n): ");
        if (resp.equalsIgnoreCase("s")) {
            System.out.println(repo.eliminar(codigo) ? "Eliminado." : "No existia.");
        } else {
            System.out.println("Cancelado.");
        }
    }

    private void reporte() {
        double total = 0;
        System.out.println("Categoria  Prod.  Unidades     Valor");
        for (ResumenCategoria r : repo.resumenPorCategoria()) {
            System.out.printf("%-10s %5d %9d %9.2f%n",
                    r.categoria(), r.productos(), r.unidades(), r.valorInventario());
            total += r.valorInventario();
        }
        System.out.printf("%-10s %27s%n", "TOTAL", String.format("%9.2f", total));
    }

    private void mostrar(List<Producto> productos) {
        if (productos.isEmpty()) {
            System.out.println("(sin resultados)");
        }
        productos.forEach(System.out::println);
    }

    private Producto leerProducto() {
        return new Producto(
                leerTexto("Codigo: "),
                leerTexto("Nombre: "),
                leerTexto("Categoria: "),
                leerDecimal("Precio: "),
                leerEntero("Existencia: "));
    }

    private String leerTexto(String mensaje) {
        String texto;
        do {
            System.out.print(mensaje);
            texto = entrada.nextLine().trim();
        } while (texto.isEmpty());
        return texto;
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escriba un numero entero.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Escriba un numero, por ejemplo 2.50");
            }
        }
    }
}