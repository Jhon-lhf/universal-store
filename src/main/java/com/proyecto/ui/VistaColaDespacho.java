package com.proyecto.ui;

import com.proyecto.estructuras.ColaFIFO;
import com.proyecto.modelo.EstadoPedido;
import com.proyecto.modelo.ItemPedido;
import com.proyecto.modelo.Pedido;
import com.proyecto.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Cola de despacho del vendedor (FIFO): muestra los pedidos pendientes que
 * contienen productos suyos, ordenados de más antiguo a más reciente.
 * Permite "Despachar siguiente" (pasa el primero a ENVIADO) y "Marcar
 * entregado" sobre pedidos ya despachados.
 *
 * <p>Usa {@link ColaFIFO} para garantizar el orden de llegada sin
 * reordenamientos arbitrarios.</p>
 */
public class VistaColaDespacho {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);
    private final Label etContador = new Label();
    private final VBox listaPedidos = new VBox(10);
    private final ColaFIFO<Pedido> cola = new ColaFIFO<>();

    public VistaColaDespacho(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador) {
        this.app = app;
        this.avisador = avisador;
        construir();
        refrescar();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Label titulo = new Label("Cola de despacho (FIFO)");
        titulo.getStyleClass().add("titulo-panel");
        etContador.getStyleClass().add("texto-apagado");
        HBox cabecera = new HBox(10, titulo, etContador);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Button btnDespachar = new Button("Despachar siguiente");
        btnDespachar.getStyleClass().addAll("btn", "btn-primario");
        btnDespachar.setOnAction(e -> despacharSiguiente());

        HBox accionesCabecera = new HBox(10, btnDespachar);
        accionesCabecera.setAlignment(Pos.CENTER_LEFT);

        ScrollPane desplazamiento = new ScrollPane(listaPedidos);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        raiz.getChildren().addAll(cabecera, accionesCabecera, desplazamiento);
    }

    private void refrescar() {
        String vendedorId = app.getSesion().getUsuario().getId();
        List<Pedido> pendientes = app.getServicioPedidos().listarPendientesPorVendedor(vendedorId);

        etContador.setText(pendientes.size()
                + (pendientes.size() == 1 ? " pedido pendiente" : " pedidos pendientes"));

        cola.vaciar();
        for (Pedido pedido : pendientes) {
            cola.encolar(pedido);
        }

        listaPedidos.getChildren().clear();
        if (pendientes.isEmpty()) {
            listaPedidos.getChildren().add(estadoVacio());
            return;
        }
        for (Pedido pedido : pendientes) {
            listaPedidos.getChildren().add(tarjetaPedido(pedido));
        }
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.CAMION, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Tu cola está vacía");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Cuando un cliente compre tus productos, aparecerán aquí en orden de llegada");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node tarjetaPedido(Pedido pedido) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(18));

        Label id = new Label(pedido.getId());
        id.getStyleClass().add("nombre-sesion");
        Label fecha = new Label(pedido.getFecha());
        fecha.getStyleClass().add("hint");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        Label chipEstado = new Label(pedido.getEstado().toString());
        chipEstado.getStyleClass().addAll("chip", chipDeEstado(pedido.getEstado()));

        HBox encabezado = new HBox(10, id, fecha, espaciador, chipEstado);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label cliente = new Label("Cliente: " + pedido.getClienteNombre());
        cliente.getStyleClass().add("texto-apagado");

        VBox items = new VBox(4);
        for (ItemPedido item : pedido.getItems()) {
            if (!item.getVendedorId().equals(app.getSesion().getUsuario().getId())) {
                continue;
            }
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            Label descripcion = new Label(item.getCantidad() + " × " + item.getTitulo());
            Region espaciadorItem = new Region();
            HBox.setHgrow(espaciadorItem, Priority.ALWAYS);
            Label valor = new Label(formatear(item.subtotal()));
            fila.getChildren().addAll(descripcion, espaciadorItem, valor);
            items.getChildren().add(fila);
        }

        HBox acciones = new HBox(10);
        acciones.setAlignment(Pos.CENTER_LEFT);
        boolean esPrimero = !cola.estaVacia() && cola.primero().getId().equals(pedido.getId());

        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            Button btnDespachar = new Button("Despachar");
            btnDespachar.getStyleClass().add("btn");
            btnDespachar.setDisable(!esPrimero);
            btnDespachar.setTooltip(new Tooltip(
                    esPrimero ? "Es el siguiente en la cola FIFO"
                            : "Hay pedidos más antiguos delante"));
            btnDespachar.setOnAction(e -> despachar(pedido));
            acciones.getChildren().add(btnDespachar);
        } else if (pedido.getEstado() == EstadoPedido.ENVIADO) {
            Button btnEntregar = new Button("Marcar entregado");
            btnEntregar.getStyleClass().add("btn");
            btnEntregar.setOnAction(e -> entregar(pedido));
            acciones.getChildren().add(btnEntregar);
        }

        tarjeta.getChildren().addAll(encabezado, cliente, items, acciones);
        return tarjeta;
    }

    private void despacharSiguiente() {
        try {
            Pedido siguiente = app.getServicioPedidos().despacharSiguiente(
                    app.getSesion().getUsuario().getId());
            refrescar();
            avisador.accept(Avisos.Tipo.EXITO, "Pedido " + siguiente.getId()
                    + " despachado (→ ENVIADO)");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void despachar(Pedido pedido) {
        despacharSiguiente();
    }

    private void entregar(Pedido pedido) {
        try {
            app.getServicioPedidos().marcarEntregado(pedido.getId(),
                    app.getSesion().getUsuario().getId());
            refrescar();
            avisador.accept(Avisos.Tipo.EXITO, "Pedido " + pedido.getId()
                    + " marcado como ENTREGADO");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private static String chipDeEstado(EstadoPedido estado) {
        return switch (estado) {
            case PENDIENTE -> "chip-alerta";
            case ENVIADO -> "chip-info";
            case ENTREGADO -> "chip-exito";
            case CANCELADO -> "chip-peligro";
        };
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
