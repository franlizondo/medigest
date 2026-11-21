/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.entidades;

import java.time.LocalDate;

/**
 *
 * @author mcele
 */
public class Paciente {

    private int id;
    private Usuario usuario;
    private String dni;
    private LocalDate fechaNacimiento;
    private String telefono;
    private ObraSocial obraSocial;
    private boolean activo;

    public Paciente(int id, Usuario usuario, String dni, LocalDate fechaNacimiento, String telefono, ObraSocial obraSocial, boolean activo) {

        this.id = id;
        this.usuario = usuario;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.obraSocial = obraSocial;
        this.activo = activo;
    }
    
    //Tuve que agregar este constructor, por polimorfismo PAcientesDAO necesita
    public Paciente(int id, String dni, String nombre, String telefono, ObraSocial obraSocial) {
        this.id = id;
        this.dni = dni;
        // Encapsulamos el String nombre dentro del objeto Usuario para tus getters actuales
        this.usuario = new com.mycompany.medigest.entidades.Usuario(0, nombre, "");
        this.telefono = telefono;
        this.obraSocial = obraSocial;
        this.activo = true; 
        this.fechaNacimiento = null; 
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public ObraSocial getObraSocial() {
        return obraSocial;
    }

    public void setObraSocial(ObraSocial obraSocial) {
        this.obraSocial = obraSocial;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}