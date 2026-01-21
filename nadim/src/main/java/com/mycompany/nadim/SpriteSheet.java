package com.mycompany.nadim;

import java.awt.image.BufferedImage;

public class SpriteSheet {
    private final BufferedImage sheet;
    private final int tileWidth;
    private final int tileHeight;

    public SpriteSheet(BufferedImage sheet, int tileWidth, int tileHeight) {
        this.sheet = sheet;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    public BufferedImage getTile(int column, int row) {
        int x = column * tileWidth;
        int y = row * tileHeight;
        if (x + tileWidth > sheet.getWidth() || y + tileHeight > sheet.getHeight()) {
            throw new IllegalArgumentException("Requested tile is outside the spritesheet bounds.");
        }
        return sheet.getSubimage(x, y, tileWidth, tileHeight);
    }

    public BufferedImage getTileOrNull(int column, int row) {
        int x = column * tileWidth;
        int y = row * tileHeight;
        if (column < 0 || row < 0 || x + tileWidth > sheet.getWidth() || y + tileHeight > sheet.getHeight()) {
            return null;
        }
        return sheet.getSubimage(x, y, tileWidth, tileHeight);
    }

    public int getColumns() {
        return sheet.getWidth() / tileWidth;
    }

    public int getRows() {
        return sheet.getHeight() / tileHeight;
    }

    public BufferedImage getSheet() {
        return sheet;
    }
}
