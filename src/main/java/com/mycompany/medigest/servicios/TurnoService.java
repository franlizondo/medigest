/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;

import com.mycompany.medigest.DAO.TurnoDAO;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.EstadoMedico;
import com.mycompany.medigest.entidades.Paciente;
import com.mycompany.medigest.entidades.Recepcionista;
import com.mycompany.medigest.entidades.Turno;
import com.mycompany.medigest.entidades.Turno.EstadoTurno;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.mycompany.medigest.servicios.Utilidades.normalizarDni;
import static com.mycompany.medigest.servicios.Utilidades.normalizarTexto;

public class TurnoService {

    private final TurnoDAO turnoDAO = new TurnoDAO();

    private final PacienteService pacienteService;
    private final MedicoService medicoService;
    private final AgendaService agendaService;

    public TurnoService(PacienteService pacienteService,
            MedicoService medicoService,
            AgendaService agendaService) {
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
        this.agendaService = agendaService;
    }


    public TurnoService() {
        this(new PacienteService(), new MedicoService(), new AgendaService());
    }


    public List<Turno> obtenerTurnos() {
        return turnoDAO.listarTodos();
    }

    public Turno buscarPorId(int id) {
        return turnoDAO.buscarPorId(id);
    }

    public List<Turno> obtenerTurnosPorEstado(EstadoTurno estado) {
        List<Turno> resultado = new ArrayList<>();
        List<Turno> turnos = obtenerTurnos();

        for (int i = 0; i < turnos.size(); i++) {
            Turno t = turnos.get(i);
            if (t.getEstado() == estado) {
                resultado.add(t);
            }
        }

        return resultado;
    }

    public List<Turno> obtenerTurnosPorPaciente(int pacienteId) {
        return turnoDAO.listarPorPaciente(pacienteId);
    }

    public List<Turno> obtenerTurnosPorMedico(int medicoId) {
        List<Turno> resultado = new ArrayList<>();
        List<Turno> turnos = obtenerTurnos();

        for (int i = 0; i < turnos.size(); i++) {
            Turno t = turnos.get(i);
            if (t.getMedico().getId() == medicoId) {
                resultado.add(t);
            }
        }

        return resultado;
    }

    public List<Turno> obtenerTurnosPorMedicoYFecha(int medicoId, LocalDate fecha) {
        return turnoDAO.listarPorMedicoYFecha(medicoId, fecha);
    }


    public List<Turno> filtrarTurnos(LocalDate fecha, String especialidad,
            String medico, String dniOApellido) {

        String esp = normalizarTexto(especialidad);
        String med = normalizarTexto(medico);
        String busqueda = normalizarTexto(dniOApellido);
        String dniBusqueda = normalizarDni(dniOApellido);

        List<Turno> resultado = new ArrayList<>();
        List<Turno> turnos = obtenerTurnos();

        for (int i = 0; i < turnos.size(); i++) {
            Turno t = turnos.get(i);

            boolean coincideFecha = fecha == null || t.getFecha().equals(fecha);

            boolean coincideEspecialidad = esp.isEmpty()
                    || normalizarTexto(t.getMedico().getEspecialidad()).contains(esp);

            boolean coincideMedico = med.isEmpty()
                    || normalizarTexto(t.getMedico().getNombreApellido()).contains(med);

            boolean coincidePaciente = busqueda.isEmpty()
                    || normalizarTexto(t.getPaciente().getUsuario().getNombreApellido())
                            .contains(busqueda)
                    || (!dniBusqueda.isEmpty()
                    && normalizarDni(t.getPaciente().getDni()).contains(dniBusqueda));

            if (coincideFecha && coincideEspecialidad && coincideMedico && coincidePaciente) {
                resultado.add(t);
            }
        }

        return resultado;
    }

    public List<Turno> obtenerCalendarioTurnos(LocalDate fecha, String especialidad, String medico, String dniOApellido) {

        List<Turno> calendario = filtrarTurnos(fecha, especialidad, medico, dniOApellido);

        calendario.sort(Comparator.comparing(Turno::getFecha)
                .thenComparing(Turno::getHoraInicio));

        return calendario;
    }


    public List<String> obtenerAccionesDisponibles(Turno turno) {
        List<String> acciones = new ArrayList<>();

        switch (turno.getEstado()) {
            case PENDIENTE:
            case CONFIRMADO:
                acciones.add("Reprogramar");
                acciones.add("Cancelar");
                break;
            case CANCELADO:
            case AUSENTE:
                acciones.add("Reprogramar");
                break;
            default: 
                break;
        }

        return acciones;
    }


    public Turno crearNuevoTurno(LocalDate fecha, LocalTime horaInicio, int pacienteId, int medicoId, Recepcionista recepcionista, String motivoConsulta) {

        if (fecha == null || horaInicio == null) {
            throw new IllegalArgumentException(
                    "Debe completar todos los campos del turno.");
        }

        agendaService.validarFechaTurno(fecha);

        Paciente paciente = pacienteService.buscarPorId(pacienteId);

        if (paciente == null || !paciente.isActivo()) {
            throw new IllegalArgumentException("El paciente no está registrado.");
        }

        Medico medico = medicoService.buscarPorId(medicoId);

        if (medico == null || medico.getEstado() != EstadoMedico.ACTIVO) {
            throw new IllegalArgumentException(
                    "El médico no existe o se encuentra dado de baja.");
        }

        validarInicioCada30Minutos(horaInicio);

        List<Turno> turnos = obtenerTurnos();
        if (!agendaService.verificarDisponibilidadMedico(
                medico, fecha, horaInicio, turnos, null)) {
            throw new IllegalArgumentException(
                    "El horario seleccionado no está disponible o está fuera del horario de atención del médico.");
        }

        LocalTime horaFin = horaInicio.plusMinutes(AgendaService.DURACION_TURNO_MINUTOS);

        Turno nuevo = new Turno(0, fecha, horaInicio, horaFin,
                paciente, medico, recepcionista, motivoConsulta);

        int idGenerado = turnoDAO.insertar(nuevo);

        return turnoDAO.buscarPorId(idGenerado);
    }


    public Turno reprogramarTurno(int id, LocalDate nuevaFecha, LocalTime nuevaHoraInicio) {

        Turno turno = obtenerTurnoExistente(id);

        if (turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new IllegalStateException(
                    "No se puede reprogramar un turno atendido.");
        }

        if (nuevaFecha == null || nuevaHoraInicio == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una nueva fecha y horario.");
        }

        agendaService.validarFechaTurno(nuevaFecha);

        Medico medico = medicoService.buscarPorId(turno.getMedico().getId());

        if (medico == null || medico.getEstado() != EstadoMedico.ACTIVO) {
            throw new IllegalStateException("El médico no está disponible.");
        }

        validarInicioCada30Minutos(nuevaHoraInicio);

        List<Turno> turnos = obtenerTurnos();
        // Se excluye el propio turno para que no bloquee su mismo horario.
        if (!agendaService.verificarDisponibilidadMedico(
                medico, nuevaFecha, nuevaHoraInicio, turnos, id)) {
            throw new IllegalArgumentException(
                    "El nuevo horario no está disponible o está fuera del horario de atención del médico.");
        }

        turno.setFecha(nuevaFecha);
        turno.setHoraInicio(nuevaHoraInicio);
        turno.setHoraFin(nuevaHoraInicio.plusMinutes(AgendaService.DURACION_TURNO_MINUTOS));

        if (turno.getEstado() == EstadoTurno.CANCELADO
                || turno.getEstado() == EstadoTurno.AUSENTE) {
            turno.setEstado(EstadoTurno.PENDIENTE);
        }

        turnoDAO.actualizar(turno);

        return turno;
    }


    public Turno cancelarTurno(int id) {

        Turno turno = obtenerTurnoExistente(id);

        if (turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new IllegalStateException(
            "No se puede cancelar un turno atendido.");
        }
        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new IllegalStateException(
            "El turno ya se encuentra cancelado.");
        }
        if (turno.getEstado() == EstadoTurno.AUSENTE) {
            throw new IllegalStateException(
            "No se puede cancelar un turno marcado como ausente.");
        }
    turno.setEstado(EstadoTurno.CANCELADO);
    turnoDAO.cambiarEstado(id, EstadoTurno.CANCELADO);
    return turno;
    }


    public boolean eliminarTurno(int id) {
        return turnoDAO.eliminar(id);
    }
    

    public Turno cambiarEstadoTurno(int id, EstadoTurno nuevoEstado) {
        Turno turno = obtenerTurnoExistente(id);
        
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado ingresado no es válido.");
        }
        
        if (turno.getEstado() == EstadoTurno.ATENDIDO) {
            throw new IllegalStateException(
            "No se puede modificar un turno atendido.");
        }
        
        if (turno.getEstado() == EstadoTurno.CANCELADO) {
            throw new IllegalStateException(
            "El turno cancelado debe reprogramarse para volver a estar activo.");
        }
        
        if (nuevoEstado == EstadoTurno.CANCELADO) {
            throw new IllegalArgumentException(
            "Para cancelar un turno use cancelarTurno.");
        }
        
        turno.setEstado(nuevoEstado);
        turnoDAO.cambiarEstado(id, nuevoEstado);
        return turno;
    }
    

    private Turno obtenerTurnoExistente(int id) {
    
        Turno turno = buscarPorId(id);
        if (turno == null) {
            throw new IllegalArgumentException("El turno no existe.");
        }
        
        return turno;
    }
    
    private void validarInicioCada30Minutos(LocalTime horaInicio) {
        if (horaInicio.getMinute() % 30 != 0) {
            throw new IllegalArgumentException(
            "Los turnos deben comenzar cada 30 minutos.");
        }
    }
}