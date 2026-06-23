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
        AbilityDefinition ability,
        String lineId,
        List<UnitFormDefinition> forms) {

    public UnitDefinition(
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
        this(
                id,
                name,
                cost,
                maxHealth,
                maxMana,
                attackDamage,
                abilityPower,
                armor,
                magicResist,
                attackSpeed,
                range,
                traits,
                ability,
                null,
                null);
    }

    public String lineId() {
        return lineId == null || lineId.isBlank() ? id : lineId;
    }

    public List<UnitFormDefinition> forms() {
        return forms == null ? List.of() : forms;
    }

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

    public String getDefinitionId(int level) {
        var form = getForm(level);
        return form != null && form.definitionId() != null ? form.definitionId() : id;
    }

    public String getName(int level) {
        var form = getForm(level);
        return form != null && form.name() != null ? form.name() : name;
    }

    public List<String> getTraits(int level) {
        var form = getForm(level);
        return form != null && form.traits() != null && !form.traits().isEmpty() ? form.traits() : traits;
    }

    public AbilityDefinition getAbility(int level) {
        var form = getForm(level);
        return form != null && form.ability() != null ? form.ability() : ability;
    }

    public int getActiveRange(int level) {
        var form = getForm(level);
        if (form != null && form.range() != null && !form.range().isEmpty()) {
            return getVal(form.range(), level);
        }
        return getRange(level);
    }

    private UnitFormDefinition getForm(int level) {
        return forms().stream()
                .filter(form -> form.starLevel() == level)
                .findFirst()
                .orElse(null);
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
        var activeAbility = getAbility(1);
        return activeAbility != null ? activeAbility.getFormattedDescription(1) : "";
    }
}
