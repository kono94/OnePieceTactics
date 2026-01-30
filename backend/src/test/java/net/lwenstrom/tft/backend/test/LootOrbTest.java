package net.lwenstrom.tft.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.engine.Player;
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
        int initialGold = player.getGold();
        LootOrb goldOrb = new LootOrb("orb-1", 0, 0, LootType.GOLD, "", 5);
        player.addLootOrb(goldOrb);

        player.collectOrb("orb-1");

        assertEquals(initialGold + 5, player.getGold());
        assertTrue(player.toState().lootOrbs().isEmpty());
    }

    @Test
    void testCollectUnitOrb() {
        UnitDefinition unitDef = new UnitDefinition(
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

        LootOrb unitOrb = new LootOrb("orb-2", 0, 0, LootType.UNIT, "Luffy", 1);
        player.addLootOrb(unitOrb);

        player.collectOrb("orb-2");

        assertEquals(1, player.getBench().size());
        assertEquals("Luffy", player.getBench().get(0).getName());
        assertTrue(player.toState().lootOrbs().isEmpty());
    }

    @Test
    void testCollectUnitOrbBenchFull() {
        UnitDefinition unitDef = new UnitDefinition(
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

        // Fill bench (assuming max size 9)
        for (int i = 0; i < 9; i++) {
            // Manually add units to fill bench
            player.getBench().add(mock(net.lwenstrom.tft.backend.core.model.GameUnit.class));
        }

        int initialGold = player.getGold();
        LootOrb unitOrb = new LootOrb("orb-3", 0, 0, LootType.UNIT, "Luffy", 1);
        player.addLootOrb(unitOrb);

        player.collectOrb("orb-3");

        assertEquals(9, player.getBench().size());
        // Should refund gold if bench full
        assertEquals(initialGold + 1, player.getGold());
        assertTrue(player.toState().lootOrbs().isEmpty());
    }
}
