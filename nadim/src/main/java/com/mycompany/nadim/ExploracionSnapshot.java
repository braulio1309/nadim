package com.mycompany.nadim;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class ExploracionSnapshot {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ExploracionSnapshot() {
    }

    static String construir(Explorador explorador, String motivo) {
        Objects.requireNonNull(explorador, "explorador");
        Objects.requireNonNull(motivo, "motivo");
        Inventario inventario = explorador.getInventario();
        Pokedex pokedex = explorador.getPokedex();
        EquipoPokemon equipo = explorador.getEquipo();
        List<EntradaInvestigacion> entradas = pokedex.getEntradas();
        int descubiertas = 0;
        for (EntradaInvestigacion entrada : entradas) {
            if (entrada.estaDescubierta()) {
                descubiertas++;
            }
        }
        int totalEspecies = entradas.size();
        int completadas = pokedex.getEspeciesCompletadas();
        int progreso = pokedex.calcularProgreso();
        Map<String, Objeto> objetos = inventario.getObjetos();
        String objetosTexto;
        if (objetos.isEmpty()) {
            objetosTexto = "sin objetos";
        } else {
            objetosTexto = objetos.values().stream()
                    .map(obj -> obj.getNombre() + " x" + obj.getCantidad())
                    .collect(Collectors.joining(", "));
        }
        List<Pokemon> miembros = equipo.getMiembros();
        long debilitados = 0;
        for (Pokemon pokemon : miembros) {
            if (pokemon.estaDebilitado()) {
                debilitados++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(FORMATTER.format(LocalDateTime.now()))
                .append(" | Motivo: ").append(motivo)
                .append(" | Progreso Pokedex: ").append(progreso).append("% (")
                .append(completadas).append(" completadas");
        if (totalEspecies > 0) {
            sb.append(", ").append(descubiertas).append("/").append(totalEspecies).append(" descubiertas");
        }
        sb.append(") | Inventario: ")
                .append(inventario.getOcupacionActual()).append("/").append(inventario.getCapacidadMaxima())
                .append(" (").append(objetosTexto).append(") | Equipo: ")
                .append(miembros.size()).append(" miembros");
        if (debilitados > 0) {
            sb.append(" (").append(debilitados).append(" debilitados)");
        }
        return sb.toString();
    }
}
