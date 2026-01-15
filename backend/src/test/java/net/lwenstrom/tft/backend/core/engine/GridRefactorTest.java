package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import org.junit.jupiter.api.Test;

class GridRefactorTest {

    static class MockDataLoader extends DataLoader {
        public MockDataLoader(GameModeRegistry registry) {
            super(registry);
        }

        @Override
        public java.util.List<UnitDefinition> getAllUnits() {
            return Collections.emptyList();
        }
    }

    private GameModeRegistry createMockRegistry() {
        GameModeProvider provider = new GameModeProvider() {
            @Override
            public GameMode getMode() {
                return GameMode.ONEPIECE;
            }

            @Override
            public String getUnitsPath() {
                return "";
            }

            @Override
            public String getTraitsPath() {
                return "";
            }

            @Override
            public void registerTraitEffects(TraitManager traitManager) {}
        };
        return new GameModeRegistry(List.of(provider), "onepiece");
    }

    private UnitDefinition createDummyDef() {
        return new UnitDefinition("1", "Dummy", 1, 100, 100, 10, 0, 0, 0, 1.0f, 1, Collections.emptyList(), null);
    }

    @Test
    void testGridConstraints() {
        Grid grid = new Grid();
        assertEquals(Grid.PLAYER_ROWS, Grid.PLAYER_ROWS); // Tautology but verifies internal consistency check
        assertEquals(4, Grid.PLAYER_ROWS);
        assertEquals(7, Grid.COLS);

        GameUnit u = new StandardGameUnit(createDummyDef());

        // Valid placement
        grid.placeUnit(u, 3, 3); // 3 is max index for 4 rows? No, 0-3.
        assertEquals(3, u.getX());
        assertEquals(3, u.getY());

        // Invalid placement (Global coordinate 7,0 would be invalid on local grid 0-6,
        // 0-3)
        assertThrows(IllegalArgumentException.class, () -> grid.placeUnit(u, Grid.COLS, 0));
        assertThrows(IllegalArgumentException.class, () -> grid.placeUnit(u, 0, Grid.PLAYER_ROWS));
    }

    @Test
    void testCombatMerging() {
        GameModeRegistry registry = createMockRegistry();
        Player p1 = new Player("P1", new MockDataLoader(registry));
        Player p2 = new Player("P2", new MockDataLoader(registry));

        GameUnit u1 = new StandardGameUnit(createDummyDef());
        u1.setOwnerId(p1.getId());

        try {
            java.lang.reflect.Field benchField = Player.class.getDeclaredField("bench");
            benchField.setAccessible(true);
            ((java.util.List) benchField.get(p1)).add(u1);

            GameUnit u2 = new StandardGameUnit(createDummyDef());
            u2.setOwnerId(p2.getId());
            ((java.util.List) benchField.get(p2)).add(u2);

            // Move P1 unit to (3, 3) - Back Center (Local Row 3 is Backline/Edge)
            p1.moveUnit(u1.getId(), 3, 3);
            assertEquals(3, u1.getX());
            assertEquals(3, u1.getY());

            // Move P2 unit to (3, 3) - Back Center
            p2.moveUnit(u2.getId(), 3, 3);
            assertEquals(3, u2.getX());
            assertEquals(3, u2.getY());

            // Start Combat
            CombatSystem cs = new CombatSystem();

            // Ensure sorting P1 < P2 for test predictability (P1=Top, P2=Bottom)
            if (p1.getId().compareTo(p2.getId()) > 0) {
                Player tmp = p1;
                p1 = p2;
                p2 = tmp;
                GameUnit tmpU = u1;
                u1 = u2;
                u2 = tmpU;
            }

            cs.startCombat(Arrays.asList(p1, p2));

            // P1 (Top) at (3,3) Backline -> Should mirror to Arena Top Edge (0)
            // X: 6 - 3 = 3
            // Y: 3 - 3 = 0
            assertEquals(3, u1.getX(), "P1 X should be 3");
            assertEquals(0, u1.getY(), "P1 Y should be 0 (Top Edge)");

            // P2 (Bottom) at (3,3) Backline -> Should offset to Arena Bottom Edge (7)
            // X: 3
            // Y: 4 + 3 = 7
            assertEquals(3, u2.getX(), "P2 X should be 3");
            assertEquals(7, u2.getY(), "P2 Y should be 7 (Bottom Edge)");

            // End Combat
            cs.endCombat(Arrays.asList(p1, p2));

            // Restore
            assertEquals(3, u1.getX());
            assertEquals(3, u1.getY());
            assertEquals(3, u2.getX());
            assertEquals(3, u2.getY());

        } catch (Exception e) {
            fail(e);
        }
    }
}
