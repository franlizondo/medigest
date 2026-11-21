/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;

/**
 *
 * @author mcele
 */
import com.mycompany.medigest.DAO.UsuarioDAO;
import com.mycompany.medigest.entidades.Usuario;
import java.util.List;
import static com.mycompany.medigest.servicios.Utilidades.esVacio;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario iniciarSesion(String email, String password) {
        if (esVacio(email) || esVacio(password)) {
            throw new IllegalArgumentException("Debe ingresar el email y la contraseña.");
        }
        return usuarioDAO.login(email.trim(), password);
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public int registrarUsuario(Usuario usuario, String email) {
        if (usuario == null || esVacio(usuario.getNombreApellido()) || esVacio(usuario.getPassword()) || esVacio(email)) {
            throw new IllegalArgumentException("Todos los campos del usuario son obligatorios.");
        }
        if (usuarioDAO.buscarPorEmail(email.trim()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo electrónico.");
        }
        return usuarioDAO.insertar(usuario, email.trim());
    }

    public boolean darDeBaja(int idUsuario) {
        return usuarioDAO.eliminar(idUsuario);
    }
}
