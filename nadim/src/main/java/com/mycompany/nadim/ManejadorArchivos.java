package com.mycompany.nadim;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ManejadorArchivos {
    private final Path RaizAsset;
    private final Map<Path, BufferedImage> cache = new HashMap<>();

    public ManejadorArchivos(Path raizAsset) {
        this.RaizAsset = raizAsset;
    }

    public static ManejadorArchivos withDefaultRoot() {
        return new ManejadorArchivos(Paths.get("src", "main","java","com","mycompany","nadim","assets"));
    }

    public BufferedImage loadImage(String relativePath) {
        Path resolvedPath = RaizAsset.resolve(relativePath).normalize();
        if (cache.containsKey(resolvedPath)) {
            return cache.get(resolvedPath);
        }

        try (InputStream input = Files.newInputStream(resolvedPath)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new IOException("Unsupported or corrupt image: " + relativePath);
            }
            cache.put(resolvedPath, image);
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load image: " + resolvedPath, e);
        }
    }

    public SpriteSheet loadSpriteSheet(String relativePath, int tileWidth, int tileHeight) {
        BufferedImage sheet = loadImage(relativePath);
        return new SpriteSheet(sheet, tileWidth, tileHeight);
    }

    public Path getRaizAsset() {
        return RaizAsset;
    }
}
