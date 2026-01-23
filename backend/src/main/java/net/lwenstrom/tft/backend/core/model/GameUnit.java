package net.lwenstrom.tft.backend.core.model;

import java.util.List;
import java.util.Set;

public interface GameUnit {
    String getId();

    String getDefinitionId();

    String getName();

    int getCost();

    int getMaxHealth();

    int getCurrentHealth();

    int getMana();

    int getMaxMana();

    int getAttackDamage();

    int getAbilityPower();

    int getArmor();

    int getMagicResist();

    float getAttackSpeed();

    int getRange();

    Set<String> getTraits();

    List<GameItem> getItems();

    // Position on grid (x, y)
    int getX();

    int getY();

    void setPosition(int x, int y);

    void takeDamage(int amount);

    void gainMana(int amount);

    int getStarLevel();

    String getOwnerId();

    void setOwnerId(String ownerId);

    long getNextAttackTime();

    void setNextAttackTime(long time);

    long getNextMoveTime();

    void setNextMoveTime(long time);

    default void setMaxHealth(int maxHealth) {}

    default void setCurrentHealth(int currentHealth) {}

    default void setMaxMana(int maxMana) {}

    default void setAttackDamage(int attackDamage) {}

    default void setAbilityPower(int abilityPower) {}

    default void setArmor(int armor) {}

    default void setMagicResist(int magicResist) {}

    default void setAttackSpeed(float attackSpeed) {}

    default void setStarLevel(int starLevel) {}

    default void setMana(int mana) {}

    default void savePlanningPosition() {}

    default void restorePlanningPosition() {}

    AbilityDefinition getAbility();

    String getActiveAbility();

    void setActiveAbility(String abilityName);

    // Stun status (unit skips turns while > 0)
    default int getStunTicksRemaining() {
        return 0;
    }

    default void setStunTicksRemaining(int ticks) {}

    // Attack buff multiplier (1.0 = no buff)
    default float getAtkBuff() {
        return 1.0f;
    }

    default void setAtkBuff(float buff) {}

    // Speed buff multiplier (1.0 = no buff)
    default float getSpdBuff() {
        return 1.0f;
    }

    default void setSpdBuff(float buff) {}
}
