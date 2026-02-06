package net.lwenstrom.tft.backend.core.model;

import java.util.List;

/**
 * Adds stun duration to the target (backend-only).
 */
public record StunModifier(List<Integer> stunTicks) implements AbilityModifier {
    public int getStunTicks(int starLevel) {
        if (stunTicks == null || stunTicks.isEmpty()) return 0;
        int index = Math.min(starLevel - 1, stunTicks.size() - 1);
        return stunTicks.get(index);
    }
}
