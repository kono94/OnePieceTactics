package net.lwenstrom.tft.backend.core.model;

import java.util.List;
import java.util.Map;

public record GameState(
        String roomId,
        String phase, // e.g., PLANNING, COMBAT, END
        long round,
        long timeRemainingMs,
        Map<String, PlayerState> players,
        // For combat phase, simplified representation of board
        List<CombatEvent> recentEvents) {
}

// Supporting records
record PlayerState(
        String playerId,
        int health,
        int gold,
        int level,
        int xp,
        List<GameUnit> bench,
        List<GameUnit> board,
        List<Trait> activeTraits) {
}

record CombatEvent(
        long timestamp,
        String type, // DAMAGE, SKILL, DEATH, MOVE
        String sourceId,
        String targetId,
        int value) {
}
