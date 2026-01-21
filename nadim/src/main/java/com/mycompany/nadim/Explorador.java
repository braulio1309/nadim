package com.mycompany.nadim;

import java.util.Objects;

public class Explorador {
    private final String alias;
    private final RolExplorador rol;
    private final EquipoPokemon equipo;
    private final Inventario inventario;
    private final Pokedex pokedex;
    private final PerfilJugador perfil;
    private final TallerArtesania taller;

    public Explorador(String alias,
            RolExplorador rol,
            EquipoPokemon equipo,
            Inventario inventario,
            Pokedex pokedex,
            PerfilJugador perfil,
            TallerArtesania taller) {
        this.alias = Objects.requireNonNull(alias, "alias");
        this.rol = Objects.requireNonNull(rol, "rol");
        this.equipo = Objects.requireNonNull(equipo, "equipo");
        this.inventario = Objects.requireNonNull(inventario, "inventario");
        this.pokedex = Objects.requireNonNull(pokedex, "pokedex");
        this.perfil = Objects.requireNonNull(perfil, "perfil");
        this.taller = Objects.requireNonNull(taller, "taller");
    }

    public boolean capturar(Pokemon pokemon) {
        Objects.requireNonNull(pokemon, "pokemon");
        if (!equipo.agregar(pokemon)) {
            return false;
        }
        pokedex.registrarCaptura(pokemon.getEspecie());
        try {
            if (pokedex.getEspeciesCompletadas() >= 5) {
                var logros = perfil.getLogrosDesbloqueados();
                if (!logros.contains("ARCEUS_UNLOCKED") && !logros.contains("ARCEUS_COMPLETED")) {
                    perfil.desbloquearLogro("ARCEUS_UNLOCKED");
                }
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    public String getAlias() {
        return alias;
    }

    public RolExplorador getRol() {
        return rol;
    }

    public EquipoPokemon getEquipo() {
        return equipo;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public Pokedex getPokedex() {
        return pokedex;
    }

    public PerfilJugador getPerfil() {
        return perfil;
    }

    public TallerArtesania getTaller() {
        return taller;
    }

    public boolean fabricar(String nombreReceta) {
        return taller.fabricar(nombreReceta, inventario);
    }

    public boolean usarPocion(ObjetoCura pocion, Pokemon objetivo) {
        if (!inventario.consumir(pocion.getNombre(), 1)) {
            return false;
        }
        pocion.aplicar(objetivo);
        return true;
    }
}
