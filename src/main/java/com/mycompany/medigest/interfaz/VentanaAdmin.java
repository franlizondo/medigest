/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;


import javax.swing.*;
import java.awt.*;

public class VentanaAdmin extends VentanaBase {

    public VentanaAdmin(String nombreUsuario) {
        super(nombreUsuario, "Administrador");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));

        //Carga todos los paneles con permisos completos
        cargarPanelTurnos();
        cargarPanelPacientes(true);
        cargarPanelMedicos(true);
        cargarPanelDisponibilidad();
        cargarPanelObrasSociales();
        cargarPanelUsuarios();

        String[] titulosBotones = {
            "Gestión de Turnos", "Pacientes", "Medicos", 
            "Disponibilidad", "Obras Sociales", "Usuarios"
        };
        String[] idsPanelesDestino = {
            "PANEL_1", "PANEL_2", "PANEL_3", 
            "PANEL_4", "PANEL_5", "PANEL_6"
        };
        generarMenuLateral(titulosBotones, idsPanelesDestino);
    }
}