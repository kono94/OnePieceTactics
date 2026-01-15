package net.lwenstrom.tft.backend.core.model;

import java.util.List;

public interface TraitEffect {
    void apply(int count, List<GameUnit> units);
}
