package com.mycompany.medigest.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Da conexiones a la base de datos medigest (XAMPP: usuario root sin contraseña).
 * Requiere el driver mysql-connector-j agregado en Libraries.
 */
public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/medigest?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "";

    private Conexion() {
    }

    /** Abre una conexión nueva. Usarla con try-with-resources para que se cierre sola. */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
