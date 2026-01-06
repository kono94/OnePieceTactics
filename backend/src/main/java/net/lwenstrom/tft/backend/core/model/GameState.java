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

        public record PlayerState(
                        String playerId,
                        String name, // Added name for UI
                        int health,
                        int gold,
                        int level,
                        int xp,
                        List<GameUnit> bench,
                        List<GameUnit> board,
                        List<net.lwenstrom.tft.backend.core.model.Trait> activeTraits,
                        List<net.lwenstrom.tft.backend.core.data.UnitDefinition> shop) {
        }

        public record CombatEvent(
                        long timestamp,
                        String type, // DAMAGE, SKILL, DEATH, MOVE
                        String sourceId,
                        String targetId,
                        int value) {
        }
}
