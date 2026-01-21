package com.mycompany.nadim;

import java.awt.Image;
import java.util.concurrent.ThreadLocalRandom;

public class CasillaPasto extends Casilla {
    private float encounterChance;

    public CasillaPasto(Image sprite, float encounterChance) {
        super(sprite, true);
        this.encounterChance = encounterChance;
    }

    public boolean doesEncounterOccur() {
        return ThreadLocalRandom.current().nextFloat() < encounterChance;
    }

    public float getEncounterChance() {
        return encounterChance;
    }

    public void setEncounterChance(float encounterChance) {
        this.encounterChance = encounterChance;
    }
}
