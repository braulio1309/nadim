package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EquipoPokemon {
    private final int cupoMaximo;
    private final List<Pokemon> miembros = new ArrayList<>();

    public EquipoPokemon(int cupoMaximo) {
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo debe ser positivo");
        }
        this.cupoMaximo = cupoMaximo;
    }

    public Pokemon seleccionarTitular() {
        return miembros.stream()
                .filter(pokemon -> !pokemon.estaDebilitado())
                .findFirst()
                .orElse(null);
    }

    public void registrarDerrota(Pokemon pokemon) {
        Objects.requireNonNull(pokemon, "pokemon");
        if (miembros.contains(pokemon)) {
            pokemon.aplicarDanio(pokemon.getSaludActual());
        }
    }

    public boolean agregar(Pokemon pokemon) {
        Objects.requireNonNull(pokemon, "pokemon");
        if (miembros.size() >= cupoMaximo) {
            return false;
        }
        return miembros.add(pokemon);
    }

    public boolean remover(Pokemon pokemon) {
        Objects.requireNonNull(pokemon, "pokemon");
        return miembros.remove(pokemon);
    }

    public List<Pokemon> getMiembros() {
        return Collections.unmodifiableList(miembros);
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }
}
