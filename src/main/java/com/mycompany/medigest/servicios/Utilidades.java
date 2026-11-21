/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;
 
import java.text.Normalizer;
 
public final class Utilidades {
 
    private Utilidades() {
    }
 
    public static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
 
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
 
    public static String normalizarDni(String dni) {
        return dni == null ? "" : dni.replaceAll("\\D", "");
    }
 
    public static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
