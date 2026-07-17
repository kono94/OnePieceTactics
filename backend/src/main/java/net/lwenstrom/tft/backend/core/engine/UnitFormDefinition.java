package net.lwenstrom.tft.backend.core.engine;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.UnitRole;

public record UnitFormDefinition(
        int starLevel,
        String definitionId,
        String name,
        UnitRole role,
        List<String> traits,
        List<Integer> range,
        AbilityDefinition ability) {}
