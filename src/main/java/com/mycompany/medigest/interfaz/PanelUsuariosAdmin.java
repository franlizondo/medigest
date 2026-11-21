/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import com.mycompany.medigest.servicios.UsuarioService;
import com.mycompany.medigest.entidades.Administrador;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.EstadoMedico;
import com.mycompany.medigest.entidades.Recepcionista;
import com.mycompany.medigest.entidades.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelUsuariosAdmin extends JPanel {

    private JTable tablaUsuarios;
    private DefaultTableModel modeloUsuarios;
    private Color verdePrimario, verdeSecundario;

    private final UsuarioService usuarioService = new UsuarioService();

    public PanelUsuariosAdmin(Color verdePrimario, Color verdeSecundario, Color grisFondo) {
        this.verdePrimario = verdePrimario;
        this.verdeSecundario = verdeSecundario;

        setLayout(new BorderLayout(10, 10));
        setBackground(grisFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Header
        JLabel lblTitulo = new JLabel("Administración y Control de Usuarios del Sistema", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(verdePrimario);

        //Toolbar
        JPanel panelHerramientas = new JPanel(new BorderLayout(15, 0));
        panelHerramientas.setOpaque(false);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotones.setOpaque(false);

        JButton btnAgregar = new JButton("Agregar Usuario");
        btnAgregar.setBackground(verdeSecundario);
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 13));
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> abrirFormularioNuevoUsuario());

        JButton btnEliminar = new JButton("Eliminar Usuario");
        btnEliminar.setBackground(new Color(192, 57, 43));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Arial", Font.BOLD, 13));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> ejecutarBajaUsuarioSQL());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
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

        //Tabla Usuarios
        String[] columnas = {"ID Usuario", "Nombre y Apellido", "Rol del Sistema", "Detalle / Especialidad"};

        modeloUsuarios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloUsuarios);
        tablaUsuarios.setRowHeight(35);
        tablaUsuarios.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(90);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                BuscadorTabla.filtrarTabla(tablaUsuarios, txtBuscar.getText());
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        add(scrollPane, BorderLayout.CENTER);

        cargarUsuariosDesdeBD();
    }

    
    public void cargarUsuariosDesdeBD() {
        modeloUsuarios.setRowCount(0);
        try {
            List<Usuario> lista = usuarioService.listarTodos();
            for (Usuario u : lista) {
                String rolTexto = "USUARIO";
                String detalle = "-";

                if (u instanceof Administrador) {
                    rolTexto = "ADMINISTRADOR";
                    detalle = "Acceso Total";
                } else if (u instanceof Recepcionista) {
                    rolTexto = "RECEPCIONISTA";
                    detalle = "Gestión de Turnos y Pacientes";
                } else if (u instanceof Medico) {
                    rolTexto = "MÉDICO";
                    detalle = ((Medico) u).getEspecialidad();
                }

                modeloUsuarios.addRow(new Object[]{u.getId(), u.getNombreApellido(), rolTexto, detalle});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirFormularioNuevoUsuario() {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Nuevo Usuario", true);
        dialogo.setSize(400, 430);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(null);

        JLabel lblNombre = new JLabel("Nombre y Apellido:");
        lblNombre.setBounds(40, 20, 300, 20);
        dialogo.add(lblNombre);
        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(40, 45, 300, 25);
        dialogo.add(txtNombre);

        JLabel lblEmail = new JLabel("Email (Para iniciar sesión):");
        lblEmail.setBounds(40, 80, 300, 20);
        dialogo.add(lblEmail);
        JTextField txtEmail = new JTextField();
        txtEmail.setBounds(40, 105, 300, 25);
        dialogo.add(txtEmail);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setBounds(40, 140, 300, 20);
        dialogo.add(lblPass);
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBounds(40, 165, 300, 25);
        dialogo.add(txtPass);

        JLabel lblRol = new JLabel("Rol asignado:");
        lblRol.setBounds(40, 200, 300, 20);
        dialogo.add(lblRol);
        String[] roles = {"RECEPCIONISTA", "MEDICO", "ADMINISTRADOR"};
        JComboBox<String> comboRol = new JComboBox<>(roles);
        comboRol.setBounds(40, 225, 300, 25);
        dialogo.add(comboRol);

        //Campo dinamico, solo si menedico
        JLabel lblEsp = new JLabel("Especialidad Médica:");
        lblEsp.setBounds(40, 260, 300, 20);
        lblEsp.setVisible(false);
        dialogo.add(lblEsp);
        JTextField txtEsp = new JTextField();
        txtEsp.setBounds(40, 285, 300, 25);
        txtEsp.setVisible(false);
        dialogo.add(txtEsp);

        comboRol.addActionListener(e -> {
            boolean esMedico = "MEDICO".equals(comboRol.getSelectedItem());
            lblEsp.setVisible(esMedico);
            txtEsp.setVisible(esMedico);
        });

        JButton btnGuardar = new JButton("Guardar Usuario");
        btnGuardar.setBounds(120, 335, 150, 35);
        btnGuardar.setBackground(verdePrimario);
        btnGuardar.setForeground(Color.WHITE);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String rol = comboRol.getSelectedItem().toString();
            String esp = txtEsp.getText().trim();

            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Complete nombre, email y contraseña.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("MEDICO".equals(rol) && esp.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Debe indicar la especialidad del médico.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Usuario nuevoUsuario;
                if ("ADMINISTRADOR".equals(rol)) {
                    nuevoUsuario = new Administrador(0, nombre, pass);
                } else if ("MEDICO".equals(rol)) {
                    nuevoUsuario = new Medico(esp, EstadoMedico.ACTIVO, new ArrayList<>(), 0, nombre, pass);
                } else {
                    nuevoUsuario = new Recepcionista(0, nombre, pass);
                }

                usuarioService.registrarUsuario(nuevoUsuario, email);
                cargarUsuariosDesdeBD();
                JOptionPane.showMessageDialog(dialogo, "Usuario registrado correctamente en MySQL.");
                dialogo.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error al guardar usuario (verifique que el email no esté repetido):\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.add(btnGuardar);
        dialogo.setVisible(true);
    }

    private void ejecutarBajaUsuarioSQL() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario de la lista.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idUsuario = (int) tablaUsuarios.getValueAt(filaSeleccionada, 0);
        String nombre = tablaUsuarios.getValueAt(filaSeleccionada, 1).toString();

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea dar de baja al usuario " + nombre + "?",
                "Confirmar Baja", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (usuarioService.darDeBaja(idUsuario)) {
                cargarUsuariosDesdeBD();
                JOptionPane.showMessageDialog(this, "Usuario dado de baja correctamente.");
            }
        }
    }
}