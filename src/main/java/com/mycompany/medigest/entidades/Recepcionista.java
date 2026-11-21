/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.entidades;

/**
 *
 * @author mcele
 */
public class Recepcionista extends Usuario {
    
    public Recepcionista(int id, String nombreApellido, String password) {
        super(id, nombreApellido, password);
    }
    
    public Rol getRol() {
    return Rol.RECEPECIONISTA;
}
}
