package com.mycompany.nadim;

/* Clase eliminada: reemplazada por JugadorRemoto. */
/** Lightweight representation of a remote explorer for rendering. */
class JugadorRemoto {
    private String name;
    private int x;
    private int y;

    public JugadorRemoto(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
