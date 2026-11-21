package com.proyecto.ui;

import com.proyecto.modelo.Producto;
import com.proyecto.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Inventario del vendedor: muestra los productos que publicó con su stock
 * actual y permite editarlo. Un producto con stock bajo (< 5) o agotado se
 * señala con un chip de aviso.
 */
public class VistaMiStock {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);
    private final Label etContador = new Label();
    private final VBox listaProductos = new VBox(10);

    public VistaMiStock(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador) {
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

        Label titulo = new Label("Mi stock");
        titulo.getStyleClass().add("titulo-panel");
        etContador.getStyleClass().add("texto-apagado");
        HBox cabecera = new HBox(10, titulo, etContador);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        ScrollPane desplazamiento = new ScrollPane(listaProductos);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        raiz.getChildren().addAll(cabecera, desplazamiento);
    }

    private void refrescar() {
        String vendedorId = app.getSesion().getUsuario().getId();
        List<Producto> productos = app.getServicioProductos().listarPorVendedor(vendedorId);

        etContador.setText(productos.size()
                + (productos.size() == 1 ? " producto publicado" : " productos publicados"));

        listaProductos.getChildren().clear();
        if (productos.isEmpty()) {
            listaProductos.getChildren().add(estadoVacio());
            return;
        }
        for (Producto producto : productos) {
            listaProductos.getChildren().add(filaProducto(producto));
        }
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.ETIQUETA, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Aún no publicas productos");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Ve a \"Nuevo producto\" para publicar tu primer artículo en el catálogo");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node filaProducto(Producto producto) {
        String nombreCategoria = app.getServicioProductos().nombreCategoria(producto.getCategoriaId());

        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(16));

        // --- Fila principal: título, categoría, precio ---
        Label titulo = new Label(producto.getTitulo());
        titulo.getStyleClass().add("nombre-sesion");
        Label detalle = new Label(nombreCategoria + "  ·  " + formatear(producto.getPrecio()));
        detalle.getStyleClass().add("hint");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label stockActual = new Label("Stock: " + producto.getStock());
        stockActual.getStyleClass().add("nombre-sesion");

        Label chipEstado;
        if (producto.estaAgotado()) {
            chipEstado = new Label("AGOTADO");
            chipEstado.getStyleClass().addAll("chip", "chip-peligro");
        } else if (producto.tieneStockBajo()) {
            chipEstado = new Label("Stock bajo");
            chipEstado.getStyleClass().addAll("chip", "chip-alerta");
        } else {
            chipEstado = new Label("OK");
            chipEstado.getStyleClass().addAll("chip", "chip-exito");
        }

        HBox filaSuperior = new HBox(10, titulo, espaciador, stockActual, chipEstado);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        // --- Edición de stock ---
        TextField campoStock = new TextField(String.valueOf(producto.getStock()));
        campoStock.getStyleClass().add("campo");
        campoStock.setPrefWidth(70);
        campoStock.setMaxWidth(70);

        Button btnActualizar = new Button("Actualizar");
        btnActualizar.getStyleClass().add("btn");
        btnActualizar.setOnAction(e -> actualizarStock(producto, campoStock.getText()));

        Label pistaEdicion = new Label("Solo números enteros, sin negativos");
        pistaEdicion.getStyleClass().add("hint");

        HBox filaEdicion = new HBox(10);
        filaEdicion.setAlignment(Pos.CENTER_LEFT);
        filaEdicion.getChildren().addAll(
                new Label("Nuevo stock:"),
                campoStock,
                btnActualizar,
                pistaEdicion);

        tarjeta.getChildren().addAll(filaSuperior, detalle, filaEdicion);
        return tarjeta;
    }

    private void actualizarStock(Producto producto, String texto) {
        try {
            int nuevoStock = Integer.parseInt(texto.trim());
            app.getServicioProductos().actualizarStock(producto.getId(), nuevoStock);
            refrescar();
            avisador.accept(Avisos.Tipo.EXITO, "Stock de \"" + producto.getTitulo()
                    + "\" actualizado a " + nuevoStock);
        } catch (NumberFormatException e) {
            avisador.accept(Avisos.Tipo.ERROR, "El stock debe ser un número entero");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
