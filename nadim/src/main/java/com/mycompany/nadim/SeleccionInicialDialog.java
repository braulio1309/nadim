package com.mycompany.nadim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Dialogo sencillo para seleccionar inicial con una animacion de flotado.
 */
public class SeleccionInicialDialog extends JDialog {
    private int phase;
    private final AnimationPanel animationPanel = new AnimationPanel();
    private String selected;

    private SeleccionInicialDialog(JFrame owner) {
        super(owner, "Elige tu inicial", true);
        setLayout(new BorderLayout(8, 8));
        setMinimumSize(new Dimension(460, 300));

        JLabel titulo = new JLabel("Selecciona tu compañero inicial", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        add(titulo, BorderLayout.NORTH);

        add(animationPanel, BorderLayout.CENTER);

        JPanel opciones = new JPanel(new GridLayout(1, 3, 8, 8));
        JButton rowlet = new JButton("Rowlet");
        JButton cyndaquil = new JButton("Cyndaquil");
        JButton oshawott = new JButton("Oshawott");
        rowlet.addActionListener(e -> elegir("Rowlet"));
        cyndaquil.addActionListener(e -> elegir("Cyndaquil"));
        oshawott.addActionListener(e -> elegir("Oshawott"));
        opciones.add(rowlet);
        opciones.add(cyndaquil);
        opciones.add(oshawott);
        add(opciones, BorderLayout.SOUTH);

        Timer t = new Timer(40, e -> {
            phase += 1;
            animationPanel.repaint();
        });
        t.start();
        pack();
        setLocationRelativeTo(owner);
    }

    private void elegir(String nombre) {
        selected = nombre;
        dispose();
    }

    private class AnimationPanel extends JPanel {
        private final Map<String, BufferedImage> spriteCache = new HashMap<>();

        private String normalizeSpeciesName(String s) {
            if (s == null)
                return null;
            String n = Normalizer.normalize(s, Normalizer.Form.NFD);
            n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            n = n.toLowerCase().trim();
            n = n.replaceAll("\\s+", "_");
            n = n.replaceAll("[^a-z0-9_\\-]", "");
            return n;
        }

        private BufferedImage loadStarterFrame(String specie) {
            if (specie == null || specie.isBlank())
                return null;
            String key = normalizeSpeciesName(specie);
            if (spriteCache.containsKey(key))
                return spriteCache.get(key);
            try {
                SpriteSheet sheet = ManejadorArchivos.withDefaultRoot().loadSpriteSheet(key + ".png", 64, 64);
                BufferedImage frame = sheet.getTileOrNull(0, 0); // user-facing frame (column 0)
                spriteCache.put(key, frame);
                return frame;
            } catch (Exception ignored) {
                spriteCache.put(key, null);
                return null;
            }
        }

        AnimationPanel() {
            setPreferredSize(new Dimension(420, 160));
            setOpaque(true);
            setBackground(new Color(238, 238, 238));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int cy = getHeight() / 2;
            int cx1 = getWidth() / 6;
            int cx2 = getWidth() / 2;
            int cx3 = getWidth() * 5 / 6;
            int a1 = (int) (Math.sin(phase * 0.08) * 6);
            int a2 = (int) (Math.sin(phase * 0.08 + 1.6) * 6);
            int a3 = (int) (Math.sin(phase * 0.08 + 3.2) * 6);

            BufferedImage s1 = loadStarterFrame("Rowlet");
            BufferedImage s2 = loadStarterFrame("Cyndaquil");
            BufferedImage s3 = loadStarterFrame("Oshawott");

            int spriteW = (int) Math.round(64 * 1.5);
            int spriteH = (int) Math.round(64 * 1.5);

            if (s1 != null) {
                g.drawImage(s1, cx1 - spriteW / 2, cy + a1 - spriteH / 2, spriteW, spriteH, null);
            } else {
                g.setColor(new Color(129, 199, 132));
                g.fillOval(cx1 - 24, cy + a1 - 24, 48, 48);
            }
            if (s2 != null) {
                g.drawImage(s2, cx2 - spriteW / 2, cy + a2 - spriteH / 2, spriteW, spriteH, null);
            } else {
                g.setColor(new Color(255, 138, 101));
                g.fillOval(cx2 - 24, cy + a2 - 24, 48, 48);
            }
            if (s3 != null) {
                g.drawImage(s3, cx3 - spriteW / 2, cy + a3 - spriteH / 2, spriteW, spriteH, null);
            } else {
                g.setColor(new Color(100, 181, 246));
                g.fillOval(cx3 - 24, cy + a3 - 24, 48, 48);
            }
        }
    }

    public static String seleccionar(JFrame owner) {
        SeleccionInicialDialog dlg = new SeleccionInicialDialog(owner);
        dlg.setVisible(true);
        return dlg.selected;
    }
}
