package net.lwenstrom.tft.backend.core.engine;

import java.util.HashSet;

public class StandardGameUnit extends AbstractGameUnit {

    public StandardGameUnit(UnitDefinition def) {
        this(def, 1);
    }

    public StandardGameUnit(UnitDefinition def, int starLevel) {
        super(def.id(), def.name(), def.cost(), def.ability(), def.getRange(starLevel), new HashSet<>(def.traits()));
        setStarLevel(starLevel);
        setMaxHealth(def.getMaxHealth(starLevel));
        setMaxMana(def.getMaxMana(starLevel));
        setAttackDamage(def.getAttackDamage(starLevel));
        setAbilityPower(def.getAbilityPower(starLevel));
        setArmor(def.getArmor(starLevel));
        setMagicResist(def.getMagicResist(starLevel));
        setAttackSpeed(def.getAttackSpeed(starLevel));
        setCurrentHealth(def.getMaxHealth(starLevel));
    }

    public boolean isDead() {
        return getCurrentHealth() <= 0;
    }

    public void useMana(int amount) {
        setMana(Math.max(0, getMana() - amount));
    }
}
