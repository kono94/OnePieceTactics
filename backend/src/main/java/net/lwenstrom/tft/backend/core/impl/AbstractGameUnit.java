package net.lwenstrom.tft.backend.core.impl;

import net.lwenstrom.tft.backend.core.model.GameItem;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public abstract class AbstractGameUnit implements GameUnit {
    private final String id;
    private final String name;
    private final int cost;
    private final int maxHealth;
    private final int maxMana;
    private final int attackDamage;
    private final int abilityPower;
    private final int armor;
    private final int magicResist;
    private final float attackSpeed;
    private final int range;
    private final Set<String> traits;
    @Setter
    private int starLevel = 1;

    @Setter
    private int currentHealth;
    @Setter
    private int mana;
    @Setter
    private int x = -1;
    @Setter
    private int y = -1;

    private final List<GameItem> items = new ArrayList<>();

    public AbstractGameUnit(String name, int cost, int maxHealth, int maxMana, int ad, int ap, int armor, int mr,
            float as, int range, Set<String> traits) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.cost = cost;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.maxMana = maxMana;
        this.mana = 0; // Start with 0 mana usually
        this.attackDamage = ad;
        this.abilityPower = ap;
        this.armor = armor;
        this.magicResist = mr;
        this.attackSpeed = as;
        this.range = range;
        this.traits = traits != null ? traits : new HashSet<>();
    }

    @Override
    public void takeDamage(int amount) {
        // Simple mitigation formula could go here
        this.currentHealth = Math.max(0, this.currentHealth - amount);
    }

    @Override
    public void gainMana(int amount) {
        this.mana = Math.min(maxMana, this.mana + amount);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
