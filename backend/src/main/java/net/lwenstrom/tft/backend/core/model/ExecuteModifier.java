package net.lwenstrom.tft.backend.core.model;

// Deals bonus damage to low-health targets (backend-only).
public record ExecuteModifier(float hpThreshold, float bonusDamageMultiplier) implements AbilityModifier {

    // Calculate bonus damage if target is below HP threshold.
    public int calculateBonusDamage(GameUnit target, int baseDamage) {
        if (target == null) return 0;

        var hpPercent = (float) target.getCurrentHealth() / target.getMaxHealth();
        if (hpPercent <= hpThreshold) {
            return (int) (baseDamage * bonusDamageMultiplier);
        }
        return 0;
    }

    // Check if the execute threshold is met.
    public boolean isExecuteThresholdMet(GameUnit target) {
        if (target == null) return false;
        var hpPercent = (float) target.getCurrentHealth() / target.getMaxHealth();
        return hpPercent <= hpThreshold;
    }
}
