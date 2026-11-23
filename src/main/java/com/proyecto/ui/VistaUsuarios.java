package com.proyecto.ui;

import com.proyecto.modelo.Rol;
import com.proyecto.modelo.Usuario;
import com.proyecto.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Gestión de usuarios para el administrador: listar todas las cuentas con su
 * rol y estado, y acciones para bloquear/desbloquear o cambiar el rol.
 * El administrador no puede modificar su propia cuenta desde aquí.
 */
public class VistaUsuarios {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);
    private final Label etContador = new Label();
    private final VBox listaUsuarios = new VBox(8);
    private final String idSesion;

    public VistaUsuarios(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador) {
        this.app = app;
        this.avisador = avisador;
        this.idSesion = app.getSesion().getUsuario().getId();
        construir();
        refrescar();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Label titulo = new Label("Usuarios");
        titulo.getStyleClass().add("titulo-panel");
        etContador.getStyleClass().add("texto-apagado");
        HBox cabecera = new HBox(10, titulo, etContador);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        ScrollPane desplazamiento = new ScrollPane(listaUsuarios);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        raiz.getChildren().addAll(cabecera, desplazamiento);
    }

    private void refrescar() {
        List<Usuario> usuarios = app.getServicioUsuarios().listarTodos();
        etContador.setText(usuarios.size()
                + (usuarios.size() == 1 ? " usuario" : " usuarios"));

        listaUsuarios.getChildren().clear();
        if (usuarios.isEmpty()) {
            listaUsuarios.getChildren().add(estadoVacio());
            return;
        }
        for (Usuario usuario : usuarios) {
            listaUsuarios.getChildren().add(filaUsuario(usuario));
        }
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.PERSONAS, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("No hay usuarios");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Las cuentas se crean desde el registro");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node filaUsuario(Usuario usuario) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(16));

        Label nombre = new Label(usuario.getNombre());
        nombre.getStyleClass().add("nombre-sesion");
        Label datos = new Label("@" + usuario.getUsuario() + "  ·  " + usuario.getCorreo());
        datos.getStyleClass().add("hint");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label estado = new Label(usuario.isActivo() ? "Activo" : "Bloqueado");
        estado.getStyleClass().add(usuario.isActivo() ? "chip-exito" : "chip-peligro");
        estado.getStyleClass().add(0, "chip");

        HBox filaSuperior = new HBox(10, nombre, espaciador, estado);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        // --- Acciones: bloquear/desbloquear y cambiar rol ---
        Button btnBloqueo = new Button(usuario.isActivo() ? "Bloquear" : "Desbloquear");
        btnBloqueo.getStyleClass().add("btn");
        btnBloqueo.setDisable(usuario.getId().equals(idSesion));
        btnBloqueo.setTooltip(new Tooltip(
                usuario.getId().equals(idSesion) ? "No puedes bloquear tu propia cuenta"
                        : (usuario.isActivo() ? "Bloquear cuenta" : "Desbloquear cuenta")));
        btnBloqueo.setOnAction(e -> alternarBloqueo(usuario));

        ComboBox<Rol> comboRol = new ComboBox<>();
        comboRol.getItems().addAll(Rol.values());
        comboRol.setValue(usuario.getRol());
        comboRol.setDisable(usuario.getId().equals(idSesion));
        comboRol.setPrefWidth(120);
        comboRol.setOnAction(e -> cambiarRol(usuario, comboRol.getValue()));

        HBox filaAcciones = new HBox(10, btnBloqueo, new Label("Rol:"), comboRol);
        filaAcciones.setAlignment(Pos.CENTER_LEFT);

        tarjeta.getChildren().addAll(filaSuperior, datos, filaAcciones);
        return tarjeta;
    }

    private void alternarBloqueo(Usuario usuario) {
        try {
            app.getServicioUsuarios().alternarBloqueo(usuario.getId(), idSesion);
            refrescar();
            String accion = usuario.isActivo() ? "bloqueado" : "desbloqueado";
            avisador.accept(Avisos.Tipo.INFO, "Usuario \"" + usuario.getUsuario() + "\" " + accion);
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void cambiarRol(Usuario usuario, Rol nuevoRol) {
        try {
            app.getServicioUsuarios().cambiarRol(usuario.getId(), nuevoRol, idSesion);
            refrescar();
            avisador.accept(Avisos.Tipo.INFO, "Rol de \"" + usuario.getUsuario()
                    + "\" cambiado a " + nuevoRol);
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }
}
