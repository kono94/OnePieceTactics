package net.lwenstrom.tft.backend.core.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;

public record UnitDefinition(
        String id,
        String name,
        int cost,
        List<Integer> maxHealth,
        List<Integer> maxMana,
        List<Integer> attackDamage,
        List<Integer> abilityPower,
        List<Integer> armor,
        List<Integer> magicResist,
        List<Float> attackSpeed,
        List<Integer> range,
        List<String> traits,
        AbilityDefinition ability) {

    public int getMaxHealth(int level) {
        return getVal(maxHealth, level);
    }

    public int getMaxMana(int level) {
        return getVal(maxMana, level);
    }

    public int getAttackDamage(int level) {
        return getVal(attackDamage, level);
    }

    public int getAbilityPower(int level) {
        return getVal(abilityPower, level);
    }

    public int getArmor(int level) {
        return getVal(armor, level);
    }

    public int getMagicResist(int level) {
        return getVal(magicResist, level);
    }

    public float getAttackSpeed(int level) {
        return getVal(attackSpeed, level);
    }

    public int getRange(int level) {
        return getVal(range, level);
    }

    private <T> T getVal(List<T> list, int level) {
        if (list == null || list.isEmpty()) return null;
        return list.get(Math.min(level - 1, list.size() - 1));
    }

    // Compatibility getters for Shop Tooltip (always shows 1-star stats)
    @JsonProperty("maxHealth")
    public int baseMaxHealth() {
        return getMaxHealth(1);
    }

    @JsonProperty("maxMana")
    public int baseMaxMana() {
        return getMaxMana(1);
    }

    @JsonProperty("attackDamage")
    public int baseAttackDamage() {
        return getAttackDamage(1);
    }

    @JsonProperty("abilityPower")
    public int baseAbilityPower() {
        return getAbilityPower(1);
    }

    @JsonProperty("armor")
    public int baseArmor() {
        return getArmor(1);
    }

    @JsonProperty("magicResist")
    public int baseMagicResist() {
        return getMagicResist(1);
    }

    @JsonProperty("attackSpeed")
    public float baseAttackSpeed() {
        return getAttackSpeed(1);
    }

    @JsonProperty("range")
    public int baseRange() {
        return getRange(1);
    }

    @JsonProperty("formattedAbilityDescription")
    public String formattedAbilityDescription() {
        return ability != null ? ability.getFormattedDescription(1) : "";
    }
}
