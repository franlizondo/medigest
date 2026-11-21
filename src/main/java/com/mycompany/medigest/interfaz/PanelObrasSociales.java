/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import com.mycompany.medigest.servicios.ObraSocialService;
import com.mycompany.medigest.entidades.ObraSocial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelObrasSociales extends JPanel {

    private JTable tablaOS;
    private DefaultTableModel modeloOS;
    private Color verdePrimario, verdeSecundario;
    private final ObraSocialService obraSocialService = new ObraSocialService();

    public PanelObrasSociales(Color verdePrimario, Color verdeSecundario, Color grisFondo) {
        this.verdePrimario = verdePrimario;
        this.verdeSecundario = verdeSecundario;

        setLayout(new BorderLayout(10, 10));
        setBackground(grisFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Titulo del header
        JLabel lblTitulo = new JLabel("Convenios de Obras Sociales y Prepagas", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(verdePrimario);

        //Toolbar botones y buscador.
        JPanel panelHerramientas = new JPanel(new BorderLayout(15, 0));
        panelHerramientas.setOpaque(false);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotones.setOpaque(false);

        JButton btnAgregar = new JButton("+ Nueva Obra Social");
        btnAgregar.setBackground(verdeSecundario);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 13));
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> agregarObraSocial());

        JButton btnBaja = new JButton("Dar de Baja Convenio");
        btnBaja.setBackground(new Color(192, 57, 43));
        btnBaja.setForeground(Color.WHITE);
        btnBaja.setFont(new Font("Arial", Font.BOLD, 13));
        btnBaja.setFocusPainted(false);
        btnBaja.addActionListener(e -> darDeBajaObraSocial());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnBaja);
        panelHerramientas.add(panelBotones, BorderLayout.WEST);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBusqueda.setOpaque(false);
        JTextField txtBuscar = new JTextField(15);
        panelBusqueda.add(new JLabel("Buscar: "));
        panelBusqueda.add(txtBuscar);
        panelHerramientas.add(panelBusqueda, BorderLayout.EAST);

        JPanel panelNorte = new JPanel(new GridLayout(2, 1, 0, 15));
        panelNorte.setOpaque(false);
        panelNorte.add(lblTitulo);
        panelNorte.add(panelHerramientas);
        add(panelNorte, BorderLayout.NORTH);

        //Configuracion de tabla.
        String[] columnas = {"ID", "Obra Social / Prepaga", "Código / Plan", "Estado Convenio"};
        modeloOS = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevents typing directly into cells
            }
        };

        tablaOS = new JTable(modeloOS);
        tablaOS.setRowHeight(35);
        tablaOS.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaOS.getColumnModel().getColumn(0).setMaxWidth(60);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                BuscadorTabla.filtrarTabla(tablaOS, txtBuscar.getText());
            }
        });

        add(new JScrollPane(tablaOS), BorderLayout.CENTER);

        //Cargar las obras socieles con servicio
        cargarObrasSociales();
    }
    
    //El metodo llamado arriba.
    public void cargarObrasSociales() {
        modeloOS.setRowCount(0);
        try {
            for (ObraSocial os : obraSocialService.listarTodas()) {
                modeloOS.addRow(new Object[]{
                    os.getId(),
                    os.getNombre(),
                    os.getCodigo(),
                    os.isActiva() ? "ACTIVO" : "INACTIVO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar obras sociales: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarObraSocial() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la Obra Social / Prepaga:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        String codigo = JOptionPane.showInputDialog(this, "Código o Plan aceptado (ej. OSDE-210):");
        if (codigo == null || codigo.trim().isEmpty()) return;

        try {
            ObraSocial nueva = new ObraSocial(0, nombre.trim(), codigo.trim(), true);
            obraSocialService.crear(nueva);
            cargarObrasSociales();
            JOptionPane.showMessageDialog(this, "Obra social agregada correctamente.");
        } catch (IllegalArgumentException exValidacion) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + exValidacion.getMessage(), "Validacion", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        } 
    }

    private void darDeBajaObraSocial() {
        int fila = tablaOS.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una obra social de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tablaOS.getValueAt(fila, 0);
        String nombre = tablaOS.getValueAt(fila, 1).toString();

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea dar de baja el convenio con " + nombre + "?",
                "Confirmar Baja", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (obraSocialService.darDeBaja(id)) {
                cargarObrasSociales();
                JOptionPane.showMessageDialog(this, "Convenio dado de baja correctamente.");
            }
        }
    }
}