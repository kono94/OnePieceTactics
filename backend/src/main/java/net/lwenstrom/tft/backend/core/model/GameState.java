package net.lwenstrom.tft.backend.core.model;

import java.util.List;
import java.util.Map;
import net.lwenstrom.tft.backend.core.engine.CombatSystem;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;

public record GameState(
        String roomId,
        String hostId,
        GamePhase phase,
        long round,
        long timeRemainingMs,
        long totalPhaseDuration,
        Map<String, PlayerState> players,
        Map<String, String> matchups,
        List<CombatEvent> recentEvents,
        Map<String, CombatSystem.DamageEntry> damageLog,
        GameMode gameMode) {

    public record PlayerState(
            String playerId,
            String name, // Added name for UI
            int health,
            int gold,
            int level,
            int xp,
            int nextLevelXp, // Added for frontend scaling
            Integer place, // Added for game end
            String combatSide, // "TOP" or "BOTTOM"
            List<GameUnit> bench,
            List<GameUnit> board,
            List<Trait> activeTraits,
            List<UnitDefinition> shop) {}

    public record CombatEvent(
            long timestamp,
            String type, // DAMAGE, SKILL, DEATH, MOVE
            String sourceId,
            String targetId,
            int value) {}
}
