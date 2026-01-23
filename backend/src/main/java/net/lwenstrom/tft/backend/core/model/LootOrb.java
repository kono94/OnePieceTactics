package net.lwenstrom.tft.backend.core.model;

public record LootOrb(String id, int x, int y, LootType type, String contentId, int amount) {}
