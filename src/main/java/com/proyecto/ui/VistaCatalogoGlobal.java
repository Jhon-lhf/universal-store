package com.proyecto.ui;

import com.proyecto.modelo.Producto;
import com.proyecto.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Catálogo global para el administrador: todos los productos de todos los
 * vendedores, con opción de eliminar cualquiera.
 */
public class VistaCatalogoGlobal {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);
    private final Label etContador = new Label();
    private final VBox listaProductos = new VBox(10);

    public VistaCatalogoGlobal(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador) {
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

        Label titulo = new Label("Catálogo global");
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
        List<Producto> productos = app.getServicioProductos().listarTodos();
        etContador.setText(productos.size()
                + (productos.size() == 1 ? " producto" : " productos"));

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
        Icono icono = new Icono(Iconos.TIENDA, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Sin productos");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("El catálogo global está vacío");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node filaProducto(Producto producto) {
        String nombreCategoria = app.getServicioProductos().nombreCategoria(producto.getCategoriaId());

        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(16));

        // --- Imagen del producto ---
        ImagenProducto imagen = new ImagenProducto(
                producto.getNombreImagen(),
                VistaTarjetaProducto.iconoDe(nombreCategoria),
                48, 48);

        Label titulo = new Label(producto.getTitulo());
        titulo.getStyleClass().add("nombre-sesion");
        Label detalle = new Label(nombreCategoria + "  ·  " + formatear(producto.getPrecio())
                + "  ·  Stock: " + producto.getStock());
        detalle.getStyleClass().add("hint");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label vendedor = new Label("Vendedor: " + producto.getVendedorId());
        vendedor.getStyleClass().add("hint");

        HBox filaSuperior = new HBox(12, imagen, titulo, espaciador, vendedor);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn");
        btnEliminar.setOnAction(e -> eliminar(producto));

        HBox filaAcciones = new HBox(10, btnEliminar);
        filaAcciones.setAlignment(Pos.CENTER_LEFT);

        tarjeta.getChildren().addAll(filaSuperior, detalle, filaAcciones);
        return tarjeta;
    }

    private void eliminar(Producto producto) {
        try {
            app.getServicioProductos().eliminarPorId(producto.getId());
            refrescar();
            avisador.accept(Avisos.Tipo.INFO, "Producto \"" + producto.getTitulo() + "\" eliminado");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
