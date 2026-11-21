/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.entidades;

/**
 *
 * @author mcele
 */
public class ObraSocial {
    private int id;
    private String nombre;
    private String codigo;
    private boolean activa;

    public ObraSocial(int id, String nombre, String codigo, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.activa = activa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    
    //Nota MH: I need this. JComboBox calls toString(), if not this returns class name and memori direction.
    @Override
    public String toString() {
        return this.nombre + " (" + this.codigo + ")";
    }
    
}
