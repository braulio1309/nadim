package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

final class PlanoPokemon {
    private final String nombre;
    private final int saludBase;
    private final int variacionSalud;
    private final List<MovimientoSpec> movimientos;

    PlanoPokemon(String nombre, int saludBase, int variacionSalud, List<MovimientoSpec> movimientos) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.saludBase = saludBase;
        this.variacionSalud = Math.max(0, variacionSalud);
        this.movimientos = new ArrayList<>(Objects.requireNonNull(movimientos, "movimientos"));
    }

    String getNombre() {
        return nombre;
    }

    Pokemon crear(Random random, int bonificacionPotencia, int bonificacionSalud) {
        int saludTotal = saludBase + Math.max(0, bonificacionSalud);
        if (random != null && variacionSalud > 0) {
            saludTotal += random.nextInt(variacionSalud + 1);
        }
        Movimiento[] movimientosCopia = new Movimiento[movimientos.size()];
        for (int i = 0; i < movimientos.size(); i++) {
            MovimientoSpec spec = movimientos.get(i);
            int potencia = Math.max(0, spec.getPotencia() + bonificacionPotencia);
            movimientosCopia[i] = new Movimiento(spec.getNombre(), potencia, spec.getUsos());
        }
        return new Pokemon(nombre, movimientosCopia, saludTotal);
    }
}
