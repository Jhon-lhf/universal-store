package com.proyecto.servicio;

import com.proyecto.modelo.EstadoPedido;
import com.proyecto.modelo.ItemPedido;
import com.proyecto.modelo.Pedido;
import com.proyecto.modelo.Producto;
import com.proyecto.persistencia.RepositorioPedidos;
import com.proyecto.persistencia.RepositorioProductos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de métricas del negocio: ingresos (excluye cancelados), pedidos por
 * estado, ventas por vendedor y producto más vendido.
 */
class ServicioMetricasTest {

    private static final Path ARCHIVO_PEDIDOS = Paths.get("data", "test-metricas-pedidos.json");
    private static final Path ARCHIVO_PRODUCTOS = Paths.get("data", "test-metricas-productos.json");

    private RepositorioPedidos repositorioPedidos;
    private RepositorioProductos repositorioProductos;
    private ServicioMetricas servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO_PEDIDOS);
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
        repositorioPedidos = new RepositorioPedidos(ARCHIVO_PEDIDOS);
        repositorioProductos = new RepositorioProductos(ARCHIVO_PRODUCTOS);
        servicio = new ServicioMetricas(repositorioPedidos, repositorioProductos);

        repositorioProductos.guardar(new Producto("P-1", "Laptop Pro 14", "Descripción", 999.00, 8, "C-1", "U-ven", "2026-01-01"));
        repositorioProductos.guardar(new Producto("P-2", "Camiseta Premium", "Descripción", 24.99, 40, "C-2", "U-ven", "2026-01-01"));

        // Pedido 1: PENDIENTE, de U-ven, $1000
        repositorioPedidos.guardar(new Pedido("PED-1", "U-cli", "Cliente", "2026-01-10",
                EstadoPedido.PENDIENTE,
                List.of(new ItemPedido("P-1", "Laptop Pro 14", "U-ven", 1000.0, 1)),
                1000.0, 190.0, 1190.0));
        // Pedido 2: ENTREGADO, de U-ven, $500
        repositorioPedidos.guardar(new Pedido("PED-2", "U-cli", "Cliente", "2026-01-11",
                EstadoPedido.ENTREGADO,
                List.of(new ItemPedido("P-2", "Camiseta Premium", "U-ven", 500.0, 1)),
                500.0, 95.0, 595.0));
        // Pedido 3: CANCELADO, de U-otro, $200 (no debe contar en ingresos)
        repositorioPedidos.guardar(new Pedido("PED-3", "U-cli", "Cliente", "2026-01-12",
                EstadoPedido.CANCELADO,
                List.of(new ItemPedido("P-1", "Laptop Pro 14", "U-otro", 200.0, 1)),
                200.0, 38.0, 238.0));
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO_PEDIDOS);
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
    }

    @Test
    void calcularIngresosTotalesExcluyeCancelados() {
        double ingresos = servicio.calcularIngresosTotales();
        assertEquals(1785.0, ingresos); // 1190 + 595
    }

    @Test
    void totalPedidos() {
        assertEquals(3, servicio.totalPedidos());
    }

    @Test
    void pedidosPorEstado() {
        var conteo = servicio.pedidosPorEstado();
        assertEquals(1, conteo.get(EstadoPedido.PENDIENTE));
        assertEquals(1, conteo.get(EstadoPedido.ENTREGADO));
        assertEquals(1, conteo.get(EstadoPedido.CANCELADO));
    }

    @Test
    void ventasPorVendedorExcluyeCancelados() {
        var ventas = servicio.ventasPorVendedor();
        assertEquals(1, ventas.size()); // solo U-ven tiene ventas (U-otro solo tiene cancelado)
        assertEquals(1500.0, ventas.get("U-ven")); // 1000 + 500 (subtotales sin impuesto)
    }

    @Test
    void productoMasVendidoSinItemsDevuelveGuion() throws Exception {
        // Escenario sin items: no hay ventas que contar
        Path vacioPedidos = Paths.get("data", "test-metricas-vacio-pedidos.json");
        Path vacioProductos = Paths.get("data", "test-metricas-vacio-productos.json");
        try {
            Files.deleteIfExists(vacioPedidos);
            Files.deleteIfExists(vacioProductos);
            RepositorioPedidos repoP = new RepositorioPedidos(vacioPedidos);
            RepositorioProductos repoPr = new RepositorioProductos(vacioProductos);
            ServicioMetricas sinItems = new ServicioMetricas(repoP, repoPr);
            assertEquals("—", sinItems.productoMasVendido());
        } finally {
            Files.deleteIfExists(vacioPedidos);
            Files.deleteIfExists(vacioProductos);
        }
    }

    @Test
    void productoMasVendidoDevuelveTituloYUnidades() {
        // P-1 tiene 1 unidad (PED-1, no cancelado), P-2 tiene 1 unidad (PED-2, no cancelado).
        // Empate: devuelve uno de los dos.
        String resultado = servicio.productoMasVendido();
        assertTrue(resultado.contains("unidades"));
        assertTrue(resultado.contains("Laptop Pro 14") || resultado.contains("Camiseta Premium"));
    }
}
