/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.interfaz;

/**
 *
 * @author pc
 */
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

/**
 * Utilidad visual para el filtrado en vivo de tablas (JTable) en la interfaz.
 */
public final class BuscadorTabla {

    private BuscadorTabla() {
        // Evita instanciacion inecesaria mas arriba.
    }

    public static void filtrarTabla(JTable tabla, String texto) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        if (texto == null || texto.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // "(?i)" ignora mayúsculas/minúsculas y Pattern.quote evita errores con símbolos
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto.trim())));
        }
    }
}
