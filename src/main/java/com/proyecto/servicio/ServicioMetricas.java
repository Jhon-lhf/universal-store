package com.proyecto.servicio;

import com.proyecto.modelo.EstadoPedido;
import com.proyecto.modelo.ItemPedido;
import com.proyecto.modelo.Pedido;
import com.proyecto.modelo.Producto;
import com.proyecto.persistencia.RepositorioPedidos;
import com.proyecto.persistencia.RepositorioProductos;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tablero de salud del negocio para el administrador.
 *
 * <p>Calcula métricas a partir de los pedidos y productos existentes:
 * ingresos (excluye cancelados), desglose por vendedor y conteo de pedidos
 * por estado.</p>
 */
public class ServicioMetricas {

    private final RepositorioPedidos repositorioPedidos;
    private final RepositorioProductos repositorioProductos;

    public ServicioMetricas(RepositorioPedidos repositorioPedidos,
                            RepositorioProductos repositorioProductos) {
        this.repositorioPedidos = repositorioPedidos;
        this.repositorioProductos = repositorioProductos;
    }

    /**
     * Resultado de métricas del negocio.
     */
    public record Resumen(
            int totalPedidos,
            int pedidosPendientes,
            int pedidosEnviados,
            int pedidosEntregados,
            int pedidosCancelados,
            double ingresosTotales,
            double ingresosPorVendedor // mapa vendedor → monto, simplificado como string
    ) {
    }

    /**
     * Desglose de ventas por vendedor: ID del vendedor → monto total.
     */
    public Map<String, Double> ventasPorVendedor() {
        return repositorioPedidos.leerTodos().stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(
                        ItemPedido::getVendedorId,
                        Collectors.summingDouble(ItemPedido::subtotal)
                ));
    }

    /**
     * Conteo de pedidos por estado.
     */
    public Map<EstadoPedido, Long> pedidosPorEstado() {
        return repositorioPedidos.leerTodos().stream()
                .collect(Collectors.groupingBy(
                        Pedido::getEstado,
                        Collectors.counting()
                ));
    }

    /**
     * Ingresos totales excluyendo pedidos cancelados.
     * Se redondea a 2 decimales.
     */
    public double calcularIngresosTotales() {
        return redondear(repositorioPedidos.leerTodos().stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .mapToDouble(Pedido::getTotal)
                .sum());
    }

    /**
     * Cantidad total de pedidos.
     */
    public int totalPedidos() {
        return repositorioPedidos.leerTodos().size();
    }

    /**
     * Producto más vendido por cantidad de unidades (excluye cancelados).
     */
    public String productoMasVendido() {
        Map<String, Integer> unidadesPorProducto = repositorioPedidos.leerTodos().stream()
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.groupingBy(
                        ItemPedido::getProductoId,
                        Collectors.summingInt(ItemPedido::getCantidad)
                ));
        if (unidadesPorProducto.isEmpty()) {
            return "—";
        }
        return unidadesPorProducto.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> {
                    String idProducto = entry.getKey();
                    return repositorioProductos.buscarPorId(idProducto)
                            .map(Producto::getTitulo)
                            .orElse(idProducto) + " (" + entry.getValue() + " unidades)";
                })
                .orElse("—");
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
