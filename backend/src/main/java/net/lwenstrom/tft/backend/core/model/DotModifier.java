package net.lwenstrom.tft.backend.core.model;

import java.util.List;

public record DotModifier(
        DotType dotType, List<Integer> damagePerTick, List<Integer> durationSeconds, List<Integer> tickIntervalMs)
        implements AbilityModifier {
    public enum DotType {
        BURN,
        POISON
    }

    public int getDamagePerTick(int starLevel) {
        return getValue(damagePerTick, starLevel, 0);
    }

    public int getDurationSeconds(int starLevel) {
        return getValue(durationSeconds, starLevel, 0);
    }

    public int getTickIntervalMs(int starLevel) {
        return getValue(tickIntervalMs, starLevel, 1000);
    }

    private int getValue(List<Integer> values, int starLevel, int defaultValue) {
        if (values == null || values.isEmpty()) return defaultValue;
        int index = Math.min(starLevel - 1, values.size() - 1);
        return values.get(index);
    }
}
