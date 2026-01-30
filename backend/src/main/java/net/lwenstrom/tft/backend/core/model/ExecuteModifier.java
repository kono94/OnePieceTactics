package net.lwenstrom.tft.backend.core.model;

// Deals bonus damage to low-health targets (backend-only).
import java.util.List;

// Deals bonus damage to low-health targets (backend-only).
public record ExecuteModifier(List<Float> hpThreshold, List<Float> bonusDamageMultiplier)
        implements AbilityModifier {

    // Calculate bonus damage if target is below HP threshold.
    public int calculateBonusDamage(GameUnit target, int baseDamage, int starLevel) {
        if (target == null)
            return 0;
        if (hpThreshold == null || hpThreshold.isEmpty())
            return 0;
        if (bonusDamageMultiplier == null || bonusDamageMultiplier.isEmpty())
            return 0;

        int index = Math.min(starLevel - 1, hpThreshold.size() - 1);
        int mIndex = Math.min(starLevel - 1, bonusDamageMultiplier.size() - 1);

        var threshold = hpThreshold.get(index);
        var multiplier = bonusDamageMultiplier.get(mIndex);

        var hpPercent = (float) target.getCurrentHealth() / target.getMaxHealth();
        if (hpPercent <= threshold) {
            return (int) (baseDamage * multiplier);
        }
        return 0;
    }

    // Check if the execute threshold is met.
    public boolean isExecuteThresholdMet(GameUnit target, int starLevel) {
        if (target == null)
            return false;
        if (hpThreshold == null || hpThreshold.isEmpty())
            return false;

        int index = Math.min(starLevel - 1, hpThreshold.size() - 1);
        var threshold = hpThreshold.get(index);

        var hpPercent = (float) target.getCurrentHealth() / target.getMaxHealth();
        return hpPercent <= threshold;
    }
}
