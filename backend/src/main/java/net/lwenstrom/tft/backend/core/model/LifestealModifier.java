package net.lwenstrom.tft.backend.core.model;

import java.util.List;

// Converts damage dealt into healing for the caster (backend-only).
public record LifestealModifier(List<Float> lifestealPercent) implements AbilityModifier {

    // Calculate healing amount based on damage dealt.
    public int calculateHealing(int damageDealt, int starLevel) {
        if (lifestealPercent == null || lifestealPercent.isEmpty())
            return 0;
        int index = Math.min(starLevel - 1, lifestealPercent.size() - 1);
        return (int) (damageDealt * lifestealPercent.get(index));
    }
}
