package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.test.TestClock;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class GameEndCleanupTest {

    @Test
    void testGame_TransitionsToEnd_WhenOnePlayerRemains() {
        var unitDef = TestHelpers.createUnitDef("unit", "Unit", 1, 100, 10);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(unitDef));
        var testClock = new TestClock();
        var room = TestHelpers.createTestGameRoom(dataLoader, testClock);

        var p1 = room.addPlayer("P1");
        var p2 = room.addPlayer("P2");

        // Give P1 a unit so combat can proceed
        p1.setLevel(5);
        p1.addUnitToBoard(unitDef, 3, 0);

        room.startMatch();

        // Now eliminate all players except P1 by setting their health to 0
        room.getPlayers().stream().filter(p -> !p.getId().equals(p1.getId())).forEach(p -> p.setHealth(0));

        // Transition through phases to trigger game-end check
        // Move time forward to end combat phase
        testClock.advance(30000);
        room.tick(); // This should detect combat end and transition

        // Force next phase transition
        testClock.advance(30000);
        room.tick();

        // After transitioning to planning with only P1 alive, game should end
        assertTrue(room.isEnded(), "Game should transition to END phase when one player remains");
        assertEquals(GamePhase.END, room.getState().phase());
    }

    @Test
    void testRoom_RemovedFromEngine_WhenGameEnds() {
        var unitDef = TestHelpers.createUnitDef("unit", "Unit", 1, 100, 10);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(unitDef));
        var testClock = new TestClock();
        var randomProvider = TestHelpers.createSeededRandomProvider();
        var registry = TestHelpers.createMockRegistry();

        var engine = new GameEngine(dataLoader, registry, testClock, randomProvider);
        var room = engine.createRoom("test-room");

        var p1 = room.addPlayer("P1");
        var p2 = room.addPlayer("P2");

        p1.setLevel(5);
        p1.addUnitToBoard(unitDef, 3, 0);

        room.startMatch();

        // Verify room exists before game ends
        assertNotNull(engine.getRoom("test-room"), "Room should exist before game ends");

        // Eliminate all except P1
        room.getPlayers().stream().filter(p -> !p.getId().equals(p1.getId())).forEach(p -> p.setHealth(0));

        // Transition through phases
        testClock.advance(30000);
        engine.tick();
        testClock.advance(30000);
        engine.tick();

        assertTrue(room.isEnded(), "Game should have ended");
        assertNull(engine.getRoom("test-room"), "Room should be removed from engine when game ends");
    }

    @Test
    void testRoom_NotRemoved_WhenGameStillActive() {
        var unitDef = TestHelpers.createUnitDef("unit", "Unit", 1, 500, 100);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(unitDef));
        var testClock = new TestClock();
        var randomProvider = TestHelpers.createSeededRandomProvider();
        var registry = TestHelpers.createMockRegistry();

        var engine = new GameEngine(dataLoader, registry, testClock, randomProvider);
        var room = engine.createRoom("active-room");

        var p1 = room.addPlayer("P1");
        var p2 = room.addPlayer("P2");

        // Both players have healthy units
        p1.setLevel(5);
        p2.setLevel(5);
        p1.addUnitToBoard(unitDef, 3, 0);
        p2.addUnitToBoard(unitDef, 3, 0);

        room.startMatch();
        // All 8 players (2 + 6 bots) should have health > 0

        // Tick a few times
        for (int i = 0; i < 5; i++) {
            testClock.advance(5000);
            engine.tick();
        }

        assertFalse(room.isEnded(), "Game should still be active");
        assertNotNull(engine.getRoom("active-room"), "Room should still exist when game is active");
    }
}
