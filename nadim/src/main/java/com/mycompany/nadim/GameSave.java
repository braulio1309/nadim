package com.mycompany.nadim;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameSave {
    private final int version;
    private final String alias;
    private final String label;
    private final long createdAtMillis;

    private final RolExplorador rol;

    private final int inventarioCapacidadMaxima;
    private final List<ItemSave> inventarioItems;

    private final int equipoCupoMaximo;
    private final List<PokemonSave> equipo;

    private final List<PokedexEntrySave> pokedex;

    private final long mapSeed;
    private final int playerX;
    private final int playerY;

    private final LocalDate perfilFechaRegistro;
    private final int perfilExpedicionesCompletadas;
    private final Set<String> perfilLogros;

    public GameSave(
            int version,
            String alias,
            String label,
            long createdAtMillis,
            RolExplorador rol,
            int inventarioCapacidadMaxima,
            List<ItemSave> inventarioItems,
            int equipoCupoMaximo,
            List<PokemonSave> equipo,
            List<PokedexEntrySave> pokedex,
            long mapSeed,
            int playerX,
            int playerY,
            LocalDate perfilFechaRegistro,
            int perfilExpedicionesCompletadas,
            Set<String> perfilLogros) {
        this.version = version;
        this.alias = Objects.requireNonNull(alias, "alias");
        this.label = label == null ? "" : label.trim();
        this.createdAtMillis = createdAtMillis;
        this.rol = Objects.requireNonNull(rol, "rol");
        this.inventarioCapacidadMaxima = inventarioCapacidadMaxima;
        this.inventarioItems = inventarioItems == null ? new ArrayList<>() : new ArrayList<>(inventarioItems);
        this.equipoCupoMaximo = equipoCupoMaximo;
        this.equipo = equipo == null ? new ArrayList<>() : new ArrayList<>(equipo);
        this.pokedex = pokedex == null ? new ArrayList<>() : new ArrayList<>(pokedex);
        this.mapSeed = mapSeed;
        this.playerX = playerX;
        this.playerY = playerY;
        this.perfilFechaRegistro = Objects.requireNonNull(perfilFechaRegistro, "perfilFechaRegistro");
        this.perfilExpedicionesCompletadas = perfilExpedicionesCompletadas;
        this.perfilLogros = perfilLogros == null ? new HashSet<>() : new HashSet<>(perfilLogros);
    }

    public int getVersion() {
        return version;
    }

    public String getAlias() {
        return alias;
    }

    public String getLabel() {
        return label;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public RolExplorador getRol() {
        return rol;
    }

    public int getInventarioCapacidadMaxima() {
        return inventarioCapacidadMaxima;
    }

    public List<ItemSave> getInventarioItems() {
        return new ArrayList<>(inventarioItems);
    }

    public int getEquipoCupoMaximo() {
        return equipoCupoMaximo;
    }

    public List<PokemonSave> getEquipo() {
        return new ArrayList<>(equipo);
    }

    public List<PokedexEntrySave> getPokedex() {
        return new ArrayList<>(pokedex);
    }

    public long getMapSeed() {
        return mapSeed;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public LocalDate getPerfilFechaRegistro() {
        return perfilFechaRegistro;
    }

    public int getPerfilExpedicionesCompletadas() {
        return perfilExpedicionesCompletadas;
    }

    public Set<String> getPerfilLogros() {
        return new HashSet<>(perfilLogros);
    }

    public enum ItemKind {
        MATERIAL,
        CAPTURA,
        CURA
    }

    public static final class ItemSave {
        private final String nombre;
        private final String categoria;
        private final int cantidad;
        private final ItemKind kind;
        private final Integer puntosCuracion;
        private final Boolean revive;

        public ItemSave(String nombre, String categoria, int cantidad, ItemKind kind, Integer puntosCuracion,
                Boolean revive) {
            this.nombre = Objects.requireNonNull(nombre, "nombre");
            this.categoria = categoria == null ? "" : categoria;
            this.cantidad = cantidad;
            this.kind = Objects.requireNonNull(kind, "kind");
            this.puntosCuracion = puntosCuracion;
            this.revive = revive;
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

        public ItemKind getKind() {
            return kind;
        }

        public Integer getPuntosCuracion() {
            return puntosCuracion;
        }

        public Boolean getRevive() {
            return revive;
        }
    }

    public static final class PokemonSave {
        private final String especie;
        private final int saludMaxima;
        private final int saludActual;
        private final List<MoveSave> movimientos;

        public PokemonSave(String especie, int saludMaxima, int saludActual, List<MoveSave> movimientos) {
            this.especie = Objects.requireNonNull(especie, "especie");
            this.saludMaxima = saludMaxima;
            this.saludActual = saludActual;
            this.movimientos = movimientos == null ? new ArrayList<>() : new ArrayList<>(movimientos);
        }

        public String getEspecie() {
            return especie;
        }

        public int getSaludMaxima() {
            return saludMaxima;
        }

        public int getSaludActual() {
            return saludActual;
        }

        public List<MoveSave> getMovimientos() {
            return new ArrayList<>(movimientos);
        }
    }

    public static final class MoveSave {
        private final String nombre;
        private final int potencia;
        private final int usosMaximos;
        private final int usosRestantes;

        public MoveSave(String nombre, int potencia, int usosMaximos, int usosRestantes) {
            this.nombre = Objects.requireNonNull(nombre, "nombre");
            this.potencia = potencia;
            this.usosMaximos = usosMaximos;
            this.usosRestantes = usosRestantes;
        }

        public String getNombre() {
            return nombre;
        }

        public int getPotencia() {
            return potencia;
        }

        public int getUsosMaximos() {
            return usosMaximos;
        }

        public int getUsosRestantes() {
            return usosRestantes;
        }
    }

    public static final class PokedexEntrySave {
        private final String nombreOficial;
        private final boolean descubierta;
        private final int nivelInvestigacion;

        public PokedexEntrySave(String nombreOficial, boolean descubierta, int nivelInvestigacion) {
            this.nombreOficial = Objects.requireNonNull(nombreOficial, "nombreOficial");
            this.descubierta = descubierta;
            this.nivelInvestigacion = nivelInvestigacion;
        }

        public String getNombreOficial() {
            return nombreOficial;
        }

        public boolean isDescubierta() {
            return descubierta;
        }

        public int getNivelInvestigacion() {
            return nivelInvestigacion;
        }
    }
}
