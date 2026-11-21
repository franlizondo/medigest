/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

/**
 *
 * @author pc
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GestorFrameToDB {


    // Configuración de tu base de datos (se define una sola vez aquí)
    private static final String URL_BD = "jdbc:mysql://localhost:3306/tu_base_datos";
    private static final String USER_BD = "root";
    private static final String PASS_BD = "tu_contraseña";

    // ==========================================
    // MÉTODO 1: ACTUALIZACIÓN DE BASE DE DATOS
    // ==========================================
    /**
     * Ejecuta cualquier consulta SQL de actualización (INSERT, UPDATE, DELETE).
     * @param sql La consulta SQL con signos de interrogación '?' como comodines.
     * @param parametros Los valores que reemplazarán a los '?' en orden.
     * @return true si la operación afectó al menos a una fila, false de lo contrario.
     */
    public static boolean ejecutarActualizacion(String sql, Object... parametros) {
        // El bloque try-with-resources asegura que la conexión y el statement se cierren solos
        try (Connection conn = DriverManager.getConnection(URL_BD, USER_BD, PASS_BD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Inyectamos los parámetros dinámicamente sin importar el tipo (String, int, etc.)
            for (int i = 0; i < parametros.length; i++) {
                pstmt.setObject(i + 1, parametros[i]);
            }
            
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error en la Base de Datos: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // MÉTODO 2: FILTRADO EN TIEMPO REAL DE TABLAS
    // ==========================================
    /**
     * Aplica un filtro de búsqueda regular a cualquier JTable en base a un texto.
     * @param tabla La tabla visual que se desea filtrar.
     * @param textoBusqueda El texto escrito por el usuario (ej: desde un JTextField).
     */
    public static void filtrarTabla(JTable tabla, String textoBusqueda) {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
            tabla.setRowSorter(sorter);
            
            // (?i) hace que la búsqueda ignore mayúsculas y minúsculas (case-insensitive)
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textoBusqueda));
        } catch (Exception e) {
            System.err.println("⚠️ Error al aplicar el filtro en la tabla: " + e.getMessage());
        }
    }
    
}
