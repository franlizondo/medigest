/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;

/**
 *
 * @author mcele
 */
import com.mycompany.medigest.DAO.ObraSocialDAO;
import com.mycompany.medigest.entidades.ObraSocial;
import java.util.List;
import static com.mycompany.medigest.servicios.Utilidades.esVacio;

public class ObraSocialService {

    private final ObraSocialDAO obraSocialDAO = new ObraSocialDAO();

    public List<ObraSocial> listarTodas() {
        return obraSocialDAO.listarTodas();
    }

    public List<ObraSocial> listarActivas() {
        return obraSocialDAO.listarActivas();
    }

    public int crear(ObraSocial os) {
        if (os == null || esVacio(os.getNombre()) || esVacio(os.getCodigo())) {
            throw new IllegalArgumentException("El nombre y el código de la obra social son obligatorios.");
        }
        if (obraSocialDAO.buscarPorNombre(os.getNombre().trim()) != null) {
            throw new IllegalArgumentException("Ya existe una obra social con ese nombre.");
        }
        return obraSocialDAO.insertar(os);
    }

    public boolean darDeBaja(int id) {
        return obraSocialDAO.eliminar(id);
    }
}