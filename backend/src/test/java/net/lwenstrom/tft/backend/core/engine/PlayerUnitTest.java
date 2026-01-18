package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class PlayerUnitTest {

    @Test
    void testTakeDamage_ReducesHealth() {
        var player = TestHelpers.createTestPlayer("TestPlayer");
        assertEquals(100, player.getHealth());

        player.takeDamage(30);

        assertEquals(70, player.getHealth());
    }

    @Test
    void testTakeDamage_HealthCapsAt0() {
        var player = TestHelpers.createTestPlayer("TestPlayer");

        player.takeDamage(150);

        assertEquals(0, player.getHealth(), "Health should not go below 0");
    }

    @Test
    void testGainGold_IncreasesGold() {
        var player = TestHelpers.createTestPlayer("TestPlayer");
        int initialGold = player.getGold();

        player.gainGold(5);

        assertEquals(initialGold + 5, player.getGold());
    }

    @Test
    void testGainXp_TriggersLevelUp() {
        var player = TestHelpers.createTestPlayer("TestPlayer");
        assertEquals(1, player.getLevel());

        // Level 1 requires 2 XP to level up
        player.gainXp(2);

        assertEquals(2, player.getLevel(), "Should level up after gaining enough XP");
        assertEquals(0, player.getXp(), "XP should reset after level up");
    }

    @Test
    void testGainXp_MultipleLevelUps() {
        var player = TestHelpers.createTestPlayer("TestPlayer");
        assertEquals(1, player.getLevel());

        // Level 1->2: 2 XP, Level 2->3: 6 XP = 8 XP total
        player.gainXp(10);

        assertEquals(3, player.getLevel(), "Should level up twice");
        assertEquals(2, player.getXp(), "Remaining XP after level ups");
    }

    @Test
    void testBuyUnit_DeductsGold() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(100);
        player.refreshShop();

        int goldBefore = player.getGold();
        var unitInShop = player.getShop().get(0);
        int unitCost = unitInShop != null ? unitInShop.cost() : 1;

        player.buyUnit(0);

        assertEquals(goldBefore - unitCost, player.getGold(), "Gold should be deducted by unit cost");
    }

    @Test
    void testBuyUnit_AddsToRoster() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(100);
        player.refreshShop();

        assertEquals(0, player.getBench().size());

        player.buyUnit(0);

        assertEquals(1, player.getBench().size(), "Unit should be added to bench");
    }

    @Test
    void testBuyUnit_RemovesFromShop() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(100);
        player.refreshShop();

        player.buyUnit(0);

        assertNull(player.getShop().get(0), "Bought slot should be null");
    }

    @Test
    void testBuyUnit_InsufficientGold_DoesNotBuy() {
        var units = List.of(TestHelpers.createUnitDef("expensive", "ExpensiveUnit", 50, 100, 10));
        var dataLoader = TestHelpers.createMockDataLoader(units);
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(10); // Not enough for 50 cost unit
        player.refreshShop();

        player.buyUnit(0);

        assertEquals(0, player.getBench().size(), "Should not buy with insufficient gold");
    }

    @Test
    void testRefreshShop_CostsGold() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(10);

        player.refreshShop();

        assertEquals(8, player.getGold(), "Should deduct 2 gold for refresh");
    }

    @Test
    void testRefreshShop_FillsShop() {
        var units = List.of(
                TestHelpers.createUnitDef("u1", "Unit1", 1, 100, 10),
                TestHelpers.createUnitDef("u2", "Unit2", 1, 100, 10),
                TestHelpers.createUnitDef("u3", "Unit3", 1, 100, 10),
                TestHelpers.createUnitDef("u4", "Unit4", 1, 100, 10),
                TestHelpers.createUnitDef("u5", "Unit5", 1, 100, 10));
        var dataLoader = TestHelpers.createMockDataLoader(units);
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(10);

        player.refreshShop();

        assertEquals(5, player.getShop().size(), "Shop should have 5 units");
    }

    @Test
    void testRefreshShop_DeterministicWithSeed() {
        var units = List.of(
                TestHelpers.createUnitDef("u1", "Unit1", 1, 100, 10),
                TestHelpers.createUnitDef("u2", "Unit2", 1, 100, 10),
                TestHelpers.createUnitDef("u3", "Unit3", 1, 100, 10),
                TestHelpers.createUnitDef("u4", "Unit4", 1, 100, 10),
                TestHelpers.createUnitDef("u5", "Unit5", 1, 100, 10));
        var dataLoader = TestHelpers.createMockDataLoader(units);

        var player1 = createTestPlayer("P1", dataLoader, createSeededRandomProvider(123L));
        player1.setGold(100);
        player1.refreshShop();
        var shop1 =
                player1.getShop().stream().map(u -> u != null ? u.name() : null).toList();

        var player2 = createTestPlayer("P2", dataLoader, createSeededRandomProvider(123L));
        player2.setGold(100);
        player2.refreshShop();
        var shop2 =
                player2.getShop().stream().map(u -> u != null ? u.name() : null).toList();

        assertEquals(shop1, shop2, "Same seed should produce same shop");
    }

    @Test
    void testMoveUnit_BenchToBoard() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setGold(100);
        player.setLevel(3); // Allow placing units
        player.refreshShop();
        player.buyUnit(0);

        var unitId = player.getBench().get(0).getId();

        player.moveUnit(unitId, 3, 2);

        assertEquals(0, player.getBench().size(), "Bench should be empty");
        assertEquals(1, player.getBoardUnits().size(), "Board should have 1 unit");
        assertEquals(3, player.getBoardUnits().get(0).getX());
        assertEquals(2, player.getBoardUnits().get(0).getY());
    }

    @Test
    void testMoveUnit_BoardToBench() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setLevel(3);
        var def = TestHelpers.createDefaultUnitDef();
        player.addUnitToBoard(def, 3, 2);

        var unitId = player.getBoardUnits().get(0).getId();

        player.moveUnit(unitId, 0, -1); // y < 0 means bench

        assertEquals(1, player.getBench().size(), "Unit should be on bench");
        assertEquals(0, player.getBoardUnits().size(), "Board should be empty");
    }

    @Test
    void testAddUnitToBoard_RespectsLevelCap() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var player = createTestPlayer("TestPlayer", dataLoader);
        player.setLevel(1); // Can only have 1 unit on board

        var def = TestHelpers.createDefaultUnitDef();
        player.addUnitToBoard(def, 0, 0);
        player.addUnitToBoard(def, 1, 0); // Should fail

        assertEquals(1, player.getBoardUnits().size(), "Should only have 1 unit at level 1");
    }
}
