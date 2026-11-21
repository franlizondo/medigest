package com.mycompany.medigest.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Código JDBC que repiten todos los DAO: abrir la conexión, cargar los parámetros (?),
 * ejecutar y cerrar todo. Cada DAO solo escribe su SQL y cómo convertir una fila en objeto.
 */
public abstract class BaseDAO {

    /**
     * En las entidades, el id de un Medico es su id de usuario (porque Medico extiende Usuario).
     * Las tablas turno y disponibilidad usan id_medico, así que se traduce con esta subconsulta.
     */
    protected static final String ID_MEDICO = "(SELECT id_medico FROM medico WHERE id_usuario = ?)";

    /** Convierte la fila actual del ResultSet en un objeto. */
    protected interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    /** SELECT que devuelve una fila, o null si no hay resultados. */
    protected <T> T buscarUno(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> lista = buscarLista(sql, mapeador, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** SELECT que devuelve varias filas. */
    protected <T> List<T> buscarLista(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> lista = new ArrayList<T>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            cargarParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapeador.mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar la base: " + e.getMessage(), e);
        }
        return lista;
    }

    /** SELECT COUNT(*) ...: true si el resultado es mayor a 0. */
    protected boolean existe(String sql, Object... parametros) {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            cargarParametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar la base: " + e.getMessage(), e);
        }
    }

    /** UPDATE o DELETE. Devuelve la cantidad de filas afectadas. */
    protected int ejecutar(String sql, Object... parametros) {
        try (Connection con = Conexion.getConexion()) {
            return ejecutarEn(con, sql, parametros);
        } catch (SQLException e) {
            throw new DAOException("Error al modificar la base: " + e.getMessage(), e);
        }
    }

    /** INSERT. Devuelve el id autoincremental generado. */
    protected int insertar(String sql, Object... parametros) {
        try (Connection con = Conexion.getConexion()) {
            return insertarEn(con, sql, parametros);
        } catch (SQLException e) {
            throw new DAOException("Error al insertar en la base: " + e.getMessage(), e);
        }
    }

    /** Igual que ejecutar(), pero con una conexión ya abierta (para transacciones). */
    protected static int ejecutarEn(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            cargarParametros(ps, parametros);
            return ps.executeUpdate();
        }
    }

    /** Igual que insertar(), pero con una conexión ya abierta (para transacciones). */
    protected static int insertarEn(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(ps, parametros);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("La base no devolvió el id generado");
            }
        }
    }

    /** Hace rollback sin tapar el error original. */
    protected static void deshacer(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                // el error importante es el que provocó el rollback
            }
        }
    }

    /** Carga los ? en orden. Los enum se guardan por su nombre (por ejemplo CONFIRMADO). */
    private static void cargarParametros(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            Object valor = parametros[i];
            if (valor instanceof Enum) {
                valor = ((Enum<?>) valor).name();
            }
            ps.setObject(i + 1, valor);
        }
    }

    // ---- nombre y apellido ----
    // Las entidades usan un solo campo "nombreApellido"; la tabla usuario tiene nombre y apellido.

    /** "Carlos Gomez" -> {"Carlos", "Gomez"}. Toma la última palabra como apellido. */
    protected static String[] separarNombreApellido(String nombreApellido) {
        String texto = nombreApellido == null ? "" : nombreApellido.trim();
        int espacio = texto.lastIndexOf(' ');
        if (espacio < 0) {
            return new String[]{texto, ""};
        }
        return new String[]{texto.substring(0, espacio).trim(), texto.substring(espacio + 1)};
    }

    /** {"Carlos", "Gomez"} -> "Carlos Gomez" */
    protected static String unirNombreApellido(String nombre, String apellido) {
        return (nombre + " " + apellido).trim();
    }
}
