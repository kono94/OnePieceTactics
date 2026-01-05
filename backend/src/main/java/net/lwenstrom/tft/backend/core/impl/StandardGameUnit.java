package net.lwenstrom.tft.backend.core.impl;

import net.lwenstrom.tft.backend.core.data.UnitDefinition;
import java.util.HashSet;

public class StandardGameUnit extends AbstractGameUnit {

    private int currentHealth;
    private int currentMana;
    private int gridX;
    private int gridY;

    public StandardGameUnit(UnitDefinition def) {
        super(
                def.name(),
                def.cost(),
                def.maxHealth(),
                def.maxMana(),
                def.attackDamage(),
                def.abilityPower(),
                def.armor(),
                def.magicResist(),
                def.attackSpeed(),
                def.range(),
                new HashSet<>(def.traits()));

        this.currentHealth = def.maxHealth();
        this.currentMana = 0;
        this.gridX = -1; // Not placed
        this.gridY = -1;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public void takeDamage(int amount) {
        this.currentHealth = Math.max(0, this.currentHealth - amount);
    }

    public boolean isDead() {
        return this.currentHealth <= 0;
    }

    public void gainMana(int amount) {
        this.currentMana = Math.min(getMaxMana(), this.currentMana + amount);
    }

    public void useMana(int amount) {
        this.currentMana = Math.max(0, this.currentMana - amount);
    }
}
