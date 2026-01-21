package com.mycompany.nadim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Inventario {
    private final int capacidadMaxima;
    private int ocupacionActual;
    private final Map<String, Objeto> objetos = new HashMap<>();


    public Inventario(int capacidadMaxima) {
        if (capacidadMaxima <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser positiva");
        }
        this.capacidadMaxima = capacidadMaxima;
    }

    public boolean agregar(String nombre, int cantidad) {
        Objects.requireNonNull(nombre, "nombre");
        if (cantidad <= 0 || ocupacionActual + cantidad > capacidadMaxima) {
            return false;
        }
        Objeto existente = objetos.get(nombre);
        if (existente == null) {
            objetos.put(nombre, new ObjetoMaterial(nombre, "Generico", cantidad));
        } else {
            existente.aumentarCantidad(cantidad);
        }
        ocupacionActual += cantidad;
        return true;
    }

    public boolean consumir(String nombre, int cantidad) {
        Objects.requireNonNull(nombre, "nombre");
        if (cantidad <= 0) {
            return false;
        }
        Objeto existente = objetos.get(nombre);
        if (existente == null || !existente.reducirCantidad(cantidad)) {
            return false;
        }
        ocupacionActual -= cantidad;
        if (existente.getCantidad() == 0) {
            objetos.remove(nombre);
        }
        return true;
    }

    public void registrarObjeto(Objeto objeto) {
        Objects.requireNonNull(objeto, "objeto");
        if (ocupacionActual + objeto.getCantidad() > capacidadMaxima) {
            throw new IllegalStateException("No hay espacio suficiente");
        }
        objetos.merge(objeto.getNombre(), objeto, (actual, nuevo) -> {
            if (!actual.getClass().equals(nuevo.getClass())) {
                throw new IllegalArgumentException("Ya existe un objeto de otro tipo registrado con ese nombre");
            }
            actual.aumentarCantidad(nuevo.getCantidad());
            return actual;
        });
        ocupacionActual += objeto.getCantidad();
    }

    public int getDisponibles(String nombre) {
        Objeto objeto = objetos.get(nombre);
        return objeto == null ? 0 : objeto.getCantidad();
    }

    public Objeto obtener(String nombre) {
        return objetos.get(nombre);
    }

    public Map<String, Objeto> getObjetos() {
        return Collections.unmodifiableMap(objetos);
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public int getOcupacionActual() {
        return ocupacionActual;
    }
}
