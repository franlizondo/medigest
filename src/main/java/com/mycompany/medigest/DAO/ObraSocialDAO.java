package com.mycompany.medigest.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import com.mycompany.medigest.entidades.ObraSocial;

/** Acceso a la tabla obra_social. */
public class ObraSocialDAO extends BaseDAO {

    private static final String SELECT = "SELECT id_obra_social, nombre, codigo, activa FROM obra_social";

    /** Inserta y le asigna el id generado. */
    public int insertar(ObraSocial o) {
        int id = insertar("INSERT INTO obra_social (nombre, codigo, activa) VALUES (?, ?, ?)",
                o.getNombre(), o.getCodigo(), o.isActiva());
        o.setId(id);
        return id;
    }

    public boolean actualizar(ObraSocial o) {
        return ejecutar("UPDATE obra_social SET nombre = ?, codigo = ?, activa = ? WHERE id_obra_social = ?",
                o.getNombre(), o.getCodigo(), o.isActiva(), o.getId()) > 0;
    }

    /** Baja lógica (activa = false). Los pacientes que la tienen la conservan. */
    public boolean eliminar(int id) {
        return ejecutar("UPDATE obra_social SET activa = 0 WHERE id_obra_social = ?", id) > 0;
    }

    public ObraSocial buscarPorId(int id) {
        return buscarUno(SELECT + " WHERE id_obra_social = ?", this::mapear, id);
    }

    public ObraSocial buscarPorNombre(String nombre) {
        return buscarUno(SELECT + " WHERE nombre = ?", this::mapear, nombre);
    }

    /** Todas, activas e inactivas. */
    public List<ObraSocial> listarTodas() {
        return buscarLista(SELECT + " ORDER BY nombre", this::mapear);
    }

    /** Las que se pueden asignar a un paciente (por ejemplo, para un combo). */
    public List<ObraSocial> listarActivas() {
        return buscarLista(SELECT + " WHERE activa = 1 ORDER BY nombre", this::mapear);
    }

    private ObraSocial mapear(ResultSet rs) throws SQLException {
        return new ObraSocial(
                rs.getInt("id_obra_social"),
                rs.getString("nombre"),
                rs.getString("codigo"),
                rs.getBoolean("activa"));
    }
}
