package com.mycompany.medigest.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.mycompany.medigest.entidades.Recepcionista;
import com.mycompany.medigest.entidades.Turno;
import com.mycompany.medigest.entidades.Turno.EstadoTurno;
import com.mycompany.medigest.entidades.Usuario;

/**
 * Acceso a la tabla turno. Devuelve cada Turno con su Paciente, Medico y Recepcionista completos.
 * El motivo de consulta se guarda en la columna observacion.
 * En todos los métodos, idMedico es el id del Medico (medico.getId()).
 */
public class TurnoDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT t.id_turno, t.id_paciente, m.id_usuario AS id_usuario_medico, t.id_recepcionista,"
            + " t.fecha, t.hora_inicio, t.hora_fin, t.estado, t.observacion"
            + " FROM turno t JOIN medico m ON m.id_medico = t.id_medico";

    /** Turnos que ocupan horario (los cancelados liberan el lugar). */
    private static final String OCUPA_HORARIO = " AND t.estado <> 'CANCELADO'";

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Guarda el turno y devuelve el id generado.
     * (Turno no tiene setId: si necesitan el objeto con id, usar buscarPorId con el valor devuelto.)
     * La recepcionista puede ser null.
     */
    public int insertar(Turno t) {
        return insertar("INSERT INTO turno (id_paciente, id_medico, id_recepcionista, fecha, hora_inicio, hora_fin, estado, observacion)"
                + " VALUES (?, " + ID_MEDICO + ", ?, ?, ?, ?, ?, ?)",
                t.getPaciente().getId(), t.getMedico().getId(), idRecepcionista(t),
                t.getFecha(), t.getHoraInicio(), t.getHoraFin(), estado(t), t.getMotivoConsulta());
    }

    public boolean actualizar(Turno t) {
        return ejecutar("UPDATE turno SET id_paciente = ?, id_medico = " + ID_MEDICO + ", id_recepcionista = ?,"
                + " fecha = ?, hora_inicio = ?, hora_fin = ?, estado = ?, observacion = ? WHERE id_turno = ?",
                t.getPaciente().getId(), t.getMedico().getId(), idRecepcionista(t),
                t.getFecha(), t.getHoraInicio(), t.getHoraFin(), estado(t), t.getMotivoConsulta(), t.getId()) > 0;
    }

    /** Para confirmar, cancelar, marcar ausente o atendido. */
    public boolean cambiarEstado(int idTurno, EstadoTurno estado) {
        return ejecutar("UPDATE turno SET estado = ? WHERE id_turno = ?", estado, idTurno) > 0;
    }

    /** Borra el turno. Para cancelarlo conviene usar cambiarEstado(id, CANCELADO) y no perder el registro. */
    public boolean eliminar(int id) {
        return ejecutar("DELETE FROM turno WHERE id_turno = ?", id) > 0;
    }

    public Turno buscarPorId(int id) {
        return buscarUno(SELECT + " WHERE t.id_turno = ?", this::mapear, id);
    }

    public List<Turno> listarTodos() {
        return buscarLista(SELECT + " ORDER BY t.fecha, t.hora_inicio", this::mapear);
    }

    /** Todos los turnos de un día, de todos los médicos. */
    public List<Turno> listarPorFecha(LocalDate fecha) {
        return buscarLista(SELECT + " WHERE t.fecha = ? ORDER BY t.hora_inicio, m.id_medico", this::mapear, fecha);
    }

    /** Agenda del médico en un día. */
    public List<Turno> listarPorMedicoYFecha(int idMedico, LocalDate fecha) {
        return buscarLista(SELECT + " WHERE m.id_usuario = ? AND t.fecha = ? ORDER BY t.hora_inicio",
                this::mapear, idMedico, fecha);
    }

    /** Turnos del médico entre dos fechas (incluidas). */
    public List<Turno> listarPorMedicoEntreFechas(int idMedico, LocalDate desde, LocalDate hasta) {
        return buscarLista(SELECT + " WHERE m.id_usuario = ? AND t.fecha BETWEEN ? AND ? ORDER BY t.fecha, t.hora_inicio",
                this::mapear, idMedico, desde, hasta);
    }

    /** Turnos del paciente, del más reciente al más viejo. */
    public List<Turno> listarPorPaciente(int idPaciente) {
        return buscarLista(SELECT + " WHERE t.id_paciente = ? ORDER BY t.fecha DESC, t.hora_inicio DESC",
                this::mapear, idPaciente);
    }

    /**
     * true si el médico ya tiene un turno no cancelado que se pisa con ese horario.
     * idTurnoExcluido sirve al modificar un turno para no compararlo consigo mismo; al crear, pasar 0.
     */
    public boolean existeSuperposicionMedico(int idMedico, LocalDate fecha, LocalTime inicio, LocalTime fin,
            int idTurnoExcluido) {
        // dos horarios se pisan si cada uno empieza antes de que termine el otro
        return existe("SELECT COUNT(*) FROM turno t JOIN medico m ON m.id_medico = t.id_medico"
                + " WHERE m.id_usuario = ? AND t.fecha = ? AND t.hora_inicio < ? AND t.hora_fin > ? AND t.id_turno <> ?"
                + OCUPA_HORARIO, idMedico, fecha, fin, inicio, idTurnoExcluido);
    }

    /** Igual que existeSuperposicionMedico, pero con los turnos del paciente. */
    public boolean existeSuperposicionPaciente(int idPaciente, LocalDate fecha, LocalTime inicio, LocalTime fin,
            int idTurnoExcluido) {
        return existe("SELECT COUNT(*) FROM turno t"
                + " WHERE t.id_paciente = ? AND t.fecha = ? AND t.hora_inicio < ? AND t.hora_fin > ? AND t.id_turno <> ?"
                + OCUPA_HORARIO, idPaciente, fecha, fin, inicio, idTurnoExcluido);
    }

    private Turno mapear(ResultSet rs) throws SQLException {
        Recepcionista recepcionista = null;
        Integer idRecepcionista = rs.getObject("id_recepcionista", Integer.class);
        if (idRecepcionista != null) {
            Usuario u = usuarioDAO.buscarPorId(idRecepcionista);
            if (u instanceof Recepcionista) {
                recepcionista = (Recepcionista) u;
            }
        }
        Turno t = new Turno(
                rs.getInt("id_turno"),
                rs.getObject("fecha", LocalDate.class),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fin", LocalTime.class),
                pacienteDAO.buscarPorId(rs.getInt("id_paciente")),
                medicoDAO.buscarPorId(rs.getInt("id_usuario_medico")),
                recepcionista,
                rs.getString("observacion"));
        t.setEstado(EstadoTurno.valueOf(rs.getString("estado")));
        return t;
    }

    private static Integer idRecepcionista(Turno t) {
        return t.getRecepcionista() == null ? null : t.getRecepcionista().getId();
    }

    private static EstadoTurno estado(Turno t) {
        return t.getEstado() == null ? EstadoTurno.PENDIENTE : t.getEstado();
    }
}
