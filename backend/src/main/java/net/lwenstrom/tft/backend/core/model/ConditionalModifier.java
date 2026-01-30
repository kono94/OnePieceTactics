package net.lwenstrom.tft.backend.core.model;

// Apply ability effects only when specified conditions are met (backend-only).
public record ConditionalModifier(ConditionType conditionType, float threshold) implements AbilityModifier {

    public enum ConditionType {
        /** Target must be below specified HP percentage */
        TARGET_HP_BELOW,

        /** Target must be above specified HP percentage */
        TARGET_HP_ABOVE,

        /** Target must be stunned */
        TARGET_STUNNED,

        /** Caster must be below specified HP percentage */
        CASTER_HP_BELOW,

        /** Caster must have full mana */
        CASTER_FULL_MANA
    }

    // Check if the condition is met for the ability to apply its effect.
    public boolean isMet(GameUnit caster, GameUnit target) {
        return switch (conditionType) {
            case TARGET_HP_BELOW -> {
                if (target == null) yield false;
                var hpPercent = (float) target.getCurrentHealth() / target.getMaxHealth();
                yield hpPercent < threshold;
            }
            case TARGET_HP_ABOVE -> {
                if (target == null) yield false;
                var hpPercent = (float) target.getCurrentHealth() / target.getMaxHealth();
                yield hpPercent > threshold;
            }
            case TARGET_STUNNED -> {
                if (target == null) yield false;
                yield target.getStunTicksRemaining() > 0;
            }
            case CASTER_HP_BELOW -> {
                var hpPercent = (float) caster.getCurrentHealth() / caster.getMaxHealth();
                yield hpPercent < threshold;
            }
            case CASTER_FULL_MANA -> caster.getMana() >= caster.getMaxMana();
        };
    }
}
