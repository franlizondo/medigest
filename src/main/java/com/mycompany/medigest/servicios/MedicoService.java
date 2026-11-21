/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;

import com.mycompany.medigest.DAO.DisponibilidadDAO;
import com.mycompany.medigest.DAO.MedicoDAO;
import com.mycompany.medigest.entidades.Medico;
import com.mycompany.medigest.entidades.Medico.Disponibilidad;
import com.mycompany.medigest.entidades.Medico.EstadoMedico;

import java.text.Collator;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mycompany.medigest.servicios.Utilidades.esVacio;
import static com.mycompany.medigest.servicios.Utilidades.normalizarTexto;

public class MedicoService {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-AR");

    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final DisponibilidadDAO disponibilidadDAO = new DisponibilidadDAO();


    public List<Medico> obtenerMedicos() {
        return medicoDAO.listarTodos();
    }

    public Medico buscarPorId(int id) {
        return medicoDAO.buscarPorId(id);
    }

    public List<Medico> filtrar(String apellido, String especialidad) {
        String busqueda = normalizarTexto(apellido);
        String esp = normalizarTexto(especialidad);

        List<Medico> resultado = new ArrayList<>();
        List<Medico> medicos = obtenerMedicos();

        for (int i = 0; i < medicos.size(); i++) {
            Medico m = medicos.get(i);
            boolean coincideApellido = busqueda.isEmpty()
                    || normalizarTexto(m.getNombreApellido()).contains(busqueda);

            boolean coincideEspecialidad = esp.isEmpty()
                    || normalizarTexto(m.getEspecialidad()).equals(esp);

            if (coincideApellido && coincideEspecialidad) {
                resultado.add(m);
            }
        }

        return resultado;
    }

    public List<Medico> obtenerMedicosActivos() {
        return medicoDAO.listarActivos();
    }

    public List<Medico> obtenerListado(String apellido, String especialidad) {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));

        List<Medico> listado = new ArrayList<>();

        List<Medico> filtrados = filtrar(apellido, especialidad);

        for (int i = 0; i < filtrados.size(); i++) {
            Medico m = filtrados.get(i);
            if (m.getEstado() == EstadoMedico.ACTIVO) {
                listado.add(m);
            }
        }

        listado.sort(Comparator.comparing(
                (Medico m) -> m.getNombreApellido(), collator));

        return listado;
    }

    public List<String> obtenerEspecialidades() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));

        List<String> especialidades = new ArrayList<>();

        List<Medico> activos = obtenerMedicosActivos();

        for (int i = 0; i < activos.size(); i++) {
            Medico m = activos.get(i);
            if (!especialidades.contains(m.getEspecialidad())) {
                especialidades.add(m.getEspecialidad());
            }
        }

        especialidades.sort(collator);

        return especialidades;
    }

    public List<Medico> obtenerMedicosPorEspecialidad(String especialidad) {
        return medicoDAO.listarPorEspecialidad(especialidad);
    }

    public List<Disponibilidad> obtenerHorarios(int id) {
        Medico medico = buscarPorId(id);

        if (medico == null) {
            throw new IllegalArgumentException("El médico no existe.");
        }

        return disponibilidadDAO.listarPorMedico(id);
    }

    public void agregarDisponibilidad(int idMedico, Disponibilidad nueva) {
        List<Disponibilidad> actuales = obtenerHorarios(idMedico);
        actuales.add(nueva);
        validarHorarios(actuales);
        disponibilidadDAO.agregar(idMedico, nueva);
    }


    public Medico crear(Medico medico, String email) {

        validar(medico);

        medico.setNombreApellido(medico.getNombreApellido().trim());
        medico.setEspecialidad(medico.getEspecialidad().trim());
        medico.setEstado(EstadoMedico.ACTIVO);

        //Tuve que cambiar a dao, en vez de listas.
        medicoDAO.insertar(medico, email);

        return medico;
    }

    
    public Medico modificar(int id, Medico nuevosDatos) {

        Medico existente = buscarPorId(id);

        if (existente == null) {
            throw new IllegalArgumentException("El médico no existe.");
        }

        if (existente.getEstado() != EstadoMedico.ACTIVO) {
            throw new IllegalStateException(
                    "No se puede modificar un médico dado de baja.");
        }

        validar(nuevosDatos);

        existente.setNombreApellido(nuevosDatos.getNombreApellido().trim());
        existente.setEspecialidad(nuevosDatos.getEspecialidad().trim());
        existente.setDisponibilidades(nuevosDatos.getDisponibilidades());

        medicoDAO.actualizar(existente);

        return existente;
    }


    public Medico darDeBaja(int id) {

        Medico medico = buscarPorId(id);

        if (medico == null) {
            throw new IllegalArgumentException("El médico no existe.");
        }

        if (medico.getEstado() == EstadoMedico.INACTIVO) {
            throw new IllegalStateException(
                    "El médico ya se encuentra dado de baja.");
        }

        medico.setEstado(EstadoMedico.INACTIVO);
        medicoDAO.eliminar(id);

        return medico;
    }

    public Medico reactivar(int id) {

        Medico medico = buscarPorId(id);

        if (medico == null) {
            throw new IllegalArgumentException("El médico no existe.");
        }

        if (medico.getEstado() == EstadoMedico.ACTIVO) {
            throw new IllegalStateException(
                    "El médico ya se encuentra activo.");
        }

        medico.setEstado(EstadoMedico.ACTIVO);
        medicoDAO.actualizar(medico);

        return medico;
    }


    private void validar(Medico m) {

        if (m == null
                || esVacio(m.getNombreApellido())
                || esVacio(m.getEspecialidad())) {
            throw new IllegalArgumentException(
                    "Todos los campos del médico son obligatorios.");
        }

        if (m.getNombreApellido().trim().length() < 3) {
            throw new IllegalArgumentException(
                    "El nombre del médico no es válido.");
        }

        validarHorarios(m.getDisponibilidades());
    }

    public void validarHorarios(List<Disponibilidad> horarios) {

        if (horarios == null || horarios.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar al menos un horario de atención.");
        }

        Map<DayOfWeek, List<Disponibilidad>> porDia = new EnumMap<>(DayOfWeek.class);

        for (int i = 0; i < horarios.size(); i++) {
            Disponibilidad d = horarios.get(i);

            if (d == null || d.getDia() == null
                    || d.getHoraInicio() == null || d.getHoraFin() == null) {
                throw new IllegalArgumentException(
                        "Cada horario debe tener día, hora de inicio y hora de fin.");
            }

            LocalTime inicio = d.getHoraInicio();
            LocalTime fin = d.getHoraFin();

            if (!inicio.isBefore(fin)) {
                throw new IllegalArgumentException(
                        "La hora de inicio debe ser anterior a la hora de fin.");
            }

            if (inicio.getMinute() % 30 != 0 || fin.getMinute() % 30 != 0) {
                throw new IllegalArgumentException(
                        "Los horarios médicos deben respetar intervalos de 30 minutos.");
            }

            porDia.computeIfAbsent(d.getDia(), k -> new ArrayList<>()).add(d);
        }

        List<DayOfWeek> dias = new ArrayList<>(porDia.keySet());

        for (int k = 0; k < dias.size(); k++) {

            DayOfWeek dia = dias.get(k);
            List<Disponibilidad> bloques = new ArrayList<>(porDia.get(dia));
            bloques.sort(Comparator.comparing(Disponibilidad::getHoraInicio));

            for (int i = 1; i < bloques.size(); i++) {
                if (bloques.get(i).getHoraInicio()
                        .isBefore(bloques.get(i - 1).getHoraFin())) {

                    String nombreDia = dia.getDisplayName(TextStyle.FULL, LOCALE_ES);

                    throw new IllegalArgumentException(
                            "Los horarios del día " + nombreDia + " se superponen.");
                }
            }
        }
    }
}