package net.lwenstrom.tft.backend.core.model;

import java.util.List;

/**
 * Knocks the target back by a specified number of cells (backend-only).
 */
public record KnockbackModifier(List<Integer> cells) implements AbilityModifier {
    public int getCells(int starLevel) {
        if (cells == null || cells.isEmpty()) return 0;
        int index = Math.min(starLevel - 1, cells.size() - 1);
        return cells.get(index);
    }
}
