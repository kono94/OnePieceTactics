package net.lwenstrom.tft.backend.core.analytics;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.model.GameMode;

public interface GameplayAnalyticsRecorder {
    GameplayAnalyticsRecorder NO_OP = new GameplayAnalyticsRecorder() {};

    default void matchStarted(String roomId, GameMode mode, long occurredAt, List<Player> players) {}

    default void roundStarted(String roomId, int round, long occurredAt, List<Player> players) {}

    default void combatResolved(
            String roomId,
            int round,
            long occurredAt,
            String winnerId,
            String loserId,
            boolean draw,
            List<Player> participants) {}

    default void playerAbandoned(String roomId, String playerId, long occurredAt) {}

    default void matchCompleted(String roomId, int finalRound, long occurredAt, List<Player> players) {}
}
