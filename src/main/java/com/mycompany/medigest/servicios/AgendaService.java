/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.Disponibilidad;
import com.mycompany.medigest.entidades.Medico.EstadoMedico;
import com.mycompany.medigest.entidades.Turno;
import com.mycompany.medigest.entidades.Turno.EstadoTurno;


public class AgendaService {

    public static final int DURACION_TURNO_MINUTOS = 30;

    public void validarFechaTurno(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha ingresada no es válida.");
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se pueden asignar turnos en fechas pasadas.");
        }
    }

    public List<Disponibilidad> obtenerHorarioAtencion(Medico medico, LocalDate fecha) {
        List<Disponibilidad> resultado = new ArrayList<>();

        if (medico == null || fecha == null) {
            return resultado;
        }

        List<Disponibilidad> disponibilidades = medico.getDisponibilidades();

        for (int i = 0; i < disponibilidades.size(); i++) {
            Disponibilidad d = disponibilidades.get(i);
            if (d.getDia() == fecha.getDayOfWeek()) {
                resultado.add(d);
            }
        }

        return resultado;
    }

    public List<LocalTime> generarHorariosDisponibles(Medico medico, LocalDate fecha, List<Turno> turnos, Integer idTurnoExcluir) {

        validarFechaTurno(fecha);

        List<LocalTime> disponibles = new ArrayList<>();

        if (medico == null || medico.getEstado() != EstadoMedico.ACTIVO) {
            return disponibles;
        }

        List<Disponibilidad> bloques = obtenerHorarioAtencion(medico, fecha);

        for (int i = 0; i < bloques.size(); i++) {
            Disponibilidad d = bloques.get(i);

            int inicio = aMinutos(d.getHoraInicio());
            int fin = aMinutos(d.getHoraFin());

            if (inicio >= fin || inicio % 30 != 0 || fin % 30 != 0) {
                continue;
            }

            for (int m = inicio; m + DURACION_TURNO_MINUTOS <= fin; m += DURACION_TURNO_MINUTOS) {

                LocalTime horaInicio = LocalTime.of(m / 60, m % 60);
                LocalTime horaFin = horaInicio.plusMinutes(DURACION_TURNO_MINUTOS);

                if (!estaOcupado(medico, fecha, horaInicio, horaFin, turnos, idTurnoExcluir)) {
                    disponibles.add(horaInicio);
                }
            }
        }

        Collections.sort(disponibles);

        return disponibles;
    }

    public boolean verificarDisponibilidadMedico(
            Medico medico, LocalDate fecha, LocalTime horaInicio,
            List<Turno> turnos, Integer idTurnoExcluir) {

        if (medico == null || medico.getEstado() != EstadoMedico.ACTIVO
                || fecha == null || horaInicio == null) {
            return false;
        }

        try {
            validarFechaTurno(fecha);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (horaInicio.getMinute() % 30 != 0) {
            return false;
        }

        return generarHorariosDisponibles(medico, fecha, turnos, idTurnoExcluir)
                .contains(horaInicio);
    }

    public List<LocalTime> obtenerDisponibilidadDiaria(
            Medico medico, LocalDate fecha, List<Turno> turnos) {

        if (medico == null) {
            throw new IllegalArgumentException("El médico no existe.");
        }

        return generarHorariosDisponibles(medico, fecha, turnos, null);
    }

    private boolean estaOcupado(
            Medico medico, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
            List<Turno> turnos, Integer idTurnoExcluir) {

        for (int i = 0; i < turnos.size(); i++) {
            Turno t = turnos.get(i);

            if (idTurnoExcluir != null && t.getId() == idTurnoExcluir) {
                continue;
            }

            boolean mismoMedico = t.getMedico().getId() == medico.getId();
            boolean mismaFecha = t.getFecha().equals(fecha);
            boolean ocupaHorario = t.getEstado() != EstadoTurno.CANCELADO;
            boolean seSuperpone = horaInicio.isBefore(t.getHoraFin())
                    && horaFin.isAfter(t.getHoraInicio());

            if (mismoMedico && mismaFecha && ocupaHorario && seSuperpone) {
                return true;
            }
        }

        return false;
    }

    private int aMinutos(LocalTime hora) {
        return hora.getHour() * 60 + hora.getMinute();
    }
}