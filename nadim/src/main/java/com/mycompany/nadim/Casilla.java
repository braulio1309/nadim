package com.mycompany.nadim;

import java.awt.Image;

public class Casilla {
    private Image sprite;
    private boolean traversable;
    private String spriteKey;

    public Casilla(Image sprite, boolean traversable) {
        this.sprite = sprite;
        this.traversable = traversable;
    }

    public Image getSprite() {
        return sprite;
    }

    public void setSprite(Image sprite) {
        this.sprite = sprite;
    }

    public boolean canBeTraversed() {
        return traversable;
    }

    public void setTraversable(boolean traversable) {
        this.traversable = traversable;
    }

    public String getSpriteKey() {
        return spriteKey;
    }

    public void setSpriteKey(String spriteKey) {
        this.spriteKey = spriteKey;
    }
}
