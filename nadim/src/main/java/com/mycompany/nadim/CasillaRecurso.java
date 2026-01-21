package com.mycompany.nadim;

import java.awt.Image;

public class CasillaRecurso extends Casilla {
    private final Image displaySprite;
    private final String resourceName;
    private final int yieldAmount;
    private final int cooldownTicks;
    private final boolean persistent;

    private boolean depleted;
    private int remainingCooldown;
    private String lastHarvester;

    public CasillaRecurso(Image sprite, String resourceName, int yieldAmount, int cooldownTicks, boolean persistent) {
        super(sprite, true);
        this.displaySprite = sprite;
        this.resourceName = resourceName;
        this.yieldAmount = yieldAmount;
        this.cooldownTicks = cooldownTicks;
        this.persistent = persistent;
        this.depleted = false;
        this.remainingCooldown = 0;
        this.lastHarvester = null;
    }

    public Image getDisplaySprite() {
        return displaySprite;
    }

    public boolean isDepleted() {
        return depleted;
    }

    public String getLastHarvester() {
        return lastHarvester;
    }

    public int getRemainingCooldown() {
        return remainingCooldown;
    }

    public int getYieldAmount() {
        return yieldAmount;
    }

    public String getResourceName() {
        return resourceName;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }


    public int harvest(String harvesterAlias) {
        if (depleted) {
            return 0;
        }
        this.depleted = true;
        this.remainingCooldown = cooldownTicks;
        this.lastHarvester = harvesterAlias;
        return yieldAmount;
    }

    public boolean tickCooldown() {
        if (!depleted) {
            return false;
        }
        if (remainingCooldown > 0) {
            remainingCooldown--;
        }
        if (remainingCooldown <= 0) {
            boolean became = depleted;
            depleted = false;
            remainingCooldown = 0;
            lastHarvester = null;
            return became;
        }
        return false;
    }

    public void applyRemoteState(boolean depleted, int remaining, String harvesterAlias) {
        this.depleted = depleted;
        this.remainingCooldown = Math.max(0, remaining);
        this.lastHarvester = harvesterAlias;
    }
}
