package com.proyecto.servicio;

import com.proyecto.modelo.Rol;
import com.proyecto.modelo.Usuario;
import com.proyecto.persistencia.RepositorioUsuarios;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestión de usuarios para el administrador: listar todas las cuentas,
 * bloquear/desbloquear y cambiar rol. El administrador no puede desactivar ni
 * cambiar el rol de su propia cuenta (evita quedarse sin acceso al sistema).
 */
public class ServicioUsuarios {

    private final RepositorioUsuarios repositorioUsuarios;

    public ServicioUsuarios(RepositorioUsuarios repositorioUsuarios) {
        this.repositorioUsuarios = repositorioUsuarios;
    }

    /**
     * Todos los usuarios, ordenados alfabéticamente por nombre de usuario.
     */
    public List<Usuario> listarTodos() {
        return repositorioUsuarios.leerTodos().stream()
                .sorted(Comparator.comparing(Usuario::getUsuario, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Usuario buscado por ID.
     */
    public java.util.Optional<Usuario> buscarPorId(String id) {
        return repositorioUsuarios.buscarPorId(id);
    }

    /**
     * Bloquea o desbloquea un usuario. Un bloqueado no puede iniciar sesión.
     *
     * @throws ExcepcionNegocio si el usuario no existe o es la sesión propia.
     */
    public void alternarBloqueo(String idUsuario, String idSesion) {
        Usuario usuario = buscarUsuario(idUsuario);
        validarNoEsSesionPropia(usuario, idSesion);
        usuario.setActivo(!usuario.isActivo());
        repositorioUsuarios.guardar(usuario);
    }

    /**
     * true si el usuario está bloqueado.
     */
    public boolean estaBloqueado(Usuario usuario) {
        return !usuario.isActivo();
    }

    /**
     * Cambia el rol de un usuario (CLIENTE ↔ VENDEDOR → ADMINISTRADOR).
     *
     * @throws ExcepcionNegocio si el usuario no existe, el rol es inválido o
     *                          es la sesión propia.
     */
    public void cambiarRol(String idUsuario, Rol nuevoRol, String idSesion) {
        Usuario usuario = buscarUsuario(idUsuario);
        validarNoEsSesionPropia(usuario, idSesion);
        if (nuevoRol == null) {
            throw new ExcepcionNegocio("Selecciona un rol válido");
        }
        usuario.setRol(nuevoRol);
        repositorioUsuarios.guardar(usuario);
    }

    private Usuario buscarUsuario(String idUsuario) {
        return repositorioUsuarios.buscarPorId(idUsuario)
                .orElseThrow(() -> new ExcepcionNegocio("El usuario no existe"));
    }

    private void validarNoEsSesionPropia(Usuario usuario, String idSesion) {
        if (usuario.getId().equals(idSesion)) {
            throw new ExcepcionNegocio("No puedes modificar tu propia cuenta desde aquí");
        }
    }
}
