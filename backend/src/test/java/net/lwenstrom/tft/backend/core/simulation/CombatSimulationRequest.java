package net.lwenstrom.tft.backend.core.simulation;

import net.lwenstrom.tft.backend.core.GameConstants;
import net.lwenstrom.tft.backend.core.model.GameMode;

public record CombatSimulationRequest(
        GameMode mode, BoardSpec boardOne, BoardSpec boardTwo, int runs, long seed, long tickMs, long maxDurationMs) {

    public CombatSimulationRequest(GameMode mode, BoardSpec boardOne, BoardSpec boardTwo, int runs, long seed) {
        this(mode, boardOne, boardTwo, runs, seed, GameConstants.TICK_RATE_MS, GameConstants.COMBAT_PHASE_MS);
    }
}
