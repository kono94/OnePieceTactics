package net.lwenstrom.tft.backend.core.model;

public enum AbilityType {
    DAMAGE, // Deal damage to enemies
    STUN, // Disables target for X seconds (value = stun duration in ticks)
    HEAL, // Restore HP to self or allies (value = heal amount)
    BUFF_ATK, // Increase ATK for all allied units (value = % increase)
    BUFF_SPD, // Decrease attack cooldown for allies (value = % increase)
    SHIELD // Grant shield to self or allies (value = shield amount)
}
