package com.proyecto.ui;

import com.proyecto.modelo.SolicitudCategoria;
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
import java.util.function.BiConsumer;

/**
 * Bandeja de solicitudes de categoría para el administrador: muestra las
 * solicitudes pendientes de los vendedores y permite aprobarlas (se crea la
 * categoría) o rechazarlas (no se crea nada).
 */
public class VistaSolicitudesCategorias {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);
    private final Label etContador = new Label();
    private final VBox listaSolicitudes = new VBox(10);

    public VistaSolicitudesCategorias(GestorEscenas app,
                                      BiConsumer<Avisos.Tipo, String> avisador) {
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

        Label titulo = new Label("Solicitudes de categoría");
        titulo.getStyleClass().add("titulo-panel");
        etContador.getStyleClass().add("texto-apagado");
        HBox cabecera = new HBox(10, titulo, etContador);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        ScrollPane desplazamiento = new ScrollPane(listaSolicitudes);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        raiz.getChildren().addAll(cabecera, desplazamiento);
    }

    private void refrescar() {
        List<SolicitudCategoria> solicitudes = app.getServicioCategorias().listarPendientes();
        etContador.setText(solicitudes.size()
                + (solicitudes.size() == 1 ? " solicitud pendiente" : " solicitudes pendientes"));

        listaSolicitudes.getChildren().clear();
        if (solicitudes.isEmpty()) {
            listaSolicitudes.getChildren().add(estadoVacio());
            return;
        }
        for (SolicitudCategoria solicitud : solicitudes) {
            listaSolicitudes.getChildren().add(tarjetaSolicitud(solicitud));
        }
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.CAPAS, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Sin solicitudes pendientes");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Los vendedores pueden pedir categorías nuevas desde el formulario de producto");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node tarjetaSolicitud(SolicitudCategoria solicitud) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(16));

        Label nombre = new Label(solicitud.getNombre());
        nombre.getStyleClass().add("nombre-sesion");
        Label fecha = new Label(solicitud.getFecha());
        fecha.getStyleClass().add("hint");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label chip = new Label(solicitud.getEstado().toString());
        chip.getStyleClass().addAll("chip", "chip-alerta");

        HBox filaSuperior = new HBox(10, nombre, fecha, espaciador, chip);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        Label justificacion = new Label(solicitud.getJustificacion());
        justificacion.setWrapText(true);
        justificacion.getStyleClass().add("texto-apagado");

        Button btnAprobar = new Button("Aprobar");
        btnAprobar.getStyleClass().addAll("btn", "btn-primario");
        btnAprobar.setOnAction(e -> aprobar(solicitud));

        Button btnRechazar = new Button("Rechazar");
        btnRechazar.getStyleClass().add("btn");
        btnRechazar.setOnAction(e -> rechazar(solicitud));

        HBox filaAcciones = new HBox(10, btnAprobar, btnRechazar);
        filaAcciones.setAlignment(Pos.CENTER_LEFT);

        tarjeta.getChildren().addAll(filaSuperior, justificacion, filaAcciones);
        return tarjeta;
    }

    private void aprobar(SolicitudCategoria solicitud) {
        try {
            app.getServicioCategorias().aprobar(solicitud.getId());
            refrescar();
            avisador.accept(Avisos.Tipo.EXITO, "Categoría \"" + solicitud.getNombre()
                    + "\" aprobada y creada");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void rechazar(SolicitudCategoria solicitud) {
        try {
            app.getServicioCategorias().rechazar(solicitud.getId());
            refrescar();
            avisador.accept(Avisos.Tipo.INFO, "Solicitud \"" + solicitud.getNombre()
                    + "\" rechazada");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }
}
