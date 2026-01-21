package com.mycompany.nadim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MapaPanel extends JPanel {
    private static final int TILE_SIZE = 32;
    private static final int VISTA_COLUMNAS = 28;
    private static final int VISTA_FILAS = 18;
    private static final int MAPA_COLUMNAS = 84;
    private static final int MAPA_FILAS = 54;
    private static final int RESOURCE_TICK_MS = 1000;
    private static final int MOVE_DURATION_MS = 260;

    private final Runnable exitCallback;
    private final EncounterListener encounterListener;
    private final Runnable menuCallback;
    private final Explorador explorador;
    private final ToastManager toast;
    private final Mapa mapa;
    private final Timer tickTimer;
    private final Timer moveTimer;
    private boolean forceLegendaryNextEncounter = false;
    private final SpriteSheet spriteJugador;

    private int jugadorX;
    private int jugadorY;
    private int camaraX;
    private int camaraY;
    private double camaraOffsetX;
    private double camaraOffsetY;
    private double renderX;
    private double renderY;
    private boolean moving = false;
    private int moveFromX;
    private int moveFromY;
    private int moveToX;
    private int moveToY;
    private long moveStartMs;
    private boolean keyUp;
    private boolean keyDown;
    private boolean keyLeft;
    private boolean keyRight;
    private int lastInputDx;
    private int lastInputDy;
    private int facingDx = 0;
    private int facingDy = 1;
    private Facing facing = Facing.DOWN;
    private int stepFrame = 0;

    public MapaPanel() {
        this(null, null, null, null, null);
    }

    public MapaPanel(Runnable exitCallback) {
        this(exitCallback, null, null, null, null);
    }

    public MapaPanel(Runnable exitCallback, EncounterListener encounterListener) {
        this(exitCallback, encounterListener, null, null, null);
    }

    public MapaPanel(Runnable exitCallback, EncounterListener encounterListener, Runnable menuCallback,
            Explorador explorador, ToastManager toast) {
        setPreferredSize(new Dimension(VISTA_COLUMNAS * TILE_SIZE, VISTA_FILAS * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        this.exitCallback = exitCallback;
        this.encounterListener = encounterListener;
        this.menuCallback = menuCallback;
        this.explorador = explorador;
        this.toast = toast;
        this.mapa = new Mapa(MAPA_COLUMNAS, MAPA_FILAS);
        this.spriteJugador = cargarSpriteJugador();

        mapa.generate();
        ubicarJugadorInicial();
        actualizarCamara();
        renderX = jugadorX;
        renderY = jugadorY;

        addKeyListener(new MovimientosJugador());

        tickTimer = new Timer(RESOURCE_TICK_MS, e -> {
            mapa.tickResources();
            repaint();
        });
        tickTimer.start();

        moveTimer = new Timer(16, e -> tickMovimiento());
        moveTimer.start();
    }

    public void setMapSeed(long seed) {
        if (seed == 0L) {
            return;
        }
        mapa.generateWithSeed(seed);
        repaint();
    }

    public long getMapSeed() {
        try {
            return mapa.getSeed();
        } catch (Exception e) {
            return 0L;
        }
    }

    public int getPlayerX() {
        return jugadorX;
    }

    public int getPlayerY() {
        return jugadorY;
    }

    public void setPlayerPosition(int x, int y) {
        if (x < 0 || y < 0)
            return;
        if (x >= 0 && x < MapaPanel.MAPA_COLUMNAS && y >= 0 && y < MapaPanel.MAPA_FILAS) {
            if (esTransitable(x, y)) {
                this.jugadorX = x;
                this.jugadorY = y;
                this.renderX = x;
                this.renderY = y;
                actualizarCamara();
                repaint();
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        dibujarMapa(g);
        dibujarJugador(g);
        if (toast != null) {
            toast.draw(g, getWidth(), getHeight());
        }
    }

    private void dibujarMapa(Graphics g) {
        int filas = VISTA_FILAS + 1;
        int columnas = VISTA_COLUMNAS + 1;
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                int mapaX = camaraX + columna;
                int mapaY = camaraY + fila;
                if (mapaX < 0 || mapaY < 0 || mapaX >= MAPA_COLUMNAS || mapaY >= MAPA_FILAS) {
                    continue;
                }
                Casilla tile = mapa.getTileAt(mapaX, mapaY);
                int x = (int) Math.round(columna * TILE_SIZE - camaraOffsetX);
                int y = (int) Math.round(fila * TILE_SIZE - camaraOffsetY);

                if (tile instanceof CasillaRecurso) {
                    CasillaRecurso recurso = (CasillaRecurso) tile;
                    var sprite = recurso.getDisplaySprite();
                    if (sprite != null) {
                        g.drawImage(sprite, x, y, TILE_SIZE, TILE_SIZE, null);
                    } else {
                        g.setColor(new Color(120, 180, 100));
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                    if (recurso.isDepleted()) {
                        g.setColor(new Color(0, 0, 0, 120));
                        g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                } else if (tile != null && tile.getSprite() != null) {
                    g.drawImage(tile.getSprite(), x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(120, 180, 100));
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

            }
        }
    }

    private void dibujarJugador(Graphics g) {
        int pantallaX = (int) Math.round((renderX - camaraX) * TILE_SIZE - camaraOffsetX) + 2;
        int pantallaY = (int) Math.round((renderY - camaraY) * TILE_SIZE - camaraOffsetY) + 2;
        int baseSize = TILE_SIZE - 4;
        int spriteSize = (int) Math.round(baseSize * 1.5);
        int spriteX = pantallaX + (baseSize - spriteSize) / 2;
        int spriteY = pantallaY + (baseSize - spriteSize) / 2;
        SpriteSheet sheet = obtenerSpriteLocal();
        BufferedImage sprite = sheet != null ? obtenerSprite(sheet, facing, moving, stepFrame) : null;
        if (sprite != null) {
            g.drawImage(sprite, spriteX, spriteY, spriteSize, spriteSize, null);
        } else {
            g.setColor(new Color(244, 67, 54));
            g.fillRect(pantallaX, pantallaY, baseSize, baseSize);
            g.setColor(Color.WHITE);
            g.drawRect(pantallaX, pantallaY, baseSize, baseSize);
            int fx = pantallaX + baseSize / 2 + facingDx * 8;
            int fy = pantallaY + baseSize / 2 + facingDy * 8;
            g.setColor(Color.WHITE);
            g.fillOval(fx - 2, fy - 2, 4, 4);
        }
    }

    private void moverJugador(int dx, int dy) {
        actualizarFacing(dx, dy);
        if (moving) {
            return;
        }
        intentarMover(dx, dy, false);
    }

    private void intentarMover(int dx, int dy, boolean continuous) {
        if (dx == 0 && dy == 0) {
            return;
        }
        actualizarFacing(dx, dy);
        int nuevoX = jugadorX + dx;
        int nuevoY = jugadorY + dy;
        if (nuevoX < 0 || nuevoX >= MAPA_COLUMNAS || nuevoY < 0 || nuevoY >= MAPA_FILAS) {
            return;
        }
        Casilla tile = mapa.getTileAt(nuevoX, nuevoY);
        if (tile == null || !tile.canBeTraversed()) {
            return;
        }
        moving = true;
        moveFromX = jugadorX;
        moveFromY = jugadorY;
        moveToX = nuevoX;
        moveToY = nuevoY;
        moveStartMs = System.currentTimeMillis();
        if (continuous) {
            stepFrame = (stepFrame + 1) % 2;
        } else {
            stepFrame = 0;
        }
    }

    private void tickMovimiento() {
        if (!moving) {
            return;
        }
        long elapsed = System.currentTimeMillis() - moveStartMs;
        double t = Math.min(1.0, elapsed / (double) MOVE_DURATION_MS);
        renderX = moveFromX + (moveToX - moveFromX) * t;
        renderY = moveFromY + (moveToY - moveFromY) * t;
        actualizarCamara();
        if (t >= 1.0) {
            finalizarMovimiento();
        }
        repaint();
    }

    private void finalizarMovimiento() {
        jugadorX = moveToX;
        jugadorY = moveToY;
        renderX = jugadorX;
        renderY = jugadorY;
        moving = false;
        actualizarCamara();

        Casilla tile = mapa.getTileAt(jugadorX, jugadorY);
        boolean encontro = procesarEncuentro(tile);

        if (!encontro) {
            int[] dir = obtenerDireccionActiva();
            if (dir != null) {
                intentarMover(dir[0], dir[1], true);
            }
        }
    }

    private int[] obtenerDireccionActiva() {
        if ((lastInputDx != 0 || lastInputDy != 0) && direccionPresionada(lastInputDx, lastInputDy)) {
            return new int[] { lastInputDx, lastInputDy };
        }
        if (keyUp) {
            return new int[] { 0, -1 };
        }
        if (keyDown) {
            return new int[] { 0, 1 };
        }
        if (keyLeft) {
            return new int[] { -1, 0 };
        }
        if (keyRight) {
            return new int[] { 1, 0 };
        }
        return null;
    }

    private boolean direccionPresionada(int dx, int dy) {
        if (dx == 0 && dy == -1) {
            return keyUp;
        }
        if (dx == 0 && dy == 1) {
            return keyDown;
        }
        if (dx == -1 && dy == 0) {
            return keyLeft;
        }
        if (dx == 1 && dy == 0) {
            return keyRight;
        }
        return false;
    }

    private void actualizarLastInputDesdeTeclas() {
        if (keyUp) {
            lastInputDx = 0;
            lastInputDy = -1;
            return;
        }
        if (keyDown) {
            lastInputDx = 0;
            lastInputDy = 1;
            return;
        }
        if (keyLeft) {
            lastInputDx = -1;
            lastInputDy = 0;
            return;
        }
        if (keyRight) {
            lastInputDx = 1;
            lastInputDy = 0;
            return;
        }
        lastInputDx = 0;
        lastInputDy = 0;
    }

    private boolean procesarEncuentro(Casilla tile) {
        if (tile == null || encounterListener == null || !(tile instanceof CasillaPasto)) {
            return false;
        }
        CasillaPasto grass = (CasillaPasto) tile;
        if (!grass.doesEncounterOccur()) {
            return false;
        }

        if (forceLegendaryNextEncounter) {
            forceLegendaryNextEncounter = false;
            if (toast != null) {
                toast.show("Depuración: encuentro forzado con un legendario.");
            }
            encounterListener.onEncounter(-1);
            return true;
        }

        if (explorador != null) {
            try {
                var perfil = explorador.getPerfil();
                var logros = perfil.getLogrosDesbloqueados();
                boolean unlocked = logros.contains("ARCEUS_UNLOCKED");
                boolean triggered = logros.contains("ARCEUS_TRIGGERED");
                boolean completed = logros.contains("ARCEUS_COMPLETED");
                if (unlocked && !triggered && !completed) {
                    perfil.desbloquearLogro("ARCEUS_TRIGGERED");
                    if (toast != null) {
                        toast.show("¡Encuentro legendario desbloqueado! Arceus aparece en la siguiente hierba.");
                    }
                    encounterListener.onEncounter(-1);
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        encounterListener.onEncounter(2);
        return true;
    }

    private void actualizarCamara() {
        int medioCols = VISTA_COLUMNAS / 2;
        int medioFilas = VISTA_FILAS / 2;
        double centroX = moving ? renderX : jugadorX;
        double centroY = moving ? renderY : jugadorY;
        double minCentroX = medioCols;
        double maxCentroX = MAPA_COLUMNAS - VISTA_COLUMNAS + medioCols;
        double minCentroY = medioFilas;
        double maxCentroY = MAPA_FILAS - VISTA_FILAS + medioFilas;
        centroX = Math.max(minCentroX, Math.min(centroX, maxCentroX));
        centroY = Math.max(minCentroY, Math.min(centroY, maxCentroY));

        double leftX = centroX - medioCols;
        double topY = centroY - medioFilas;
        camaraX = (int) Math.floor(leftX);
        camaraY = (int) Math.floor(topY);
        camaraX = Math.max(0, Math.min(camaraX, MAPA_COLUMNAS - VISTA_COLUMNAS));
        camaraY = Math.max(0, Math.min(camaraY, MAPA_FILAS - VISTA_FILAS));
        camaraOffsetX = (leftX - camaraX) * TILE_SIZE;
        camaraOffsetY = (topY - camaraY) * TILE_SIZE;
    }

    private void ubicarJugadorInicial() {
        int intentoX = MAPA_COLUMNAS / 2;
        int intentoY = MAPA_FILAS / 2;
        if (esTransitable(intentoX, intentoY)) {
            jugadorX = intentoX;
            jugadorY = intentoY;
            return;
        }

        for (int radio = 1; radio < Math.max(MAPA_COLUMNAS, MAPA_FILAS); radio++) {
            for (int dx = -radio; dx <= radio; dx++) {
                for (int dy = -radio; dy <= radio; dy++) {
                    int tx = intentoX + dx;
                    int ty = intentoY + dy;
                    if (esTransitable(tx, ty)) {
                        jugadorX = tx;
                        jugadorY = ty;
                        return;
                    }
                }
            }
        }
        jugadorX = 0;
        jugadorY = 0;
    }

    private boolean esTransitable(int x, int y) {
        if (x < 0 || x >= MAPA_COLUMNAS || y < 0 || y >= MAPA_FILAS) {
            return false;
        }
        Casilla tile = mapa.getTileAt(x, y);
        return tile != null && tile.canBeTraversed();
    }

    private void recogerRecurso() {
        if (explorador == null) {
            return;
        }
        ResourceCandidate candidato = obtenerRecursoInteractuable();
        if (candidato == null) {
            return;
        }

        CasillaRecurso recurso = candidato.recurso;

        if (recurso.isDepleted()) {
            if (toast != null) {
                String harvester = recurso.getLastHarvester();
                int remaining = recurso.getRemainingCooldown();
                if (harvester != null && !harvester.isBlank()) {
                    toast.show("Recurso recogido por " + harvester + ". Vuelve en " + remaining + "s.");
                } else {
                    toast.show("Este recurso se esta recuperando. Vuelve en " + remaining + "s.");
                }
            }
            return;
        }

        String alias = explorador.getAlias();
        int obtenido = recurso.harvest(alias == null ? "" : alias);
        boolean agregado = explorador.getInventario().agregar(recurso.getResourceName(), obtenido);
        if (agregado) {
            if (toast != null) {
                toast.show("Recogiste " + obtenido + "x " + recurso.getResourceName() + ".");
            }
        } else if (toast != null) {
            toast.show("No hay espacio en el inventario.");
        }
        repaint();
    }

    private static final class ResourceCandidate {
        final CasillaRecurso recurso;
        final int x;
        final int y;

        ResourceCandidate(CasillaRecurso r, int x, int y) {
            this.recurso = r;
            this.x = x;
            this.y = y;
        }
    }

    private ResourceCandidate obtenerRecursoInteractuable() {
        int nx = jugadorX + facingDx;
        int ny = jugadorY + facingDy;
        if (nx < 0 || nx >= MAPA_COLUMNAS || ny < 0 || ny >= MAPA_FILAS) {
            return null;
        }
        Casilla candidato = mapa.getTileAt(nx, ny);
        if (candidato instanceof CasillaRecurso) {
            CasillaRecurso recurso = (CasillaRecurso) candidato;
            return new ResourceCandidate(recurso, nx, ny);
        }
        return null;
    }

    private class MovimientosJugador extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                    keyUp = true;
                    lastInputDx = 0;
                    lastInputDy = -1;
                    moverJugador(0, -1);
                }
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                    keyDown = true;
                    lastInputDx = 0;
                    lastInputDy = 1;
                    moverJugador(0, 1);
                }
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                    keyLeft = true;
                    lastInputDx = -1;
                    lastInputDy = 0;
                    moverJugador(-1, 0);
                }
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                    keyRight = true;
                    lastInputDx = 1;
                    lastInputDy = 0;
                    moverJugador(1, 0);
                }
                case KeyEvent.VK_SPACE -> interactuar();
                case KeyEvent.VK_0 -> {
                    forceLegendaryNextEncounter = true;
                    if (toast != null) {
                        toast.show("Depuración: el próximo combate será Arceus");
                    }
                }
                case KeyEvent.VK_ESCAPE -> {
                    if (menuCallback != null) {
                        menuCallback.run();
                    }
                }
                case KeyEvent.VK_I, KeyEvent.VK_M -> {
                    if (menuCallback != null) {
                        menuCallback.run();
                    }
                }
                default -> {
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                    keyUp = false;
                    if (lastInputDx == 0 && lastInputDy == -1) {
                        actualizarLastInputDesdeTeclas();
                    }
                }
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                    keyDown = false;
                    if (lastInputDx == 0 && lastInputDy == 1) {
                        actualizarLastInputDesdeTeclas();
                    }
                }
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                    keyLeft = false;
                    if (lastInputDx == -1 && lastInputDy == 0) {
                        actualizarLastInputDesdeTeclas();
                    }
                }
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                    keyRight = false;
                    if (lastInputDx == 1 && lastInputDy == 0) {
                        actualizarLastInputDesdeTeclas();
                    }
                }
                default -> {
                }
            }
        }
    }

    private void interactuar() {
        if (moving) {
            return;
        }
        recogerRecurso();
    }

    private void actualizarFacing(int dx, int dy) {
        if (dx == 0 && dy == 0) {
            return;
        }
        facingDx = Integer.compare(dx, 0);
        facingDy = Integer.compare(dy, 0);
        if (facingDx != 0 && facingDy != 0) {
            facingDy = 0;
        }
        if (facingDx == 0 && facingDy < 0) {
            facing = Facing.UP;
        } else if (facingDx == 0 && facingDy > 0) {
            facing = Facing.DOWN;
        } else if (facingDx < 0) {
            facing = Facing.LEFT;
        } else if (facingDx > 0) {
            facing = Facing.RIGHT;
        }
        repaint();
    }

    private SpriteSheet obtenerSpriteLocal() {
        return spriteJugador;
    }

    private SpriteSheet cargarSpriteJugador() {
        try {
            ManejadorArchivos fm = ManejadorArchivos.withDefaultRoot();
            String[] opciones = { "chico_1.png", "chico_2.png", "chica_1.png", "chica_2.png" };
            for (String archivo : opciones) {
                SpriteSheet sheet = fm.loadSpriteSheet(archivo, 32, 32);
                if (sheet != null) {
                    return sheet;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private BufferedImage obtenerSprite(SpriteSheet sheet, Facing dir, boolean moving, int step) {
        if (sheet == null) {
            return null;
        }
        int col = 0;
        int row = 0;
        if (!moving) {
            switch (dir) {
                case UP -> {
                    col = 0;
                    row = 0;
                }
                case RIGHT -> {
                    col = 1;
                    row =   0;
                }
                case LEFT -> {
                    col = 0;
                    row = 2;
                }
                case DOWN -> {
                    col = 2;
                    row = 1;
                }
            }
        } else {
            boolean walk2 = step % 2 == 1;
            switch (dir) {
                case UP -> {
                    col = walk2 ? 1 : 2;
                    row = walk2 ? 3 : 0;
                }
                case RIGHT -> {
                    col = 1;
                    row = walk2 ? 2 : 1;
                }
                case LEFT -> {
                    col = 0;
                    row = walk2 ? 3 : 1;
                }
                case DOWN -> {
                    col = 2;
                    row = walk2 ? 3 : 2;
                }
            }
        }
        try {
            return sheet.getTileOrNull(col, row);
        } catch (Exception ignored) {
            return null;
        }
    }

    private enum Facing {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    @FunctionalInterface
    public interface EncounterListener {
        void onEncounter(int tileType);
    }
}
