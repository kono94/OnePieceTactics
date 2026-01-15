package net.lwenstrom.tft.backend.core.model;

public record GameAction(
        ActionType type, String playerId, String unitId, Integer targetX, Integer targetY, Integer shopIndex) {}
