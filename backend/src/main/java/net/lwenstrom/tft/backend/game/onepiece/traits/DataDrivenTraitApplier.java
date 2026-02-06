package net.lwenstrom.tft.backend.game.onepiece.traits;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

/**
 * A data-driven trait applier that reads effect values from trait definition
 * JSON.
 * The traits_onepiece.json is the single source of truth for all trait values.
 */
public class DataDrivenTraitApplier implements TraitEffect {

    private final String traitId;
    private final String effectType;
    private final List<JsonNode> effects;

    public DataDrivenTraitApplier(String traitId, String effectType, List<JsonNode> effects) {
        this.traitId = traitId;
        this.effectType = effectType;
        this.effects = effects;
    }

    @Override
    public void apply(int count, List<GameUnit> units) {
        // Find the highest matching breakpoint
        JsonNode activeEffect = null;
        for (JsonNode effect : effects) {
            int minUnits = effect.get("minUnits").asInt();
            if (count >= minUnits) {
                activeEffect = effect;
            }
        }

        if (activeEffect == null || !activeEffect.has("values")) {
            return;
        }

        var values = activeEffect.get("values");

        // Apply effect based on effectType
        switch (effectType) {
            case "HP" -> applyHp(units, values);
            case "HP_AND_AS" -> applyHpAndAs(units, values);
            case "AS" -> applyAs(units, values);
            case "ARMOR_AND_MR" -> applyArmorAndMr(units, values);
            case "ATK_BUFF" -> applyAtkBuff(units, values);
            case "START_MANA" -> applyStartMana(units, values);

            // Placeholder effects - values stored but logic TODO
            case "ABILITY_DAMAGE",
                    "LOW_HP_DAMAGE",
                    "LIFESTEAL",
                    "EXTRA_ATTACK_CHANCE",
                    "MANA_GAIN",
                    "LOW_HP_AS",
                    "DISTANCE_DAMAGE",
                    "GOLD_ON_WIN",
                    "HEAL_AMP",
                    "AS_ON_CAST" -> {
                // TODO: Implement these effects in combat system
                // Values are available in the JSON for when implemented
            }
        }
    }

    private void applyHp(List<GameUnit> units, JsonNode values) {
        int bonusHp = values.has("hp") ? values.get("hp").asInt() : 0;
        if (bonusHp <= 0) return;

        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setMaxHealth(unit.getMaxHealth() + bonusHp);
                unit.setCurrentHealth(unit.getCurrentHealth() + bonusHp);
            }
        }
    }

    private void applyHpAndAs(List<GameUnit> units, JsonNode values) {
        int bonusHp = values.has("hp") ? values.get("hp").asInt() : 0;
        float bonusAs = values.has("as") ? (float) values.get("as").asDouble() : 0f;

        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                if (bonusHp > 0) {
                    unit.setMaxHealth(unit.getMaxHealth() + bonusHp);
                    unit.setCurrentHealth(unit.getCurrentHealth() + bonusHp);
                }
                if (bonusAs > 0) {
                    unit.setAttackSpeed(unit.getAttackSpeed() + bonusAs);
                }
            }
        }
    }

    private void applyAs(List<GameUnit> units, JsonNode values) {
        float bonusAs = values.has("as") ? (float) values.get("as").asDouble() : 0f;
        if (bonusAs <= 0) return;

        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setAttackSpeed(unit.getAttackSpeed() + bonusAs);
            }
        }
    }

    private void applyArmorAndMr(List<GameUnit> units, JsonNode values) {
        int bonusArmor = values.has("armor") ? values.get("armor").asInt() : 0;
        int bonusMr = values.has("mr") ? values.get("mr").asInt() : 0;

        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                if (bonusArmor > 0) {
                    unit.setArmor(unit.getArmor() + bonusArmor);
                }
                if (bonusMr > 0) {
                    unit.setMagicResist(unit.getMagicResist() + bonusMr);
                }
            }
        }
    }

    private void applyAtkBuff(List<GameUnit> units, JsonNode values) {
        float atkBuff = values.has("atkBuff") ? (float) values.get("atkBuff").asDouble() : 0f;
        if (atkBuff <= 0) return;

        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setAtkBuff(unit.getAtkBuff() * (1f + atkBuff));
            }
        }
    }

    private void applyStartMana(List<GameUnit> units, JsonNode values) {
        int bonusMana = values.has("mana") ? values.get("mana").asInt() : 0;
        if (bonusMana <= 0) return;

        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setMana(Math.min(unit.getMaxMana(), unit.getMana() + bonusMana));
            }
        }
    }

    private boolean hasTrait(GameUnit unit) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
