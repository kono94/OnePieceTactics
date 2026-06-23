package net.lwenstrom.tft.backend.core.engine;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;

public record UnitFormDefinition(
        int starLevel,
        String definitionId,
        String name,
        List<String> traits,
        List<Integer> range,
        AbilityDefinition ability) {}
