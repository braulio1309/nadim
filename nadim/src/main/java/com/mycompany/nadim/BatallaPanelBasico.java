package com.mycompany.nadim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class BatallaPanelBasico extends BatallaPanelBase {
    public enum Resultado {
        VICTORIA,
        DERROTA,
        HUIDA,
        CAPTURA
    }

    public static class ResultadoCombate {
        private final Resultado tipo;
        private final Pokemon capturado;
        private final String lostItemName;
        private final int lostItemQty;
        private final boolean legendaryCompleted;
        private final String legendarySpecies;

        public ResultadoCombate(Resultado tipo, Pokemon capturado) {
            this(tipo, capturado, null, 0, false, null);
        }

        public ResultadoCombate(Resultado tipo, Pokemon capturado, String lostItemName, int lostItemQty,
                boolean legendaryCompleted, String legendarySpecies) {
            this.tipo = tipo;
            this.capturado = capturado;
            this.lostItemName = lostItemName;
            this.lostItemQty = lostItemQty;
            this.legendaryCompleted = legendaryCompleted;
            this.legendarySpecies = legendarySpecies;
        }

        public Resultado getTipo() {
            return tipo;
        }

        public Pokemon getCapturado() {
            return capturado;
        }

        public String getLostItemName() {
            return lostItemName;
        }

        public int getLostItemQty() {
            return lostItemQty;
        }

        public boolean isLegendaryCompleted() {
            return legendaryCompleted;
        }

        public String getLegendarySpecies() {
            return legendarySpecies;
        }
    }

    private final Explorador explorador;
    private final EquipoPokemon equipo;
    private final Inventario inventario;
    private Pokemon activo;
    private final Pokemon enemigo;
    private Movimiento[] movimientosJugador;
    private final Movimiento[] movimientosEnemigo;
    private final Consumer<ResultadoCombate> onFinish;
    private final Random random = new Random();
    private final ServicioCaptura servicioCaptura = new ServicioCaptura(0.35);

    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel jugadorNombreLabel = new JLabel();
    private final JLabel jugadorHpLabel = new JLabel();
    private final JLabel enemigoHpLabel = new JLabel();
    private final JButton[] botonesMovimiento = new JButton[4];
    private final JButton botonCapturar = new JButton("Capturar");
    private final JButton botonCambiar = new JButton("Cambiar");
    private final JButton botonHuir = new JButton("Huir");
    private final JButton botonContinuar = new JButton("Continuar");
    private final CampoPanel campoPanel = new CampoPanel();
    private final ToastManager toast;

    private boolean batallaActiva = true;
    private Resultado resultadoFinal = Resultado.VICTORIA;
    private Pokemon pokemonCapturado;
    private boolean controlesBloqueados = false;
    private boolean capturaRealizada = false;
    private String penalizedItemName;
    private int penalizedItemQty;
    private boolean legendaryCompleted = false;
    private String legendarySpecies = null;

    public BatallaPanelBasico(Explorador explorador, Pokemon enemigo, Consumer<ResultadoCombate> onFinish,
            ToastManager toast) {
        this.explorador = explorador;
        this.equipo = explorador.getEquipo();
        this.inventario = explorador.getInventario();
        this.activo = equipo.seleccionarTitular();
        if (activo == null) {
            throw new IllegalStateException("El equipo no tiene un pokemon disponible");
        }
        this.enemigo = enemigo;
        this.onFinish = onFinish;
        this.toast = toast;
        this.movimientosJugador = prepararMovimientos(activo.getMovimientos(), activo.getEspecie() + " move");
        this.movimientosEnemigo = prepararMovimientos(enemigo.getMovimientos(), enemigo.getEspecie() + " move");

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(640, 480));
        setBackground(Color.BLACK);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 18f));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(40, 40, 40));
        statusLabel.setText("Un " + enemigo.getEspecie() + " salvaje aparecio!");
        add(statusLabel, BorderLayout.NORTH);

        add(crearPanelInfo(), BorderLayout.WEST);
        add(campoPanel, BorderLayout.CENTER);
        add(crearPanelControles(), BorderLayout.SOUTH);

        actualizarEstadoInterfaz();
    }

    private Movimiento[] prepararMovimientos(Movimiento[] base, String nombreGenerico) {
        Movimiento[] movimientos = base;
        if (movimientos == null || movimientos.length == 0) {
            movimientos = new Movimiento[] { new Movimiento(nombreGenerico, 12, 30) };
        }
        if (movimientos.length < 4) {
            Movimiento[] extendido = new Movimiento[4];
            for (int i = 0; i < movimientos.length; i++) {
                extendido[i] = movimientos[i];
            }
            for (int i = movimientos.length; i < extendido.length; i++) {
                extendido[i] = new Movimiento(nombreGenerico + " " + (i + 1), 10, 25);
            }
            movimientos = extendido;
        }
        return movimientos;
    }

    private JPanel crearPanelInfo() {
        JPanel contenedor = new JPanel(new GridLayout(2, 1, 0, 8));
        contenedor.setOpaque(false);

        jugadorNombreLabel.setText(activo.getEspecie());
        jugadorNombreLabel.setForeground(Color.WHITE);
        jugadorHpLabel.setForeground(Color.WHITE);

        JPanel jugadorPanel = new JPanel(new GridLayout(2, 1));
        jugadorPanel.setOpaque(false);
        jugadorPanel.add(jugadorNombreLabel);
        jugadorPanel.add(jugadorHpLabel);

        JLabel enemigoNombre = new JLabel(enemigo.getEspecie(), SwingConstants.LEFT);
        enemigoNombre.setForeground(Color.WHITE);
        enemigoHpLabel.setForeground(Color.WHITE);

        JPanel enemigoPanel = new JPanel(new GridLayout(2, 1));
        enemigoPanel.setOpaque(false);
        enemigoPanel.add(enemigoNombre);
        enemigoPanel.add(enemigoHpLabel);

        contenedor.add(jugadorPanel);
        contenedor.add(enemigoPanel);
        return contenedor;
    }

    private JPanel crearPanelControles() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);

        JPanel movimientosPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        movimientosPanel.setOpaque(false);

        for (int i = 0; i < botonesMovimiento.length; i++) {
            final int idx = i;
            JButton boton = new JButton();
            boton.addActionListener(e -> ejecutarTurnoJugador(idx));
            botonesMovimiento[i] = boton;
            movimientosPanel.add(boton);
        }

        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        accionesPanel.setOpaque(false);
        botonCapturar.addActionListener(e -> intentarCaptura());
        botonCambiar.addActionListener(e -> cambiarPokemon());
        botonHuir.addActionListener(e -> finalizarBatalla(Resultado.HUIDA, "Escapaste sin pelear."));
        botonContinuar.addActionListener(e -> {
            if (onFinish != null) {
                onFinish.accept(new ResultadoCombate(resultadoFinal, pokemonCapturado, penalizedItemName,
                        penalizedItemQty, legendaryCompleted, legendarySpecies));
            }
        });
        botonContinuar.setVisible(false);
        accionesPanel.add(botonCapturar);
        accionesPanel.add(botonCambiar);
        accionesPanel.add(botonHuir);
        accionesPanel.add(botonContinuar);

        contenedor.add(movimientosPanel, BorderLayout.CENTER);
        contenedor.add(accionesPanel, BorderLayout.SOUTH);
        return contenedor;
    }

    private void ejecutarTurnoJugador(int idx) {
        if (!batallaActiva) {
            return;
        }
        Movimiento movimiento = movimientosJugador[idx];
        if (movimiento.getUsosRestantes() <= 0) {
            statusLabel.setText(movimiento.getNombre() + " no tiene PP suficientes!");
            return;
        }
        bloquearControles(true);
        int dano = movimiento.ejecutar(activo, enemigo);
        statusLabel.setText(activo.getEspecie() + " usa " + movimiento.getNombre() + "! Dano " + dano + ".");
        actualizarEstadoInterfaz();
        if (enemigo.estaDebilitado()) {
            finalizarBatalla(Resultado.VICTORIA, "El " + enemigo.getEspecie() + " se debilito.");
            return;
        }
        Timer turnoEnemigo = new Timer(650, e -> ejecutarTurnoEnemigo());
        turnoEnemigo.setRepeats(false);
        turnoEnemigo.start();
    }

    private void ejecutarTurnoEnemigo() {
        if (!batallaActiva) {
            return;
        }
        Movimiento movimiento = seleccionarMovimientoEnemigo();
        if (movimiento.getUsosRestantes() <= 0) {
            movimiento.restaurarUsos();
        }
        int dano = movimiento.ejecutar(enemigo, activo);
        statusLabel.setText(enemigo.getEspecie() + " usa " + movimiento.getNombre() + "! Dano " + dano + ".");
        actualizarEstadoInterfaz();
        if (activo.estaDebilitado()) {
            Pokemon reemplazo = equipo.getMiembros().stream()
                    .filter(p -> p != activo && !p.estaDebilitado())
                    .findFirst()
                    .orElse(null);
            if (reemplazo == null) {
                finalizarBatalla(Resultado.DERROTA, activo.getEspecie() + " se debilito.");
                return;
            }
            setActivo(reemplazo);
            statusLabel.setText("Tu " + activo.getEspecie() + " entra al combate!");
        }
        bloquearControles(false);
    }

    private void cambiarPokemon() {
        if (!batallaActiva || controlesBloqueados) {
            return;
        }
        List<Pokemon> disponibles = equipo.getMiembros().stream()
                .filter(p -> p != activo && !p.estaDebilitado())
                .collect(Collectors.toList());
        if (disponibles.isEmpty()) {
            statusLabel.setText("No hay otro pokemon disponible.");
            return;
        }
        String[] opciones = new String[disponibles.size()];
        for (int i = 0; i < disponibles.size(); i++) {
            Pokemon p = disponibles.get(i);
            opciones[i] = p.getEspecie() + " (HP " + p.getSaludActual() + "/" + p.getSaludMaxima() + ")";
        }
        int seleccion = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(this),
                "Elige un pokemon",
                "Cambiar pokemon",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]);
        if (seleccion < 0) {
            return;
        }
        Pokemon elegido = disponibles.get(seleccion);
        bloquearControles(true);
        setActivo(elegido);
        statusLabel.setText("Adelante, " + activo.getEspecie() + "!");
        Timer turnoEnemigo = new Timer(650, e -> ejecutarTurnoEnemigo());
        turnoEnemigo.setRepeats(false);
        turnoEnemigo.start();
    }

    private Movimiento seleccionarMovimientoEnemigo() {
        for (int intentos = 0; intentos < movimientosEnemigo.length; intentos++) {
            Movimiento candidatos = movimientosEnemigo[random.nextInt(movimientosEnemigo.length)];
            if (candidatos.getUsosRestantes() > 0) {
                return candidatos;
            }
        }
        return movimientosEnemigo[0];
    }

    private void bloquearControles(boolean deshabilitarBotones) {
        controlesBloqueados = deshabilitarBotones;
        for (int i = 0; i < botonesMovimiento.length; i++) {
            JButton boton = botonesMovimiento[i];
            Movimiento movimiento = movimientosJugador[i];
            boton.setEnabled(!controlesBloqueados && movimiento.getUsosRestantes() > 0 && batallaActiva);
        }
        boolean puedeCambiar = equipo.getMiembros().stream()
                .anyMatch(p -> p != activo && !p.estaDebilitado());
        botonCambiar.setEnabled(!controlesBloqueados && batallaActiva && puedeCambiar);
        botonHuir.setEnabled(!controlesBloqueados && batallaActiva);
        botonCapturar.setEnabled(!controlesBloqueados && batallaActiva && !enemigo.estaDebilitado()
                && inventario.getDisponibles("Pokeball") > 0);
    }

    private void finalizarBatalla(Resultado resultado, String mensaje) {
        if (!batallaActiva) {
            return;
        }
        batallaActiva = false;
        resultadoFinal = resultado;
        if (resultado == Resultado.VICTORIA) {
            explorador.getPokedex().registrarVictoria(enemigo.getEspecie());
            try {
                if (explorador.getPokedex().getEspeciesCompletadas() >= 5) {
                    var perfil = explorador.getPerfil();
                    var logros = perfil.getLogrosDesbloqueados();
                    if (!logros.contains("ARCEUS_UNLOCKED") && !logros.contains("ARCEUS_COMPLETED")) {
                        perfil.desbloquearLogro("ARCEUS_UNLOCKED");
                        if (toast != null) {
                            toast.show("Has desbloqueado un encuentro legendario (Arceus)!");
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (resultado == Resultado.DERROTA) {
            penalizedItemName = null;
            penalizedItemQty = 0;
            boolean lostSomething = false;
            for (Objeto obj : inventario.getObjetos().values()) {
                if (!(obj instanceof ObjetoMaterial)) {
                    if (inventario.consumir(obj.getNombre(), 1)) {
                        penalizedItemName = obj.getNombre();
                        penalizedItemQty = 1;
                        lostSomething = true;
                        if (toast != null) {
                            toast.show("Perdiste 1x " + obj.getNombre() + ".");
                        }
                    }
                    break;
                }
            }
            if (!lostSomething && toast != null) {
                toast.show("No habia objetos crafteados para perder.");
            }
        }
        legendaryCompleted = false;
        legendarySpecies = null;
        if ((resultado == Resultado.VICTORIA || resultado == Resultado.CAPTURA)
                && "Arceus".equalsIgnoreCase(enemigo.getEspecie())) {
            try {
                explorador.getPokedex().aplicarEstadoEntrada(enemigo.getEspecie(), true,
                        EntradaInvestigacion.NIVEL_COMPLETO);
                explorador.getPokedex().recalcularEspeciesCompletadas();
                legendaryCompleted = true;
                legendarySpecies = enemigo.getEspecie();
                if (toast != null) {
                    toast.show("Has completado el encuentro legendario: " + enemigo.getEspecie());
                }
                try {
                    var perfil = explorador.getPerfil();
                    perfil.desbloquearLogro("ARCEUS_COMPLETED");
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
        }
        if (resultado != Resultado.CAPTURA) {
            pokemonCapturado = null;
        }
        statusLabel.setText(mensaje);
        if (toast != null) {
            switch (resultado) {
                case VICTORIA -> toast.show("Victoria en combate.");
                case DERROTA -> toast.show("Has sido derrotado.");
                case CAPTURA -> toast.show("Capturaste a " + enemigo.getEspecie() + ".");
                case HUIDA -> toast.show("Escapaste del combate.");
            }
        }
        for (JButton boton : botonesMovimiento) {
            boton.setEnabled(false);
        }
        botonCambiar.setEnabled(false);
        botonHuir.setEnabled(false);
        botonCapturar.setEnabled(false);
        botonContinuar.setVisible(true);
        campoPanel.repaint();
        actualizarEstadoInterfaz();
        botonContinuar.requestFocusInWindow();
    }

    private void intentarCaptura() {
        if (!batallaActiva || capturaRealizada || enemigo.estaDebilitado()) {
            return;
        }
        if (inventario.getDisponibles("Pokeball") <= 0) {
            statusLabel.setText("No quedan Pokeball. Debes fabricarlas en el taller.");
            actualizarEstadoInterfaz();
            return;
        }
        bloquearControles(true);
        capturaRealizada = true;
        boolean exito = servicioCaptura.intentar(explorador, enemigo);
        if (exito) {
            enemigo.aplicarDanio(enemigo.getSaludActual());
            pokemonCapturado = enemigo;
            finalizarBatalla(Resultado.CAPTURA, "Capturaste a " + enemigo.getEspecie() + "!");
        } else {
            statusLabel.setText("El " + enemigo.getEspecie() + " se resiste a la captura!");
            Timer reanudar = new Timer(600, e -> {
                capturaRealizada = false;
                if (batallaActiva) {
                    bloquearControles(false);
                    actualizarEstadoInterfaz();
                }
            });
            reanudar.setRepeats(false);
            reanudar.start();
            actualizarEstadoInterfaz();
        }
    }

    private void setActivo(Pokemon nuevo) {
        activo = nuevo;
        movimientosJugador = prepararMovimientos(activo.getMovimientos(), activo.getEspecie() + " move");
        capturaRealizada = false;
        actualizarEstadoInterfaz();
    }

    private void actualizarEstadoInterfaz() {
        jugadorNombreLabel.setText(activo.getEspecie());
        jugadorHpLabel.setText("HP: " + activo.getSaludActual() + " / " + activo.getSaludMaxima());
        enemigoHpLabel.setText("HP: " + enemigo.getSaludActual() + " / " + enemigo.getSaludMaxima());
        for (int i = 0; i < botonesMovimiento.length; i++) {
            Movimiento movimiento = movimientosJugador[i];
            JButton boton = botonesMovimiento[i];
            boton.setText(movimiento.getNombre() + " (" + movimiento.getUsosRestantes() + ")");
            boton.setEnabled(batallaActiva && !controlesBloqueados && movimiento.getUsosRestantes() > 0);
        }
        boolean puedeCambiar = equipo.getMiembros().stream()
                .anyMatch(p -> p != activo && !p.estaDebilitado());
        botonCambiar.setEnabled(batallaActiva && !controlesBloqueados && puedeCambiar);
        botonCapturar.setEnabled(batallaActiva && !controlesBloqueados && !enemigo.estaDebilitado()
                && inventario.getDisponibles("Pokeball") > 0);
        campoPanel.repaint();
    }

    private class CampoPanel extends JPanel {
        private int phase;
        private final Timer anim = new Timer(40, e -> {
            phase++;
            repaint();
        });

        CampoPanel() {
            setPreferredSize(new Dimension(420, 360));
            setOpaque(true);
            anim.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint fondo = new GradientPaint(0, 0, new Color(100, 181, 246), 0, getHeight(),
                    new Color(200, 230, 201));
            g2.setPaint(fondo);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int playerGroundY = getHeight() - 120;
            int enemyGroundY = 200; 
            g2.setColor(new Color(255, 224, 130));
            g2.fillOval(60, playerGroundY, 200, 50);
            g2.setColor(new Color(179, 157, 219));
            g2.fillOval(getWidth() - 260, enemyGroundY, 200, 50);

            int off1 = (int) (Math.sin(phase * 0.08) * 5);
            int off2 = (int) (Math.sin(phase * 0.08 + 1.5) * 5);
            int playerSpriteY = getHeight() - 265 + off1; 
            int enemySpriteBaseY = 75; 

            BufferedImage playerSprite = loadSpeciesFrame(activo.getEspecie(), 1);
            BufferedImage enemySprite = loadSpeciesFrame(enemigo.getEspecie(), 0);

            int spriteW = (int) Math.round(110 * 1.5);
            int spriteH = (int) Math.round(90 * 1.5);

            if (playerSprite != null) {
                g2.drawImage(playerSprite, 90, playerSpriteY, spriteW, spriteH, null);
            } else {
                g2.setColor(new Color(76, 175, 80));
                g2.fillRoundRect(90, playerSpriteY, spriteW, spriteH, 20, 20);
            }

            if (enemySprite != null) {
                g2.drawImage(enemySprite, getWidth() - 220, enemySpriteBaseY + off2, spriteW, spriteH, null);
            } else {
                g2.setColor(new Color(233, 30, 99));
                g2.fillRoundRect(getWidth() - 220, enemySpriteBaseY + off2, spriteW, spriteH, 20, 20);
            }

            int anchoBarra = 160;
            double ratioJugador = activo.getSaludActual() / (double) activo.getSaludMaxima();
            double ratioEnemigo = enemigo.getSaludActual() / (double) enemigo.getSaludMaxima();

            g2.setColor(Color.BLACK);
            g2.drawRect(30, getHeight() - 140, anchoBarra, 12);
            g2.setColor(new Color(67, 160, 71));
            g2.fillRect(30, getHeight() - 140, (int) (anchoBarra * ratioJugador), 12);

            g2.setColor(Color.BLACK);
            g2.drawRect(getWidth() - 210, 40, anchoBarra, 12);
            g2.setColor(new Color(211, 47, 47));
            g2.fillRect(getWidth() - 210, 40, (int) (anchoBarra * ratioEnemigo), 12);

            g2.setColor(Color.DARK_GRAY);
            g2.drawString(activo.getEspecie(), 30, getHeight() - 150);
            g2.drawString(enemigo.getEspecie(), getWidth() - 210, 30);

            if (toast != null) {
                toast.draw(g2, getWidth(), getHeight());
            }
        }
    }
}
