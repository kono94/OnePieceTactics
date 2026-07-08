package net.lwenstrom.tft.backend.core.simulation;

public record UnitCombatStats(
        String definitionId,
        String unitName,
        int appearances,
        double averageDamage,
        double averageHealing,
        double averageShielding) {}
