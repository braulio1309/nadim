package com.mycompany.nadim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ZonaExploracion {
    private final String nombre;
    private final int dificultad;
    private final Map<String, Objeto> recursosDisponibles = new HashMap<>();

    public ZonaExploracion(String nombre, int dificultad) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        if (dificultad < 1) {
            throw new IllegalArgumentException("La dificultad debe ser al menos 1");
        }
        this.dificultad = dificultad;
    }

    public Pokemon generarEncuentro() {
        try {
            return PokemonFactory.crearPokemonSalvaje(dificultad, ThreadLocalRandom.current());
        } catch (Exception ex) {
            int base = dificultad * 10;
            int variacion = ThreadLocalRandom.current().nextInt(5, 15);
            int salud = base + variacion;
            int potencia = Math.max(5, dificultad * 3);
            Movimiento[] movimientos = {
                    new Movimiento("Placaje", potencia, 35),
                    new Movimiento("Ataque Rápido", potencia + 5, 30)
            };
            return new Pokemon(nombre + " salvaje", movimientos, salud);
        }
    }

    public void agotarRecurso(String nombreRecurso, int cantidad) {
        Objects.requireNonNull(nombreRecurso, "nombreRecurso");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        Objeto recurso = recursosDisponibles.get(nombreRecurso);
        if (recurso == null || !recurso.reducirCantidad(cantidad)) {
            throw new IllegalStateException("El recurso no es suficiente");
        }
        if (recurso.getCantidad() == 0) {
            recursosDisponibles.remove(nombreRecurso);
        }
    }

    public void registrarRecurso(Objeto objeto) {
        Objects.requireNonNull(objeto, "objeto");
        recursosDisponibles.merge(
                objeto.getNombre(),
                objeto,
                (actual, nuevo) -> {
                    actual.aumentarCantidad(nuevo.getCantidad());
                    return actual;
                });
    }

    public Map<String, Objeto> getRecursosDisponibles() {
        return Collections.unmodifiableMap(recursosDisponibles);
    }

    public String getNombre() {
        return nombre;
    }

    public int getDificultad() {
        return dificultad;
    }
}
