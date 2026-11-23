package com.proyecto.ui;

import com.proyecto.modelo.EstadoPedido;
import com.proyecto.servicio.ServicioMetricas;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Tablero de salud del negocio para el administrador: ingresos (excluye
 * cancelados), desglose de pedidos por estado, ventas por vendedor y
 * producto más vendido.
 */
public class VistaMetricas {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);

    public VistaMetricas(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador) {
        this.app = app;
        this.avisador = avisador;
        construir();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Label titulo = new Label("Métricas del negocio");
        titulo.getStyleClass().add("titulo-panel");
        Label subtitulo = new Label("Resumen de pedidos, ingresos y ventas del sistema");
        subtitulo.getStyleClass().add("texto-apagado");

        VBox tarjetas = new VBox(12);
        tarjetas.getStyleClass().add("panel");
        tarjetas.setPadding(new Insets(20));

        ServicioMetricas metricas = app.getServicioMetricas();

        // --- Tarjeta de ingresos ---
        HBox tarjetaIngresos = tarjetaMetrica(
                "Ingresos totales",
                formatear(metricas.calcularIngresosTotales()),
                "Excluye pedidos cancelados",
                Iconos.GLOBO);

        // --- Pedidos por estado ---
        Map<EstadoPedido, Long> porEstado = metricas.pedidosPorEstado();
        HBox tarjetaPedidos = tarjetaMetrica(
                "Pedidos por estado",
                estadoResumen(porEstado),
                "Total: " + metricas.totalPedidos(),
                Iconos.PAQUETE);

        // --- Ventas por vendedor ---
        Map<String, Double> porVendedor = metricas.ventasPorVendedor();
        HBox tarjetaVendedores = tarjetaMetrica(
                "Ventas por vendedor",
                vendedoresResumen(porVendedor),
                "Ingresos por cada vendedor",
                Iconos.PERSONAS);

        // --- Producto más vendido ---
        HBox tarjetaProducto = tarjetaMetrica(
                "Producto más vendido",
                metricas.productoMasVendido(),
                "Unidades vendidas (excluye cancelados)",
                Iconos.BOLSA);

        tarjetas.getChildren().addAll(tarjetaIngresos, tarjetaPedidos, tarjetaVendedores, tarjetaProducto);

        raiz.getChildren().addAll(titulo, subtitulo, tarjetas);
    }

    private HBox tarjetaMetrica(String titulo, String valor, String pista, String iconoSvg) {
        HBox tarjeta = new HBox(16);
        tarjeta.getStyleClass().add("fila-metrica");
        tarjeta.setPadding(new Insets(16));
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        Icono icono = new Icono(iconoSvg, 40);
        icono.getStyleClass().add("icono-metrica");

        VBox contenido = new VBox(4);
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("valor-metrica");
        lblValor.setWrapText(true);
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("nombre-sesion");
        Label lblPista = new Label(pista);
        lblPista.getStyleClass().add("hint");
        lblPista.setWrapText(true);

        contenido.getChildren().addAll(lblValor, lblTitulo, lblPista);
        HBox.setHgrow(contenido, Priority.ALWAYS);

        tarjeta.getChildren().addAll(icono, contenido);
        return tarjeta;
    }

    private String estadoResumen(Map<EstadoPedido, Long> porEstado) {
        StringBuilder sb = new StringBuilder();
        if (porEstado.containsKey(EstadoPedido.PENDIENTE)) {
            sb.append("Pendientes: ").append(porEstado.get(EstadoPedido.PENDIENTE)).append("  ");
        }
        if (porEstado.containsKey(EstadoPedido.ENVIADO)) {
            sb.append("Enviados: ").append(porEstado.get(EstadoPedido.ENVIADO)).append("  ");
        }
        if (porEstado.containsKey(EstadoPedido.ENTREGADO)) {
            sb.append("Entregados: ").append(porEstado.get(EstadoPedido.ENTREGADO)).append("  ");
        }
        if (porEstado.containsKey(EstadoPedido.CANCELADO)) {
            sb.append("Cancelados: ").append(porEstado.get(EstadoPedido.CANCELADO));
        }
        return sb.length() == 0 ? "Sin pedidos" : sb.toString().trim();
    }

    private String vendedoresResumen(Map<String, Double> porVendedor) {
        if (porVendedor.isEmpty()) {
            return "Sin ventas";
        }
        return porVendedor.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> entry.getKey() + ": " + formatear(entry.getValue()))
                .collect(java.util.stream.Collectors.joining("  ·  "));
    }

    private static String formatear(double valor) {
        return String.format(java.util.Locale.US, "$%,.2f", valor);
    }
}
