package net.lwenstrom.tft.backend.core.model;

import java.util.List;

// Additional scaling beyond default star-level scaling (CASTER_MISSING_HP, CASTER_MANA_PERCENT, etc)
public record ScalingModifier(ScalingType scalingType, List<Float> factor) implements AbilityModifier {

    public enum ScalingType {
        /** Scale with caster's missing HP percentage (0.0 to 1.0) */
        CASTER_MISSING_HP,

        /** Scale with caster's current mana percentage (0.0 to 1.0) */
        CASTER_MANA_PERCENT,

        /**
         * Scale with target's max HP (percentage of target's max HP as bonus damage)
         */
        TARGET_MAX_HP_PERCENT,

        /** Scale with target's missing HP percentage (0.0 to 1.0) */
        TARGET_MISSING_HP
    }

    // Calculate the scaling multiplier based on unit states (1.0 = no change, 1.5 =
    // 50% increase)
    public float calculateMultiplier(GameUnit caster, GameUnit target, int starLevel) {
        if (factor == null || factor.isEmpty())
            return 1.0f;
        int index = Math.min(starLevel - 1, factor.size() - 1);
        var currentFactor = factor.get(index);

        return switch (scalingType) {
            case CASTER_MISSING_HP -> {
                var missingHpPercent = 1.0f - ((float) caster.getCurrentHealth() / caster.getMaxHealth());
                yield 1.0f + (missingHpPercent * currentFactor);
            }
            case CASTER_MANA_PERCENT -> {
                var manaPercent = (float) caster.getMana() / caster.getMaxMana();
                yield 1.0f + (manaPercent * currentFactor);
            }
            case TARGET_MAX_HP_PERCENT -> {
                if (target == null)
                    yield 1.0f;
                var bonusDamage = (int) (target.getMaxHealth() * currentFactor);
                // Return as additional flat damage, not multiplier
                yield 1.0f + ((float) bonusDamage / 100.0f); // Normalize to reasonable scale
            }
            case TARGET_MISSING_HP -> {
                if (target == null)
                    yield 1.0f;
                var missingHpPercent = 1.0f - ((float) target.getCurrentHealth() / target.getMaxHealth());
                yield 1.0f + (missingHpPercent * currentFactor);
            }
        };
    }
}
