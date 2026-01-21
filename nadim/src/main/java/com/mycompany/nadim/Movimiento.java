package com.mycompany.nadim;

import java.util.Objects;

public class Movimiento {
    private final String nombre;
    private final int potencia;
    private final int usosMaximos;
    private int usosRestantes;

    public Movimiento(String nombre, int potencia, int usosMaximos) {
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        if (potencia < 0) {
            throw new IllegalArgumentException("La potencia no puede ser negativa");
        }
        if (usosMaximos <= 0) {
            throw new IllegalArgumentException("Los usos máximos deben ser positivos");
        }
        this.potencia = potencia;
        this.usosMaximos = usosMaximos;
        this.usosRestantes = usosMaximos;
    }

    public int ejecutar(Pokemon atacante, Pokemon objetivo) {
        Objects.requireNonNull(atacante, "atacante");
        Objects.requireNonNull(objetivo, "objetivo");
        if (usosRestantes <= 0) {
            return 0;
        }
        usosRestantes--;
        objetivo.aplicarDanio(potencia);
        return potencia;
    }

    public void registrarUsoFallido() {
        if (usosRestantes > 0) {
            usosRestantes--;
        }
    }

    public void restaurarUsos() {
        usosRestantes = usosMaximos;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPotencia() {
        return potencia;
    }

    public int getUsosRestantes() {
        return usosRestantes;
    }

    public int getUsosMaximos() {
        return usosMaximos;
    }
}
