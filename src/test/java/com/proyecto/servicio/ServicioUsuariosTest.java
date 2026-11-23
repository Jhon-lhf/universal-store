package com.proyecto.servicio;

import com.proyecto.modelo.Rol;
import com.proyecto.modelo.Usuario;
import com.proyecto.persistencia.RepositorioUsuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de gestión de usuarios para el administrador: bloquear, cambiar rol
 * y la restricción de no modificar la propia cuenta.
 */
class ServicioUsuariosTest {

    private static final Path ARCHIVO = Paths.get("data", "test-usuarios.json");

    private RepositorioUsuarios repositorioUsuarios;
    private ServicioUsuarios servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
        repositorioUsuarios = new RepositorioUsuarios(ARCHIVO);
        repositorioUsuarios.guardar(new Usuario("U-1", "admin", "hash", "Administrador",
                "admin@tienda.com", Rol.ADMINISTRADOR, true));
        repositorioUsuarios.guardar(new Usuario("U-2", "cliente", "hash", "Cliente Demo",
                "cliente@tienda.com", Rol.CLIENTE, true));
        repositorioUsuarios.guardar(new Usuario("U-3", "vendedor", "hash", "Vendedor Demo",
                "vendedor@tienda.com", Rol.VENDEDOR, true));
        servicio = new ServicioUsuarios(repositorioUsuarios);
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
    }

    @Test
    void listarTodosDevuelveTodosOrdenados() {
        assertEquals(3, servicio.listarTodos().size());
        assertEquals("admin", servicio.listarTodos().get(0).getUsuario());
        assertEquals("cliente", servicio.listarTodos().get(1).getUsuario());
        assertEquals("vendedor", servicio.listarTodos().get(2).getUsuario());
    }

    @Test
    void alternarBloqueoFunciona() {
        assertFalse(servicio.estaBloqueado(repositorioUsuarios.buscarPorNombreUsuario("cliente").orElseThrow()));

        servicio.alternarBloqueo("U-2", "U-1");
        assertTrue(servicio.estaBloqueado(repositorioUsuarios.buscarPorNombreUsuario("cliente").orElseThrow()));

        servicio.alternarBloqueo("U-2", "U-1");
        assertFalse(servicio.estaBloqueado(repositorioUsuarios.buscarPorNombreUsuario("cliente").orElseThrow()));
    }

    @Test
    void cambiarRolFunciona() {
        servicio.cambiarRol("U-2", Rol.VENDEDOR, "U-1");
        assertEquals(Rol.VENDEDOR, repositorioUsuarios.buscarPorId("U-2").orElseThrow().getRol());

        servicio.cambiarRol("U-2", Rol.ADMINISTRADOR, "U-1");
        assertEquals(Rol.ADMINISTRADOR, repositorioUsuarios.buscarPorId("U-2").orElseThrow().getRol());
    }

    @Test
    void noPuedeModificarSuPropiaCuenta() {
        assertThrows(ExcepcionNegocio.class, () -> servicio.alternarBloqueo("U-1", "U-1"));
        assertThrows(ExcepcionNegocio.class, () -> servicio.cambiarRol("U-1", Rol.CLIENTE, "U-1"));

        // La cuenta del admin no debe haber cambiado.
        assertTrue(repositorioUsuarios.buscarPorId("U-1").orElseThrow().isActivo());
        assertEquals(Rol.ADMINISTRADOR, repositorioUsuarios.buscarPorId("U-1").orElseThrow().getRol());
    }

    @Test
    void usuarioInexistenteLanzaExcepcion() {
        assertThrows(ExcepcionNegocio.class, () -> servicio.alternarBloqueo("U-999", "U-1"));
        assertThrows(ExcepcionNegocio.class, () -> servicio.cambiarRol("U-999", Rol.CLIENTE, "U-1"));
    }
}
