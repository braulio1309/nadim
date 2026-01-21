package com.mycompany.nadim;

import java.util.Objects;

final class MovimientoSpec {
    private final String nombre;
    private final int potencia;
    private final int usos;

    MovimientoSpec(String nombre, int potencia, int usos) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.potencia = potencia;
        this.usos = Math.max(1, usos);
    }

    String getNombre() {
        return nombre;
    }

    int getPotencia() {
        return potencia;
    }

    int getUsos() {
        return usos;
    }
}
