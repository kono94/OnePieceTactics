package net.lwenstrom.tft.backend.core.engine;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import net.lwenstrom.tft.backend.core.model.CustomEffectHandler;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

/**
 * A data-driven trait applier that reads effect values from trait definition
 * JSON.
 * Can be used by any theme (One Piece, Pokemon, etc.) for standard effect
 * types.
 */
public class GenericTraitApplier implements TraitEffect {

    private final String traitId;
    private final String effectType;
    private final List<JsonNode> effects;
    private final Map<String, CustomEffectHandler> customHandlers;

    public GenericTraitApplier(String traitId, String effectType, List<JsonNode> effects) {
        this(traitId, effectType, effects, Map.of());
    }

    public GenericTraitApplier(
            String traitId,
            String effectType,
            List<JsonNode> effects,
            Map<String, CustomEffectHandler> customHandlers) {
        this.traitId = traitId;
        this.effectType = effectType;
        this.effects = effects;
        this.customHandlers = customHandlers;
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
            case "ABILITY_DAMAGE" -> applyAbilityDamage(units, values);
            case "LOW_HP_DAMAGE" -> applyLowHpDamage(units, values);
            case "LIFESTEAL" -> applyLifesteal(units, values);
            case "EXTRA_ATTACK_CHANCE" -> applyExtraAttackChance(units, values);
            case "MANA_GAIN" -> applyManaGain(units, values);
            case "LOW_HP_AS" -> applyLowHpAs(units, values);
            case "DISTANCE_DAMAGE" -> applyDistanceDamage(units, values);
            case "GOLD_ON_WIN" -> applyGoldOnWin(units, values);
            case "HEAL_AMP" -> applyHealAmp(units, values);
            case "AS_ON_CAST" -> applyAsOnCast(units, values);
            case "CUSTOM" -> applyCustom(count, units, activeEffect);
        }
    }

    private void applyCustom(int count, List<GameUnit> units, JsonNode effect) {
        if (!effect.has("customHandler")) {
            return;
        }
        var handlerId = effect.get("customHandler").asText();
        var handler = customHandlers.get(handlerId);
        if (handler != null) {
            handler.apply(count, units, effect.get("values"));
        }
    }

    private void applyAbilityDamage(List<GameUnit> units, JsonNode values) {
        float multiplier = values.has("abilityDamage")
                ? (float) values.get("abilityDamage").asDouble()
                : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setAbilityDamageMultiplier(1.0f + multiplier);
            }
        }
    }

    private void applyLowHpDamage(List<GameUnit> units, JsonNode values) {
        float bonus =
                values.has("damageBonus") ? (float) values.get("damageBonus").asDouble() : 0f;
        float threshold =
                values.has("hpThreshold") ? (float) values.get("hpThreshold").asDouble() : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setLowHpDamageBonus(bonus);
                unit.setLowHpDamageThreshold(threshold);
            }
        }
    }

    private void applyLifesteal(List<GameUnit> units, JsonNode values) {
        float lifesteal =
                values.has("lifesteal") ? (float) values.get("lifesteal").asDouble() : 0f;
        boolean canRevive = values.has("revive") && values.get("revive").asBoolean();
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setLifesteal(lifesteal);
                if (canRevive) unit.setHasRevive(true);
            }
        }
    }

    private void applyExtraAttackChance(List<GameUnit> units, JsonNode values) {
        float chance = values.has("chance") ? (float) values.get("chance").asDouble() : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setExtraAttackChance(chance);
            }
        }
    }

    private void applyManaGain(List<GameUnit> units, JsonNode values) {
        float multiplier =
                values.has("manaGain") ? (float) values.get("manaGain").asDouble() : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setManaGainMultiplier(1.0f + multiplier);
            }
        }
    }

    private void applyLowHpAs(List<GameUnit> units, JsonNode values) {
        float bonus = values.has("as") ? (float) values.get("as").asDouble() : 0f;
        float threshold =
                values.has("hpThreshold") ? (float) values.get("hpThreshold").asDouble() : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setLowHpAsBonus(bonus);
                unit.setLowHpAsThreshold(threshold);
            }
        }
    }

    private void applyDistanceDamage(List<GameUnit> units, JsonNode values) {
        float bonus = values.has("damagePerCell")
                ? (float) values.get("damagePerCell").asDouble()
                : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setDamagePerCell(bonus);
            }
        }
    }

    private void applyGoldOnWin(List<GameUnit> units, JsonNode values) {
        int min = values.has("goldMin") ? values.get("goldMin").asInt() : 0;
        int max = values.has("goldMax") ? values.get("goldMax").asInt() : 0;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setGoldBonusMin(min);
                unit.setGoldBonusMax(max);
            }
        }
    }

    private void applyHealAmp(List<GameUnit> units, JsonNode values) {
        float amp = values.has("healAmp") ? (float) values.get("healAmp").asDouble() : 0f;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setHealAmplification(1.0f + amp);
            }
        }
    }

    private void applyAsOnCast(List<GameUnit> units, JsonNode values) {
        float as = values.has("as") ? (float) values.get("as").asDouble() : 0f;
        int duration = values.has("duration") ? values.get("duration").asInt() : 0;
        for (GameUnit unit : units) {
            if (hasTrait(unit)) {
                unit.setAsOnCast(as);
                unit.setAsOnCastDuration(duration);
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
