/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

import javax.swing.*;
import java.awt.*;

public class VentanaRecepcionista extends VentanaBase {

    public VentanaRecepcionista(String nombreUsuario) {
        super(nombreUsuario, "Recepcionista");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));

        //Cargo los paneles del recep.
        cargarPanelTurnos();
        cargarPanelPacientes(true);   //Puede registrar y dar de baja pacientes
        cargarPanelMedicos(false);    //false = solo consulta directorio medico y horarios
        cargarPanelDisponibilidad();

        String[] titulosBotones = {
            "Gestión de Turnos", "Pacientes", "Medicos", "Disponibilidad"
        };
        String[] idsPanelesDestino = {
            "PANEL_1", "PANEL_2", "PANEL_3", "PANEL_4"
        };
        generarMenuLateral(titulosBotones, idsPanelesDestino);
    }
}