package com.mycompany.nadim;

import java.util.Arrays;
import java.util.Objects;

public class Pokemon {
    private final String especie; 
    private final Movimiento[] movimientos;
    private final int saludMaxima;
    private int saludActual;

    public Pokemon(String especie, Movimiento[] movimientos, int saludMaxima) {
        this.especie = Objects.requireNonNull(especie, "especie");
        this.movimientos = movimientos == null ? new Movimiento[0] : Arrays.copyOf(movimientos, movimientos.length);
        if (saludMaxima <= 0) {
            throw new IllegalArgumentException("La salud máxima debe ser positiva");
        }
        this.saludMaxima = saludMaxima;
        this.saludActual = saludMaxima;
    }

    public void aplicarDanio(int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException("El daño no puede ser negativo");
        }
        saludActual = Math.max(0, saludActual - valor);
    }

    public boolean estaDebilitado() {
        return saludActual <= 0;
    }

    public void curarCompleto() {
        saludActual = saludMaxima;
        for (Movimiento movimiento : movimientos) {
            movimiento.restaurarUsos();
        }
    }

    public boolean curar(int puntos, boolean revivir) {
        if (puntos < 0) {
            throw new IllegalArgumentException("La curacion no puede ser negativa");
        }
        int saludAntes = saludActual;
        if (estaDebilitado()) {
            if (!revivir) {
                return false;
            }
            int restauracion = puntos > 0 ? puntos : Math.max(1, saludMaxima / 2);
            saludActual = Math.min(saludMaxima, restauracion);
        } else {
            if (puntos == 0) {
                return false;
            }
            saludActual = Math.min(saludMaxima, saludActual + puntos);
        }
        return saludActual > saludAntes;
    }

    public String getEspecie() {
        return especie;
    }

    public Movimiento[] getMovimientos() {
        return Arrays.copyOf(movimientos, movimientos.length);
    }

    public int getSaludActual() {
        return saludActual;
    }

    public int getSaludMaxima() {
        return saludMaxima;
    }
}
