package com.mycompany.medigest.DAO;

/**
 * Error al acceder a la base de datos. Envuelve la SQLException para que
 * la capa de servicios no tenga que manejar excepciones de JDBC.
 */
public class DAOException extends RuntimeException {

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
