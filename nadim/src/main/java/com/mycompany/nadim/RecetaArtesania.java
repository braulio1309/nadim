package com.mycompany.nadim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class RecetaArtesania {
    private final String nombreResultado;
    private final Map<String, Integer> costoMateriales;
    private final Supplier<Objeto> proveedorResultado;


    // Construye la receta validando insumos y asegurando un mapa inmutable del
    // costo.
    public RecetaArtesania(String nombreResultado,
            Map<String, Integer> costoMateriales,
            Supplier<Objeto> proveedorResultado) {
        this.nombreResultado = Objects.requireNonNull(nombreResultado, "nombreResultado");
        Objects.requireNonNull(costoMateriales, "costoMateriales");
        this.proveedorResultado = Objects.requireNonNull(proveedorResultado, "proveedorResultado");
        if (costoMateriales.isEmpty()) {
            throw new IllegalArgumentException("La receta debe tener materiales");
        }
        Map<String, Integer> copia = new HashMap<>();
        for (Map.Entry<String, Integer> entry : costoMateriales.entrySet()) {
            String clave = Objects.requireNonNull(entry.getKey(), "material");
            Integer valor = Objects.requireNonNull(entry.getValue(), "cantidad");
            if (valor <= 0) {
                throw new IllegalArgumentException("Las cantidades deben ser positivas");
            }
            copia.put(clave, valor);
        }
        this.costoMateriales = Collections.unmodifiableMap(copia);
    }

    // Verifica si el inventario posee suficiente cantidad de cada material
    // requerido.
    public boolean esAplicable(Inventario inventario) {
        Objects.requireNonNull(inventario, "inventario");
        for (Map.Entry<String, Integer> entry : costoMateriales.entrySet()) {
            if (inventario.getDisponibles(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, Integer> obtenerCosto() {
        return costoMateriales;
    }

    public String getNombreResultado() {
        return nombreResultado;
    }

    public Objeto crearResultado() {
        return proveedorResultado.get();
    }

    @Override
    public String toString() {
        return nombreResultado;
    }
}
