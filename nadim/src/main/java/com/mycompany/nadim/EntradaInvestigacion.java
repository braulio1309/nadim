package com.mycompany.nadim;

import java.util.Objects;

public class EntradaInvestigacion {
    public static final int NIVEL_COMPLETO = 10;
    public static final int PUNTOS_POR_CAPTURA = 2;
    public static final int PUNTOS_POR_VICTORIA = 1;

    private final String nombreOficial;
    private int nivelInvestigacion;
    private boolean descubierta;

    public EntradaInvestigacion(String nombreOficial, boolean descubierta) {
        this.nombreOficial = Objects.requireNonNull(nombreOficial, "nombreOficial");
        this.descubierta = descubierta;
    }

    public void sumarPuntos(int puntos) {
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos deben ser positivos");
        }
        descubierta = true;
        nivelInvestigacion = Math.min(NIVEL_COMPLETO, nivelInvestigacion + puntos);
    }

    public boolean estaCompleta() {
        return nivelInvestigacion >= NIVEL_COMPLETO;
    }

    public boolean estaDescubierta() {
        return descubierta;
    }

    public void marcarDescubierta() {
        descubierta = true;
    }

    public String getNombreOficial() {
        return nombreOficial;
    }

    public String getNombreVisible() {
        return descubierta ? nombreOficial : "???";
    }

    public int getNivelInvestigacion() {
        return nivelInvestigacion;
    }
}
