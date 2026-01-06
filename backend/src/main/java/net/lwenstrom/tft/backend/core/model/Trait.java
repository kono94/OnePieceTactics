package net.lwenstrom.tft.backend.core.model;

public interface Trait {
    String getId();

    String getName();

    String getDescription();
    // Logic for bonuses would be handled by the engine based on this ID
}
