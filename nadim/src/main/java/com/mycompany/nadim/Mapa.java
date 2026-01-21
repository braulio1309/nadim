package com.mycompany.nadim;

import java.awt.image.BufferedImage;
import java.util.Random;

public class Mapa {
    private final Casilla[][] tiles;
    private final int width;
    private final int height;
    private long seed;
    private boolean hasSeed;

    public Mapa(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Casilla[width][height];
    }

    public Casilla getTileAt(int x, int y) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException();
        }
        return tiles[x][y];
    }

    public void setTileAt(int x, int y, Casilla tile) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException();
        }
        tiles[x][y] = tile;
    }

    public void generate() {
        if (!hasSeed) {
            seed = new Random().nextLong();
            hasSeed = true;
        }
        generateWithSeed(seed);
    }

    public void generateWithSeed(long seed) {
        this.seed = seed;
        this.hasSeed = true;
        ManejadorArchivos fm = ManejadorArchivos.withDefaultRoot();
        SpriteSheet tileset = fm.loadSpriteSheet("tileset1.png", 16, 16);

        BufferedImage roadNormal = tileset.getTileOrNull(0, 0);
        BufferedImage normalGrass = tileset.getTileOrNull(5, 0);
        BufferedImage encounterGrass = tileset.getTileOrNull(6, 0);
        BufferedImage normalGrass2 = tileset.getTileOrNull(4, 1);
        BufferedImage normalGrass3 = tileset.getTileOrNull(5, 1);
        BufferedImage normalGrass4 = tileset.getTileOrNull(6, 1);
        BufferedImage treeSprite = tileset.getTileOrNull(4, 2);
        BufferedImage flowerSprite = tileset.getTileOrNull(5, 2);

        Random rnd = new Random(seed);
        BufferedImage baseGrass = firstNonNull(normalGrass, normalGrass2, normalGrass3, normalGrass4, encounterGrass,
                roadNormal);
        BufferedImage[] grassVariants = new BufferedImage[] { normalGrass, normalGrass2, normalGrass3, normalGrass4,
                baseGrass };

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                BufferedImage pick = grassVariants[rnd.nextInt(grassVariants.length)];
                if (pick == null) {
                    pick = baseGrass;
                }
                CasillaTerreno t = new CasillaTerreno(pick, true);
                tiles[x][y] = t;
            }
        }

        int patchCount = Math.max(2, (width * height) / 80);
        for (int p = 0; p < patchCount; p++) {
            int cx = rnd.nextInt(width);
            int cy = rnd.nextInt(height);
            int radius = 1 + rnd.nextInt(2);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) + Math.abs(dy) <= radius) {
                        int tx = cx + dx;
                        int ty = cy + dy;
                        if (inBounds(tx, ty)) {
                            CasillaPasto g = new CasillaPasto(encounterGrass != null ? encounterGrass : baseGrass,
                                    0.18f);
                            tiles[tx][ty] = g;
                        }
                    }
                }
            }
        }

        int treeAttempts = (width * height) / 10;
        for (int i = 0; i < treeAttempts; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            if (!inBounds(x, y)) {
                continue;
            }
            Casilla current = tiles[x][y];
            if (current != null && current.canBeTraversed() && rnd.nextFloat() < 0.35f) {
                String resource = rnd.nextBoolean() ? "Guijarro" : "Baya";
                int yield = 1 + rnd.nextInt(4);
                CasillaRecurso rt = new CasillaRecurso(treeSprite, resource, yield, 300, false);
                tiles[x][y] = rt;
            }
        }

        int flowerAttempts = (width * height) / 15;
        for (int i = 0; i < flowerAttempts; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            if (!inBounds(x, y)) {
                continue;
            }
            Casilla current = tiles[x][y];
            if (current != null && current.canBeTraversed() && !(current instanceof CasillaPasto)
                    && rnd.nextFloat() < 0.25f) {
                String resource = rnd.nextBoolean() ? "Planta" : "Baya";
                int yield = 1 + rnd.nextInt(3);
                CasillaRecurso rt = new CasillaRecurso(flowerSprite, resource, yield, 200, true);
                tiles[x][y] = rt;
            }
        }
    }

    public long getSeed() {
        return seed;
    }

    public void tickResources() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Casilla tile = tiles[x][y];
                if (tile instanceof CasillaRecurso) {
                    CasillaRecurso resource = (CasillaRecurso) tile;
                    resource.tickCooldown();
                }
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private BufferedImage firstNonNull(BufferedImage... options) {
        for (BufferedImage img : options) {
            if (img != null) {
                return img;
            }
        }
        return null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
