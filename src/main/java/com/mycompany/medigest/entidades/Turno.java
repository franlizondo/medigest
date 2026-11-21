/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.entidades;

/**
 *
 * @author mcele
 */
import java.time.LocalDate;
import java.time.LocalTime;

public class Turno {

  //--
    public enum EstadoTurno { PENDIENTE, CONFIRMADO, CANCELADO, AUSENTE, ATENDIDO }
//--

    private int id;
    private LocalDate fecha;
    private LocalTime horaInicio, horaFin;
    private Paciente paciente;
    private Medico medico;
    private Recepcionista recepcionista;
    private String motivoConsulta;
    private EstadoTurno estado;

    public Turno(int id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                 Paciente paciente, Medico medico, Recepcionista recepcionista,
                 String motivoConsulta) {
        this.id = id;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.paciente = paciente;
        this.medico = medico;
        this.recepcionista = recepcionista;
        this.motivoConsulta = motivoConsulta;
        this.estado = EstadoTurno.PENDIENTE;
    }

    public int getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public Recepcionista getRecepcionista() {
        return recepcionista;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }
}
    
    
    
    
    
    
   

