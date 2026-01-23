package net.lwenstrom.tft.backend.core.engine;

import java.util.HashSet;

public class StandardGameUnit extends AbstractGameUnit {

    public StandardGameUnit(UnitDefinition def) {
        super(def.id(), def.name(), def.cost(), def.ability(), def.range(), new HashSet<>(def.traits()));
        setMaxHealth(def.maxHealth());
        setMaxMana(def.maxMana());
        setAttackDamage(def.attackDamage());
        setAbilityPower(def.abilityPower());
        setArmor(def.armor());
        setMagicResist(def.magicResist());
        setAttackSpeed(def.attackSpeed());
        setCurrentHealth(def.maxHealth());
    }

    public boolean isDead() {
        return getCurrentHealth() <= 0;
    }

    public void useMana(int amount) {
        setMana(Math.max(0, getMana() - amount));
    }
}
