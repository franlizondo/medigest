/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.medigest;

import com.mycompany.medigest.interfaz.LoginFrame;

public class MediGest {

    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
        
    }
}
