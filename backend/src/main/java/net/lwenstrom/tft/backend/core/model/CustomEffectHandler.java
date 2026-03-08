package net.lwenstrom.tft.backend.core.model;

import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * Interface for theme-specific trait effects that cannot be generalized.
 * Implement this in theme packages (e.g., onepiece, pokemon) for unique
 * mechanics.
 */
public interface CustomEffectHandler {

    String getHandlerId();

    void apply(int count, List<GameUnit> units, JsonNode values);
}
