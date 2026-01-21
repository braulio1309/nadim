package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GestorSesiones {
    private final List<SesionExpedicion> sesionesActivas = new ArrayList<>();
    private final int minimoJugadores;

    public GestorSesiones(int minimoJugadores) {
        if (minimoJugadores < 1) {
            throw new IllegalArgumentException("El mínimo de jugadores debe ser positivo");
        }
        this.minimoJugadores = minimoJugadores;
    }

    public SesionExpedicion crearSesion(Explorador host) {
        Objects.requireNonNull(host, "host");
        String codigo = UUID.randomUUID().toString();
        SesionExpedicion sesion = new SesionExpedicion(codigo, minimoJugadores);
        sesion.agregarExplorador(host);
        sesionesActivas.add(sesion);
        return sesion;
    }

    public void cerrarSesion(String id) {
        Objects.requireNonNull(id, "id");
        Optional<SesionExpedicion> sesion = sesionesActivas.stream()
                .filter(s -> s.getCodigo().equals(id))
                .findFirst();
        if (sesion.isEmpty()) {
            throw new IllegalArgumentException("No existe la sesión con id " + id);
        }
        SesionExpedicion expedicion = sesion.get();
        expedicion.setEstado(EstadoSesion.CERRADA);
        sesionesActivas.remove(expedicion);
    }

    public List<SesionExpedicion> getSesionesActivas() {
        return Collections.unmodifiableList(sesionesActivas);
    }

    public int getMinimoJugadores() {
        return minimoJugadores;
    }
}
