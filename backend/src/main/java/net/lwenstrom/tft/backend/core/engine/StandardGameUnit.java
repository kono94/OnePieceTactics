package net.lwenstrom.tft.backend.core.engine;

import java.util.HashSet;

public class StandardGameUnit extends AbstractGameUnit {

    public StandardGameUnit(UnitDefinition def) {
        this(def, 1);
    }

    public StandardGameUnit(UnitDefinition def, int starLevel) {
        super(
                def.getDefinitionId(starLevel),
                def.lineId(),
                def.getName(starLevel),
                def.cost(),
                def.getAbility(starLevel),
                def.getActiveRange(starLevel),
                new HashSet<>(def.getTraits(starLevel)));
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

    private StandardGameUnit(StandardGameUnit other) {
        super(other);
    }

    @Override
    public StandardGameUnit cloneUnit() {
        return new StandardGameUnit(this);
    }
}
