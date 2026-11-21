package com.mycompany.medigest.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.mycompany.medigest.entidades.ObraSocial;
import com.mycompany.medigest.entidades.Paciente;

/**
 * Acceso a la tabla paciente. El contacto se guarda en la columna telefono.
 * Trae la obra social completa con un JOIN.
 */
public class PacienteDAO extends BaseDAO {

    private static final String SELECT =
            "SELECT p.id_paciente, p.dni, p.nombre, p.telefono,"
            + " o.id_obra_social, o.nombre AS os_nombre, o.codigo AS os_codigo, o.activa AS os_activa"
            + " FROM paciente p LEFT JOIN obra_social o ON o.id_obra_social = p.id_obra_social";

    /** Inserta y le asigna el id generado. Falla si ya existe un paciente con ese DNI. */
    public int insertar(Paciente p) {
        int id = insertar("INSERT INTO paciente (dni, nombre, telefono, id_obra_social) VALUES (?, ?, ?, ?)",
                p.getDni(), p.getUsuario().getNombreApellido(), p.getTelefono(), idObraSocial(p));
        p.setId(id);
        return id;
    }
    //Correccion: es p.getUsuario().getNombreApellido() no solo p.getUsuario
    public boolean actualizar(Paciente p) {
        return ejecutar("UPDATE paciente SET dni = ?, nombre = ?, telefono = ?, id_obra_social = ? WHERE id_paciente = ?",
                p.getDni(), p.getUsuario().getNombreApellido(), p.getTelefono(), idObraSocial(p), p.getId()) > 0;
    }

    /** Baja lógica: deja de aparecer en los listados, pero se conservan sus turnos. */
    public boolean eliminar(int id) {
        return ejecutar("UPDATE paciente SET activo = 0 WHERE id_paciente = ?", id) > 0;
    }

    /** Vuelve a activar un paciente dado de baja. */
    public boolean reactivar(int id) {
        return ejecutar("UPDATE paciente SET activo = 1 WHERE id_paciente = ?", id) > 0;
    }

    /** Devuelve el paciente (activo o no) o null. */
    public Paciente buscarPorId(int id) {
        return buscarUno(SELECT + " WHERE p.id_paciente = ?", this::mapear, id);
    }

    /** Devuelve el paciente (activo o no) o null. */
    public Paciente buscarPorDni(String dni) {
        return buscarUno(SELECT + " WHERE p.dni = ?", this::mapear, dni);
    }

    /** Pacientes activos cuyo nombre contiene el texto (para un buscador). */
    public List<Paciente> buscarPorNombre(String texto) {
        return buscarLista(SELECT + " WHERE p.activo = 1 AND p.nombre LIKE ? ORDER BY p.nombre",
                this::mapear, "%" + texto + "%");
    }

    /** Pacientes activos. */
    public List<Paciente> listarTodos() {
        return buscarLista(SELECT + " WHERE p.activo = 1 ORDER BY p.nombre", this::mapear);
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        ObraSocial obraSocial = null;
        Integer idObraSocial = rs.getObject("id_obra_social", Integer.class);
        if (idObraSocial != null) {
            obraSocial = new ObraSocial(idObraSocial, rs.getString("os_nombre"),
                    rs.getString("os_codigo"), rs.getBoolean("os_activa"));
        }
        return new Paciente(
                rs.getInt("id_paciente"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                obraSocial);
    }

    private static Integer idObraSocial(Paciente p) {
        return p.getObraSocial() == null ? null : p.getObraSocial().getId();
    }
}
