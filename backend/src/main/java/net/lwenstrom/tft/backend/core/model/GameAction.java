package net.lwenstrom.tft.backend.core.model;

public record GameAction(
        String type, // BUY, SELL, MOVE, REROLL, EXP
        String playerId,
        String unitId,
        int targetX,
        int targetY,
        int shopIndex) {
}
