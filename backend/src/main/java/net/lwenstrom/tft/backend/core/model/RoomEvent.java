package net.lwenstrom.tft.backend.core.model;

public record RoomEvent<T>(RoomEventType type, T payload) {}
