package com.mycompany.nadim;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SaveGameStorage {
    private static final Path BASE_DIR = Paths.get("data", "saves");
    private static final Path ONLINE_BASE_DIR = Paths.get("data", "saves_online");
    private static final String EXTENSION = ".sav";
    private static final int SAVE_VERSION = 3;

    private SaveGameStorage() {
    }

    public static SaveSlotInfo guardar(String alias, String label, Explorador explorador, int playerX, int playerY,
            long mapSeed) throws IOException {
        return guardar(alias, label, explorador, playerX, playerY, mapSeed, false);
    }

    public static SaveSlotInfo guardarOnline(String alias, String label, Explorador explorador, int playerX,
            int playerY, long mapSeed) throws IOException {
        return guardar(alias, label, explorador, playerX, playerY, mapSeed, true);
    }

    private static SaveSlotInfo guardar(String alias, String label, Explorador explorador, int playerX, int playerY,
            long mapSeed, boolean online) throws IOException {
        Objects.requireNonNull(alias, "alias");
        Objects.requireNonNull(explorador, "explorador");

        ensureUserDir(alias, online);

        long now = System.currentTimeMillis();
        String safeLabel = normalizarLabel(label);
        String baseName = now + (safeLabel.isBlank() ? "" : "_" + safeLabel);
        String fileName = baseName + EXTENSION;
        Path userDir = userDir(alias, online);
        Path target = userDir.resolve(fileName);

        // If saving online and there is an existing slot, overwrite the most recent one
        if (online) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(userDir, "*" + EXTENSION)) {
                Path latest = null;
                for (Path p : stream) {
                    if (latest == null || p.getFileName().toString().compareTo(latest.getFileName().toString()) > 0) {
                        latest = p;
                    }
                }
                if (latest != null) {
                    target = latest;
                    fileName = latest.getFileName().toString();
                } else {
                    // no existing file: fall through to create unique name
                    int intentos = 0;
                    while (Files.exists(target) && intentos < 1000) {
                        intentos++;
                        fileName = baseName + "_" + intentos + EXTENSION;
                        target = userDir.resolve(fileName);
                    }
                    if (Files.exists(target)) {
                        throw new IOException("No se pudo crear un nombre de archivo unico para el guardado.");
                    }
                }
            }
        } else {
            int intentos = 0;
            while (Files.exists(target) && intentos < 1000) {
                intentos++;
                fileName = baseName + "_" + intentos + EXTENSION;
                target = userDir.resolve(fileName);
            }
            if (Files.exists(target)) {
                throw new IOException("No se pudo crear un nombre de archivo unico para el guardado.");
            }
        }

        GameSave save = construirSave(alias, label, now, explorador, playerX, playerY, mapSeed);
        writeSaveFile(target, save, online && Files.exists(target));

        return new SaveSlotInfo(fileName, save.getLabel(), save.getCreatedAtMillis());
    }

    public static List<SaveSlotInfo> listarSlots(String alias) throws IOException {
        return listarSlots(alias, false);
    }

    public static List<SaveSlotInfo> listarSlotsOnline(String alias) throws IOException {
        return listarSlots(alias, true);
    }

    private static List<SaveSlotInfo> listarSlots(String alias, boolean online) throws IOException {
        Objects.requireNonNull(alias, "alias");
        ensureUserDir(alias, online);

        List<SaveSlotInfo> slots = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(userDir(alias, online), "*" + EXTENSION)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                try {
                    GameSave save = leerSave(path);
                    if (!alias.equalsIgnoreCase(save.getAlias())) {
                        continue;
                    }
                    slots.add(new SaveSlotInfo(fileName, save.getLabel(), save.getCreatedAtMillis()));
                } catch (Exception ignored) {
                    // Si un slot está corrupto o es de otra versión, lo omitimos.
                }
            }
        }

        slots.sort(Comparator.comparingLong(SaveSlotInfo::getCreatedAtMillis).reversed());
        return slots;
    }

    public static GameSave cargar(String alias, SaveSlotInfo slot) throws IOException {
        return cargar(alias, slot, false);
    }

    public static GameSave cargarOnline(String alias, SaveSlotInfo slot) throws IOException {
        return cargar(alias, slot, true);
    }

    private static GameSave cargar(String alias, SaveSlotInfo slot, boolean online)
            throws IOException {
        Objects.requireNonNull(alias, "alias");
        Objects.requireNonNull(slot, "slot");
        Path file = userDir(alias, online).resolve(slot.getFileName());
        GameSave save = leerSave(file);
        if (!alias.equalsIgnoreCase(save.getAlias())) {
            throw new IOException("El guardado no pertenece al usuario activo.");
        }
        return save;
    }

    private static GameSave construirSave(String alias, String label, long nowMillis, Explorador explorador,
            int playerX, int playerY, long mapSeed) {
        Inventario inventario = explorador.getInventario();
        List<GameSave.ItemSave> items = new ArrayList<>();
        for (Objeto objeto : inventario.getObjetos().values()) {
            GameSave.ItemKind kind;
            Integer puntosCuracion = null;
            Boolean revive = null;

            if (objeto instanceof ObjetoCaptura) {
                kind = GameSave.ItemKind.CAPTURA;
            } else if (objeto instanceof ObjetoCura) {
                kind = GameSave.ItemKind.CURA;
                if (objeto instanceof PocionCurativa) {
                    PocionCurativa curativa = (PocionCurativa) objeto;
                    puntosCuracion = curativa.getPuntosCuracion();
                    revive = curativa.esRevivir();
                }
            } else {
                kind = GameSave.ItemKind.MATERIAL;
            }

            items.add(new GameSave.ItemSave(
                    objeto.getNombre(),
                    objeto.getCategoria(),
                    objeto.getCantidad(),
                    kind,
                    puntosCuracion,
                    revive));
        }

        EquipoPokemon equipo = explorador.getEquipo();
        List<GameSave.PokemonSave> equipoSave = new ArrayList<>();
        for (Pokemon pokemon : equipo.getMiembros()) {
            List<GameSave.MoveSave> moves = new ArrayList<>();
            for (Movimiento movimiento : pokemon.getMovimientos()) {
                moves.add(new GameSave.MoveSave(
                        movimiento.getNombre(),
                        movimiento.getPotencia(),
                        movimiento.getUsosMaximos(),
                        movimiento.getUsosRestantes()));
            }
            equipoSave.add(new GameSave.PokemonSave(
                    pokemon.getEspecie(),
                    pokemon.getSaludMaxima(),
                    pokemon.getSaludActual(),
                    moves));
        }

        Pokedex pokedex = explorador.getPokedex();
        List<GameSave.PokedexEntrySave> pokedexSave = new ArrayList<>();
        for (EntradaInvestigacion entrada : pokedex.getEntradas()) {
            pokedexSave.add(new GameSave.PokedexEntrySave(
                    entrada.getNombreOficial(),
                    entrada.estaDescubierta(),
                    entrada.getNivelInvestigacion()));
        }

        PerfilJugador perfil = explorador.getPerfil();

        return new GameSave(
                SAVE_VERSION,
                alias,
                label,
                nowMillis,
                explorador.getRol(),
                inventario.getCapacidadMaxima(),
                items,
                equipo.getCupoMaximo(),
                equipoSave,
                pokedexSave,
                mapSeed,
                playerX,
                playerY,
                perfil.getFechaRegistro(),
                perfil.getExpedicionesCompletadas(),
                perfil.getLogrosDesbloqueados());
    }

    private static GameSave leerSave(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("No existe el guardado: " + file.getFileName());
        }
        String json = Files.readString(file, StandardCharsets.UTF_8);
        Object parsed = JsonSupport.parse(json);
        if (!(parsed instanceof Map)) {
            throw new IOException("Formato de guardado inválido.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) parsed;
        return mapToGameSave(data);
    }

    private static void ensureUserDir(String alias, boolean online) throws IOException {
        Path base = online ? ONLINE_BASE_DIR : BASE_DIR;
        if (!Files.exists(base)) {
            Files.createDirectories(base);
        }
        Path dir = userDir(alias, online);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private static Path userDir(String alias, boolean online) {
        String normalized = alias.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        Path base = online ? ONLINE_BASE_DIR : BASE_DIR;
        return base.resolve(normalized);
    }

    private static String normalizarLabel(String label) {
        if (label == null) {
            return "";
        }
        String trimmed = label.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        // Mantener legible en nombre de archivo.
        String safe = trimmed.replaceAll("[^A-Za-z0-9_-]", "_");
        if (safe.length() > 32) {
            safe = safe.substring(0, 32);
        }
        return safe;
    }

    private static void writeSaveFile(Path target, GameSave save, boolean overwrite) throws IOException {
        Map<String, Object> data = gameSaveToMap(save);
        String json = JsonSupport.stringify(data);
        if (overwrite) {
            Files.writeString(target, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } else {
            Files.writeString(target, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        }
    }

    private static Map<String, Object> gameSaveToMap(GameSave save) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", save.getVersion());
        data.put("alias", save.getAlias());
        data.put("label", save.getLabel());
        data.put("createdAtMillis", save.getCreatedAtMillis());
        data.put("rol", save.getRol().name());
        data.put("inventarioCapacidadMaxima", save.getInventarioCapacidadMaxima());

        List<Map<String, Object>> items = new ArrayList<>();
        for (GameSave.ItemSave item : save.getInventarioItems()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("nombre", item.getNombre());
            entry.put("categoria", item.getCategoria());
            entry.put("cantidad", item.getCantidad());
            entry.put("kind", item.getKind().name());
            entry.put("puntosCuracion", item.getPuntosCuracion());
            entry.put("revive", item.getRevive());
            items.add(entry);
        }
        data.put("inventarioItems", items);

        data.put("equipoCupoMaximo", save.getEquipoCupoMaximo());
        List<Map<String, Object>> equipo = new ArrayList<>();
        for (GameSave.PokemonSave pokemon : save.getEquipo()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("especie", pokemon.getEspecie());
            entry.put("saludMaxima", pokemon.getSaludMaxima());
            entry.put("saludActual", pokemon.getSaludActual());

            List<Map<String, Object>> moves = new ArrayList<>();
            for (GameSave.MoveSave move : pokemon.getMovimientos()) {
                Map<String, Object> moveEntry = new LinkedHashMap<>();
                moveEntry.put("nombre", move.getNombre());
                moveEntry.put("potencia", move.getPotencia());
                moveEntry.put("usosMaximos", move.getUsosMaximos());
                moveEntry.put("usosRestantes", move.getUsosRestantes());
                moves.add(moveEntry);
            }
            entry.put("movimientos", moves);
            equipo.add(entry);
        }
        data.put("equipo", equipo);

        List<Map<String, Object>> pokedex = new ArrayList<>();
        for (GameSave.PokedexEntrySave entry : save.getPokedex()) {
            Map<String, Object> pokeEntry = new LinkedHashMap<>();
            pokeEntry.put("nombreOficial", entry.getNombreOficial());
            pokeEntry.put("descubierta", entry.isDescubierta());
            pokeEntry.put("nivelInvestigacion", entry.getNivelInvestigacion());
            pokedex.add(pokeEntry);
        }
        data.put("pokedex", pokedex);

        data.put("mapSeed", save.getMapSeed());
        data.put("playerX", save.getPlayerX());
        data.put("playerY", save.getPlayerY());
        data.put("perfilFechaRegistro", save.getPerfilFechaRegistro().toString());
        data.put("perfilExpedicionesCompletadas", save.getPerfilExpedicionesCompletadas());
        data.put("perfilLogros", new ArrayList<>(save.getPerfilLogros()));
        return data;
    }

    private static GameSave mapToGameSave(Map<String, Object> data) throws IOException {
        int version = asInt(data.get("version"), "version");
        if (version != SAVE_VERSION) {
            throw new IOException("Versión de guardado no compatible: " + version);
        }

        String alias = asString(data.get("alias"), "alias");
        String label = data.get("label") == null ? "" : asString(data.get("label"), "label");
        long createdAtMillis = asLong(data.get("createdAtMillis"), "createdAtMillis");
        RolExplorador rol = RolExplorador.valueOf(asString(data.get("rol"), "rol"));
        int inventarioCapacidad = asInt(data.get("inventarioCapacidadMaxima"), "inventarioCapacidadMaxima");

        List<GameSave.ItemSave> items = new ArrayList<>();
        for (Object rawItem : asList(data.get("inventarioItems"), "inventarioItems")) {
            Map<String, Object> itemMap = asObject(rawItem, "inventarioItems[]");
            GameSave.ItemKind kind = GameSave.ItemKind.valueOf(asString(itemMap.get("kind"), "item.kind"));
            Integer puntosCuracion = asNullableInt(itemMap.get("puntosCuracion"), "item.puntosCuracion");
            Boolean revive = asNullableBoolean(itemMap.get("revive"), "item.revive");
            items.add(new GameSave.ItemSave(
                    asString(itemMap.get("nombre"), "item.nombre"),
                    itemMap.get("categoria") == null ? "" : asString(itemMap.get("categoria"), "item.categoria"),
                    asInt(itemMap.get("cantidad"), "item.cantidad"),
                    kind,
                    puntosCuracion,
                    revive));
        }

        int equipoCupo = asInt(data.get("equipoCupoMaximo"), "equipoCupoMaximo");
        List<GameSave.PokemonSave> equipo = new ArrayList<>();
        for (Object rawPokemon : asList(data.get("equipo"), "equipo")) {
            Map<String, Object> pokemonMap = asObject(rawPokemon, "equipo[]");
            List<GameSave.MoveSave> moves = new ArrayList<>();
            for (Object rawMove : asList(pokemonMap.get("movimientos"), "movimientos")) {
                Map<String, Object> moveMap = asObject(rawMove, "movimientos[]");
                moves.add(new GameSave.MoveSave(
                        asString(moveMap.get("nombre"), "move.nombre"),
                        asInt(moveMap.get("potencia"), "move.potencia"),
                        asInt(moveMap.get("usosMaximos"), "move.usosMaximos"),
                        asInt(moveMap.get("usosRestantes"), "move.usosRestantes")));
            }
            equipo.add(new GameSave.PokemonSave(
                    asString(pokemonMap.get("especie"), "pokemon.especie"),
                    asInt(pokemonMap.get("saludMaxima"), "pokemon.saludMaxima"),
                    asInt(pokemonMap.get("saludActual"), "pokemon.saludActual"),
                    moves));
        }

        List<GameSave.PokedexEntrySave> pokedex = new ArrayList<>();
        for (Object rawEntry : asList(data.get("pokedex"), "pokedex")) {
            Map<String, Object> entryMap = asObject(rawEntry, "pokedex[]");
            pokedex.add(new GameSave.PokedexEntrySave(
                    asString(entryMap.get("nombreOficial"), "pokedex.nombreOficial"),
                    asBoolean(entryMap.get("descubierta"), "pokedex.descubierta"),
                    asInt(entryMap.get("nivelInvestigacion"), "pokedex.nivelInvestigacion")));
        }

        long mapSeed = asLong(data.get("mapSeed"), "mapSeed");
        int playerX = asInt(data.get("playerX"), "playerX");
        int playerY = asInt(data.get("playerY"), "playerY");
        LocalDate fechaRegistro = LocalDate.parse(asString(data.get("perfilFechaRegistro"), "perfilFechaRegistro"));
        int expediciones = asInt(data.get("perfilExpedicionesCompletadas"), "perfilExpedicionesCompletadas");
        Set<String> logros = new HashSet<>();
        for (Object rawLogro : asList(data.get("perfilLogros"), "perfilLogros")) {
            logros.add(asString(rawLogro, "perfilLogros[]"));
        }

        return new GameSave(
                version,
                alias,
                label,
                createdAtMillis,
                rol,
                inventarioCapacidad,
                items,
                equipoCupo,
                equipo,
                pokedex,
                mapSeed,
                playerX,
                playerY,
                fechaRegistro,
                expediciones,
                logros);
    }

    private static Map<String, Object> asObject(Object value, String field) throws IOException {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return map;
        }
        throw new IOException("Campo " + field + " no es un objeto JSON");
    }

    private static List<?> asList(Object value, String field) throws IOException {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof List) {
            return (List<?>) value;
        }
        throw new IOException("Campo " + field + " no es una lista JSON");
    }

    private static String asString(Object value, String field) throws IOException {
        if (value instanceof String) {
            return (String) value;
        }
        throw new IOException("Campo " + field + " no es un texto");
    }

    private static int asInt(Object value, String field) throws IOException {
        Number number = asNumber(value, field);
        return number.intValue();
    }

    private static long asLong(Object value, String field) throws IOException {
        Number number = asNumber(value, field);
        return number.longValue();
    }

    private static Integer asNullableInt(Object value, String field) throws IOException {
        if (value == null) {
            return null;
        }
        return asInt(value, field);
    }

    private static Boolean asNullableBoolean(Object value, String field) throws IOException {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IOException("Campo " + field + " no es booleano");
    }

    private static boolean asBoolean(Object value, String field) throws IOException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new IOException("Campo " + field + " no es booleano");
    }

    private static Number asNumber(Object value, String field) throws IOException {
        if (value instanceof Number) {
            return (Number) value;
        }
        throw new IOException("Campo " + field + " no es numérico");
    }
}
