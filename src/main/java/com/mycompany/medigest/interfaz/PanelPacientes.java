/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelPacientes extends JPanel {

    private JTable tablaPacientes;
    private JTable tablaHistorial;
    private DefaultTableModel modeloPacientes;
    private DefaultTableModel modeloHistorial;
    private Color verdePrimario, verdeSecundario;

    public PanelPacientes(Color verdePrimario, Color verdeSecundario, Color grisFondo) {
        this.verdePrimario = verdePrimario;
        this.verdeSecundario = verdeSecundario;

        setLayout(new BorderLayout(10, 10));
        setBackground(grisFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Título de la Sección
        JLabel lblTitulo = new JLabel("Registro General de Pacientes e Historias Clínicas", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(verdePrimario);
        add(lblTitulo, BorderLayout.NORTH);

        // 2. CONFIGURACIÓN DE LA TABLA PRINCIPAL (PACIENTES)
        String[] colPacientes = {"ID Paciente", "Nombre Completo", "Información de Contacto"};
        Object[][] datosPacientes = {
            {"1020", "María Luz Delgado", "555-0192 | maria@email.com"},
            {"1021", "Juan Carlos Benítez", "555-0143 | juan@email.com"},
            {"1022", "Ana Laura Rosas", "555-0177 | ana@email.com"}
        };

        modeloPacientes = new DefaultTableModel(datosPacientes, colPacientes) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable directamente desde la celda
            }
        };
        tablaPacientes = new JTable(modeloPacientes);
        tablaPacientes.setRowHeight(35);
        tablaPacientes.setFont(new Font("Arial", Font.PLAIN, 14));

        // 3. CONFIGURACIÓN DE LA TABLA SECUNDARIA (HISTORIAL MÉDICO)
        String[] colHistorial = {"Fecha", "Diagnóstico / Motivo", "Médico Tratante", "Tratamiento / Notas"};
        modeloHistorial = new DefaultTableModel(null, colHistorial) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setFont(new Font("Arial", Font.PLAIN, 13));

        // 4. SISTEMA DE EVENTO: Detectar clic en paciente para cargar su historial
        tablaPacientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaPacientes.getSelectedRow();
                if (filaSeleccionada != -1) {
                    String idPaciente = tablaPacientes.getValueAt(filaSeleccionada, 0).toString();
                    cargarHistorialMedicoSQL(idPaciente);
                }
            }
        });

        // 5. DISEÑO CON JSPLITPANE EN LÍNEAS INDEPENDIENTES PARA EVITAR CONFLICTOS
        JScrollPane scrollPacientes = new JScrollPane(tablaPacientes);
        scrollPacientes.setBorder(BorderFactory.createTitledBorder("Lista de Pacientes"));

        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);
        scrollHistorial.setBorder(BorderFactory.createTitledBorder("Historial Clínico Seleccionado"));

        // Inicialización explícita del panel dividido horizontalmente [2]
        JSplitPane panelDividido = new JSplitPane();
        panelDividido.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        panelDividido.setLeftComponent(scrollPacientes);
        panelDividido.setRightComponent(scrollHistorial);
        panelDividido.setDividerLocation(450); 
        panelDividido.setResizeWeight(0.4);   
        panelDividido.setOpaque(false);

        add(panelDividido, BorderLayout.CENTER);
    }

    /**
     * Contiene la lógica SQL exacta para buscar las bitácoras en la base de datos de XAMPP.
     * Mantiene las llamadas intactas utilizando el GestorFrameToDB.
     */
    private void cargarHistorialMedicoSQL(String idPaciente) {
        // Limpiamos los registros del paciente anterior antes de intentar cargar los nuevos
        modeloHistorial.setRowCount(0);

        // CONSULTA SQL INTACTA PARA EL FUTURO:
        // Vincula las bitácoras de historia clínica que correspondan al ID del paciente seleccionado
        String sql = "SELECT fecha, diagnostico, medico, tratamiento FROM historial_clinico WHERE id_paciente = ? ORDER BY fecha DESC";
        
        try {
            // Nota: Esta línea ejecutará tu puente hacia MySQL cuando XAMPP esté activo.
            // Para tu arquitectura orientada a consultas, aquí recuperarías el ResultSet.
            GestorFrameToDB.ejecutarActualizacion(sql, idPaciente);
        } catch (Exception ex) {
            System.out.println("⚠️ SQL de Historial Clínico preparado para ID: " + idPaciente + " (Esperando conexión MySQL).");
        }

        // Simulación de datos para que visualices la estructura durante el diseño gráfico:
        if (idPaciente.equals("1020")) {
            modeloHistorial.addRow(new Object[]{"12/10/2026", "Hipertensión Arterial", "Dr. Alberto Sosa", "Enalapril 10mg diario."});
            modeloHistorial.addRow(new Object[]{"05/05/2026", "Chequeo Preventivo Anual", "Dra. Elena Rivas", "Resultados de laboratorio estables."});
        } else if (idPaciente.equals("1021")) {
            modeloHistorial.addRow(new Object[]{"24/09/2026", "Esguince de Tobillo Grado 1", "Dr. Alberto Sosa", "Reposo 7 días y vendaje de compresión."});
        } else if (idPaciente.equals("1022")) {
            modeloHistorial.addRow(new Object[]{"01/11/2026", "Faringitis Aguda", "Dra. Marta Gómez", "Amoxicilina 500mg cada 8h por 7 días."});
        }
    }
}