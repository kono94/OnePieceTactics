package net.lwenstrom.tft.backend.core.simulation;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AugmentTier;

public record BoardSpec(
        String name, int level, List<UnitSpec> units, List<String> augmentIds, AugmentTier augmentTier) {

    public BoardSpec(String name, List<UnitSpec> units) {
        this(name, Math.max(1, units.size()), units, List.of(), AugmentTier.GOLD);
    }

    public BoardSpec(String name, int level, List<UnitSpec> units) {
        this(name, level, units, List.of(), AugmentTier.GOLD);
    }
}
