package net.lwenstrom.tft.backend.core.engine;

import java.util.HashSet;

public class StandardGameUnit extends AbstractGameUnit {

    private String ownerId;
    private long nextAttackTime;

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
    }

    public boolean isDead() {
        return getCurrentHealth() <= 0;
    }

    public void useMana(int amount) {
        setMana(Math.max(0, getMana() - amount));
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getNextAttackTime() {
        return nextAttackTime;
    }

    public void setNextAttackTime(long time) {
        this.nextAttackTime = time;
    }
}
