package net.lwenstrom.tft.backend.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;

public interface GameUnit {
    String getId();

    String getDefinitionId();

    default String getLineId() {
        return getDefinitionId();
    }

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

    @JsonIgnore
    default List<DotEffect> getDotEffects() {
        return List.of();
    }

    default void addDotEffect(DotEffect effect) {}

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

    // Get formatted ability description with scaled stats based on star level
    @JsonProperty("formattedAbilityDescription")
    default String formattedAbilityDescription() {
        var ability = getAbility();
        if (ability == null) return "";
        return ability.getFormattedDescription(getStarLevel());
    }

    String getActiveAbility();

    void setActiveAbility(String abilityName);

    // Stun status (unit skips turns while > 0)
    default float getStunSecondsRemaining() {
        return 0;
    }

    default void setStunSecondsRemaining(float seconds) {}

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

    GameUnit cloneUnit();

    // Trait specific effects
    default float getAbilityDamageMultiplier() {
        return 1.0f;
    }

    default void setAbilityDamageMultiplier(float multiplier) {}

    default float getLifesteal() {
        return 0.0f;
    }

    default void setLifesteal(float lifesteal) {}

    default float getManaGainMultiplier() {
        return 1.0f;
    }

    default void setManaGainMultiplier(float multiplier) {}

    default float getExtraAttackChance() {
        return 0.0f;
    }

    default void setExtraAttackChance(float chance) {}

    default float getDamagePerCell() {
        return 0.0f;
    }

    default void setDamagePerCell(float damage) {}

    default float getHealAmplification() {
        return 1.0f;
    }

    default void setHealAmplification(float amp) {}

    default boolean hasRevive() {
        return false;
    }

    default void setHasRevive(boolean hasRevive) {}

    default boolean isReviveUsed() {
        return false;
    }

    default void setReviveUsed(boolean used) {}

    default int getGoldBonusMin() {
        return 0;
    }

    default void setGoldBonusMin(int min) {}

    default int getGoldBonusMax() {
        return 0;
    }

    default void setGoldBonusMax(int max) {}

    default float getAsOnCast() {
        return 0.0f;
    }

    default void setAsOnCast(float as) {}

    default int getAsOnCastDuration() {
        return 0;
    }

    default void setAsOnCastDuration(int duration) {}

    default float getLowHpDamageBonus() {
        return 0.0f;
    }

    default void setLowHpDamageBonus(float bonus) {}

    default float getLowHpDamageThreshold() {
        return 0.0f;
    }

    default void setLowHpDamageThreshold(float threshold) {}

    default float getLowHpAsBonus() {
        return 0.0f;
    }

    default void setLowHpAsBonus(float bonus) {}

    default float getLowHpAsThreshold() {
        return 0.0f;
    }

    default void setLowHpAsThreshold(float threshold) {}

    default boolean hasShieldOnDeath() {
        return false;
    }

    default void setShieldOnDeath(boolean hasShield) {}

    default int getShield() {
        return 0;
    }

    default void setShield(int amount) {}
}
