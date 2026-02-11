package net.lwenstrom.tft.backend.core.model;

import java.util.List;

/**
 * Adds stun duration to the target (backend-only).
 */
public record StunModifier(List<Integer> stunSeconds) implements AbilityModifier {
    public int getStunSeconds(int starLevel) {
        if (stunSeconds == null || stunSeconds.isEmpty())
            return 0;
        int index = Math.min(starLevel - 1, stunSeconds.size() - 1);
        return stunSeconds.get(index);
    }
}
