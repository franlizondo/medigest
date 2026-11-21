package com.mycompany.medigest.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.mycompany.medigest.entidades.Administrador;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Recepcionista;
import com.mycompany.medigest.entidades.Rol;
import com.mycompany.medigest.entidades.Usuario;

/**
 * Acceso a la tabla usuario. Devuelve el objeto según el rol:
 * Administrador, Recepcionista o Medico (con su especialidad y disponibilidades).
 */
public class UsuarioDAO extends BaseDAO {

    private static final String SELECT = "SELECT id_usuario, nombre, apellido, contrasena, rol FROM usuario";

    /** Los pacientes no inician sesión: solo se trabaja con estos roles. */
    private static final String ROLES_DEL_SISTEMA = " rol IN ('ADMINISTRADOR', 'RECEPCIONISTA', 'MEDICO')";

    /** Devuelve el usuario activo con ese email y contraseña, o null si no coinciden. */
    public Usuario login(String email, String password) {
        return buscarUno(SELECT + " WHERE email = ? AND contrasena = ? AND activo = 1 AND" + ROLES_DEL_SISTEMA,
                this::mapear, email, password);
    }

    /** Devuelve el usuario o null. */
    public Usuario buscarPorId(int id) {
        return buscarUno(SELECT + " WHERE id_usuario = ? AND" + ROLES_DEL_SISTEMA, this::mapear, id);
    }

    public Usuario buscarPorEmail(String email) {
        return buscarUno(SELECT + " WHERE email = ? AND" + ROLES_DEL_SISTEMA, this::mapear, email);
    }

    /** Usuarios activos. */
    public List<Usuario> listarTodos() {
        return buscarLista(SELECT + " WHERE activo = 1 AND" + ROLES_DEL_SISTEMA + " ORDER BY apellido, nombre",
                this::mapear);
    }

    /** Usuarios activos de un rol. */
    public List<Usuario> listarPorRol(Rol rol) {
        return buscarLista(SELECT + " WHERE activo = 1 AND rol = ? ORDER BY apellido, nombre",
                this::mapear, rolEnBase(rol));
    }

    /**
     * Da de alta un usuario y le asigna el id generado.
     * El email va aparte porque la entidad Usuario no lo tiene (en la base es obligatorio y único).
     * Si es un Medico, también guarda su especialidad y disponibilidades.
     */
    public int insertar(Usuario u, String email) {
        if (u instanceof Medico) {
            return new MedicoDAO().insertar((Medico) u, email);
        }
        String[] nombre = separarNombreApellido(u.getNombreApellido());
        int id = insertar("INSERT INTO usuario (nombre, apellido, email, contrasena, rol) VALUES (?, ?, ?, ?, ?)",
                nombre[0], nombre[1], email, u.getPassword(), rolEnBase(u));
        u.setId(id);
        return id;
    }

    /** Actualiza nombre y contraseña (y si es Medico, también sus datos de médico). */
    public boolean actualizar(Usuario u) {
        if (u instanceof Medico) {
            return new MedicoDAO().actualizar((Medico) u);
        }
        String[] nombre = separarNombreApellido(u.getNombreApellido());
        return ejecutar("UPDATE usuario SET nombre = ?, apellido = ?, contrasena = ? WHERE id_usuario = ?",
                nombre[0], nombre[1], u.getPassword(), u.getId()) > 0;
    }

    public boolean cambiarEmail(int idUsuario, String email) {
        return ejecutar("UPDATE usuario SET email = ? WHERE id_usuario = ?", email, idUsuario) > 0;
    }

    /** Baja lógica: el usuario deja de poder iniciar sesión, pero no se borra. */
    public boolean eliminar(int id) {
        return ejecutar("UPDATE usuario SET activo = 0 WHERE id_usuario = ?", id) > 0;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nombreApellido = unirNombreApellido(rs.getString("nombre"), rs.getString("apellido"));
        String password = rs.getString("contrasena");
        String rol = rs.getString("rol");

        if (rol.equals("ADMINISTRADOR")) {
            return new Administrador(id, nombreApellido, password);
        }
        if (rol.equals("RECEPCIONISTA")) {
            return new Recepcionista(id, nombreApellido, password);
        }
        if (rol.equals("MEDICO")) {
            return new MedicoDAO().buscarPorId(id);
        }
        throw new SQLException("Rol no soportado por el sistema: " + rol);
    }

    /** Texto que va en la columna rol según la clase del objeto. */
    private static String rolEnBase(Usuario u) {
        if (u instanceof Administrador) {
            return "ADMINISTRADOR";
        }
        if (u instanceof Recepcionista) {
            return "RECEPCIONISTA";
        }
        if (u instanceof Medico) {
            return "MEDICO";
        }
        throw new IllegalArgumentException("Usar Administrador, Recepcionista o Medico, no Usuario directamente");
    }

    /** Texto que va en la columna rol. Funciona con Rol.RECEPECIONISTA (como está ahora) o si lo corrigen. */
    private static String rolEnBase(Rol rol) {
        if (rol.name().startsWith("RECEP")) {
            return "RECEPCIONISTA";
        }
        return rol.name();
    }
}
