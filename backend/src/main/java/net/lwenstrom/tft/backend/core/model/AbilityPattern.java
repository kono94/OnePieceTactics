package net.lwenstrom.tft.backend.core.model;

/**
 * Defines the targeting pattern for unit abilities.
 */
public enum AbilityPattern {
    SINGLE, // Targets a single unit (enemy or ally)
    LINE, // Targets units in a straight line from source to target
    SURROUND // Targets units in a square/radius around a center point
}
