package com.mycompany.nadim;

import java.awt.image.BufferedImage;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

public abstract class BatallaPanelBase extends JPanel {
    private final Map<String, SpriteSheet> speciesSheets = new HashMap<>();

    protected BufferedImage loadSpeciesFrame(String species, int column) {
        if (species == null || species.isBlank()) {
            return null;
        }
        String key = normalizeSpeciesName(species);
        SpriteSheet sheet = speciesSheets.get(key);
        if (sheet == null) {
            try {
                sheet = ManejadorArchivos.withDefaultRoot().loadSpriteSheet(key + ".png", 64, 64);
            } catch (Exception ignored) {
                sheet = null;
            }
            speciesSheets.put(key, sheet);
        }
        if (sheet == null) {
            return null;
        }
        return sheet.getTileOrNull(column, 0);
    }

    private String normalizeSpeciesName(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.toLowerCase().trim();
        n = n.replaceAll("\\s+", "_");
        n = n.replaceAll("[^a-z0-9_\\-]", "");
        return n;
    }
}
