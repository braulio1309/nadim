package com.mycompany.nadim;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PerfilJugador {
    private final LocalDate fechaRegistro;
    private int expedicionesCompletadas;
    private final Set<String> logrosDesbloqueados = new HashSet<>();

    public PerfilJugador(LocalDate fechaRegistro) {
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro, "fechaRegistro");
    }

    public PerfilJugador(LocalDate fechaRegistro, int expedicionesCompletadas, Set<String> logrosDesbloqueados) {
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro, "fechaRegistro");
        if (expedicionesCompletadas < 0) {
            throw new IllegalArgumentException("Las expediciones completadas deben ser no negativas");
        }
        this.expedicionesCompletadas = expedicionesCompletadas;
        if (logrosDesbloqueados != null) {
            for (String logro : logrosDesbloqueados) {
                if (logro != null && !logro.isBlank()) {
                    this.logrosDesbloqueados.add(logro);
                }
            }
        }
    }

    public void actualizarEstadisticas(String resultado) {
        if (resultado != null && resultado.equalsIgnoreCase("exito")) {
            expedicionesCompletadas++;
        }
    }

    public void desbloquearLogro(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            logrosDesbloqueados.add(nombre);
        }
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public int getExpedicionesCompletadas() {
        return expedicionesCompletadas;
    }

    public Set<String> getLogrosDesbloqueados() {
        return Collections.unmodifiableSet(logrosDesbloqueados);
    }
}
