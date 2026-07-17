package net.lwenstrom.tft.backend.core.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.UnitRole;

public record UnitDefinition(
        String id,
        String name,
        int cost,
        UnitRole role,
        List<Integer> maxHealth,
        List<Integer> maxMana,
        List<Integer> attackDamage,
        List<Integer> abilityPower,
        List<Integer> defense,
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
            UnitRole role,
            List<Integer> maxHealth,
            List<Integer> maxMana,
            List<Integer> attackDamage,
            List<Integer> abilityPower,
            List<Integer> defense,
            List<Float> attackSpeed,
            List<Integer> range,
            List<String> traits,
            AbilityDefinition ability) {
        this(
                id,
                name,
                cost,
                role,
                maxHealth,
                maxMana,
                attackDamage,
                abilityPower,
                defense,
                attackSpeed,
                range,
                traits,
                ability,
                null,
                null);
    }

    public UnitDefinition {
        Objects.requireNonNull(role, "Unit role is required");
        if (defense == null || defense.size() != 3) {
            throw new IllegalArgumentException("Unit defense must contain exactly three star-level values");
        }
        if (defense.stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("Unit defense values must be non-negative");
        }
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

    public int getDefense(int level) {
        return getVal(defense, level);
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

    public UnitRole getRole(int level) {
        var form = getForm(level);
        return form != null && form.role() != null ? form.role() : role;
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

    @JsonProperty("defense")
    public int baseDefense() {
        return getDefense(1);
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
