package com.mycompany.nadim;

import java.awt.CardLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class VentanaJuego extends JFrame implements MenuPrincipalPanel.AccionesMenu {
    private static final String AYUDA_TEXTO = String.join(
            "\n",
            "Leyendas Pokemon: Arceus - Guia Rapida",
            "",
            "Exploracion y Recursos:",
            "- Desplazate por las zonas buscando materiales como guijarros, plantas y bayas.",
            "- El inventario tiene capacidad limitada; recolectar o fabricar comprueba el espacio disponible.",
            "",
            "Artesania:",
            "- Transforma recursos en objetos clave, por ejemplo Pokeball basica (2 plantas + 3 guijarros).",
            "- Si no hay materiales suficientes, la fabricacion falla y se informa al jugador.",
            "",
            "Captura e Investigacion:",
            "- Cada especie tiene nivel de investigacion de 0 a 10.",
            "- Capturar suma +2 puntos y ganar combates suma +1 punto; al alcanzar 10 la investigacion se completa.",
            "- Capturar exige consumir una Pokeball del inventario, de lo contrario la accion se rechaza con un aviso.",
            "",
            "Combates:",
            "- Las batallas son por turnos; elige movimientos considerando poder, tipo y salud restante.",
            "- Victoria otorga progreso de investigacion y botin de recursos. Derrota consume un objeto fabricado.",
            "- Los Pokemon debilitados requieren objetos de curacion para volver a luchar.",
            "",
            "Encuentro Legendario:",
            "- Se desbloquea tras completar 5 especies investigadas.",
            "- Al superarlo, la entrada legendaria se marca en nivel 10 como cierre de la aventura.",
            "",
            "Persistencia:",
            "- Guarda y carga inventario, progresos de Pokedex y estadisticas desde el modulo de sesiones.");

    private static final String ACERCA_DE_TEXTO = String.join(
            "\n",
            "Leyendas Pokemon: Arceus (Demo)",
            "",
            "Lenguaje: Java",
            "Bibliotecas: Swing (Java SE)",
            "Version: 0.1",
            "Desarrolladores: Equipo Nadim");

    private static final String CARD_REGISTRO = "REGISTRO";
    private static final String CARD_MENU = "MENU";
    private static final String CARD_MAPA = "MAPA";
    private static final String CARD_BATALLA = "BATALLA";

    private final CardLayout layout = new CardLayout();
    private final JPanel contenedor = new JPanel(layout);
    private final Random random = new Random();
    private RegistroUsuarioPanel registroPanel;
    private MenuPrincipalPanel menuPanel;
    private MapaPanel mapaPanel;
    private final ToastManager toast = new ToastManager(3000);
    private String alias;
    private BatallaPanelBasico batallaPanel;
    private EquipoPokemon equipoJugador;
    private Explorador explorador;
    private boolean expedicionActiva;

    public VentanaJuego() {
        setTitle("Leyendas Pokemon: Arceus");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        registroPanel = new RegistroUsuarioPanel(new RegistroUsuarioPanel.Listener() {
            @Override
            public void onAliasSeleccionado(String seleccionado) {
                inicializarSesion(seleccionado);
            }

            @Override
            public void onCancelar() {
                salir();
            }
        });

        contenedor.add(registroPanel, CARD_REGISTRO);
        add(contenedor);
        pack();
        setLocationRelativeTo(null);
        mostrarRegistro();
    }

    private void inicializarSesion(String aliasSeleccionado) {
        if (aliasSeleccionado == null || aliasSeleccionado.isBlank()) {
            return;
        }
        if (Objects.equals(this.alias, aliasSeleccionado)) {
            mostrarMenu();
            return;
        }
       
        this.alias = aliasSeleccionado;


        setTitle("Leyendas Pokemon: Arceus - " + alias);
        menuPanel = new MenuPrincipalPanel(this);
        equipoJugador = new EquipoPokemon(6);
        Inventario inventario = new Inventario(50);


        inventario.registrarObjeto(new ObjetoMaterial("Planta", "Ingrediente", 6));
        inventario.registrarObjeto(new ObjetoMaterial("Guijarro", "Ingrediente", 9));
        inventario.registrarObjeto(new ObjetoMaterial("Baya", "Ingrediente", 6));


        
        Set<String> especiesBase = PokemonFactory.obtenerEspeciesRegistradas();
        Pokedex pokedex = new Pokedex(especiesBase);
      
      
        PerfilJugador perfil = new PerfilJugador(LocalDate.now());
        TallerArtesania taller = crearTallerBase();
        explorador = new Explorador(this.alias, RolExplorador.LIDER, equipoJugador, inventario, pokedex, perfil,
                taller);
        Pokemon inicial = crearPokemonJugador();
      
        equipoJugador.agregar(inicial);
        mapaPanel = new MapaPanel(this::mostrarMenu, this::iniciarEncuentro, this::mostrarMenuExplorador,
                explorador, toast);



        // If the loaded/initial progress already meets the threshold, unlock the
        // legendary encounter
        try {
            if (explorador.getPokedex().getEspeciesCompletadas() >= 5) {
                var logros = perfil.getLogrosDesbloqueados();
                if (!logros.contains("ARCEUS_UNLOCKED") && !logros.contains("ARCEUS_COMPLETED")) {
                    perfil.desbloquearLogro("ARCEUS_UNLOCKED");
                    toast.show("Encuentro legendario desbloqueado: Arceus ahora puede aparecer.");
                }
            }
        } catch (Exception ignored) {
        }

        contenedor.add(menuPanel, CARD_MENU);
        contenedor.add(mapaPanel, CARD_MAPA);

        // Restaura el tamaño original del juego (menu/mapa) tras el login.
        pack();
        setLocationRelativeTo(null);
        mostrarMenu();
    }

    private void mostrarRegistro() {
        layout.show(contenedor, CARD_REGISTRO);
        if (registroPanel != null) {
            registroPanel.requestFocusInWindow();
        }
    }

    private void mostrarMenu() {
        if (menuPanel == null) {
            mostrarRegistro();
            return;
        }
        if (expedicionActiva && explorador != null) {
            registrarExploracion("Retorno al menu principal");
            expedicionActiva = false;
        }
        layout.show(contenedor, CARD_MENU);
        menuPanel.requestFocusInWindow();
    }

    private void mostrarMapa() {
        if (mapaPanel == null) {
            mostrarRegistro();
            return;
        }
        layout.show(contenedor, CARD_MAPA);
        mapaPanel.requestFocusInWindow();
    }

    private void mostrarMenuExplorador() {
        if (explorador == null) {
            mostrarRegistro();
            return;
        }
        MenuExploradorDialog dialogo = new MenuExploradorDialog(this, explorador, this::guardarPartida,
                this::mostrarToast, this::mostrarMenu);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        mapaPanel.requestFocusInWindow();
    }

    private void mostrarBatalla(Pokemon enemigo) {
        if (batallaPanel != null) {
            contenedor.remove(batallaPanel);
        }
        batallaPanel = new BatallaPanelBasico(explorador, enemigo, this::finalizarBatalla, toast);
        contenedor.add(batallaPanel, CARD_BATALLA);
        layout.show(contenedor, CARD_BATALLA);
        batallaPanel.requestFocusInWindow();
        contenedor.revalidate();
    }

    private void iniciarEncuentro(int tileType) {
        if (equipoJugador == null) {
            return;
        }
        if (equipoJugador.seleccionarTitular() == null) {
            manejarEquipoSinSalud();
            return;
        }
        Pokemon enemigo = crearPokemonSalvaje(tileType);
        SwingUtilities.invokeLater(() -> mostrarBatalla(enemigo));
    }

    private void finalizarBatalla(BatallaPanelBasico.ResultadoCombate resultado) {
        BatallaPanelBasico.Resultado tipo = resultado.getTipo();
        switch (tipo) {
            case VICTORIA -> JOptionPane.showMessageDialog(
                    this,
                    "El combate finalizo con victoria.",
                    "Victoria",
                    JOptionPane.INFORMATION_MESSAGE);
            case DERROTA -> JOptionPane.showMessageDialog(
                    this,
                    resultado.getLostItemName() != null && resultado.getLostItemQty() > 0
                            ? "Tu equipo ha quedado fuera de combate.\nHas perdido " + resultado.getLostItemQty()
                                    + "x " + resultado.getLostItemName() + "."
                            : "Tu equipo ha quedado fuera de combate.",
                    "Derrota",
                    JOptionPane.WARNING_MESSAGE);
            case HUIDA -> JOptionPane.showMessageDialog(
                    this,
                    "Escapaste del encuentro.",
                    "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            case CAPTURA -> manejarCaptura(resultado.getCapturado());
        }
        // If a legendary was completed, show end-game choices
        if (resultado.isLegendaryCompleted()) {
            String especie = resultado.getLegendarySpecies();
            VictoriaPanel vs = new VictoriaPanel(this, especie, new VictoriaPanel.Listener() {
                @Override
                public void onSaveAndExit() {
                    String etiqueta = JOptionPane.showInputDialog(VentanaJuego.this,
                            "Introduce una etiqueta para el guardado:",
                            "Guardar partida", JOptionPane.PLAIN_MESSAGE);
                    if (etiqueta == null) {
                        etiqueta = "Guardado Arceus";
                    }
                    try {
                        guardarPartida(etiqueta);
                        JOptionPane.showMessageDialog(VentanaJuego.this, "Partida guardada. Saliendo...", "Guardar",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(VentanaJuego.this,
                                "No se pudo guardar la partida:\n" + e.getMessage(),
                                "Error", JOptionPane.WARNING_MESSAGE);
                    }
                    salir();
                }

                @Override
                public void onExit() {
                    salir();
                }

                @Override
                public void onContinue() {
                    // nothing special, just continue playing
                }
            });
            vs.setVisible(true);
        }

        mostrarMapa();
    }

    private void manejarCaptura(Pokemon capturado) {
        if (capturado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "El pokemon capturado no pudo unirse al equipo.",
                    "Captura",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boolean agregado = explorador.capturar(capturado);
        JOptionPane.showMessageDialog(
                this,
                agregado
                        ? "Tu equipo crece con " + capturado.getEspecie() + "."
                        : "El equipo esta lleno y no puede guardar a " + capturado.getEspecie() + ".",
                "Captura",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void manejarEquipoSinSalud() {
        String[] opciones = { "Ir al menu", "Cancelar" };
        int seleccion = JOptionPane.showOptionDialog(
                this,
                "Todo tu equipo esta fuera de combate. Cura a tus Pokemon antes de enfrentarte a otro encuentro.",
                "Equipo debilitado",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[0]);
        if (seleccion == 0) {
            mostrarMenu();
        }
    }

    private Pokemon crearPokemonSalvaje(int tileType) {
        if (tileType == -1) {
            try {
                // `PokemonSpecies` is not available; assume Arceus by name
                if (explorador != null && explorador.getPerfil() != null) {
                    var logros = explorador.getPerfil().getLogrosDesbloqueados();
                    if (logros.contains("ARCEUS_COMPLETED")) {
                        return PokemonFactory.crearPokemonSalvaje(tileType, random);
                    }
                }
                return PokemonFactory.crearPokemonPorNombre("Arceus", random, tileType);
            } catch (Exception ignored) {
            }
        }

        return PokemonFactory.crearPokemonSalvaje(tileType, random);
    }

    private TallerArtesania crearTallerBase() {
        TallerArtesania taller = new TallerArtesania();
        taller.registrarReceta(new RecetaArtesania("Pokeball", Map.of(
                "Planta", 2,
                "Guijarro", 3), () -> new ObjetoCaptura("Pokeball", "Herramienta", 1)));

        taller.registrarReceta(new RecetaArtesania("Pocion Curativa", Map.of(
                "Planta", 2,
                "Baya", 2), () -> new PocionCurativa("Pocion Curativa", "Curacion", 1, 40, false)));

        taller.registrarReceta(new RecetaArtesania("Pocion Revivir", Map.of(
                "Baya", 4,
                "Guijarro", 2), () -> new PocionCurativa("Pocion Revivir", "Curacion", 1, 0, true)));
        return taller;
    }

    @Override
    public void iniciarJuego() {
        if (explorador == null) {
            mostrarRegistro();
            return;
        }
        seleccionarPokemonInicial();
        expedicionActiva = true;
        mostrarMapa();
    }

    private void seleccionarPokemonInicial() {
        if (equipoJugador == null) {
            return;
        }
        String starter = SeleccionInicialDialog.seleccionar(this);
        if (starter == null || starter.isBlank()) {
            return;
        }
        Pokemon elegido = PokemonFactory.crearPokemonInicial(starter);
        if (equipoJugador.getMiembros().isEmpty()) {
            equipoJugador.agregar(elegido);
        } else {
            Pokemon primero = equipoJugador.getMiembros().get(0);
            equipoJugador.remover(primero);
            equipoJugador.agregar(elegido);
        }
        mostrarToast("Has elegido a " + starter + ".");
    }

    private Pokemon crearPokemonJugador() {
        String[] candidatos = { "Rowlet", "Cyndaquil", "Oshawott" };
        for (String nombre : candidatos) {
            try {
                return PokemonFactory.crearPokemonInicial(nombre);
            } catch (Exception ignored) {
            }
        }
        try {
            return PokemonFactory.crearPokemonSalvaje(1, random);
        } catch (Exception ignored) {
        }
        return new Pokemon("Explorador", new Movimiento[] { new Movimiento("Golpe", 10, 20) }, 60);
    }

    @Override
    public void cargarPartida() {
        if (alias == null) {
            mostrarRegistro();
            return;
        }
        List<SaveSlotInfo> slots;
        try {
            slots = SaveGameStorage.listarSlots(alias);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron listar los guardados." + System.lineSeparator() + e.getMessage(),
                    "Cargar partida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (slots.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay guardados disponibles para " + alias + ".",
                    "Cargar partida",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object seleccion = JOptionPane.showInputDialog(
                this,
                "Selecciona un guardado:",
                "Cargar partida",
                JOptionPane.QUESTION_MESSAGE,
                null,
                slots.toArray(),
                slots.get(0));
        if (!(seleccion instanceof SaveSlotInfo)) {
            return;
        }
        SaveSlotInfo slot = (SaveSlotInfo) seleccion;

        try {
            GameSave save = SaveGameStorage.cargar(alias, slot);
            aplicarGuardado(save);
            JOptionPane.showMessageDialog(
                    this,
                    "Partida cargada correctamente.",
                    "Cargar partida",
                    JOptionPane.INFORMATION_MESSAGE);

            expedicionActiva = true;
            if (menuPanel == null) {
                menuPanel = new MenuPrincipalPanel(this);
                contenedor.add(menuPanel, CARD_MENU);
            }
            if (mapaPanel == null) {
                mapaPanel = new MapaPanel(this::mostrarMenu, this::iniciarEncuentro, this::mostrarMenuExplorador,
                        explorador, toast);
                contenedor.add(mapaPanel, CARD_MAPA);
            }
            try {
                long seed = save.getMapSeed();
                if (seed != 0L) {
                    mapaPanel.setMapSeed(seed);
                }
                int px = save.getPlayerX();
                int py = save.getPlayerY();
                mapaPanel.setPlayerPosition(px, py);
            } catch (Exception ignored) {
            }
            pack();
            setLocationRelativeTo(null);
            mostrarMapa();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo cargar la partida." + System.lineSeparator() + e.getMessage(),
                    "Cargar partida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void mostrarAyuda() {
        JOptionPane.showMessageDialog(
                this,
                AYUDA_TEXTO,
                "Ayuda",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(
                this,
                ACERCA_DE_TEXTO,
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void mostrarHistorial() {
        if (alias == null) {
            mostrarRegistro();
            return;
        }
        List<String> lineas;
        try {
            lineas = UsuarioStorage.leerHistorial(alias);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo leer el historial." + System.lineSeparator() + e.getMessage(),
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (lineas.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Aun no hay exploraciones registradas para " + alias + ".",
                    "Historial",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextArea area = new JTextArea(String.join(System.lineSeparator(), lineas));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(40);
        area.setRows(Math.min(15, lineas.size() + 2));
        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, scroll, "Historial de " + alias, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void salir() {
        if (expedicionActiva && explorador != null) {
            registrarExploracion("Fin de sesion (salida del juego)");
            expedicionActiva = false;
        }
        dispose();
        System.exit(0);
    }

    private void guardarPartida(String etiqueta) throws IOException {
        if (alias == null || explorador == null) {
            throw new IOException("No hay una sesión activa.");
        }
        int px = 0, py = 0;
        long seed = 0L;
        if (mapaPanel != null) {
            try {
                px = mapaPanel.getPlayerX();
                py = mapaPanel.getPlayerY();
                seed = mapaPanel.getMapSeed();
            } catch (Exception ignored) {
            }
        }
        SaveGameStorage.guardar(alias, etiqueta, explorador, px, py, seed);
    }

    private void aplicarGuardado(GameSave save) {
        if (save == null) {
            return;
        }
        // Inventario
        Inventario inventario = new Inventario(save.getInventarioCapacidadMaxima());
        for (GameSave.ItemSave item : save.getInventarioItems()) {
            if (item == null || item.getCantidad() <= 0) {
                continue;
            }
            String nombre = item.getNombre();
            String categoria = item.getCategoria();
            Objeto objeto;
            switch (item.getKind()) {
                case CAPTURA -> objeto = new ObjetoCaptura(nombre, categoria, item.getCantidad());
                case CURA -> {
                    int puntos = item.getPuntosCuracion() == null ? 0 : item.getPuntosCuracion();
                    boolean revive = item.getRevive() != null && item.getRevive();
                    objeto = new PocionCurativa(nombre, categoria, item.getCantidad(), puntos, revive);
                }
                default -> objeto = new ObjetoMaterial(nombre, categoria, item.getCantidad());
            }
            inventario.registrarObjeto(objeto);
        }

        // Equipo
        EquipoPokemon equipo = new EquipoPokemon(save.getEquipoCupoMaximo());
        for (GameSave.PokemonSave pokemonSave : save.getEquipo()) {
            if (pokemonSave == null) {
                continue;
            }
            List<GameSave.MoveSave> moves = pokemonSave.getMovimientos();
            List<Movimiento> movimientos = new ArrayList<>();
            for (GameSave.MoveSave moveSave : moves) {
                if (moveSave == null) {
                    continue;
                }
                Movimiento movimiento = new Movimiento(
                        moveSave.getNombre(),
                        moveSave.getPotencia(),
                        moveSave.getUsosMaximos());
                int restantes = Math.max(0, Math.min(moveSave.getUsosRestantes(), moveSave.getUsosMaximos()));
                int fallos = moveSave.getUsosMaximos() - restantes;
                for (int i = 0; i < fallos; i++) {
                    movimiento.registrarUsoFallido();
                }
                movimientos.add(movimiento);
            }

            // Construct Pokemon by its species name (PokemonSpecies class is not present)
            Pokemon pokemon = new Pokemon(
                    pokemonSave.getEspecie(),
                    movimientos.toArray(new Movimiento[0]),
                    pokemonSave.getSaludMaxima());
            int objetivo = Math.max(0, Math.min(pokemonSave.getSaludActual(), pokemonSave.getSaludMaxima()));
            int danio = pokemonSave.getSaludMaxima() - objetivo;
            if (danio > 0) {
                pokemon.aplicarDanio(danio);
            }
            equipo.agregar(pokemon);
        }

        // Pokedex
        Set<String> especiesBase = PokemonFactory.obtenerEspeciesRegistradas();
        Pokedex pokedex = new Pokedex(especiesBase);
        for (GameSave.PokedexEntrySave entrada : save.getPokedex()) {
            if (entrada == null) {
                continue;
            }
            pokedex.aplicarEstadoEntrada(entrada.getNombreOficial(), entrada.isDescubierta(),
                    entrada.getNivelInvestigacion());
        }
        pokedex.recalcularEspeciesCompletadas();

        // Perfil + taller
        PerfilJugador perfil = new PerfilJugador(save.getPerfilFechaRegistro(), save.getPerfilExpedicionesCompletadas(),
                save.getPerfilLogros());
        TallerArtesania taller = crearTallerBase();

        equipoJugador = equipo;
        explorador = new Explorador(alias, save.getRol(), equipoJugador, inventario, pokedex, perfil, taller);
        expedicionActiva = false;
    }

    private void registrarExploracion(String motivo) {
        if (explorador == null || alias == null) {
            return;
        }
        try {
            String resumen = ExploracionSnapshot.construir(explorador, motivo);
            UsuarioStorage.registrarExploracion(alias, resumen);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar el historial." + System.lineSeparator() + e.getMessage(),
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarToast(String mensaje) {
        toast.show(mensaje);
        if (mapaPanel != null && mapaPanel.isShowing()) {
            mapaPanel.repaint();
        } else if (batallaPanel != null && batallaPanel.isShowing()) {
            batallaPanel.repaint();
        } else if (menuPanel != null && menuPanel.isShowing()) {
            menuPanel.repaint();
        }
    }
}
