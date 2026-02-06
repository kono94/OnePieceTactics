package net.lwenstrom.tft.backend.core.engine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.random.RandomProvider;

/**
 * TFT-style level-based probability distribution for shop unit tiers.
 * Odds are percentages for each cost tier (1-5) at each player level (1-9).
 */
public final class ShopOdds {

    // Odds[playerLevel - 1] = { 1-cost%, 2-cost%, 3-cost%, 4-cost%, 5-cost% }
    // More generous odds - higher tiers available earlier than standard TFT
    private static final int[][] ODDS_BY_LEVEL = {
        {100, 0, 0, 0, 0}, // Level 1
        {70, 30, 0, 0, 0}, // Level 2
        {50, 35, 15, 0, 0}, // Level 3
        {35, 35, 25, 5, 0}, // Level 4
        {25, 30, 30, 13, 2}, // Level 5
        {18, 27, 30, 20, 5}, // Level 6
        {14, 22, 30, 25, 9}, // Level 7
        {12, 18, 27, 28, 15}, // Level 8
        {10, 15, 22, 30, 23}, // Level 9
    };

    private ShopOdds() {}

    public static UnitDefinition rollUnit(
            int playerLevel, List<UnitDefinition> allUnits, RandomProvider randomProvider) {
        var unitsByCost = groupUnitsByCost(allUnits);
        var costTier = rollCostTier(playerLevel, randomProvider);

        var unitsOfTier = unitsByCost.get(costTier);
        if (unitsOfTier == null || unitsOfTier.isEmpty()) {
            // Fallback: if no units of rolled tier, try lower tiers
            for (var fallbackTier = costTier - 1; fallbackTier >= 1; fallbackTier--) {
                unitsOfTier = unitsByCost.get(fallbackTier);
                if (unitsOfTier != null && !unitsOfTier.isEmpty()) {
                    break;
                }
            }
            // Final fallback: just pick any unit
            if (unitsOfTier == null || unitsOfTier.isEmpty()) {
                var randomIndex = randomProvider.nextInt(allUnits.size());
                return allUnits.get(randomIndex);
            }
        }

        var randomIndex = randomProvider.nextInt(unitsOfTier.size());
        return unitsOfTier.get(randomIndex);
    }

    private static int rollCostTier(int playerLevel, RandomProvider randomProvider) {
        var levelIndex = Math.max(0, Math.min(playerLevel - 1, ODDS_BY_LEVEL.length - 1));
        var odds = ODDS_BY_LEVEL[levelIndex];

        var roll = randomProvider.nextInt(100);
        var cumulative = 0;

        for (var tier = 0; tier < odds.length; tier++) {
            cumulative += odds[tier];
            if (roll < cumulative) {
                return tier + 1; // tiers are 1-indexed
            }
        }
        return 1; // fallback
    }

    private static Map<Integer, List<UnitDefinition>> groupUnitsByCost(List<UnitDefinition> units) {
        return units.stream().collect(Collectors.groupingBy(UnitDefinition::cost));
    }

    public static int[] getOddsForLevel(int playerLevel) {
        var levelIndex = Math.max(0, Math.min(playerLevel - 1, ODDS_BY_LEVEL.length - 1));
        return ODDS_BY_LEVEL[levelIndex].clone();
    }
}
