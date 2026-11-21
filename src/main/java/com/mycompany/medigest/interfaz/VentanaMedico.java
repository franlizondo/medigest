/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;


import javax.swing.*;
import java.awt.*;

public class VentanaMedico extends VentanaBase {

    public VentanaMedico(String nombreUsuario) {
        super(nombreUsuario, "Médico");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));

        //Carga los paneles del medico.
        cargarPanelTurnos();
        cargarPanelPacientes(false);  // false = solo consulta pacientes e historial clínico
        cargarPanelDisponibilidad();

        //Le tengo que pasar esto para poder generar los botones del menu lateral.
        String[] titulosBotones = {
            "Mi Agenda / Turnos", "Pacientes", "Disponibilidad"
        };
        String[] idsPanelesDestino = {
            "PANEL_1", "PANEL_2", "PANEL_4"
        };
        generarMenuLateral(titulosBotones, idsPanelesDestino);
    }
}