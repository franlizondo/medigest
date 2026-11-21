/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.medigest.servicios;

import com.mycompany.medigest.DAO.PacienteDAO;
import com.mycompany.medigest.entidades.Paciente;
import com.mycompany.medigest.entidades.Turno;
import com.mycompany.medigest.entidades.Turno.EstadoTurno;

import java.text.Collator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.mycompany.medigest.servicios.Utilidades.esVacio;
import static com.mycompany.medigest.servicios.Utilidades.normalizarDni;
import static com.mycompany.medigest.servicios.Utilidades.normalizarTexto;

public class PacienteService {

    private static final Pattern FORMATO_DNI = Pattern.compile("^\\d{7,8}$");
    private static final Pattern FORMATO_TELEFONO = Pattern.compile("^[0-9+\\-\\s()]{6,20}$");

    //Reemplaze lista en memoria por el DAO
    private final PacienteDAO pacienteDAO = new PacienteDAO();


    public List<Paciente> obtenerPacientes() {
        return pacienteDAO.listarTodos();
    }

    public Paciente buscarPorId(int id) {
        return pacienteDAO.buscarPorId(id);
    }

    public Paciente buscarPorDni(String dni) {
        String dniBuscado = normalizarDni(dni);

        if (dniBuscado.isEmpty()) {
            return null;
        }

        return pacienteDAO.buscarPorDni(dniBuscado);
    }

    public List<Paciente> filtrar(String dniOApellido, String obraSocial) {
        String busqueda = normalizarTexto(dniOApellido);
        String dniBusqueda = normalizarDni(dniOApellido);
        String obra = normalizarTexto(obraSocial);

        List<Paciente> resultado = new ArrayList<>();
        List<Paciente> pacientes = obtenerPacientes();

        for (int i = 0; i < pacientes.size(); i++) {
            Paciente p = pacientes.get(i);

            boolean coincideNombre = normalizarTexto(
                    p.getUsuario().getNombreApellido()).contains(busqueda);

            boolean coincideDni = !dniBusqueda.isEmpty()
                    && normalizarDni(p.getDni()).contains(dniBusqueda);

            boolean coincideBusqueda = busqueda.isEmpty() || coincideNombre || coincideDni;

            boolean coincideObra = obra.isEmpty()
                    || (p.getObraSocial() != null
                    && normalizarTexto(p.getObraSocial().getNombre()).equals(obra));

            if (coincideBusqueda && coincideObra) {
                resultado.add(p);
            }
        }

        return resultado;
    }


    public List<Paciente> obtenerListado(String dniOApellido, String obraSocial) {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));

        List<Paciente> listado = new ArrayList<>();

        List<Paciente> filtrados = filtrar(dniOApellido, obraSocial);

        for (int i = 0; i < filtrados.size(); i++) {
            Paciente p = filtrados.get(i);
            if (p.isActivo()) {
                listado.add(p);
            }
        }

        listado.sort(Comparator.comparing(
                (Paciente p) -> p.getUsuario().getNombreApellido(), collator));

        return listado;
    }


    public Paciente crear(Paciente paciente) {

        validar(paciente);

        if (buscarPorDni(paciente.getDni()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un paciente registrado con ese DNI.");
        }

        paciente.getUsuario().setNombreApellido(
                paciente.getUsuario().getNombreApellido().trim());
        paciente.setDni(normalizarDni(paciente.getDni()));
        paciente.setTelefono(paciente.getTelefono().trim());
        paciente.setActivo(true);

        // Guardando usando dao
        pacienteDAO.insertar(paciente);
        paciente.getUsuario().setId(paciente.getId());

        return paciente;
    }


    public Paciente modificar(int id, Paciente nuevosDatos) {

        Paciente existente = buscarPorId(id);

        if (existente == null) {
            throw new IllegalArgumentException("El paciente no existe.");
        }

        if (!existente.isActivo()) {
            throw new IllegalStateException(
                    "No se puede modificar un paciente dado de baja.");
        }

        validar(nuevosDatos);

        Paciente conMismoDni = buscarPorDni(nuevosDatos.getDni());

        if (conMismoDni != null && conMismoDni.getId() != id) {
            throw new IllegalArgumentException(
                    "El DNI ingresado ya pertenece a otro paciente.");
        }

        existente.getUsuario().setNombreApellido(
                nuevosDatos.getUsuario().getNombreApellido().trim());
        existente.setDni(normalizarDni(nuevosDatos.getDni()));
        existente.setTelefono(nuevosDatos.getTelefono().trim());
        existente.setFechaNacimiento(nuevosDatos.getFechaNacimiento());
        existente.setObraSocial(nuevosDatos.getObraSocial());


        pacienteDAO.actualizar(existente);

        return existente;
    }

    
    public void eliminar(int id, List<Turno> turnos) {

        Paciente paciente = buscarPorId(id);

        if (paciente == null) {
            throw new IllegalArgumentException("El paciente no existe.");
        }

        if (!paciente.isActivo()) {
            throw new IllegalStateException("El paciente ya fue dado de baja.");
        }

        for (int i = 0; i < turnos.size(); i++) {
            Turno t = turnos.get(i);
            boolean esDelPaciente = t.getPaciente().getId() == id;
            boolean estaActivo = t.getEstado() == EstadoTurno.PENDIENTE
                    || t.getEstado() == EstadoTurno.CONFIRMADO;

            if (esDelPaciente && estaActivo) {
                throw new IllegalStateException(
                        "No se puede eliminar el paciente porque tiene turnos pendientes o confirmados.");
            }
        }

        // Persistimos la baja lógica en MySQL
        paciente.setActivo(false);
        pacienteDAO.eliminar(id);
    }


    private void validar(Paciente p) {

        if (p == null
                || p.getUsuario() == null
                || esVacio(p.getUsuario().getNombreApellido())
                || esVacio(p.getDni())
                || esVacio(p.getTelefono())) {
            throw new IllegalArgumentException(
                    "Todos los campos del paciente son obligatorios.");
        }

        if (!FORMATO_DNI.matcher(normalizarDni(p.getDni())).matches()) {
            throw new IllegalArgumentException(
                    "El DNI debe contener entre 7 y 8 números.");
        }

        if (p.getUsuario().getNombreApellido().trim().length() < 3) {
            throw new IllegalArgumentException(
                    "El nombre del paciente no es válido.");
        }

        if (!FORMATO_TELEFONO.matcher(p.getTelefono().trim()).matches()) {
            throw new IllegalArgumentException(
                    "El número de contacto no es válido.");
        }

        if (p.getFechaNacimiento() != null
                && p.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no es válida.");
        }
    }
}