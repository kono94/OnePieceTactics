package net.lwenstrom.tft.backend.core.model;

// Converts damage dealt into healing for the caster (backend-only).
public record LifestealModifier(float lifestealPercent) implements AbilityModifier {

    // Calculate healing amount based on damage dealt.
    public int calculateHealing(int damageDealt) {
        return (int) (damageDealt * lifestealPercent);
    }
}
