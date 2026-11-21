/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.entidades;

/**
 *
 * @author mcele
 */
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Medico extends Usuario {
    private String especialidad;
    private EstadoMedico estado;
    private List<Disponibilidad> disponibilidades;

    public enum EstadoMedico { ACTIVO, INACTIVO }

    // --
    public static class Disponibilidad {
        private final DayOfWeek dia;
        private final LocalTime horaInicio;
        private final LocalTime horaFin;

        public Disponibilidad(DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
            this.dia = dia;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }

        public DayOfWeek getDia() {
            return dia;
        }

        public LocalTime getHoraInicio() {
            return horaInicio;
        }

        public LocalTime getHoraFin() {
            return horaFin;
        }
    }
    //--

    public Medico(String especialidad, EstadoMedico estado, List<Disponibilidad> disponibilidades, int id, String nombreApellido, String password) {
        super(id, nombreApellido, password);
        this.especialidad = especialidad;
        this.estado = estado;
        this.disponibilidades = disponibilidades;
    }

   



    public Rol getRol() {
        return Rol.MEDICO;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public EstadoMedico getEstado() {
        return estado;
    }

  public void setEstado(EstadoMedico estado) {
    this.estado = estado;
}

    public List<Disponibilidad> getDisponibilidades() {
        return new ArrayList<>(disponibilidades);
    }

    public void setDisponibilidades(List<Disponibilidad> disponibilidades) {
        this.disponibilidades = new ArrayList<>(disponibilidades);
    }
}