package net.lwenstrom.tft.backend.core.model;

/**
 * Generic effect types for data-driven trait system.
 * These can be reused across all themes (One Piece, Pokemon, etc.).
 */
public enum EffectType {
    HP,
    HP_AND_AS,
    AS,
    ARMOR_AND_MR,
    ATK_BUFF,
    START_MANA,
    ABILITY_DAMAGE,
    LOW_HP_DAMAGE,
    LIFESTEAL,
    EXTRA_ATTACK_CHANCE,
    MANA_GAIN,
    LOW_HP_AS,
    DISTANCE_DAMAGE,
    GOLD_ON_WIN,
    HEAL_AMP,
    AS_ON_CAST,
    CUSTOM // Escape hatch for theme-specific effects
}
