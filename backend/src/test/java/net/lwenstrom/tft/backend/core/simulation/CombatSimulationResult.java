package net.lwenstrom.tft.backend.core.simulation;

import java.util.Map;
import net.lwenstrom.tft.backend.core.model.GameMode;

public record CombatSimulationResult(
        GameMode mode,
        String boardOneName,
        String boardTwoName,
        int runs,
        int boardOneWins,
        int boardTwoWins,
        int draws,
        double averageDurationMs,
        double averageBoardOneRemainingHealth,
        double averageBoardTwoRemainingHealth,
        Map<String, UnitCombatStats> unitStats) {

    public double boardOneWinRate() {
        return runs == 0 ? 0 : (double) boardOneWins / runs;
    }

    public double boardTwoWinRate() {
        return runs == 0 ? 0 : (double) boardTwoWins / runs;
    }

    public double drawRate() {
        return runs == 0 ? 0 : (double) draws / runs;
    }
}
