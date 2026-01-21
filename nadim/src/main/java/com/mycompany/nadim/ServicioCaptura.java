package com.mycompany.nadim;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ServicioCaptura {
    private final double probabilidadBase;

    public ServicioCaptura(double probabilidadBase) {
        if (probabilidadBase <= 0.0 || probabilidadBase > 1.0) {
            throw new IllegalArgumentException("La probabilidad base debe estar en (0,1]");
        }
        this.probabilidadBase = probabilidadBase;
    }

    public boolean intentar(Explorador explorador, Pokemon objetivo) {
        Objects.requireNonNull(explorador, "explorador");
        Objects.requireNonNull(objetivo, "objetivo");
        Inventario inventario = explorador.getInventario();
        if (inventario.getDisponibles("Pokeball") <= 0) {
            return false;
        }
        consumirPokeball(inventario);
        double saludFactor = 1.0 - (objetivo.getSaludActual() / (double) objetivo.getSaludMaxima());
        double probabilidad = Math.min(1.0, probabilidadBase + saludFactor * 0.5);
        return ThreadLocalRandom.current().nextDouble() <= probabilidad;
    }

    public void consumirPokeball(Inventario inventario) {
        Objects.requireNonNull(inventario, "inventario");
        if (!inventario.consumir("Pokeball", 1)) {
            throw new IllegalStateException("No quedan Pokeball disponibles");
        }
    }

    public double getProbabilidadBase() {
        return probabilidadBase;
    }
}
