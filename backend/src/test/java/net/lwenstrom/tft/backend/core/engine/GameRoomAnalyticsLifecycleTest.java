package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.analytics.GameplayAnalyticsRecorder;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.test.TestClock;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameRoomAnalyticsLifecycleTest {
    private TestClock clock;
    private RecordingAnalyticsRecorder recorder;
    private GameRoom room;

    @BeforeEach
    void setUp() {
        clock = TestHelpers.createTestClock();
        recorder = new RecordingAnalyticsRecorder();
        room = new GameRoom(
                "analytics-room",
                TestHelpers.createMockDataLoader(),
                TestHelpers.createMockRegistry(),
                clock,
                TestHelpers.createSeededRandomProvider(),
                GameMode.ONEPIECE,
                recorder);
    }

    @Test
    void recordsMatchAndRoundForHumansOnly() {
        room.tryAddPlayer("First", "browser-1", "token-1").orElseThrow();
        room.tryAddPlayer("Second", "browser-2", "token-2").orElseThrow();
        room.addBot().orElseThrow();

        room.startMatch();

        assertEquals(1, recorder.matchStarts.size());
        assertEquals(2, recorder.matchStarts.getFirst().size());
        assertTrue(recorder.matchStarts.getFirst().stream().noneMatch(Player::isBot));

        clock.advance(room.getState().timeRemainingMs() + 1);
        room.tick();

        assertEquals(1, recorder.roundStarts.size());
        assertEquals(1, recorder.roundStarts.getFirst().round());
        assertEquals(2, recorder.roundStarts.getFirst().players().size());
    }

    @Test
    void reconnectBeforeGracePeriodPreventsAbandonment() {
        var player = room.tryAddPlayer("First", "browser-1", "secret-token").orElseThrow();
        room.tryAddPlayer("Second", "browser-2", "other-token").orElseThrow();
        room.startMatch();

        room.disconnectPlayer(player.getId());
        assertNotNull(player.getDisconnectedAt());
        clock.advance(59_999L);
        room.tick();

        assertTrue(room.reconnectPlayer("secret-token").isPresent());
        assertNull(player.getDisconnectedAt());
        clock.advance(60_001L);
        room.tick();

        assertFalse(player.isAbandoned());
        assertTrue(recorder.abandonedPlayerIds.isEmpty());
    }

    @Test
    void recordsAbandonmentOnceAfterGracePeriod() {
        var player = room.tryAddPlayer("First", "browser-1", "secret-token").orElseThrow();
        room.tryAddPlayer("Second", "browser-2", "other-token").orElseThrow();
        room.startMatch();

        room.disconnectPlayer(player.getId());
        clock.advance(60_000L);
        room.tick();
        room.tick();

        assertTrue(player.isAbandoned());
        assertEquals(List.of(player.getId()), recorder.abandonedPlayerIds);
        assertTrue(room.reconnectPlayer("secret-token").isPresent());
        assertTrue(player.isAbandoned());
    }

    @Test
    void disconnectInLobbyStillRemovesPlayer() {
        var player = room.tryAddPlayer("First", "browser-1", "secret-token").orElseThrow();

        room.disconnectPlayer(player.getId());

        assertNull(room.getPlayer(player.getId()));
    }

    @Test
    void completesMatchImmediatelyWhenOnlyBotsRemain() {
        var player = room.tryAddPlayer("First", "browser-1", "token-1").orElseThrow();
        room.startMatch();
        player.setHealth(0);

        room.tick();

        assertEquals(GamePhase.END_CELEBRATION, room.getState().phase());
        assertEquals(8, player.getPlace());
        assertEquals(1, recorder.matchCompletions);
    }

    private static final class RecordingAnalyticsRecorder implements GameplayAnalyticsRecorder {
        private final List<List<Player>> matchStarts = new ArrayList<>();
        private final List<RoundStart> roundStarts = new ArrayList<>();
        private final List<String> abandonedPlayerIds = new ArrayList<>();
        private int matchCompletions;

        @Override
        public void matchStarted(String roomId, GameMode mode, long occurredAt, List<Player> players) {
            matchStarts.add(players);
        }

        @Override
        public void roundStarted(String roomId, int round, long occurredAt, List<Player> players) {
            roundStarts.add(new RoundStart(round, players));
        }

        @Override
        public void playerAbandoned(String roomId, String playerId, long occurredAt) {
            abandonedPlayerIds.add(playerId);
        }

        @Override
        public void matchCompleted(String roomId, int finalRound, long occurredAt, List<Player> players) {
            matchCompletions++;
        }
    }

    private record RoundStart(int round, List<Player> players) {}
}
