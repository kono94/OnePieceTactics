package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class GameRoomLootOrbTest {

    @Test
    void unitLootPrefersOwnedLowCostLineWhenOwnedBranchHits() {
        var oneCost = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var twoCost = TestHelpers.createUnitDef("two-cost", "Two Cost", 2, 100, 10);
        var units = List.of(oneCost, twoCost);
        var randomProvider = mock(RandomProvider.class);
        when(randomProvider.nextInt(100)).thenReturn(0);
        when(randomProvider.nextInt(17)).thenReturn(0);
        var room = createRoom(units, randomProvider);
        var player = createPlayer(units, randomProvider);
        player.getBenchSlots().set(0, new StandardGameUnit(oneCost));
        player.getBenchSlots().set(1, new StandardGameUnit(twoCost));

        var selected = room.chooseLootUnitDefinitionForPlayer(player, units);

        assertEquals(oneCost.id(), selected.id());
    }

    @Test
    void unitLootFallsBackToShopOddsWhenNoOwnedLineIsEligible() {
        var oneCost = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var units = List.of(oneCost);
        var randomProvider = mock(RandomProvider.class);
        when(randomProvider.nextInt(100)).thenReturn(0);
        when(randomProvider.nextInt(1)).thenReturn(0);
        var room = createRoom(units, randomProvider);
        var player = createPlayer(units, randomProvider);

        var selected = room.chooseLootUnitDefinitionForPlayer(player, units);

        assertEquals(oneCost.id(), selected.id());
    }

    @Test
    void unitLootDoesNotTargetCompletedThreeStarOnlyLines() {
        var completed = TestHelpers.createUnitDef("completed", "Completed", 1, 100, 10);
        var fallback = TestHelpers.createUnitDef("fallback", "Fallback", 1, 100, 10);
        var units = List.of(completed, fallback);
        var randomProvider = mock(RandomProvider.class);
        when(randomProvider.nextInt(100)).thenReturn(0);
        when(randomProvider.nextInt(2)).thenReturn(1);
        var room = createRoom(units, randomProvider);
        var player = createPlayer(units, randomProvider);
        player.getBenchSlots().set(0, new StandardGameUnit(completed, 3));

        var selected = room.chooseLootUnitDefinitionForPlayer(player, units);

        assertEquals(fallback.id(), selected.id());
    }

    private GameRoom createRoom(List<UnitDefinition> units, RandomProvider randomProvider) {
        return new GameRoom(
                "loot-orb-test",
                TestHelpers.createMockDataLoader(units),
                TestHelpers.createMockRegistry(),
                TestHelpers.createTestClock(),
                randomProvider,
                GameMode.ONEPIECE);
    }

    private Player createPlayer(List<UnitDefinition> units, RandomProvider randomProvider) {
        return new Player("Test Player", GameMode.ONEPIECE, TestHelpers.createMockDataLoader(units), randomProvider);
    }
}
