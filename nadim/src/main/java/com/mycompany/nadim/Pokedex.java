package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Pokedex {
    private final Map<String, EntradaInvestigacion> indice = new LinkedHashMap<>();
    private final List<EntradaInvestigacion> entradas = new ArrayList<>();
    private int especiesCompletadas;

    public Pokedex() {
        this(null);
    }

    public Pokedex(Collection<String> especiesIniciales) {
        if (especiesIniciales != null) {
            for (String especie : especiesIniciales) {
                registrarEspecieInicial(especie);
            }
        }
    }

    private void registrarEspecieInicial(String nombre) {
        String clave = normalizar(nombre);
        if (indice.containsKey(clave)) {
            return;
        }
        EntradaInvestigacion entrada = new EntradaInvestigacion(nombre, false);
        indice.put(clave, entrada);
        entradas.add(entrada);
    }

    private String normalizar(String nombre) {
        return Objects.requireNonNull(nombre, "nombre").trim().toLowerCase();
    }

    private EntradaInvestigacion obtenerOCrear(String nombre, boolean descubrir) {
        String clave = normalizar(nombre);
        EntradaInvestigacion entrada = indice.get(clave);
        if (entrada == null) {
            entrada = new EntradaInvestigacion(nombre, descubrir);
            indice.put(clave, entrada);
            entradas.add(entrada);
        } else if (descubrir) {
            entrada.marcarDescubierta();
        }
        return entrada;
    }

    private void actualizarCompletadas(EntradaInvestigacion entrada, boolean estabaCompleta) {
        if (!estabaCompleta && entrada.estaCompleta()) {
            especiesCompletadas++;
        }
    }

    public void registrarCaptura(String nombre) {
        EntradaInvestigacion entrada = obtenerOCrear(nombre, true);
        boolean estabaCompleta = entrada.estaCompleta();
        entrada.sumarPuntos(EntradaInvestigacion.PUNTOS_POR_CAPTURA);
        actualizarCompletadas(entrada, estabaCompleta);
    }

    public void registrarVictoria(String nombre) {
        EntradaInvestigacion entrada = obtenerOCrear(nombre, true);
        boolean estabaCompleta = entrada.estaCompleta();
        entrada.sumarPuntos(EntradaInvestigacion.PUNTOS_POR_VICTORIA);
        actualizarCompletadas(entrada, estabaCompleta);
    }

    public int calcularProgreso() {
        if (entradas.isEmpty()) {
            return 0;
        }
        double porcentaje = (especiesCompletadas * 100.0) / entradas.size();
        return (int) Math.round(porcentaje);
    }

    public List<EntradaInvestigacion> getEntradas() {
        return Collections.unmodifiableList(entradas);
    }

    public int getEspeciesCompletadas() {
        return especiesCompletadas;
    }

    public void aplicarEstadoEntrada(String nombreOficial, boolean descubierta, int nivelInvestigacion) {
        Objects.requireNonNull(nombreOficial, "nombreOficial");
        if (nivelInvestigacion < 0) {
            nivelInvestigacion = 0;
        }
        if (nivelInvestigacion > EntradaInvestigacion.NIVEL_COMPLETO) {
            nivelInvestigacion = EntradaInvestigacion.NIVEL_COMPLETO;
        }

        String clave = normalizar(nombreOficial);
        EntradaInvestigacion entrada = indice.get(clave);
        if (entrada == null) {
            entrada = new EntradaInvestigacion(nombreOficial, descubierta);
            indice.put(clave, entrada);
            entradas.add(entrada);
        } else if (descubierta) {
            entrada.marcarDescubierta();
        }

        if (nivelInvestigacion > 0) {
            entrada.sumarPuntos(nivelInvestigacion);
        } else if (descubierta) {
            entrada.marcarDescubierta();
        }
    }

    public void recalcularEspeciesCompletadas() {
        int completadas = 0;
        for (EntradaInvestigacion entrada : entradas) {
            if (entrada.estaCompleta()) {
                completadas++;
            }
        }
        especiesCompletadas = completadas;
    }
}
