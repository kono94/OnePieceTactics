package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AugmentDefinition;
import net.lwenstrom.tft.backend.core.model.AugmentEffectType;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.test.TestClock;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class GameRoomAugmentTest {

    @Test
    void roundTwoChoicesDoNotPausePlanningAndSelectionDoesNotResetTimer() {
        var clock = new TestClock();
        var room = createRoom(clock, TestHelpers.createDefaultAugments());
        var first = room.addPlayer("P1");
        var second = room.addPlayer("P2");

        startPlanningRound(room, 2);

        var initialTimeRemaining = room.getState().timeRemainingMs();

        assertFalse(room.getState().planningTimerPaused());
        assertNull(room.getState().planningPauseReason());
        assertEquals(
                3, room.getState().players().get(first.getId()).augmentChoices().size());
        assertEquals(
                3,
                room.getState().players().get(second.getId()).augmentChoices().size());

        var firstChoice = first.getAugmentChoices().get(0).id();
        assertTrue(room.selectAugment(first.getId(), firstChoice));

        clock.advance(5000);
        room.tick();
        assertFalse(room.getState().planningTimerPaused());
        assertTrue(room.getState().timeRemainingMs() < initialTimeRemaining);

        var secondChoice = second.getAugmentChoices().get(0).id();
        assertTrue(room.selectAugment(second.getId(), secondChoice));

        assertFalse(room.getState().planningTimerPaused());
        assertTrue(room.getState().timeRemainingMs() < initialTimeRemaining);
    }

    @Test
    void pendingChoicesAreRandomlySelectedWhenPlanningExpires() {
        var clock = new TestClock();
        var room = createRoom(clock, TestHelpers.createDefaultAugments());
        var first = room.addPlayer("P1");
        var second = room.addPlayer("P2");

        startPlanningRound(room, 2);

        assertEquals(3, first.getAugmentChoices().size());
        assertEquals(3, second.getAugmentChoices().size());

        clock.advance(room.getState().timeRemainingMs() + 1);
        room.tick();

        assertEquals(GamePhase.COMBAT, room.getState().phase());
        assertEquals(0, first.getAugmentChoices().size());
        assertEquals(0, second.getAugmentChoices().size());
        assertEquals(1, first.getSelectedAugments().size());
        assertEquals(1, second.getSelectedAugments().size());
    }

    @Test
    void invalidSelectionDoesNotClearChoicesOrApplyReward() {
        var room = createRoom(
                new TestClock(),
                List.of(
                        TestHelpers.createAugment("treasure-cache", AugmentEffectType.GOLD, List.of(8, 14, 22)),
                        TestHelpers.createAugment("training-arc", AugmentEffectType.XP, List.of(4, 8, 12)),
                        TestHelpers.createAugment(
                                "guarded-formation", AugmentEffectType.TEAM_DAMAGE_REDUCTION, List.of(5, 10, 15))));
        var player = room.addPlayer("P1");
        room.addPlayer("P2");

        startPlanningRound(room, 2);
        var gold = player.getGold();

        assertFalse(room.selectAugment(player.getId(), "missing-augment"));
        assertEquals(gold, player.getGold());
        assertEquals(3, player.getAugmentChoices().size());
        assertEquals(0, player.getSelectedAugments().size());
    }

    private GameRoom createRoom(TestClock clock, List<AugmentDefinition> augments) {
        var unitDef = TestHelpers.createUnitDef("unit", "Unit", 1, 100, 10);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(unitDef), augments);
        return TestHelpers.createTestGameRoom(dataLoader, clock);
    }

    private void startPlanningRound(GameRoom room, int round) {
        for (var i = 0; i < round; i++) {
            TestHelpers.setPhase(room, net.lwenstrom.tft.backend.core.model.GamePhase.PLANNING);
        }
    }
}
