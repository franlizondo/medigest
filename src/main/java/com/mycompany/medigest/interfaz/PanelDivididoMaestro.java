/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelDivididoMaestro extends JPanel {

    private JTable tablaIzquierda;
    private JTable tablaDerecha;
    private DefaultTableModel modeloIzquierdo;
    private DefaultTableModel modeloDerecho;
    private CargadorDetallesSQL cargadorSQL;
    private AccionesMaestro accionesCRUD; // Nueva interfaz para Agregar y Eliminar

    public PanelDivididoMaestro(String tituloSeccion, 
                                String[] colIzquierda, Object[][] datosIzquierda, 
                                String[] colDerecha, 
                                Color verdePrimario, Color verdeSecundario, Color grisFondo,
                                CargadorDetallesSQL cargadorSQL,
                                AccionesMaestro accionesCRUD) {
        
        this.cargadorSQL = cargadorSQL;
        this.accionesCRUD = accionesCRUD;

        setBackground(grisFondo);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Cabecera con Titulo y Barra de Acciones (Agregar / Eliminar)
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));
        panelNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel(tituloSeccion, SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(verdePrimario);
        panelNorte.add(lblTitulo, BorderLayout.WEST);

        //Solo mostramos los botones si le pasamos una implementacion de accionesCRUD
        if (this.accionesCRUD != null) {
            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            panelBotones.setOpaque(false);

            JButton btnAgregar = new JButton("+ Agregar Nuevo");
            btnAgregar.setBackground(verdeSecundario);
            btnAgregar.setForeground(Color.WHITE);
            btnAgregar.setFont(new Font("Arial", Font.BOLD, 13));
            btnAgregar.setFocusPainted(false);
            btnAgregar.addActionListener(e -> this.accionesCRUD.alHacerClicAgregar(modeloIzquierdo));

            JButton btnEliminar = new JButton("Eliminar Seleccionado");
            btnEliminar.setBackground(new Color(192, 57, 43)); // Rojo alerta
            btnEliminar.setForeground(Color.WHITE);
            btnEliminar.setFont(new Font("Arial", Font.BOLD, 13));
            btnEliminar.setFocusPainted(false);
            btnEliminar.addActionListener(e -> {
                int fila = tablaIzquierda.getSelectedRow();
                if (fila == -1) {
                    JOptionPane.showMessageDialog(this, "Por favor, seleccione una fila de la tabla izquierda.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String id = tablaIzquierda.getValueAt(fila, 0).toString();
                this.accionesCRUD.alHacerClicEliminar(id, fila, modeloIzquierdo, modeloDerecho);
            });

            panelBotones.add(btnAgregar);
            panelBotones.add(btnEliminar);
            panelNorte.add(panelBotones, BorderLayout.EAST);
        }

        add(panelNorte, BorderLayout.NORTH);

        // 2. Tabla Izquierda (Principal)
        modeloIzquierdo = new DefaultTableModel(datosIzquierda, colIzquierda) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaIzquierda = new JTable(modeloIzquierdo);
        tablaIzquierda.setRowHeight(35);
        tablaIzquierda.setFont(new Font("Arial", Font.PLAIN, 14));

        // 3. Tabla Derecha (Detalles)
        modeloDerecho = new DefaultTableModel(null, colDerecha) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaDerecha = new JTable(modeloDerecho);
        tablaDerecha.setRowHeight(30);
        tablaDerecha.setFont(new Font("Arial", Font.PLAIN, 13));

        // 4. Evento de Seleccion unificado
        tablaIzquierda.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = tablaIzquierda.getSelectedRow();
                if (filaSeleccionada != -1) {
                    String id = tablaIzquierda.getValueAt(filaSeleccionada, 0).toString();
                    modeloDerecho.setRowCount(0);
                    if (PanelDivididoMaestro.this.cargadorSQL != null) {
                        PanelDivididoMaestro.this.cargadorSQL.cargarDatosSegundaTabla(id, modeloDerecho);
                    }
                }
            }
        });

        // 5. Construccion del JSplitPane
        JScrollPane scrollIzquierdo = new JScrollPane(tablaIzquierda);
        JScrollPane scrollDerecho = new JScrollPane(tablaDerecha);

        JSplitPane panelDividido = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollIzquierdo, scrollDerecho);
        panelDividido.setDividerLocation(480); 
        panelDividido.setResizeWeight(0.4);   
        panelDividido.setOpaque(false);

        add(panelDividido, BorderLayout.CENTER);
    }

    // Metodo util para refrescar la tabla izquierda desde afuera
    public DefaultTableModel getModeloIzquierdo() {
        return modeloIzquierdo;
    }
}

// INTERFACES DE SOPORTE
interface CargadorDetallesSQL {
    void cargarDatosSegundaTabla(String idSeleccionado, DefaultTableModel modeloDerecho);
}

interface AccionesMaestro {
    void alHacerClicAgregar(DefaultTableModel modeloIzquierdo);
    void alHacerClicEliminar(String idSeleccionado, int filaSeleccionada, DefaultTableModel modeloIzquierdo, DefaultTableModel modeloDerecho);
}