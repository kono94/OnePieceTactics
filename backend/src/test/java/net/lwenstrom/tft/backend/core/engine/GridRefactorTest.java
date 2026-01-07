package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import org.junit.jupiter.api.Test;

class GridRefactorTest {

    static class MockDataLoader extends DataLoader {
        public MockDataLoader() {
            super(null);
        }

        @Override
        public java.util.List<UnitDefinition> getAllUnits() {
            return Collections.emptyList();
        }
    }

    private UnitDefinition createDummyDef() {
        return new UnitDefinition("1", "Dummy", 1, 100, 100, 10, 0, 0, 0, 1.0f, 1, Collections.emptyList(), null);
    }

    @Test
    void testGridConstraints() {
        Grid grid = new Grid();
        assertEquals(4, Grid.ROWS);
        assertEquals(7, Grid.COLS);

        GameUnit u = new StandardGameUnit(createDummyDef());

        // Valid placement
        grid.placeUnit(u, 3, 6);
        assertEquals(3, u.getX());
        assertEquals(6, u.getY());

        // Invalid placement (Global coordinate 7,0 would be invalid on local grid)
        assertThrows(IllegalArgumentException.class, () -> grid.placeUnit(u, 7, 0));
        assertThrows(IllegalArgumentException.class, () -> grid.placeUnit(u, 4, 0));
    }

    @Test
    void testCombatMerging() {
        Player p1 = new Player("P1", new MockDataLoader());
        Player p2 = new Player("P2", new MockDataLoader());

        // Override IDs for deterministic sorting
        // Reflection or just reliance on CombatSystem sorting logic?
        // CombatSystem sorts by ID. Let's assume UUIDs are random, but we can verify
        // relation.

        GameUnit u1 = new StandardGameUnit(createDummyDef());
        u1.setOwnerId(p1.getId());
        // Place P1 at (0, 0)
        p1.moveUnit(u1.getId(), -1, -1); // Ensure on bench
        // Wait, Need to add to bench first manually or via buy?
        // Player has private fields. We can manually add to boardUnits via moveUnit
        // logic if in bench.
        // Let's use reflection or helper if possible?
        // Actually moveUnit checks bench. We need to add to bench.
        // buyUnit does that. But Shop is empty.
        // Let's Hack:
        try {
            java.lang.reflect.Field benchField = Player.class.getDeclaredField("bench");
            benchField.setAccessible(true);
            ((java.util.List) benchField.get(p1)).add(u1);

            GameUnit u2 = new StandardGameUnit(createDummyDef());
            u2.setOwnerId(p2.getId());
            ((java.util.List) benchField.get(p2)).add(u2);

            // Move P1 unit to (3, 3) - Front Center
            p1.moveUnit(u1.getId(), 3, 3);
            assertEquals(3, u1.getX());
            assertEquals(3, u1.getY());

            // Move P2 unit to (3, 3) - Front Center (Their perspective)
            p2.moveUnit(u2.getId(), 3, 3);
            assertEquals(3, u2.getX());
            assertEquals(3, u2.getY());

            // Start Combat
            CombatSystem cs = new CombatSystem();

            // Ensure sorting P1 < P2 for test predictability
            if (p1.getId().compareTo(p2.getId()) > 0) {
                Player tmp = p1;
                p1 = p2;
                p2 = tmp;
                GameUnit tmpU = u1;
                u1 = u2;
                u2 = tmpU;
            }

            cs.startCombat(Arrays.asList(p1, p2));

            // P1 (Bottom) should stay at (3, 3)
            assertEquals(3, u1.getX(), "P1 X should be unchanged");
            assertEquals(3, u1.getY(), "P1 Y should be unchanged");

            // P2 (Top) should mirror.
            // Original (3, 3).
            // New X = 7 - 3 = 4.
            // New Y = 6 - 3 = 3.
            assertEquals(4, u2.getX(), "P2 X should be mirrored to 4 (Global Row)");
            assertEquals(3, u2.getY(), "P2 Y should be mirrored to 3");

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
