/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;


import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Paciente;
import com.mycompany.medigest.entidades.Turno;
import com.mycompany.medigest.servicios.MedicoService;
import com.mycompany.medigest.servicios.PacienteService;
import com.mycompany.medigest.servicios.TurnoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelTurnos extends JPanel {

    private JTable tablaTurnos;
    private DefaultTableModel modelo;
    private Color verdePrimario, verdeSecundario;

    //Instancias de la Capa de Negocio
    private final TurnoService turnoService = new TurnoService();
    private final PacienteService pacienteService = new PacienteService();
    private final MedicoService medicoService = new MedicoService();

    public PanelTurnos(Color verdePrimario, Color verdeSecundario, Color grisFondo) {
        this.verdePrimario = verdePrimario;
        this.verdeSecundario = verdeSecundario;

        setLayout(new BorderLayout(10, 10));
        setBackground(grisFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Titulo
        JLabel lblTitulo = new JLabel("Gestión y Control de Turnos", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(verdePrimario);

        //Toolbar
        JPanel panelAccionesSuperior = new JPanel(new BorderLayout(10, 0));
        panelAccionesSuperior.setOpaque(false);
        

        //Subpanel izquierdo agrupa ambos botones uno al lado del otro
        JPanel panelBotonesIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBotonesIzquierda.setOpaque(false);

        JButton btnNuevoTurno = new JButton("Nuevo Turno");
        btnNuevoTurno.setBackground(verdeSecundario);
        btnNuevoTurno.setForeground(Color.WHITE);
        btnNuevoTurno.setFont(new Font("Arial", Font.BOLD, 14));
        btnNuevoTurno.setFocusPainted(false);
        btnNuevoTurno.addActionListener(e -> abrirFormularioNuevoTurno());

        JButton btnEstado = new JButton("Asistencia / Estado");
        btnEstado.setBackground(verdePrimario);
        btnEstado.setForeground(Color.WHITE);
        btnEstado.setFont(new Font("Arial", Font.BOLD, 13));
        btnEstado.setFocusPainted(false);
        btnEstado.addActionListener(e -> gestionarAsistencia());

        panelBotonesIzquierda.add(btnNuevoTurno);
        panelBotonesIzquierda.add(btnEstado);
        panelAccionesSuperior.add(panelBotonesIzquierda, BorderLayout.WEST);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        panelBusqueda.setOpaque(false);
        JTextField txtBuscar = new JTextField(15);
        panelBusqueda.add(new JLabel("Buscar: "));
        panelBusqueda.add(txtBuscar);
        panelAccionesSuperior.add(panelBusqueda, BorderLayout.EAST);
        JPanel panelNorte = new JPanel(new GridLayout(2, 1, 0, 10));
        panelNorte.setOpaque(false);
        panelNorte.add(lblTitulo);
        panelNorte.add(panelAccionesSuperior);
        add(panelNorte, BorderLayout.NORTH);

        //Configuracion de la Tabla
        String[] columnas = {"ID", "Fecha y Hora", "Paciente", "Médico", "Estado", "Acciones"};

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; //Solo la columna "Acciones" recibe clics
            }
        };

        tablaTurnos = new JTable(modelo);
        tablaTurnos.setRowHeight(40);
        tablaTurnos.setFont(new Font("Arial", Font.PLAIN, 14));

        //Config
        tablaTurnos.getColumnModel().getColumn(0).setMaxWidth(50);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                BuscadorTabla.filtrarTabla(tablaTurnos, txtBuscar.getText());
            }
        });

        // Asignacion de botones en la columna 5
        tablaTurnos.getColumnModel().getColumn(5).setCellRenderer(new RenderizadorAcciones(verdeSecundario));
        tablaTurnos.getColumnModel().getColumn(5).setCellEditor(new EditorAcciones(new JCheckBox(), this, verdeSecundario));

        JScrollPane scrollPane = new JScrollPane(tablaTurnos);
        add(scrollPane, BorderLayout.CENTER);

        //Cargar datos reales
        cargarTurnosDesdeBD();
    }

    //Turnos desde servicio
    public void cargarTurnosDesdeBD() {
        modelo.setRowCount(0);
        try {
            List<Turno> listaTurnos = turnoService.obtenerTurnos();
            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");

            for (Turno t : listaTurnos) {
                String fechaHora = t.getFecha().format(fmtFecha) + " " + t.getHoraInicio().format(fmtHora);
                String nombrePaciente = (t.getPaciente() != null) ? t.getPaciente().getUsuario().getNombreApellido() : "Sin asignar";
                String nombreMedico = (t.getMedico() != null) ? t.getMedico().getNombreApellido() : "Sin asignar";

                modelo.addRow(new Object[]{
                    t.getId(),
                    fechaHora,
                    nombrePaciente,
                    nombreMedico,
                    t.getEstado().name(),
                    "Acciones"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar turnos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirFormularioNuevoTurno() {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Agendar Nuevo Turno", true);
        dialogo.setSize(400, 380);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(null);

        JLabel lblFecha = new JLabel("Fecha (AAAA-MM-DD):");
        lblFecha.setBounds(30, 20, 320, 20);
        dialogo.add(lblFecha);
        JTextField txtFecha = new JTextField(LocalDate.now().toString());
        txtFecha.setBounds(30, 45, 320, 25);
        dialogo.add(txtFecha);

        // AgendaService calcula intervalos +30 min, asi que solo pedimos Hora Inicio
        JLabel lblHora = new JLabel("Hora de Inicio (HH:MM - Intervalos de 30 min):");
        lblHora.setBounds(30, 80, 320, 20);
        dialogo.add(lblHora);
        JTextField txtHoraInicio = new JTextField("09:00");
        txtHoraInicio.setBounds(30, 105, 320, 25);
        dialogo.add(txtHoraInicio);

        JLabel lblPaciente = new JLabel("Seleccionar Paciente:");
        lblPaciente.setBounds(30, 140, 320, 20);
        dialogo.add(lblPaciente);
        JComboBox<PacienteItem> cmbPacientes = new JComboBox<>();
        cmbPacientes.setBounds(30, 165, 320, 25);
        for (Paciente p : pacienteService.obtenerPacientes()) {
            cmbPacientes.addItem(new PacienteItem(p));
        }
        dialogo.add(cmbPacientes);

        JLabel lblMedico = new JLabel("Seleccionar Médico:");
        lblMedico.setBounds(30, 200, 320, 20);
        dialogo.add(lblMedico);
        JComboBox<MedicoItem> cmbMedicos = new JComboBox<>();
        cmbMedicos.setBounds(30, 225, 320, 25);
        for (Medico m : medicoService.obtenerMedicosActivos()) {
            cmbMedicos.addItem(new MedicoItem(m));
        }
        dialogo.add(cmbMedicos);

        JLabel lblMotivo = new JLabel("Motivo / Observación:");
        lblMotivo.setBounds(30, 260, 320, 20);
        dialogo.add(lblMotivo);
        JTextField txtMotivo = new JTextField("Consulta general");
        txtMotivo.setBounds(30, 285, 320, 25);
        dialogo.add(txtMotivo);

        JButton btnGuardar = new JButton("Guardar Turno");
        btnGuardar.setBounds(120, 325, 150, 35);
        btnGuardar.setBackground(verdePrimario);
        btnGuardar.setForeground(Color.WHITE);

        btnGuardar.addActionListener(e -> {
            try {
                LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
                LocalTime inicio = LocalTime.parse(txtHoraInicio.getText().trim());

                PacienteItem pSel = (PacienteItem) cmbPacientes.getSelectedItem();
                MedicoItem mSel = (MedicoItem) cmbMedicos.getSelectedItem();

                if (pSel == null || mSel == null) {
                    JOptionPane.showMessageDialog(dialogo, "Debe seleccionar un paciente y un médico.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //Service ejecuta todas las reglas de negocio y guarda en BD
                turnoService.crearNuevoTurno(fecha, inicio, pSel.paciente.getId(), mSel.medico.getId(), null, txtMotivo.getText().trim());

                cargarTurnosDesdeBD();
                JOptionPane.showMessageDialog(dialogo, "Turno agendado correctamente.");
                dialogo.dispose();

            } catch (IllegalArgumentException | IllegalStateException exNegocio) {
                //Muestra las aletras
                JOptionPane.showMessageDialog(dialogo, exNegocio.getMessage(), "Validación de Agenda", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error al agendar turno (revise formato AAAA-MM-DD y HH:MM):\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.add(btnGuardar);
        dialogo.setVisible(true);
    }

    public void gestionarReprogramar(int fila) {
        int idTurno = (int) tablaTurnos.getValueAt(fila, 0);
        String paciente = tablaTurnos.getValueAt(fila, 2).toString();

        String nuevaFechaStr = JOptionPane.showInputDialog(this, "Nueva Fecha (AAAA-MM-DD) para " + paciente + ":", LocalDate.now().toString());
        if (nuevaFechaStr == null || nuevaFechaStr.trim().isEmpty()) return;

        String nuevaHoraStr = JOptionPane.showInputDialog(this, "Nueva Hora de Inicio (HH:MM):", "10:00");
        if (nuevaHoraStr == null || nuevaHoraStr.trim().isEmpty()) return;

        try {
            LocalDate nuevaFecha = LocalDate.parse(nuevaFechaStr.trim());
            LocalTime nuevaInicio = LocalTime.parse(nuevaHoraStr.trim());

            //Service valida que no este atendido y horario libre
            turnoService.reprogramarTurno(idTurno, nuevaFecha, nuevaInicio);
            cargarTurnosDesdeBD();
            JOptionPane.showMessageDialog(this, "Turno reprogramado con éxito.");

        } catch (IllegalArgumentException | IllegalStateException exNegocio) {
            JOptionPane.showMessageDialog(this, exNegocio.getMessage(), "No se puede reprogramar", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al reprogramar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void gestionarCancelar(int fila) {
        int idTurno = (int) tablaTurnos.getValueAt(fila, 0);
        String paciente = tablaTurnos.getValueAt(fila, 2).toString();

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar permanentemente el turno de " + paciente + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (turnoService.eliminarTurno(idTurno)) {
                cargarTurnosDesdeBD();
                JOptionPane.showMessageDialog(this, "Turno eliminado correctamente.");
            }
        }
    }
    
        private void gestionarAsistencia() {
        int fila = tablaTurnos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un turno de la tabla primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idTurno = (int) tablaTurnos.getValueAt(fila, 0);
        String[] opciones = {"ATENDIDO (Asistió)", "AUSENTE (No asistió)", "CONFIRMADO"};
        String seleccion = (String) JOptionPane.showInputDialog(this, "Seleccione el estado de asistencia:",
                "Actualizar Asistencia", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        if (seleccion != null) {
            try {
                Turno.EstadoTurno nuevoEstado = seleccion.startsWith("ATENDIDO") ? Turno.EstadoTurno.ATENDIDO
                        : seleccion.startsWith("AUSENTE") ? Turno.EstadoTurno.AUSENTE : Turno.EstadoTurno.CONFIRMADO;
                turnoService.cambiarEstadoTurno(idTurno, nuevoEstado);
                cargarTurnosDesdeBD();
                JOptionPane.showMessageDialog(this, "Estado actualizado a " + nuevoEstado.name());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    //Auxiliares para JComboBox
    private static class PacienteItem {
        Paciente paciente;
        PacienteItem(Paciente p) { this.paciente = p; }
        @Override
        public String toString() { return paciente.getDni() + " - " + paciente.getUsuario().getNombreApellido(); }
    }
    
    private static class MedicoItem {
        Medico medico;
        MedicoItem(Medico m) { this.medico = m; }
        @Override
        public String toString() { return "Dr/a. " + medico.getNombreApellido() + " (" + medico.getEspecialidad() + ")"; }
    }
}

class RenderizadorAcciones extends JPanel implements TableCellRenderer {

    private JButton btnReprogramar = new JButton("Reprogramar");
    private JButton btnCancelar = new JButton("Eliminar");

    public RenderizadorAcciones(Color colorBtn) {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
        setOpaque(true);
        btnReprogramar.setBackground(colorBtn);
        btnReprogramar.setForeground(Color.WHITE);
        btnCancelar.setBackground(new Color(192, 57, 43));
        btnCancelar.setForeground(Color.WHITE);
        add(btnReprogramar);
        add(btnCancelar);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return this;
    }
}

class EditorAcciones extends DefaultCellEditor {

    private JPanel panel = new JPanel();
    private JButton btnReprogramar = new JButton("Reprogramar");
    private JButton btnCancelar = new JButton("Cancelar");
    private int filaActual;

    public EditorAcciones(JCheckBox checkBox, PanelTurnos panelPadre, Color colorBtn) {
        super(checkBox);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
        btnReprogramar.setBackground(colorBtn);
        btnReprogramar.setForeground(Color.WHITE);
        btnCancelar.setBackground(new Color(192, 57, 43));
        btnCancelar.setForeground(Color.WHITE);
        btnReprogramar.addActionListener((ActionEvent e) -> {
            fireEditingStopped();
            panelPadre.gestionarReprogramar(filaActual);
        });
        btnCancelar.addActionListener((ActionEvent e) -> {
            fireEditingStopped();
            panelPadre.gestionarCancelar(filaActual);
        });
        panel.add(btnReprogramar);
        panel.add(btnCancelar);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        filaActual = row;
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}
