/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//ENTIDADES (Modelos de datos)
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.Disponibilidad;
import com.mycompany.medigest.entidades.Medico.EstadoMedico;
import com.mycompany.medigest.entidades.ObraSocial;
import com.mycompany.medigest.entidades.Paciente;
import com.mycompany.medigest.entidades.Turno;

//CAPA DE NEGOCIO / SERVICIOS
import com.mycompany.medigest.servicios.MedicoService;
import com.mycompany.medigest.servicios.ObraSocialService;
import com.mycompany.medigest.servicios.PacienteService;
import com.mycompany.medigest.servicios.TurnoService;

public class VentanaBase extends JFrame {

    //Paleta de colores.
    protected final Color VERDE_PRIMARIO = new Color(48, 78, 78);
    protected final Color VERDE_SECUNDARIO = new Color(66, 115, 109);
    protected final Color BLANCO = Color.WHITE;
    protected final Color GRIS_FONDO = new Color(240, 242, 245);
    
    //Componentes principales de navegacion
    private JPanel panelMenuLateral;
    protected JPanel panelCuerpoDinamico; 
    protected CardLayout navegadorPaneles; 
    
    //Paneles que necesitan actualizarse al cambiar de ventana (add on de ultimo momento, test better.)
    protected PanelTurnos panelTurnosReal;
    protected PanelDisponibilidad panelDisponibilidadReal;
    protected PanelObrasSociales panelObrasSocialesReal;
    protected PanelUsuariosAdmin panelUsuariosReal;

    public VentanaBase(String nombreUsuario, String tipoUsuario) {
        //Datos base.
        setTitle("MediGest - " + tipoUsuario);
        setSize(1024, 768); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //Creo un header, el metodo esta mas abajo.
        add(crearHeader(nombreUsuario, tipoUsuario), BorderLayout.NORTH);

        //Panel principal, tiene el menu a la izquierda y el cuerpoDinamico a la derecha
        JPanel contenedorPrincipal = new JPanel(new BorderLayout());
        
        panelMenuLateral = new JPanel();
        panelMenuLateral.setBackground(VERDE_SECUNDARIO);
        panelMenuLateral.setLayout(new BoxLayout(panelMenuLateral, BoxLayout.Y_AXIS));
        panelMenuLateral.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        contenedorPrincipal.add(panelMenuLateral, BorderLayout.WEST);

        //Admin de tarjetas.
        navegadorPaneles = new CardLayout();
        //Creo el panel y lo conecto a cardLayout
        panelCuerpoDinamico = new JPanel(navegadorPaneles);
        panelCuerpoDinamico.setBackground(GRIS_FONDO);
        contenedorPrincipal.add(panelCuerpoDinamico, BorderLayout.CENTER);

        add(contenedorPrincipal, BorderLayout.CENTER);
    }
    
    //Heder, el mismo en todas las ventanas
    private JPanel crearHeader(String usuario, String tipo) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(VERDE_PRIMARIO);
        header.setPreferredSize(new Dimension(1024, 65));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel lblAppName = new JLabel("MediGest");
        lblAppName.setForeground(BLANCO);
        lblAppName.setFont(new Font("Arial", Font.BOLD, 20));
        header.add(lblAppName, BorderLayout.WEST);

        JPanel panelInfoUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelInfoUsuario.setOpaque(false); 

        JLabel lblUsuarioInfo = new JLabel(usuario + " ; " + tipo);
        lblUsuarioInfo.setForeground(BLANCO);
        lblUsuarioInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        panelInfoUsuario.add(lblUsuarioInfo);

        JButton btnLogOut = new JButton("Log Out");
        btnLogOut.setBackground(VERDE_SECUNDARIO);
        btnLogOut.setForeground(BLANCO);
        btnLogOut.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogOut.setFocusPainted(false);
        btnLogOut.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnLogOut.addActionListener(e -> accionCerrarSesion());
        
        panelInfoUsuario.add(btnLogOut);
        header.add(panelInfoUsuario, BorderLayout.EAST);

        return header;
    }

    //Esto deberia ir abajo tecnicamente porque lo uso para generar los botones del menu lateral, whatever at this point.
    protected void agregarBotonMenu(String texto, String nombrePanelDestino) {
        JButton boton = new JButton(texto);
        boton.setMaximumSize(new Dimension(180, 40));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setBackground(VERDE_PRIMARIO);
        boton.setForeground(BLANCO);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        //Lo uso para refrescar datos, osea, como pueden tener datos no estaticos, los vuelvo a cargar.
        //Podria usar un switch, por ahora no cambiar nada.
        boton.addActionListener(e -> {
            if ("PANEL_1".equals(nombrePanelDestino) && panelTurnosReal != null) {
                panelTurnosReal.cargarTurnosDesdeBD();
            } else if ("PANEL_4".equals(nombrePanelDestino) && panelDisponibilidadReal != null) {
                panelDisponibilidadReal.cargarMedicosEnCombo();
            } else if ("PANEL_5".equals(nombrePanelDestino) && panelObrasSocialesReal != null) {
                panelObrasSocialesReal.cargarObrasSociales();
            } else if ("PANEL_6".equals(nombrePanelDestino) && panelUsuariosReal != null) {
                panelUsuariosReal.cargarUsuariosDesdeBD();
            }

            //Muestra el panel destino del cuerpo dinamico.
            navegadorPaneles.show(panelCuerpoDinamico, nombrePanelDestino);
        });
        
        panelMenuLateral.add(boton);
        panelMenuLateral.add(Box.createRigidArea(new Dimension(0, 15))); 
    }

    //Genera el menu lateral, deberia ser un simple for.
    protected void generarMenuLateral(String[] titulosBotones, String[] idsPanelesDestino) {
        if (titulosBotones.length != idsPanelesDestino.length) {
            throw new IllegalArgumentException("La cantidad de títulos y de IDs de paneles debe ser la misma.");
        }
        for (int i = 0; i < titulosBotones.length; i++) {
            agregarBotonMenu(titulosBotones[i], idsPanelesDestino[i]);
        }
    }

    //Pop_up para cerrar sesion
    private void accionCerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro que desea cerrar sesión?", "Cerrar Sesión", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (respuesta == JOptionPane.YES_OPTION) {
            this.dispose(); 
            new LoginFrame().setVisible(true); 
        }
    }    
    
    //Constructores de turnos y pacientes.
    protected void cargarPanelTurnos() {
        panelTurnosReal = new PanelTurnos(VERDE_PRIMARIO, VERDE_SECUNDARIO, GRIS_FONDO);
        panelCuerpoDinamico.add(panelTurnosReal, "PANEL_1");
    }

    //Solo voy a explicar este, el panel de medicos.
    protected void cargarPanelPacientes(boolean permitirEdicion) {
        //Necesito estos servicios de la otra capa.
        PacienteService pacienteService = new PacienteService();
        ObraSocialService obraSocialService = new ObraSocialService();
        TurnoService turnoService = new TurnoService();
        
        //Uso esto para armar los encabezados de la tabla.
        String[] colPacientes = {"ID", "DNI", "Nombre Completo", "Teléfono", "Obra Social"};
        String[] colHistorial = {"Fecha", "Motivo / Observación", "Médico Tratante", "Estado"};

        //Consulto todos los pacientes -> Los vuelco en una matriz.
        List<Paciente> listaInicial = pacienteService.obtenerPacientes();
        Object[][] datosPacientesReales = new Object[listaInicial.size()][5];
        for (int i = 0; i < listaInicial.size(); i++) {
            Paciente p = listaInicial.get(i);
            String nombreOS = (p.getObraSocial() != null) ? p.getObraSocial().getNombre() : "Particular / Sin OS";
            datosPacientesReales[i][0] = p.getId();
            datosPacientesReales[i][1] = p.getDni();
            datosPacientesReales[i][2] = p.getUsuario().getNombreApellido();
            datosPacientesReales[i][3] = p.getTelefono();
            datosPacientesReales[i][4] = nombreOS;
        }

        //Solo si lo puedo editar, procedo. Si no los puedo editar, no agrego los botones para agregar/eliminar.
        AccionesMaestro acciones = !permitirEdicion ? null : new AccionesMaestro() {
            @Override
            //Abro un dialogo, es para poner datos y pasarlcelos a service.
            public void alHacerClicAgregar(DefaultTableModel modeloIzquierdo) {
                JDialog dialogo = new JDialog(VentanaBase.this, "Registrar Nuevo Paciente", true);
                dialogo.setSize(380, 340);
                dialogo.setLocationRelativeTo(VentanaBase.this);
                dialogo.setLayout(null);

                JLabel lblDni = new JLabel("DNI:");
                lblDni.setBounds(30, 20, 300, 20);
                dialogo.add(lblDni);
                JTextField txtDni = new JTextField();
                txtDni.setBounds(30, 45, 300, 25);
                dialogo.add(txtDni);

                JLabel lblNombre = new JLabel("Nombre y Apellido:");
                lblNombre.setBounds(30, 80, 300, 20);
                dialogo.add(lblNombre);
                JTextField txtNombre = new JTextField();
                txtNombre.setBounds(30, 105, 300, 25);
                dialogo.add(txtNombre);

                JLabel lblTel = new JLabel("Teléfono:");
                lblTel.setBounds(30, 140, 300, 20);
                dialogo.add(lblTel);
                JTextField txtTel = new JTextField();
                txtTel.setBounds(30, 165, 300, 25);
                dialogo.add(txtTel);

                JLabel lblOS = new JLabel("Obra Social:");
                lblOS.setBounds(30, 200, 300, 20);
                dialogo.add(lblOS);

                JComboBox<Object> cmbObraSocial = new JComboBox<>();
                cmbObraSocial.setBounds(30, 225, 300, 25);
                cmbObraSocial.addItem("Particular / Sin Obra Social");
                for (ObraSocial os : obraSocialService.listarActivas()) {
                    cmbObraSocial.addItem(os);
                }
                dialogo.add(cmbObraSocial);

                JButton btnGuardar = new JButton("Guardar Paciente");
                btnGuardar.setBounds(110, 265, 150, 35);
                btnGuardar.setBackground(VERDE_PRIMARIO);
                btnGuardar.setForeground(Color.WHITE);

                //La accion del boton guardar, confirma que todos los datos que requiere esten en orden.
                btnGuardar.addActionListener(ev -> {
                    String dni = txtDni.getText().trim();
                    String nombre = txtNombre.getText().trim();
                    String tel = txtTel.getText().trim();

                    if (dni.isEmpty() || nombre.isEmpty()) {
                        JOptionPane.showMessageDialog(dialogo, "DNI y Nombre son obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    ObraSocial osSeleccionada = (cmbObraSocial.getSelectedItem() instanceof ObraSocial)
                            ? (ObraSocial) cmbObraSocial.getSelectedItem() : null;

                    try {
                        Paciente nuevoPaciente = new Paciente(0, dni, nombre, tel, osSeleccionada);
                        Paciente pacienteCreado = pacienteService.crear(nuevoPaciente);

                        String nombreOS = (osSeleccionada != null) ? osSeleccionada.getNombre() : "Particular / Sin OS";
                        modeloIzquierdo.addRow(new Object[]{pacienteCreado.getId(), dni, nombre, tel, nombreOS});

                        JOptionPane.showMessageDialog(dialogo, "Paciente registrado correctamente.");
                        dialogo.dispose();
                    } catch (IllegalArgumentException exVal) {
                        JOptionPane.showMessageDialog(dialogo, exVal.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialogo, "Error al guardar paciente:\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                    }
                });

                dialogo.add(btnGuardar);
                dialogo.setVisible(true);
            }

            @Override
            //El boton de eliminar, usa los servicios para eliminarlos de la base de datos.
            public void alHacerClicEliminar(String idSeleccionado, int filaSeleccionada, DefaultTableModel modeloIzquierdo, DefaultTableModel modeloDerecho) {
                int confirmar = JOptionPane.showConfirmDialog(VentanaBase.this,
                        "¿Seguro que desea dar de baja a este paciente? (Sus turnos históricos se conservarán)",
                        "Confirmar Baja", JOptionPane.YES_NO_OPTION);

                if (confirmar == JOptionPane.YES_OPTION) {
                    try {
                        int idPaciente = Integer.parseInt(idSeleccionado);
                        pacienteService.eliminar(idPaciente, turnoService.obtenerTurnos());

                        modeloIzquierdo.removeRow(filaSeleccionada);
                        modeloDerecho.setRowCount(0);
                        JOptionPane.showMessageDialog(VentanaBase.this, "Paciente dado de baja correctamente.");
                    } catch (IllegalStateException | IllegalArgumentException exNegocio) {
                        JOptionPane.showMessageDialog(VentanaBase.this, exNegocio.getMessage(), "No se puede eliminar", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(VentanaBase.this, "Error al dar de baja: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        //Armado del panel dividido con JComboBox, le tengo que pasar todos los datos que pide el constructor.
        PanelDivididoMaestro panelPacientesReal = new PanelDivididoMaestro(
            "Registro General de Pacientes e Historial de Consultas",
            colPacientes, datosPacientesReales, colHistorial,
            VERDE_PRIMARIO, VERDE_SECUNDARIO, GRIS_FONDO,
            (idSeleccionado, modeloDerecho) -> {
                try {
                    int idPaciente = Integer.parseInt(idSeleccionado);
                    List<Turno> turnosPaciente = turnoService.obtenerTurnosPorPaciente(idPaciente);
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    for (Turno t : turnosPaciente) {
                        String fecha = t.getFecha().format(fmt);
                        String motivo = (t.getMotivoConsulta() != null) ? t.getMotivoConsulta() : "Sin observaciones";
                        String medico = (t.getMedico() != null) ? "Dr/a. " + t.getMedico().getNombreApellido() : "No asignado";
                        modeloDerecho.addRow(new Object[]{fecha, motivo, medico, t.getEstado().name()});
                    }
                } catch (Exception ex) {
                    System.out.println("Error al cargar historial del paciente: " + ex.getMessage());
                }
            },
            acciones
        );
        panelCuerpoDinamico.add(panelPacientesReal, "PANEL_2");
    }     
    
    //Same as the above, won't waste time explainning, it just uses different parameters.
    protected void cargarPanelMedicos(boolean permitirEdicion) {
        MedicoService medicoService = new MedicoService();

        String[] colMedicos = {"ID", "Nombre y Apellido", "Especialidad", "Estado"};
        String[] colHorarios = {"Día de la Semana", "Hora Inicio", "Hora Fin", "Estado"};

        List<Medico> listaMedicos = medicoService.obtenerMedicosActivos();
        Object[][] datosMedicosReales = new Object[listaMedicos.size()][4];
        for (int i = 0; i < listaMedicos.size(); i++) {
            Medico m = listaMedicos.get(i);
            datosMedicosReales[i][0] = m.getId();
            datosMedicosReales[i][1] = "Dr/a. " + m.getNombreApellido();
            datosMedicosReales[i][2] = m.getEspecialidad();
            datosMedicosReales[i][3] = m.getEstado().name();
        }

        AccionesMaestro acciones = !permitirEdicion ? null : new AccionesMaestro() {
            @Override
            public void alHacerClicAgregar(DefaultTableModel modeloIzquierdo) {
                JDialog dialogo = new JDialog(VentanaBase.this, "Registrar Nuevo Médico", true);
                dialogo.setSize(400, 460);
                dialogo.setLocationRelativeTo(VentanaBase.this);
                dialogo.setLayout(null);

                JLabel lblNombre = new JLabel("Nombre y Apellido:");
                lblNombre.setBounds(30, 15, 320, 20);
                dialogo.add(lblNombre);
                JTextField txtNombre = new JTextField();
                txtNombre.setBounds(30, 40, 320, 25);
                dialogo.add(txtNombre);

                JLabel lblEspecialidad = new JLabel("Especialidad:");
                lblEspecialidad.setBounds(30, 75, 320, 20);
                dialogo.add(lblEspecialidad);
                JTextField txtEspecialidad = new JTextField();
                txtEspecialidad.setBounds(30, 100, 320, 25);
                dialogo.add(txtEspecialidad);

                JLabel lblEmail = new JLabel("Email de Acceso (Login):");
                lblEmail.setBounds(30, 135, 320, 20);
                dialogo.add(lblEmail);
                JTextField txtEmail = new JTextField();
                txtEmail.setBounds(30, 160, 320, 25);
                dialogo.add(txtEmail);

                JLabel lblPass = new JLabel("Contraseña:");
                lblPass.setBounds(30, 195, 320, 20);
                dialogo.add(lblPass);
                JPasswordField txtPass = new JPasswordField("123");
                txtPass.setBounds(30, 220, 320, 25);
                dialogo.add(txtPass);

                JLabel lblHorario = new JLabel("Horario Inicial (Día / Inicio / Fin):");
                lblHorario.setBounds(30, 255, 320, 20);
                dialogo.add(lblHorario);

                String[] dias = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO"};
                JComboBox<String> cmbDia = new JComboBox<>(dias);
                cmbDia.setBounds(30, 280, 120, 25);
                dialogo.add(cmbDia);

                JTextField txtInicio = new JTextField("08:00");
                txtInicio.setBounds(160, 280, 90, 25);
                dialogo.add(txtInicio);

                JTextField txtFin = new JTextField("12:00");
                txtFin.setBounds(260, 280, 90, 25);
                dialogo.add(txtFin);

                JButton btnGuardar = new JButton("Guardar Médico");
                btnGuardar.setBounds(120, 340, 150, 35);
                btnGuardar.setBackground(VERDE_PRIMARIO);
                btnGuardar.setForeground(Color.WHITE);

                btnGuardar.addActionListener(ev -> {
                    String nombre = txtNombre.getText().trim();
                    String esp = txtEspecialidad.getText().trim();
                    String email = txtEmail.getText().trim();
                    String pass = new String(txtPass.getPassword()).trim();

                    if (nombre.isEmpty() || esp.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                        JOptionPane.showMessageDialog(dialogo, "Todos los campos son obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    try {
                        DayOfWeek diaEnum = convertirDiaAEnum((String) cmbDia.getSelectedItem());
                        LocalTime hInicio = LocalTime.parse(txtInicio.getText().trim());
                        LocalTime hFin = LocalTime.parse(txtFin.getText().trim());

                        List<Disponibilidad> disps = new ArrayList<>();
                        disps.add(new Disponibilidad(diaEnum, hInicio, hFin));

                        Medico nuevoMedico = new Medico(esp, EstadoMedico.ACTIVO, disps, 0, nombre, pass);
                        Medico medicoCreado = medicoService.crear(nuevoMedico, email);

                        modeloIzquierdo.addRow(new Object[]{medicoCreado.getId(), "Dr/a. " + nombre, esp, "ACTIVO"});
                        JOptionPane.showMessageDialog(dialogo, "Médico registrado con éxito.");
                        dialogo.dispose();
                    } catch (IllegalArgumentException exVal) {
                        JOptionPane.showMessageDialog(dialogo, exVal.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialogo, "Error al registrar médico:\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                    }
                });

                dialogo.add(btnGuardar);
                dialogo.setVisible(true);
            }

            @Override
            public void alHacerClicEliminar(String idSeleccionado, int filaSeleccionada, DefaultTableModel modeloIzquierdo, DefaultTableModel modeloDerecho) {
                int confirmar = JOptionPane.showConfirmDialog(VentanaBase.this,
                        "¿Seguro que desea dar de baja a este médico? (Dejará de aparecer activo pero conservará sus turnos)",
                        "Confirmar Baja", JOptionPane.YES_NO_OPTION);

                if (confirmar == JOptionPane.YES_OPTION) {
                    try {
                        int idMedico = Integer.parseInt(idSeleccionado);
                        medicoService.darDeBaja(idMedico);

                        modeloIzquierdo.removeRow(filaSeleccionada);
                        modeloDerecho.setRowCount(0);
                        JOptionPane.showMessageDialog(VentanaBase.this, "Médico dado de baja correctamente.");
                    } catch (IllegalStateException | IllegalArgumentException exNegocio) {
                        JOptionPane.showMessageDialog(VentanaBase.this, exNegocio.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(VentanaBase.this, "Error al dar de baja al médico: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        PanelDivididoMaestro panelMedicosReal = new PanelDivididoMaestro(
            "Panel de Control y Disponibilidad de Médicos",
            colMedicos, datosMedicosReales, colHorarios,
            VERDE_PRIMARIO, VERDE_SECUNDARIO, GRIS_FONDO,
            (idSeleccionado, modeloDerecho) -> {
                try {
                    int idMedico = Integer.parseInt(idSeleccionado);
                    List<Disponibilidad> horarios = medicoService.obtenerHorarios(idMedico);
                    DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");

                    for (Disponibilidad d : horarios) {
                        modeloDerecho.addRow(new Object[]{
                            traducirDia(d.getDia()),
                            d.getHoraInicio().format(fmtHora),
                            d.getHoraFin().format(fmtHora),
                            "Disponible"
                        });
                    }
                } catch (Exception ex) {
                    System.out.println("Error al cargar horarios del médico: " + ex.getMessage());
                }
            },
            acciones
        );
        panelCuerpoDinamico.add(panelMedicosReal, "PANEL_3");
    }

    //Los otros paneles menos complicados.
    protected void cargarPanelDisponibilidad() {
        panelDisponibilidadReal = new PanelDisponibilidad(VERDE_PRIMARIO, VERDE_SECUNDARIO, GRIS_FONDO);
        panelCuerpoDinamico.add(panelDisponibilidadReal, "PANEL_4");
    }

    protected void cargarPanelObrasSociales() {
        panelObrasSocialesReal = new PanelObrasSociales(VERDE_PRIMARIO, VERDE_SECUNDARIO, GRIS_FONDO);
        panelCuerpoDinamico.add(panelObrasSocialesReal, "PANEL_5");
    }

    protected void cargarPanelUsuarios() {
        panelUsuariosReal = new PanelUsuariosAdmin(VERDE_PRIMARIO, VERDE_SECUNDARIO, GRIS_FONDO);
        panelCuerpoDinamico.add(panelUsuariosReal, "PANEL_6");
    }

    
    //AUXULIAR: TRADUCIR DayOfWeek DE INGLES A ESPANOL. TAMBIEN AL REVES
    private String traducirDia(DayOfWeek dia) {
        switch (dia) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return dia.name();
        }
    }

    private DayOfWeek convertirDiaAEnum(String diaEsp) {
        switch (diaEsp.toUpperCase()) {
            case "LUNES": return DayOfWeek.MONDAY;
            case "MARTES": return DayOfWeek.TUESDAY;
            case "MIÉRCOLES": return DayOfWeek.WEDNESDAY;
            case "JUEVES": return DayOfWeek.THURSDAY;
            case "VIERNES": return DayOfWeek.FRIDAY;
            case "SÁBADO": return DayOfWeek.SATURDAY;
            case "DOMINGO": return DayOfWeek.SUNDAY;
            default: return DayOfWeek.MONDAY;
        }
    }
    
}