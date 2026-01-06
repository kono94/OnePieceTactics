package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.data.DataLoader;
import net.lwenstrom.tft.backend.core.data.UnitDefinition;
import net.lwenstrom.tft.backend.engine.GameRoom;
import net.lwenstrom.tft.backend.engine.Player;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {

    @Test
    public void testGameLoopSimulation() {
        // Mock DataLoader
        DataLoader dataLoader = new DataLoader(null) {
            @Override
            public List<UnitDefinition> getAllUnits() {
                return List
                        .of(new UnitDefinition("1", "TestUnit", 1, 100, 50, 50, 0, 10, 10, 1.0f, 1, List.of("Trait")));
            }

            @Override
            public UnitDefinition getUnitDefinition(String id) {
                return getAllUnits().get(0);
            }
        };

        GameRoom room = new GameRoom("sim-room", dataLoader);

        // Initial State
        assertEquals(1, room.getState().round(), "Should start at Round 1");
        assertEquals("PLANNING", room.getState().phase(), "Should start in PLANNING phase");

        // Simulate Planning Phase (30s = 300 ticks of 100ms)
        // We can cheat by advancing time if we mock time, but here we might just run
        // enough ticks?
        // Or we can modify GameRoom to accept a time provider.
        // For simplicity, let's just assert state transitions after calling tick many
        // times?
        // Actually, without time mocking, waiting 30s in a test is bad.
        // I should probably inject a Clock or TimeProvider, but for now I can just hack
        // it by forcing phase end?
        // But `tick` relies on System.currentTimeMillis().

        // BETTER APPROACH: Verify logic by checking `GameRoom` internals if possible,
        // or just accept that I can't easily wait 30s.
        // Alternative: Use reflection to set phaseEndTime to "now".

        try {
            java.lang.reflect.Field endTimeField = GameRoom.class.getDeclaredField("phaseEndTime");
            endTimeField.setAccessible(true);

            // Fast forward Planning
            endTimeField.set(room, System.currentTimeMillis() - 1);
            room.tick(); // Should trigger nextPhase()

            assertEquals("COMBAT", room.getState().phase(), "Should switch to COMBAT phase");
            assertEquals(1, room.getState().round(), "Round should still be 1 in Combat");

            // Fast forward Combat
            endTimeField.set(room, System.currentTimeMillis() - 1);
            room.tick(); // Should trigger nextPhase()

            assertEquals("PLANNING", room.getState().phase(), "Should switch back to PLANNING phase");
            assertEquals(2, room.getState().round(), "Round should be 2 now");

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testCombatInteraction() {
        DataLoader dataLoader = new DataLoader(null) {
            @Override
            public List<UnitDefinition> getAllUnits() {
                return List
                        .of(new UnitDefinition("1", "TestUnit", 1, 100, 50, 50, 0, 10, 10, 1.0f, 1, List.of("Trait")));
            }

            @Override
            public UnitDefinition getUnitDefinition(String id) {
                return getAllUnits().get(0);
            }
        };

        GameRoom room = new GameRoom("combat-room", dataLoader);
        Player p1 = room.addPlayer("P1");
        Player p2 = room.addPlayer("P2");

        // Setup units
        p1.buyUnit(0);
        String u1Id = p1.getBench().get(0).getId();
        p1.moveUnit(u1Id, 3, 3);

        p2.buyUnit(0);
        String u2Id = p2.getBench().get(0).getId();
        p2.moveUnit(u2Id, 3, 4); // Adjacent

        // Force Combat Phase
        try {
            java.lang.reflect.Field phaseField = GameRoom.class.getDeclaredField("phase");
            phaseField.setAccessible(true);
            phaseField.set(room, GameRoom.GamePhase.COMBAT);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Run ticks
        for (int i = 0; i < 20; i++) { // Enough ticks for attack (100ms tick, 1.0 atk speed = 10 ticks/atk)
            room.tick();
        }

        // Assert damage taken
        // P2's unit should have taken damage from P1's unit
        // P1's unit has 50 AD. P2's unit has 100 HP. Should be 50 HP.
        // Assume simultaneous attacks?

        int hp1 = p1.getBoardUnits().get(0).getCurrentHealth();
        int hp2 = p2.getBoardUnits().get(0).getCurrentHealth();

        assertTrue(hp1 < 100 || hp2 < 100, "Units should have taken damage");
    }
}
