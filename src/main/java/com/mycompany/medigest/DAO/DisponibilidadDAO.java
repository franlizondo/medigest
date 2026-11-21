package com.mycompany.medigest.DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.mycompany.medigest.entidades.Medico.Disponibilidad;

/**
 * Acceso a la tabla disponibilidad (días y horarios en que atiende cada médico).
 * En todos los métodos, idMedico es el id del Medico (medico.getId()).
 */
public class DisponibilidadDAO extends BaseDAO {

    private static final String SELECT = "SELECT dia_semana, hora_inicio, hora_fin FROM disponibilidad"
            + " WHERE id_medico = " + ID_MEDICO + " AND activa = 1";

    /** Ordena de lunes a domingo (el enum de la base sigue ese orden). */
    private static final String ORDEN = " ORDER BY dia_semana, hora_inicio";

    public List<Disponibilidad> listarPorMedico(int idMedico) {
        return buscarLista(SELECT + ORDEN, this::mapear, idMedico);
    }

    public List<Disponibilidad> listarPorMedicoYDia(int idMedico, DayOfWeek dia) {
        return buscarLista(SELECT + " AND dia_semana = ?" + ORDEN, this::mapear, idMedico, dia);
    }

    /** true si el horario cae completo dentro de alguna franja en la que atiende el médico ese día. */
    public boolean atiende(int idMedico, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        return existe("SELECT COUNT(*) FROM disponibilidad WHERE id_medico = " + ID_MEDICO
                + " AND activa = 1 AND dia_semana = ? AND hora_inicio <= ? AND hora_fin >= ?",
                idMedico, fecha.getDayOfWeek(), inicio, fin);
    }

    public void agregar(int idMedico, Disponibilidad d) {
        insertar("INSERT INTO disponibilidad (id_medico, dia_semana, hora_inicio, hora_fin) VALUES ("
                + ID_MEDICO + ", ?, ?, ?)", idMedico, d.getDia(), d.getHoraInicio(), d.getHoraFin());
    }

    /** Borra la franja del médico que empieza ese día a esa hora. */
    public boolean quitar(int idMedico, DayOfWeek dia, LocalTime horaInicio) {
        return ejecutar("DELETE FROM disponibilidad WHERE id_medico = " + ID_MEDICO
                + " AND dia_semana = ? AND hora_inicio = ?", idMedico, dia, horaInicio) > 0;
    }

    /** Reemplaza todas las franjas del médico por las de la lista. */
    public void reemplazar(int idMedico, List<Disponibilidad> disponibilidades) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);
            reemplazarEn(con, idMedico, disponibilidades);
            con.commit();
        } catch (SQLException e) {
            deshacer(con);
            throw new DAOException("No se pudieron guardar las disponibilidades: " + e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ignorada) {
                    // nada que hacer
                }
            }
        }
    }

    /** Versión que usa una conexión ya abierta (la usa MedicoDAO dentro de su transacción). */
    static void reemplazarEn(Connection con, int idMedico, List<Disponibilidad> disponibilidades)
            throws SQLException {
        ejecutarEn(con, "DELETE FROM disponibilidad WHERE id_medico = " + ID_MEDICO, idMedico);
        for (Disponibilidad d : disponibilidades) {
            insertarEn(con, "INSERT INTO disponibilidad (id_medico, dia_semana, hora_inicio, hora_fin) VALUES ("
                    + ID_MEDICO + ", ?, ?, ?)", idMedico, d.getDia(), d.getHoraInicio(), d.getHoraFin());
        }
    }

    private Disponibilidad mapear(ResultSet rs) throws SQLException {
        return new Disponibilidad(
                DayOfWeek.valueOf(rs.getString("dia_semana")),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fin", LocalTime.class));
    }
}
