package com.mycompany.nadim;

import java.awt.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import javax.swing.JOptionPane;

final class UsuarioStorage {
    private static final Path BASE_DIR = Paths.get("data");
    private static final Path USUARIOS_FILE = BASE_DIR.resolve("usuarios.txt");
    private static final String NUEVO_USUARIO_OPCION = "Registrar usuario nuevo";

    private UsuarioStorage() {
    }

    static String solicitarAlias(Component parent) {
        while (true) {
            List<String> usuarios;
            try {
                usuarios = cargarUsuarios();
            } catch (IOException e) {
                mostrarError(parent,
                        "No se pudo cargar la lista de usuarios." + System.lineSeparator() + e.getMessage());
                return null;
            }
            if (usuarios.isEmpty()) {
                String alias = solicitarAliasNuevo(parent);
                if (alias == null) {
                    return null;
                }
                if (registrarSiEsNuevo(parent, alias)) {
                    return alias;
                }
            } else {
                List<String> opciones = new ArrayList<>();
                opciones.add(NUEVO_USUARIO_OPCION);
                opciones.addAll(usuarios);
                Object seleccion = JOptionPane.showInputDialog(
                        parent,
                        "Selecciona un usuario registrado o crea uno nuevo:",
                        "Seleccion de usuario",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opciones.toArray(),
                        opciones.get(0));
                if (seleccion == null) {
                    return null;
                }
                if (NUEVO_USUARIO_OPCION.equals(seleccion)) {
                    String alias = solicitarAliasNuevo(parent);
                    if (alias == null) {
                        continue;
                    }
                    if (registrarSiEsNuevo(parent, alias)) {
                        return alias;
                    }
                } else {
                    return seleccion.toString();
                }
            }
        }
    }

    static void registrarExploracion(String alias, String resumen) throws IOException {
        Objects.requireNonNull(alias, "alias");
        Objects.requireNonNull(resumen, "resumen");
        Path historial = historialPath(alias);
        ensureBaseDir();
        Files.writeString(historial, resumen + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    static List<String> listarUsuarios() throws IOException {
        return Collections.unmodifiableList(cargarUsuarios());
    }

    static void registrarAlias(String alias) throws IOException {
        registrarUsuario(alias);
    }

    static boolean esAliasValido(String alias) {
        if (alias == null) {
            return false;
        }
        return alias.matches("[A-Za-z0-9_-]{3,24}");
    }

    static List<String> leerHistorial(String alias) throws IOException {
        Objects.requireNonNull(alias, "alias");
        Path historial = historialPath(alias);
        if (!Files.exists(historial)) {
            return Collections.emptyList();
        }
        return Files.readAllLines(historial, StandardCharsets.UTF_8);
    }

    private static Path historialPath(String alias) {
        String normalizado = alias.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_");
        return BASE_DIR.resolve("historial_" + normalizado + ".txt");
    }

    private static boolean registrarSiEsNuevo(Component parent, String alias) {
        try {
            registrarUsuario(alias);
            return true;
        } catch (IllegalStateException e) {
            mostrarError(parent, e.getMessage());
        } catch (IOException e) {
            mostrarError(parent, "No se pudo registrar el usuario." + System.lineSeparator() + e.getMessage());
        }
        return false;
    }

    private static String solicitarAliasNuevo(Component parent) {
        while (true) {
            String alias = JOptionPane.showInputDialog(parent, "Ingresa un nombre de usuario:", "Nuevo usuario",
                    JOptionPane.QUESTION_MESSAGE);
            if (alias == null) {
                return null;
            }
            alias = alias.trim();
            if (alias.isEmpty()) {
                mostrarError(parent, "El nombre no puede estar vacio.");
                continue;
            }
            if (!esAliasValido(alias)) {
                mostrarError(parent, "Usa de 3 a 24 caracteres alfanumericos, guion o guion bajo.");
                continue;
            }
            return alias;
        }
    }

    private static void registrarUsuario(String alias) throws IOException {
        Objects.requireNonNull(alias, "alias");
        ensureBaseDir();
        LinkedHashSet<String> usuarios = new LinkedHashSet<>(cargarUsuariosSeguro());
        for (String existente : usuarios) {
            if (existente.equalsIgnoreCase(alias)) {
                throw new IllegalStateException("El usuario ya existe.");
            }
        }
        Files.writeString(USUARIOS_FILE, alias + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static List<String> cargarUsuarios() throws IOException {
        ensureBaseDir();
        return cargarUsuariosSeguro();
    }

    private static List<String> cargarUsuariosSeguro() throws IOException {
        if (!Files.exists(USUARIOS_FILE)) {
            return new ArrayList<>();
        }
        List<String> lineas = Files.readAllLines(USUARIOS_FILE, StandardCharsets.UTF_8);
        List<String> usuarios = new ArrayList<>();
        for (String linea : lineas) {
            String limpio = linea.trim();
            if (!limpio.isEmpty()) {
                usuarios.add(limpio);
            }
        }
        Collections.sort(usuarios, String.CASE_INSENSITIVE_ORDER);
        return usuarios;
    }

    private static void ensureBaseDir() throws IOException {
        if (!Files.exists(BASE_DIR)) {
            Files.createDirectories(BASE_DIR);
        }
    }

    private static void mostrarError(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
