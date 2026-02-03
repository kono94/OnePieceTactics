package net.lwenstrom.tft.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Objects;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.engine.StandardGameUnit;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import net.lwenstrom.tft.backend.core.model.LootOrb;
import net.lwenstrom.tft.backend.core.model.LootType;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LootOrbTest {

    private Player player;
    private DataLoader dataLoader;
    private RandomProvider randomProvider;

    @BeforeEach
    void setUp() {
        dataLoader = mock(DataLoader.class);
        randomProvider = mock(RandomProvider.class);
        player = new Player("TestPlayer", dataLoader, randomProvider);
    }

    @Test
    void testCollectGoldOrb() {
        var initialGold = player.getGold();
        var goldOrb = new LootOrb("orb-1", 0, 0, LootType.GOLD, "", 5);
        player.addLootOrb(goldOrb);

        player.collectOrb("orb-1");

        assertEquals(initialGold + 5, player.getGold());
        assertTrue(player.toState().lootOrbs().isEmpty());
    }

    @Test
    void testCollectUnitOrb() {
        var unitDef = new UnitDefinition(
                "unit-1",
                "Luffy",
                1,
                List.of(100, 100, 100),
                List.of(100, 100, 100),
                List.of(10, 10, 10),
                List.of(10, 10, 10),
                List.of(10, 10, 10),
                List.of(10, 10, 10),
                List.of(1.0f, 1.0f, 1.0f),
                List.of(1, 1, 1),
                List.of(),
                null);
        when(dataLoader.getAllUnits()).thenReturn(List.of(unitDef));

        var unitOrb = new LootOrb("orb-2", 0, 0, LootType.UNIT, "Luffy", 1);
        player.addLootOrb(unitOrb);

        player.collectOrb("orb-2");

        var benchCount = player.getBench().stream().filter(Objects::nonNull).count();
        assertEquals(1, benchCount);
        var firstUnit = player.getBench().stream().filter(Objects::nonNull).findFirst();
        assertTrue(firstUnit.isPresent());
        assertEquals("Luffy", firstUnit.get().getName());
        assertTrue(player.toState().lootOrbs().isEmpty());
    }

    @Test
    void testCollectUnitOrbBenchFull() {
        var unitDef = new UnitDefinition(
                "unit-1",
                "Luffy",
                1,
                List.of(100, 100, 100),
                List.of(100, 100, 100),
                List.of(10, 10, 10),
                List.of(10, 10, 10),
                List.of(10, 10, 10),
                List.of(10, 10, 10),
                List.of(1.0f, 1.0f, 1.0f),
                List.of(1, 1, 1),
                List.of(),
                null);
        when(dataLoader.getAllUnits()).thenReturn(List.of(unitDef));

        // Fill bench using the new Bench API (9 slots)
        for (int i = 0; i < 9; i++) {
            player.getBenchSlots().set(i, new StandardGameUnit(unitDef));
        }

        var initialGold = player.getGold();
        var unitOrb = new LootOrb("orb-3", 0, 0, LootType.UNIT, "Luffy", 1);
        player.addLootOrb(unitOrb);

        player.collectOrb("orb-3");

        var benchCount = player.getBench().stream().filter(Objects::nonNull).count();
        assertEquals(9, benchCount);
        // Should refund gold if bench full
        assertEquals(initialGold + 1, player.getGold());
        assertTrue(player.toState().lootOrbs().isEmpty());
    }
}
