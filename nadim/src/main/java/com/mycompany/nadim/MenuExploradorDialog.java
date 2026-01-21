package com.mycompany.nadim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

public class MenuExploradorDialog extends JDialog {
    public interface GuardarPartidaHandler {
        void guardarPartida(String etiqueta) throws Exception;
    }

    public interface ToastHandler {
        void showToast(String mensaje);
    }

    private final Explorador explorador;
    private final GuardarPartidaHandler guardarHandler;
    private final ToastHandler toastHandler;
    private final Runnable exitToMainHandler;
    private DefaultTableModel inventarioModel;
    private JLabel inventarioResumen;
    private DefaultTableModel equipoModel;
    private JLabel equipoResumen;

    public MenuExploradorDialog(JFrame owner, Explorador explorador, GuardarPartidaHandler guardarHandler,
            ToastHandler toastHandler, Runnable exitToMainHandler) {
        super(owner, "Menu de explorador", true);
        this.explorador = explorador;
        this.guardarHandler = guardarHandler;
        this.toastHandler = toastHandler;
        this.exitToMainHandler = exitToMainHandler;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Inventario", crearPanelInventario());
        pestanas.addTab("Equipo", crearPanelEquipo(explorador.getEquipo()));
        pestanas.addTab("Pokedex", crearPanelPokedex(explorador.getPokedex()));
        pestanas.addTab("Artesania", crearPanelArtesania());
        add(pestanas, BorderLayout.CENTER);

        JLabel leyenda = new JLabel("Pulsa ESC para volver al mapa", SwingConstants.CENTER);
        leyenda.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JButton guardar = new JButton("Guardar partida");
        guardar.addActionListener(e -> guardarPartida());
        guardar.setEnabled(guardarHandler != null);

        JButton salirMenu = new JButton("Salir al menu principal");
        salirMenu.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que quieres salir al menú principal? Se perderán los cambios no guardados.",
                    "Salir al menu principal",
                    JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                dispose();
                if (exitToMainHandler != null) {
                    exitToMainHandler.run();
                }
            }
        });

        JPanel inferior = new JPanel(new BorderLayout(8, 0));
        inferior.add(leyenda, BorderLayout.CENTER);
        JPanel botones = new JPanel();
        botones.add(salirMenu);
        botones.add(guardar);
        inferior.add(botones, BorderLayout.EAST);
        add(inferior, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(540, 380));
        pack();

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void guardarPartida() {
        if (guardarHandler == null) {
            return;
        }
        String etiqueta = JOptionPane.showInputDialog(
                this,
                "Nombre del guardado (opcional):",
                "Guardar partida",
                JOptionPane.QUESTION_MESSAGE);
        if (etiqueta == null) {
            return;
        }
        try {
            guardarHandler.guardarPartida(etiqueta);
            JOptionPane.showMessageDialog(this,
                    "Partida guardada correctamente.",
                    "Guardar",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la partida." + System.lineSeparator() + ex.getMessage(),
                    "Guardar",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new BorderLayout());
        inventarioModel = new DefaultTableModel(new Object[] { "Objeto", "Categoria", "Cantidad" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(inventarioModel);
        tabla.setFillsViewportHeight(true);
        tabla.setEnabled(false);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        inventarioResumen = new JLabel("", SwingConstants.LEFT);
        inventarioResumen.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JButton usarPocion = new JButton("Usar pocion");
        usarPocion.addActionListener(e -> usarPocion());

        JPanel inferior = new JPanel(new BorderLayout(8, 0));
        inferior.add(inventarioResumen, BorderLayout.CENTER);
        inferior.add(usarPocion, BorderLayout.EAST);
        panel.add(inferior, BorderLayout.SOUTH);

        recargarInventario();
        return panel;
    }

    private JPanel crearPanelEquipo(EquipoPokemon equipo) {
        JPanel panel = new JPanel(new BorderLayout());
        equipoModel = new DefaultTableModel(new Object[] { "Pokemon", "HP", "Estado" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(equipoModel);
        tabla.setFillsViewportHeight(true);
        tabla.setEnabled(false);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        equipoResumen = new JLabel("", SwingConstants.RIGHT);
        equipoResumen.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.add(equipoResumen, BorderLayout.SOUTH);

        recargarEquipo();
        return panel;
    }

    private JPanel crearPanelPokedex(Pokedex pokedex) {
        JPanel panel = new JPanel(new BorderLayout());
        List<EntradaInvestigacion> entradas = new ArrayList<>(pokedex.getEntradas());
        entradas.sort(Comparator.comparing(EntradaInvestigacion::getNombreOficial, String.CASE_INSENSITIVE_ORDER));
        String[] columnas = { "Especie", "Investigacion", "Estado" };
        String[][] datos = new String[entradas.size()][columnas.length];
        for (int i = 0; i < entradas.size(); i++) {
            EntradaInvestigacion entrada = entradas.get(i);
            datos[i][0] = entrada.getNombreVisible();
            datos[i][1] = Integer.toString(entrada.getNivelInvestigacion());
            if (!entrada.estaDescubierta()) {
                datos[i][2] = "No descubierto";
            } else {
                datos[i][2] = entrada.estaCompleta() ? "Completo" : "En progreso";
            }
        }
        JTable tabla = new JTable(datos, columnas);
        tabla.setFillsViewportHeight(true);
        tabla.setEnabled(false);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JLabel resumen = new JLabel("Especies completas: " + pokedex.getEspeciesCompletadas(), SwingConstants.RIGHT);
        resumen.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        panel.add(resumen, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelArtesania() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        DefaultListModel<RecetaArtesania> modelo = new DefaultListModel<>();
        List<RecetaArtesania> recetas = new ArrayList<>(explorador.getTaller().getRecetasDisponibles());
        recetas.sort(Comparator.comparing(RecetaArtesania::getNombreResultado));
        for (RecetaArtesania receta : recetas) {
            modelo.addElement(receta);
        }

        JList<RecetaArtesania> lista = new JList<>(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel etiqueta = new JLabel(value.getNombreResultado());
            etiqueta.setOpaque(true);
            if (isSelected) {
                etiqueta.setBackground(list.getSelectionBackground());
                etiqueta.setForeground(list.getSelectionForeground());
            } else {
                etiqueta.setBackground(list.getBackground());
                etiqueta.setForeground(list.getForeground());
            }
            etiqueta.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return etiqueta;
        });

        JTextArea detalle = new JTextArea();
        detalle.setEditable(false);
        detalle.setWrapStyleWord(true);
        detalle.setLineWrap(true);
        detalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Detalles"),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        lista.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                RecetaArtesania seleccion = lista.getSelectedValue();
                detalle.setText(seleccion == null ? "" : describirReceta(seleccion));
            }
        });

        if (!modelo.isEmpty()) {
            lista.setSelectedIndex(0);
            detalle.setText(describirReceta(modelo.getElementAt(0)));
        }

        JButton fabricar = new JButton("Fabricar");
        fabricar.addActionListener(e -> {
            RecetaArtesania seleccion = lista.getSelectedValue();
            if (seleccion == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una receta", "Artesania",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Inventario inventario = explorador.getInventario();
            if (!seleccion.esAplicable(inventario)) {
                JOptionPane.showMessageDialog(this,
                        "Faltan materiales para fabricar " + seleccion.getNombreResultado() + ".",
                        "Artesania",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean exito = explorador.fabricar(seleccion.getNombreResultado());
            if (exito) {
                if (toastHandler != null)
                    toastHandler.showToast("Fabricaste " + seleccion.getNombreResultado() + ".");
            } else {
                if (toastHandler != null)
                    toastHandler.showToast("No hay espacio suficiente en el inventario.");
            }
            recargarInventario();
        });

        JPanel contenidoLista = new JPanel(new BorderLayout());
        contenidoLista.add(new JScrollPane(lista), BorderLayout.CENTER);
        contenidoLista.setPreferredSize(new Dimension(200, 0));

        JPanel panelBoton = new JPanel(new BorderLayout());
        panelBoton.add(fabricar, BorderLayout.EAST);

        panel.add(contenidoLista, BorderLayout.WEST);
        panel.add(detalle, BorderLayout.CENTER);
        panel.add(panelBoton, BorderLayout.SOUTH);
        return panel;
    }

    private void recargarInventario() {
        if (inventarioModel == null || inventarioResumen == null) {
            return;
        }
        Inventario inventario = explorador.getInventario();
        inventarioModel.setRowCount(0);
        List<Objeto> objetos = new ArrayList<>(inventario.getObjetos().values());
        objetos.sort(Comparator.comparing(Objeto::getNombre));
        for (Objeto objeto : objetos) {
            inventarioModel.addRow(new Object[] {
                    objeto.getNombre(),
                    objeto.getCategoria(),
                    objeto.getCantidad()
            });
        }
        inventarioResumen
                .setText("Capacidad " + inventario.getOcupacionActual() + "/" + inventario.getCapacidadMaxima());
    }

    private void recargarEquipo() {
        if (equipoModel == null || equipoResumen == null) {
            return;
        }
        EquipoPokemon equipo = explorador.getEquipo();
        equipoModel.setRowCount(0);
        for (Pokemon pokemon : equipo.getMiembros()) {
            equipoModel.addRow(new Object[] {
                    pokemon.getEspecie(),
                    pokemon.getSaludActual() + " / " + pokemon.getSaludMaxima(),
                    pokemon.estaDebilitado() ? "Debilitado" : "Listo"
            });
        }
        equipoResumen.setText("Miembros " + equipo.getMiembros().size() + "/" + equipo.getCupoMaximo());
    }

    private String describirReceta(RecetaArtesania receta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resultado: ").append(receta.getNombreResultado()).append('\n');
        sb.append("Materiales requeridos:\n");
        receta.obtenerCosto().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sb.append(" - ")
                        .append(entry.getValue())
                        .append(" x ")
                        .append(entry.getKey())
                        .append('\n'));
        return sb.toString();
    }

    private void usarPocion() {
        Inventario inventario = explorador.getInventario();
        List<ObjetoCura> pociones = inventario.getObjetos().values().stream()
                .filter(obj -> obj instanceof ObjetoCura && obj.getCantidad() > 0)
                .map(obj -> (ObjetoCura) obj)
                .sorted(Comparator.comparing(Objeto::getNombre))
                .collect(Collectors.toList());
        if (pociones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No tienes pociones disponibles.",
                    "Inventario",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] opcionesPociones = new String[pociones.size()];
        for (int i = 0; i < pociones.size(); i++) {
            ObjetoCura pocion = pociones.get(i);
            String descripcion;
            if (pocion instanceof PocionCurativa) {
                PocionCurativa curativa = (PocionCurativa) pocion;
                descripcion = curativa.esRevivir()
                        ? "[Revive]"
                        : "+" + curativa.getPuntosCuracion() + " HP";
            } else {
                descripcion = "[Curacion]";
            }
            opcionesPociones[i] = pocion.getNombre() + " (" + pocion.getCantidad() + ") " + descripcion;
        }

        int idxPocion = JOptionPane.showOptionDialog(
                this,
                "Selecciona la pocion a usar",
                "Inventario",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcionesPociones,
                opcionesPociones[0]);
        if (idxPocion < 0) {
            return;
        }

        List<Pokemon> equipo = new ArrayList<>(explorador.getEquipo().getMiembros());
        if (equipo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No tienes Pokemon en el equipo.",
                    "Inventario",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] opcionesPokemon = new String[equipo.size()];
        for (int i = 0; i < equipo.size(); i++) {
            Pokemon pokemon = equipo.get(i);
            opcionesPokemon[i] = pokemon.getEspecie() + " (" + pokemon.getSaludActual() + "/" + pokemon.getSaludMaxima()
                    + ")";
        }

        int idxPokemon = JOptionPane.showOptionDialog(
                this,
                "Selecciona el Pokemon objetivo",
                "Inventario",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcionesPokemon,
                opcionesPokemon[0]);
        if (idxPokemon < 0) {
            return;
        }

        ObjetoCura pocionSeleccionada = pociones.get(idxPocion);
        Pokemon objetivo = equipo.get(idxPokemon);
        boolean aplicado = explorador.usarPocion(pocionSeleccionada, objetivo);
        if (aplicado) {
            if (toastHandler != null)
                toastHandler.showToast("El Pokemon se recupera.");
            recargarInventario();
            recargarEquipo();
        } else {
            if (toastHandler != null)
                toastHandler.showToast("La pocion no surte efecto.");
        }
    }
}
