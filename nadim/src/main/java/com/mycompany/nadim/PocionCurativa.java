package com.mycompany.nadim;

import java.util.Objects;

public class PocionCurativa extends ObjetoCura {
    private final int puntosCuracion;
    private final boolean revive;

    public PocionCurativa(String nombre, String categoria, int cantidad, int puntosCuracion, boolean revive) {
        super(nombre, categoria, cantidad);
        if (puntosCuracion < 0) {
            throw new IllegalArgumentException("La curacion debe ser no negativa");
        }
        this.puntosCuracion = puntosCuracion;
        this.revive = revive;
    }

    @Override
    public boolean aplicar(Pokemon objetivo) {
        Objects.requireNonNull(objetivo, "objetivo");
        return objetivo.curar(puntosCuracion, revive);
    }

    public int getPuntosCuracion() {
        return puntosCuracion;
    }

    public boolean esRevivir() {
        return revive;
    }
}
