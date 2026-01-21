package com.mycompany.nadim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

final class RegistroUsuarioPanel extends JPanel {
    interface Listener {
        void onAliasSeleccionado(String alias);

        void onCancelar();
    }

    private final DefaultListModel<String> modeloUsuarios = new DefaultListModel<>();
    private final JList<String> listaUsuarios = new JList<>(modeloUsuarios);
    private final JTextField campoNuevo = new JTextField(18);
    private final Listener listener;

    RegistroUsuarioPanel(Listener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
        setLayout(new BorderLayout(12, 12));
        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearContenidoCentral(), BorderLayout.CENTER);
        add(crearControlesInferiores(), BorderLayout.SOUTH);
        recargarUsuarios();
    }

    private JComponent crearEncabezado() {
        JLabel titulo = new JLabel("Selecciona tu usuario o registra uno nuevo");
        titulo.setHorizontalAlignment(JLabel.CENTER);
        titulo.setPreferredSize(new Dimension(360, 40));
        return titulo;
    }

    private JComponent crearContenidoCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        listaUsuarios.setVisibleRowCount(8);
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(listaUsuarios);
        scroll.setPreferredSize(new Dimension(260, 150));

        panel.add(new JLabel("Usuarios registrados:"), gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scroll, gbc);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Registrar nuevo alias:"), gbc);

        gbc.gridy = 3;
        panel.add(campoNuevo, gbc);

        JButton registrar = new JButton("Registrar");
        registrar.addActionListener(e -> registrarNuevoAlias());
        gbc.gridy = 4;
        panel.add(registrar, gbc);

        JButton refrescar = new JButton("Actualizar lista");
        refrescar.addActionListener(e -> recargarUsuarios());
        gbc.gridy = 5;
        panel.add(refrescar, gbc);

        return panel;
    }

    private JComponent crearControlesInferiores() {
        JPanel panel = new JPanel();
        JButton usar = new JButton("Continuar con seleccionado");
        usar.addActionListener(e -> usarSeleccionado());

        JButton cancelar = new JButton("Salir");
        cancelar.addActionListener(e -> listener.onCancelar());

        panel.add(usar);
        panel.add(cancelar);
        return panel;
    }

    private void registrarNuevoAlias() {
        String alias = campoNuevo.getText().trim();
        if (alias.isEmpty()) {
            mostrarAviso("Ingresa un alias antes de registrar.");
            return;
        }
        if (!UsuarioStorage.esAliasValido(alias)) {
            mostrarAviso("Usa de 3 a 24 caracteres alfanumericos, guion o guion bajo.");
            return;
        }
        try {
            UsuarioStorage.registrarAlias(alias);
            campoNuevo.setText("");
            recargarUsuarios();
            listener.onAliasSeleccionado(alias);
        } catch (IllegalStateException ex) {
            mostrarAviso(ex.getMessage());
        } catch (IOException ex) {
            mostrarAviso("No se pudo registrar el usuario.\n" + ex.getMessage());
        }
    }

    private void usarSeleccionado() {
        String alias = listaUsuarios.getSelectedValue();
        if (alias == null || alias.isBlank()) {
            mostrarAviso("Selecciona un usuario de la lista.");
            return;
        }
        listener.onAliasSeleccionado(alias);
    }

    private void recargarUsuarios() {
        modeloUsuarios.clear();
        try {
            List<String> usuarios = UsuarioStorage.listarUsuarios();
            for (String usuario : usuarios) {
                modeloUsuarios.addElement(usuario);
            }
        } catch (IOException ex) {
            mostrarAviso("No se pudo cargar la lista de usuarios.\n" + ex.getMessage());
        }
        if (!modeloUsuarios.isEmpty()) {
            listaUsuarios.setSelectedIndex(0);
        }
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
