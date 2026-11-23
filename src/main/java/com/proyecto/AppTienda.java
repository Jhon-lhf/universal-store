package com.proyecto;

import com.proyecto.persistencia.RepositorioCategorias;
import com.proyecto.persistencia.RepositorioDeseos;
import com.proyecto.persistencia.RepositorioPedidos;
import com.proyecto.persistencia.RepositorioProductos;
import com.proyecto.persistencia.RepositorioSolicitudesCategoria;
import com.proyecto.persistencia.RepositorioUsuarios;
import com.proyecto.servicio.ServicioAuth;
import com.proyecto.servicio.ServicioCarrito;
import com.proyecto.servicio.ServicioCategorias;
import com.proyecto.servicio.ServicioDeseos;
import com.proyecto.servicio.ServicioMetricas;
import com.proyecto.servicio.ServicioPedidos;
import com.proyecto.servicio.ServicioProductos;
import com.proyecto.servicio.ServicioResenas;
import com.proyecto.servicio.ServicioUsuarios;
import com.proyecto.servicio.SesionActual;
import com.proyecto.ui.GestorEscenas;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada JavaFX: construye la capa de servicios (con sus
 * repositorios), siembra los datos demo si hace falta y entrega el control
 * al {@link GestorEscenas}, que gestiona la navegación y el tema.
 */
public class AppTienda extends Application {

    @Override
    public void start(Stage escenario) {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        RepositorioProductos repoProductos = new RepositorioProductos();
        RepositorioCategorias repoCategorias = new RepositorioCategorias();
        RepositorioSolicitudesCategoria repoSolicitudes = new RepositorioSolicitudesCategoria();
        RepositorioPedidos repoPedidos = new RepositorioPedidos();
        RepositorioDeseos repoDeseos = new RepositorioDeseos();

        Semilla.ejecutarSiNecesario(repoUsuarios, repoProductos, repoCategorias, repoDeseos);

        // La sesión se crea aquí para que el carrito (que vive en ella) y el
        // gestor de escenas compartan el mismo contexto.
        SesionActual sesion = new SesionActual();
        ServicioCarrito servicioCarrito = new ServicioCarrito(repoProductos, sesion);

        GestorEscenas app = new GestorEscenas(
                escenario,
                sesion,
                new ServicioAuth(repoUsuarios),
                new ServicioProductos(repoProductos, repoCategorias),
                new ServicioCategorias(repoCategorias, repoSolicitudes, repoProductos),
                servicioCarrito,
                new ServicioPedidos(repoPedidos, repoProductos),
                new ServicioResenas(repoProductos),
                new ServicioDeseos(repoDeseos, repoProductos),
                new ServicioUsuarios(repoUsuarios),
                new ServicioMetricas(repoPedidos, repoProductos));

        escenario.setTitle("Universal Store");

        app.mostrarLogin();
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
