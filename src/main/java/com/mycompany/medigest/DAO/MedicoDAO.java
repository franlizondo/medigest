package com.mycompany.medigest.DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.Disponibilidad;
import com.mycompany.medigest.entidades.Medico.EstadoMedico;

/**
 * Acceso a las tablas medico + usuario (+ disponibilidad).
 * El id del Medico es su id_usuario. El estado ACTIVO/INACTIVO se guarda en usuario.activo.
 */
public class MedicoDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT u.id_usuario, u.nombre, u.apellido, u.contrasena, u.activo, m.especialidad"
            + " FROM medico m JOIN usuario u ON u.id_usuario = m.id_usuario";

    private final DisponibilidadDAO disponibilidadDAO = new DisponibilidadDAO();

    /**
     * Da de alta al médico: crea su usuario (rol MEDICO), la fila en medico y sus disponibilidades,
     * todo junto (si algo falla no se guarda nada). Le asigna el id generado.
     */
    public int insertar(Medico m, String email) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);

            String[] nombre = separarNombreApellido(m.getNombreApellido());
            int idUsuario = insertarEn(con,
                    "INSERT INTO usuario (nombre, apellido, email, contrasena, rol, activo) VALUES (?, ?, ?, ?, 'MEDICO', ?)",
                    nombre[0], nombre[1], email, m.getPassword(), estaActivo(m));
            insertarEn(con, "INSERT INTO medico (id_usuario, especialidad) VALUES (?, ?)",
                    idUsuario, m.getEspecialidad());
            DisponibilidadDAO.reemplazarEn(con, idUsuario, disponibilidadesDe(m));

            con.commit();
            m.setId(idUsuario);
            return idUsuario;
        } catch (SQLException e) {
            deshacer(con);
            throw new DAOException("No se pudo dar de alta el médico: " + e.getMessage(), e);
        } finally {
            cerrar(con);
        }
    }

    /** Actualiza nombre, contraseña, estado, especialidad y reemplaza sus disponibilidades. */
    public boolean actualizar(Medico m) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);

            String[] nombre = separarNombreApellido(m.getNombreApellido());
            int filas = ejecutarEn(con,
                    "UPDATE usuario SET nombre = ?, apellido = ?, contrasena = ?, activo = ? WHERE id_usuario = ? AND rol = 'MEDICO'",
                    nombre[0], nombre[1], m.getPassword(), estaActivo(m), m.getId());
            if (filas == 0) {
                con.rollback();
                return false;
            }
            ejecutarEn(con, "UPDATE medico SET especialidad = ? WHERE id_usuario = ?", m.getEspecialidad(), m.getId());
            DisponibilidadDAO.reemplazarEn(con, m.getId(), disponibilidadesDe(m));

            con.commit();
            return true;
        } catch (SQLException e) {
            deshacer(con);
            throw new DAOException("No se pudo actualizar el médico: " + e.getMessage(), e);
        } finally {
            cerrar(con);
        }
    }

    /** Baja lógica: queda INACTIVO (no puede iniciar sesión) pero se conservan sus turnos. */
    public boolean eliminar(int id) {
        return ejecutar("UPDATE usuario SET activo = 0 WHERE id_usuario = ? AND rol = 'MEDICO'", id) > 0;
    }

    /** Devuelve el médico (activo o no) o null. */
    public Medico buscarPorId(int id) {
        return buscarUno(SELECT + " WHERE u.id_usuario = ?", this::mapear, id);
    }

    /** Todos los médicos, activos e inactivos. */
    public List<Medico> listarTodos() {
        return buscarLista(SELECT + " ORDER BY u.apellido, u.nombre", this::mapear);
    }

    public List<Medico> listarActivos() {
        return buscarLista(SELECT + " WHERE u.activo = 1 ORDER BY u.apellido, u.nombre", this::mapear);
    }

    /** Médicos activos de una especialidad. */
    public List<Medico> listarPorEspecialidad(String especialidad) {
        return buscarLista(SELECT + " WHERE u.activo = 1 AND m.especialidad = ? ORDER BY u.apellido, u.nombre",
                this::mapear, especialidad);
    }

    //NOTA_MH: Crean un objeto con 5 parametros cuando le corresponden 6.
    private Medico mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        
        // 1. Averiguamos el EstadoMedico directamente antes de instanciar la clase
        Medico.EstadoMedico estado = rs.getBoolean("activo") ? Medico.EstadoMedico.ACTIVO : Medico.EstadoMedico.INACTIVO;
        
        // 2. Instanciamos el objeto con los 6 parámetros exactos que exige su constructor actual
        Medico m = new Medico(
                rs.getString("especialidad"),
                estado, // El EstadoMedico que faltaba en la llamada original
                disponibilidadDAO.listarPorMedico(id),
                id,
                unirNombreApellido(rs.getString("nombre"), rs.getString("apellido")),
                rs.getString("contrasena")
        );
        
        return m;
    }

    /** Si no le cargaron estado, se toma como activo. */
    private static boolean estaActivo(Medico m) {
        return m.getEstado() != EstadoMedico.INACTIVO;
    }

    /** Medico.getDisponibilidades() falla si se creó con la lista en null: se toma como lista vacía. */
    private static List<Disponibilidad> disponibilidadesDe(Medico m) {
        try {
            return m.getDisponibilidades();
        } catch (NullPointerException e) {
            return new ArrayList<Disponibilidad>();
        }
    }

    private static void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignorada) {
                // nada que hacer
            }
        }
    }
}
