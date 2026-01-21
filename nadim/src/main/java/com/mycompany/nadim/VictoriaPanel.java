package com.mycompany.nadim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class VictoriaPanel extends JDialog {
    public interface Listener {
        void onSaveAndExit();

        void onExit();

        void onContinue();
    }

    public VictoriaPanel(java.awt.Window owner, String especie, Listener listener) {
        super(owner);
        setModal(true);
        setTitle("Victoria Legendaria");
        setSize(600, 300);
        setLocationRelativeTo(owner);

        JLabel title = new JLabel("¡Has completado el encuentro legendario!", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(33, 150, 243));

        JLabel subtitle = new JLabel("" + especie + " ha sido investigado completamente.", SwingConstants.CENTER);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));

        JPanel center = new JPanel(new BorderLayout());
        center.add(title, BorderLayout.CENTER);
        center.add(subtitle, BorderLayout.SOUTH);

        JButton saveExit = new JButton("Guardar y salir");
        saveExit.addActionListener((ActionEvent e) -> {
            if (listener != null)
                listener.onSaveAndExit();
            dispose();
        });

        JButton exitNoSave = new JButton("Salir sin guardar");
        exitNoSave.addActionListener((ActionEvent e) -> {
            if (listener != null)
                listener.onExit();
            dispose();
        });

        JButton cont = new JButton("Continuar jugando");
        cont.addActionListener((ActionEvent e) -> {
            if (listener != null)
                listener.onContinue();
            dispose();
        });

        JPanel buttons = new JPanel();
        buttons.add(saveExit);
        buttons.add(exitNoSave);
        buttons.add(cont);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }
}
