package com.mycompany.nadim;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MenuPrincipalPanel extends JPanel {
    public interface AccionesMenu {
        void iniciarJuego();

        void cargarPartida();

        void mostrarAyuda();

        void mostrarAcercaDe();

        void mostrarHistorial();

        void salir();
    }

    public MenuPrincipalPanel(AccionesMenu accionesMenu) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Leyendas Pokémon: Arceus - Demo", SwingConstants.CENTER);
        titulo.setPreferredSize(new Dimension(320, 40));
        gbc.gridy = 0;
        add(titulo, gbc);

        JButton iniciar = new JButton("Iniciar expedición");
        iniciar.addActionListener(e -> accionesMenu.iniciarJuego());
        gbc.gridy = 1;
        add(iniciar, gbc);

        JButton cargar = new JButton("Cargar partida");
        cargar.addActionListener(e -> accionesMenu.cargarPartida());
        gbc.gridy = 2;
        add(cargar, gbc);

        JButton ayuda = new JButton("Ayuda del juego");
        ayuda.addActionListener(e -> accionesMenu.mostrarAyuda());
        gbc.gridy = 3;
        add(ayuda, gbc);

        JButton acercaDe = new JButton("Acerca de");
        acercaDe.addActionListener(e -> accionesMenu.mostrarAcercaDe());
        gbc.gridy = 4;
        add(acercaDe, gbc);

        JButton historial = new JButton("Historial de exploraciones");
        historial.addActionListener(e -> accionesMenu.mostrarHistorial());
        gbc.gridy = 5;
        add(historial, gbc);

        JButton salir = new JButton("Salir");
        salir.addActionListener(e -> accionesMenu.salir());
        gbc.gridy = 6;
        add(salir, gbc);
    }
}
