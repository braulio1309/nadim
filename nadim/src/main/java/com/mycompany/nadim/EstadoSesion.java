package com.mycompany.nadim;

import java.util.Locale;

public enum EstadoSesion {
    PLANIFICADA,
    ACTIVA,
    CERRADA;

    public boolean puedeAceptarNuevosMiembros() {
        return this == PLANIFICADA || this == ACTIVA;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
