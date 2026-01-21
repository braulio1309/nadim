package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SesionExpedicion {
    private final String codigo;
    private EstadoSesion estado;
    private final Map<String, Integer> recursosCompartidos = new HashMap<>();
    private final List<Explorador> exploradores = new ArrayList<>();
    private final int minimoJugadores;

    public SesionExpedicion(String codigo, int minimoJugadores) {
        this.codigo = Objects.requireNonNull(codigo, "codigo");
        if (minimoJugadores < 1) {
            throw new IllegalArgumentException("minimoJugadores debe ser positivo");
        }
        this.minimoJugadores = minimoJugadores;
        this.estado = EstadoSesion.PLANIFICADA;
    }

    public boolean agregarExplorador(Explorador explorador) {
        Objects.requireNonNull(explorador, "explorador");
        if (!estado.puedeAceptarNuevosMiembros()) {
            return false;
        }
        if (exploradores.contains(explorador)) {
            return false;
        }
        exploradores.add(explorador);
        if (exploradores.size() >= minimoJugadores) {
            estado = EstadoSesion.ACTIVA;
        }
        return true;
    }

    public boolean bloquearRecurso(String nombre, int cantidad) {
        Objects.requireNonNull(nombre, "nombre");
        if (cantidad <= 0 || estado == EstadoSesion.CERRADA) {
            return false;
        }
        recursosCompartidos.merge(nombre, cantidad, Integer::sum);
        return true;
    }

    public String getCodigo() {
        return codigo;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = Objects.requireNonNull(estado, "estado");
    }

    public Map<String, Integer> getRecursosCompartidos() {
        return Collections.unmodifiableMap(recursosCompartidos);
    }

    public List<Explorador> getExploradores() {
        return Collections.unmodifiableList(exploradores);
    }

    public int getMinimoJugadores() {
        return minimoJugadores;
    }
}
