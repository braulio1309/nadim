package com.mycompany.nadim;

public abstract class ObjetoCura extends Objeto {

    protected ObjetoCura(String nombre, String categoria, int cantidad) {
        super(nombre, categoria, cantidad);
    }

    public abstract boolean aplicar(Pokemon objetivo);
}
