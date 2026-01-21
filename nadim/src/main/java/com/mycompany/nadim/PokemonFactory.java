package com.mycompany.nadim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class PokemonFactory {
    private static final List<PlanoPokemon> STARTERS = new ArrayList<>();
    private static final List<PlanoPokemon> WILD_POOL = new ArrayList<>();
    private static final Map<String, PlanoPokemon> BLUEPRINTS = new LinkedHashMap<>();

    static {
        registrarStarter("Rowlet", 88, 10,
                movimiento("Picotazo", 16, 30),
                movimiento("Hoja Afilada", 20, 20),
                movimiento("Ataque Rapido", 16, 30),
                movimiento("Golpe Aereo", 18, 20));

        registrarStarter("Cyndaquil", 90, 8,
                movimiento("Ascuas", 18, 25),
                movimiento("Placaje", 12, 35),
                movimiento("Ataque Rapido", 16, 30),
                movimiento("Rueda Fuego", 22, 15));

        registrarStarter("Oshawott", 92, 8,
                movimiento("Pistola Agua", 18, 25),
                movimiento("Concha Filo", 20, 20),
                movimiento("Acua Jet", 18, 25),
                movimiento("Ataque Rapido", 16, 30));

        registrarSalvaje("Bidoof", 74, 30,
                movimiento("Placaje", 14, 35),
                movimiento("Grunido", 8, 40),
                movimiento("Golpe Cabeza", 16, 25),
                movimiento("Ataque Rapido", 16, 30));

        registrarSalvaje("Shinx", 76, 28,
                movimiento("Impactrueno", 16, 30),
                movimiento("Mordisco", 18, 25),
                movimiento("Chispa", 20, 20),
                movimiento("Rugido", 8, 40));

        registrarSalvaje("Starly", 70, 26,
                movimiento("Picotazo", 16, 30),
                movimiento("Ataque Rapido", 16, 30),
                movimiento("Ala Acero", 18, 20),
                movimiento("Grunido", 8, 40));

        registrarSalvaje("Drifloon", 78, 24,
                movimiento("Impresionar", 16, 30),
                movimiento("Viento Aciago", 18, 25),
                movimiento("Golpe Aereo", 18, 20),
                movimiento("Desarme", 16, 25));

        registrarSalvaje("Buizel", 74, 28,
                movimiento("Pistola Agua", 16, 30),
                movimiento("Acua Jet", 18, 25),
                movimiento("Cuchillada", 18, 25),
                movimiento("Sonicboom", 16, 20));

        registrarSalvaje("Luxio", 84, 26,
                movimiento("Chispa", 20, 20),
                movimiento("Mordisco", 18, 25),
                movimiento("Colmillo Rayo", 22, 15),
                movimiento("Rugido", 8, 40));

        registrarSalvaje("Ponyta", 82, 30,
                movimiento("Ascuas", 18, 25),
                movimiento("Pisoton", 18, 30),
                movimiento("Nitrocarga", 20, 20),
                movimiento("Cola Fuego", 22, 15));

        registrarSalvaje("Geodude", 86, 30,
                movimiento("Placaje", 14, 35),
                movimiento("Lanzarrocas", 18, 30),
                movimiento("Golpe Roca", 20, 25),
                movimiento("Magnitud", 22, 15));


                
        registrarLegendario("Arceus", 200, 0,
            movimiento("Juicio", 90, 10),
            movimiento("Pulso radiante", 75, 15));
    }

    private PokemonFactory() {
    }

    public static Set<String> obtenerEspeciesRegistradas() {
        Set<String> especies = new LinkedHashSet<>();
        for (PlanoPokemon blueprint : BLUEPRINTS.values()) {
            especies.add(blueprint.getNombre());
        }
        return Collections.unmodifiableSet(especies);
    }

    public static Pokemon crearPokemonInicial(String especie) {
        PlanoPokemon blueprint = obtenerBlueprint(especie);
        if (!STARTERS.contains(blueprint)) {
            throw new IllegalArgumentException("La especie " + especie + " no es un inicial disponible");
        }
        return blueprint.crear(null, 0, 0);
    }

    public static Pokemon crearPokemonSalvaje(int tipoTerreno, Random random) {
        Objects.requireNonNull(random, "random");
        if (WILD_POOL.isEmpty()) {
            throw new IllegalStateException("No hay especies registradas para encuentros salvajes");
        }
        int indice = random.nextInt(WILD_POOL.size());
        PlanoPokemon blueprint = WILD_POOL.get(indice);
        return crearDesdeBlueprint(blueprint, tipoTerreno, random);
    }

    public static Pokemon crearPokemonPorNombre(String especie, Random random, int tipoTerreno) {
        Objects.requireNonNull(random, "random");
        PlanoPokemon blueprint = obtenerBlueprint(especie);
        return crearDesdeBlueprint(blueprint, tipoTerreno, random);
    }

    private static Pokemon crearDesdeBlueprint(PlanoPokemon blueprint, int tipoTerreno, Random random) {
        int bonificacionPotencia = tipoTerreno >= 3 ? 6 : tipoTerreno == 2 ? 3 : 0;
        int bonificacionSalud = tipoTerreno >= 3 ? 15 : tipoTerreno == 2 ? 8 : 0;
        return blueprint.crear(random, bonificacionPotencia, bonificacionSalud);
    }

    private static PlanoPokemon registrarStarter(String nombre, int saludBase, int variacionSalud,
            MovimientoSpec... movimientos) {
        PlanoPokemon blueprint = registrarBlueprint(nombre, saludBase, variacionSalud, movimientos);
        STARTERS.add(blueprint);
        WILD_POOL.add(blueprint);
        return blueprint;
    }

    private static PlanoPokemon registrarSalvaje(String nombre, int saludBase, int variacionSalud,
            MovimientoSpec... movimientos) {
        PlanoPokemon blueprint = registrarBlueprint(nombre, saludBase, variacionSalud, movimientos);
        WILD_POOL.add(blueprint);
        return blueprint;
    }

    private static PlanoPokemon registrarLegendario(String nombre, int saludBase, int variacionSalud,
            MovimientoSpec... movimientos) {
        return registrarBlueprint(nombre, saludBase, variacionSalud, movimientos);
    }

    private static PlanoPokemon registrarBlueprint(String nombre, int saludBase, int variacionSalud,
            MovimientoSpec... movimientos) {
        PlanoPokemon blueprint = new PlanoPokemon(nombre, saludBase, variacionSalud, List.of(movimientos));
        String clave = normalizar(nombre);
        BLUEPRINTS.put(clave, blueprint);
        return blueprint;
    }

    private static String normalizar(String nombre) {
        return Objects.requireNonNull(nombre, "nombre").trim().toLowerCase();
    }

    private static MovimientoSpec movimiento(String nombre, int potencia, int usos) {
        return new MovimientoSpec(nombre, potencia, usos);
    }

    private static PlanoPokemon obtenerBlueprint(String especie) {
        PlanoPokemon blueprint = BLUEPRINTS.get(normalizar(especie));
        if (blueprint == null) {
            throw new IllegalArgumentException("No se reconoce la especie: " + especie);
        }
        return blueprint;
    }
}
