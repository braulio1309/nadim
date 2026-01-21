package com.mycompany.nadim;

import java.util.Objects;

public abstract class Objeto {
    private final String nombre;
    private final String categoria;
    private int cantidad;

    protected Objeto(String nombre, String categoria, int cantidad) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.categoria = Objects.requireNonNull(categoria, "categoria");
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad debe ser no negativa");
        }
        this.cantidad = cantidad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void aumentarCantidad(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("El incremento debe ser positivo");
        }
        cantidad += delta;
    }

        public boolean reducirCantidad(int delta) {
            if (delta <= 0) {
                return false;
            }
            if (cantidad < delta) {
                return false;
            }
            cantidad -= delta;
            return true;
        }
}
