package net.lwenstrom.tft.backend.core.engine;

public record BotRosterProfile(
        int maxUnits,
        int guaranteedTwoStarUnits,
        int guaranteedCheapThreeStarUnits,
        int guaranteedMidCostThreeStarUnits,
        int cheapTwoStarChance,
        int cheapThreeStarChance,
        int midCostTwoStarChance,
        int midCostThreeStarChance,
        int fiveCostTwoStarChance) {}
