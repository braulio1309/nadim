package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TallerArtesania {
    private final List<RecetaArtesania> recetasDisponibles = new ArrayList<>();

    public boolean fabricar(String nombre, Inventario inventario) {
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(inventario, "inventario");
        RecetaArtesania receta = recetasDisponibles.stream()
                .filter(r -> r.getNombreResultado().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(null);
        if (receta == null || !receta.esAplicable(inventario)) {
            return false;
        }
        for (var entry : receta.obtenerCosto().entrySet()) {
            if (!inventario.consumir(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        Objeto resultado = receta.crearResultado();
        if (resultado == null) {
            throw new IllegalStateException("La receta " + receta.getNombreResultado() + " no produjo resultado");
        }
        if (inventario.getOcupacionActual() + resultado.getCantidad() > inventario.getCapacidadMaxima()) {
            return false;
        }
        inventario.registrarObjeto(resultado);
        return true;
    }

    public void registrarReceta(RecetaArtesania receta) {
        recetasDisponibles.add(Objects.requireNonNull(receta, "receta"));
    }

    public List<RecetaArtesania> getRecetasDisponibles() {
        return Collections.unmodifiableList(recetasDisponibles);
    }
}
