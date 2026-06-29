package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.test.TestClock;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class GameRoomSoloTrainingReadyTest {

    @Test
    void soloHumanWithBotsStaysInPlanningAfterTimerExpires() {
        var testClock = new TestClock();
        var room = createRoom(testClock);
        var human = room.addPlayer("Human");

        room.startMatch();

        assertEquals(GamePhase.PLANNING, room.getState().phase());
        assertTrue(room.getState().planningTimerPaused());
        assertEquals(human.getId(), room.getState().planningReadyPlayerId());

        var bot = room.getPlayers().stream().filter(Player::isBot).findFirst().orElseThrow();
        room.removePlayer(bot.getId());
        assertEquals(room.getState().totalPhaseDuration(), room.getState().timeRemainingMs());

        testClock.advance(room.getState().totalPhaseDuration() + 1);
        room.tick();

        assertEquals(GamePhase.PLANNING, room.getState().phase());
        assertTrue(room.getState().planningTimerPaused());
        assertEquals(room.getState().totalPhaseDuration(), room.getState().timeRemainingMs());
    }

    @Test
    void soloHumanReadyStartsCombat() {
        var testClock = new TestClock();
        var room = createRoom(testClock);
        var human = room.addPlayer("Human");

        room.startMatch();

        assertTrue(room.readyForCombat(human.getId()));
        assertEquals(GamePhase.COMBAT, room.getState().phase());
        assertFalse(room.getState().planningTimerPaused());
        assertNull(room.getState().planningReadyPlayerId());
    }

    @Test
    void botDeadWrongAndSecondHumanCannotReady() {
        var botAttemptRoom = createSoloRoom();
        var bot = botAttemptRoom.getPlayers().stream()
                .filter(Player::isBot)
                .findFirst()
                .orElseThrow();

        assertFalse(botAttemptRoom.readyForCombat(bot.getId()));
        assertEquals(GamePhase.PLANNING, botAttemptRoom.getState().phase());

        var wrongIdRoom = createSoloRoom();

        assertFalse(wrongIdRoom.readyForCombat("not-a-player"));
        assertEquals(GamePhase.PLANNING, wrongIdRoom.getState().phase());

        var deadHumanRoom = createSoloRoom();
        var deadHuman = deadHumanRoom.getPlayers().stream()
                .filter(player -> !player.isBot())
                .findFirst()
                .orElseThrow();
        deadHuman.setHealth(0);

        assertFalse(deadHumanRoom.readyForCombat(deadHuman.getId()));
        assertEquals(GamePhase.PLANNING, deadHumanRoom.getState().phase());

        var twoHumanRoom = createRoom(new TestClock());
        var firstHuman = twoHumanRoom.addPlayer("P1");
        twoHumanRoom.addPlayer("P2");
        twoHumanRoom.startMatch();

        assertFalse(twoHumanRoom.getState().planningTimerPaused());
        assertNull(twoHumanRoom.getState().planningReadyPlayerId());
        assertFalse(twoHumanRoom.readyForCombat(firstHuman.getId()));
        assertEquals(GamePhase.PLANNING, twoHumanRoom.getState().phase());
    }

    @Test
    void twoRealHumansUseNormalCountdown() {
        var testClock = new TestClock();
        var room = createRoom(testClock);
        room.addPlayer("P1");
        room.addPlayer("P2");
        room.startMatch();

        assertFalse(room.getState().planningTimerPaused());

        testClock.advance(room.getState().totalPhaseDuration() + 1);
        room.tick();

        assertEquals(GamePhase.COMBAT, room.getState().phase());
    }

    @Test
    void onlyOneTotalAliveStillEndsGame() {
        var testClock = new TestClock();
        var room = createRoom(testClock);
        var human = room.addPlayer("Human");
        room.startMatch();

        room.getPlayers().stream()
                .filter(player -> !player.getId().equals(human.getId()))
                .forEach(player -> player.setHealth(0));

        room.tick();

        assertFalse(room.getState().planningTimerPaused());
        assertNull(room.getState().planningReadyPlayerId());

        testClock.advance(room.getState().totalPhaseDuration() + 1);
        room.tick();
        room.tick();

        assertEquals(GamePhase.END_CELEBRATION, room.getState().phase());
        assertEquals(1, human.getPlace());
    }

    private GameRoom createSoloRoom() {
        var room = createRoom(new TestClock());
        room.addPlayer("Human");
        room.startMatch();
        return room;
    }

    private GameRoom createRoom(TestClock testClock) {
        var unitDef = TestHelpers.createUnitDef("unit", "Unit", 1, 100, 10);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(unitDef));
        return TestHelpers.createTestGameRoom(dataLoader, testClock);
    }
}
