/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import com.mycompany.medigest.servicios.MedicoService;
import com.mycompany.medigest.servicios.TurnoService;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.Disponibilidad;
import com.mycompany.medigest.entidades.Turno;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PanelDisponibilidad extends JPanel {

    private JComboBox<MedicoItem> comboMedicos;
    private JLabel lblMesAnio;
    private JPanel panelMatrizDias;
    private Color verdePrimario, verdeSecundario;
    private Integer idMedicoFijo = null; //Necesito esto para filtro de medico-paciente

    
    private final MedicoService medicoService = new MedicoService();
    private final TurnoService turnoService = new TurnoService();

    // Motor de calendario nativo de Java
    private Calendar calendarioLogico;
    private final String[] nombresMeses = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };
    
    public PanelDisponibilidad(Color verdePrimario, Color verdeSecundario, Color grisFondo){
        
        this(verdePrimario, verdeSecundario, grisFondo, null);
    }

    public PanelDisponibilidad(Color verdePrimario, Color verdeSecundario, Color grisFondo, Integer idMedicoFijo) {
        this.idMedicoFijo = idMedicoFijo;
        this.verdePrimario = verdePrimario;
        this.verdeSecundario = verdeSecundario;
        
        calendarioLogico = Calendar.getInstance();

        setLayout(new BorderLayout(15, 15));
        setBackground(grisFondo);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Panel Superior: Selector de Médico Real
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelSuperior.setOpaque(false);

        JLabel lblSeleccionar = new JLabel("Seleccionar Médico:");
        lblSeleccionar.setFont(new Font("Arial", Font.BOLD, 16));
        lblSeleccionar.setForeground(verdePrimario);
        panelSuperior.add(lblSeleccionar);

        comboMedicos = new JComboBox<>();
        comboMedicos.setFont(new Font("Arial", Font.PLAIN, 14));
        comboMedicos.setPreferredSize(new Dimension(280, 30));
        cargarMedicosEnCombo();

        comboMedicos.addActionListener(e -> refrescarCalendarioCompleto());
        panelSuperior.add(comboMedicos);
        add(panelSuperior, BorderLayout.NORTH);

        // 2. Contenedor del Calendario Central
        JPanel panelCalendarioCentral = new JPanel(new BorderLayout(5, 5));
        panelCalendarioCentral.setOpaque(false);

        JPanel panelControlesFecha = new JPanel(new BorderLayout(10, 0));
        panelControlesFecha.setOpaque(false);

        JButton btnAnterior = new JButton("< Anterior");
        btnAnterior.setBackground(verdeSecundario);
        btnAnterior.setForeground(Color.WHITE);
        btnAnterior.setFocusPainted(false);
        btnAnterior.addActionListener(e -> {
            calendarioLogico.add(Calendar.MONTH, -1);
            refrescarCalendarioCompleto();
        });

        JButton btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.setBackground(verdeSecundario);
        btnSiguiente.setForeground(Color.WHITE);
        btnSiguiente.setFocusPainted(false);
        btnSiguiente.addActionListener(e -> {
            calendarioLogico.add(Calendar.MONTH, 1);
            refrescarCalendarioCompleto();
        });

        lblMesAnio = new JLabel("", SwingConstants.CENTER);
        lblMesAnio.setFont(new Font("Arial", Font.BOLD, 22));
        lblMesAnio.setForeground(verdePrimario);

        panelControlesFecha.add(btnAnterior, BorderLayout.WEST);
        panelControlesFecha.add(lblMesAnio, BorderLayout.CENTER);
        panelControlesFecha.add(btnSiguiente, BorderLayout.EAST);

        panelCalendarioCentral.add(panelControlesFecha, BorderLayout.NORTH);

        //Subpanel de dias de la semana y cuadrícula
        JPanel panelGridCompleto = new JPanel(new BorderLayout());

        JPanel panelDiasSemana = new JPanel(new GridLayout(1, 7));
        panelDiasSemana.setBackground(verdeSecundario);
        String[] dias = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (String dia : dias) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setFont(new Font("Arial", Font.BOLD, 14));
            lblDia.setForeground(Color.WHITE);
            lblDia.setPreferredSize(new Dimension(0, 35));
            panelDiasSemana.add(lblDia);
        }
        panelGridCompleto.add(panelDiasSemana, BorderLayout.NORTH);

        panelMatrizDias = new JPanel(new GridLayout(6, 7, 5, 5));
        panelMatrizDias.setBackground(Color.WHITE);
        panelMatrizDias.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panelGridCompleto.add(panelMatrizDias, BorderLayout.CENTER);
        panelCalendarioCentral.add(panelGridCompleto, BorderLayout.CENTER);

        add(panelCalendarioCentral, BorderLayout.CENTER);

        //Renderizado inicial
        refrescarCalendarioCompleto();
    }

    public void cargarMedicosEnCombo() {
        comboMedicos.removeAllItems();
        try {
            for (Medico m : medicoService.obtenerMedicosActivos()) {
                //Si idMedicoFijo es null (Admin/Recep), agrega todos. Si tiene ID, solo agrega al medico logueado.
                if (idMedicoFijo == null || m.getId() == idMedicoFijo) {
                    comboMedicos.addItem(new MedicoItem(m));
                }
            }
            if (idMedicoFijo != null) {
                comboMedicos.setEnabled(false); //Bloquea el desplegable para que no pueda cambiar de médico
            }
        } catch (Exception ex) {
            System.out.println("Error al cargar médicos en disponibilidad: " + ex.getMessage());
        }
    }

    private void refrescarCalendarioCompleto() {
        int mes = calendarioLogico.get(Calendar.MONTH);
        int anio = calendarioLogico.get(Calendar.YEAR);

        lblMesAnio.setText(nombresMeses[mes] + " " + anio);
        panelMatrizDias.removeAll();

        MedicoItem seleccionado = (MedicoItem) comboMedicos.getSelectedItem();
        List<Disponibilidad> dispsMedico = new ArrayList<>();
        if (seleccionado != null) {
            try {
                dispsMedico = medicoService.obtenerHorarios(seleccionado.medico.getId());
            } catch (Exception ex) {
                System.out.println("Error al consultar disponibilidad: " + ex.getMessage());
            }
        }

        Calendar copiaCalculo = (Calendar) calendarioLogico.clone();
        copiaCalculo.set(Calendar.DAY_OF_MONTH, 1);
        int desfaseDiasSemana = copiaCalculo.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDiasMes = calendarioLogico.getActualMaximum(Calendar.DAY_OF_MONTH);

        int diaActual = 1;
        for (int i = 0; i < 42; i++) {
            if (i < desfaseDiasSemana || diaActual > maxDiasMes) {
                panelMatrizDias.add(new JLabel(""));
            } else {
                JButton btnDia = new JButton(String.valueOf(diaActual));
                btnDia.setFont(new Font("Arial", Font.BOLD, 14));
                btnDia.setFocusPainted(false);

                LocalDate fechaCelda = LocalDate.of(anio, mes + 1, diaActual);
                DayOfWeek diaSemanaCelda = fechaCelda.getDayOfWeek();

                //Verificamos si el medico trabaja este dia de la semana para resaltarlo
                boolean atiendeHoy = false;
                for (Disponibilidad d : dispsMedico) {
                    if (d.getDia() == diaSemanaCelda) {
                        atiendeHoy = true;
                        break;
                    }
                }

                if (atiendeHoy) {
                    btnDia.setBackground(new Color(215, 240, 232)); // Verde suave indicando atención
                    btnDia.setForeground(verdePrimario);
                } else {
                    btnDia.setBackground(Color.WHITE);
                    btnDia.setForeground(Color.DARK_GRAY);
                }

                final LocalDate fechaFinal = fechaCelda;
                btnDia.addActionListener(e -> {
                    if (seleccionado != null) {
                        abrirDetalleHorarioDia(fechaFinal, seleccionado.medico);
                    }
                });

                panelMatrizDias.add(btnDia);
                diaActual++;
            }
        }

        panelMatrizDias.revalidate();
        panelMatrizDias.repaint();
    }

    private void abrirDetalleHorarioDia(LocalDate fecha, Medico medico) {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Agenda del " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true);
        dialogo.setSize(420, 320);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));

        JTextArea txtHorarios = new JTextArea();
        txtHorarios.setEditable(false);
        txtHorarios.setFont(new Font("Arial", Font.PLAIN, 14));
        txtHorarios.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        StringBuilder sb = new StringBuilder();
        sb.append("Profesional: Dr/a. ").append(medico.getNombreApellido()).append("\n\n");

        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");

        // 1. Consultar horarios habituales de ese dia de la semana
        List<Disponibilidad> todosLosHorarios = medicoService.obtenerHorarios(medico.getId());
        List<Disponibilidad> franjas = new ArrayList<>();
        for (Disponibilidad d : todosLosHorarios) {
            if (d.getDia() == fecha.getDayOfWeek()) {
                franjas.add(d);
            }
        }

        sb.append("=== FRANJAS DE ATENCIÓN HABITUAL ===\n");
        if (franjas.isEmpty()) {
            sb.append("• No tiene horarios asignados este día de la semana.\n");
        } else {
            for (Disponibilidad d : franjas) {
                sb.append("• De ").append(d.getHoraInicio().format(fmtHora))
                  .append(" a ").append(d.getHoraFin().format(fmtHora)).append("\n");
            }
        }

        // 2. Consultar turnos ya agendados para esa fecha exacta
        List<Turno> turnosDia = turnoService.obtenerTurnosPorMedicoYFecha(medico.getId(), fecha);
        sb.append("\n=== TURNOS AGENDADOS EN ESTA FECHA ===\n");
        if (turnosDia.isEmpty()) {
            sb.append("• Sin turnos ocupados.\n");
        } else {
            for (Turno t : turnosDia) {
                String pac = (t.getPaciente() != null) ? t.getPaciente().getUsuario().getNombreApellido() : "Paciente";
                sb.append("• ").append(t.getHoraInicio().format(fmtHora)).append(" - ")
                        .append(pac).append(" [").append(t.getEstado().name()).append("]\n");
            }
        }
        
        txtHorarios.setText(sb.toString());
        dialogo.add(new JScrollPane(txtHorarios), BorderLayout.CENTER);
        
        //Boton inferior para agregar una nueva franja horaria ese día
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAgregarFranja = new JButton("+ Agregar Franja Horaria");
        btnAgregarFranja.setBackground(verdePrimario);
        btnAgregarFranja.setForeground(Color.WHITE);
        
        btnAgregarFranja.addActionListener(e -> {
            String ini = JOptionPane.showInputDialog(dialogo, "Hora de inicio (HH:MM):", "08:00");
            if (ini == null || ini.trim().isEmpty()) {
                return;
            }
            String fin = JOptionPane.showInputDialog(dialogo, "Hora de fin (HH:MM):", "12:00");
            if (fin == null || fin.trim().isEmpty()) {
                return;
            }
            try {
                Disponibilidad nueva = new Disponibilidad(fecha.getDayOfWeek(), LocalTime.parse(ini.trim()), LocalTime.parse(fin.trim()));
                medicoService.agregarDisponibilidad(medico.getId(), nueva);
                JOptionPane.showMessageDialog(dialogo, "Horario agregado correctamente.");
                dialogo.dispose();
                refrescarCalendarioCompleto(); // Refresca el color del calendario
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Formato de hora inválido o error de BD: " + ex.getMessage());
            }
        });
        
        panelSur.add(btnAgregarFranja);
        dialogo.add(panelSur, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }
    
    //Wrapper para mostrar el nombre del medico en el JComboBox
    private static class MedicoItem {
        Medico medico;
        MedicoItem(Medico m) { this.medico = m; }
        @Override
        public String toString() {
            return "Dr/a. " + medico.getNombreApellido() + " (" + medico.getEspecialidad() + ")";
        }
    }
}