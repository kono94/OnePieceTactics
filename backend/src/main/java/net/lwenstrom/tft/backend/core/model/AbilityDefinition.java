package net.lwenstrom.tft.backend.core.model;

public record AbilityDefinition(
        String name, String description, AbilityType type, String pattern, int value, int range) {

    // Factory method for backward-compatible JSON parsing (type as String)
    public static AbilityDefinition fromJson(
            String name, String description, String type, String pattern, int value, int range) {
        var abilityType =
                switch (type.toUpperCase()) {
                    case "DMG", "DAMAGE" -> AbilityType.DAMAGE;
                    case "STUN" -> AbilityType.STUN;
                    case "HEAL" -> AbilityType.HEAL;
                    case "BUFF_ATK" -> AbilityType.BUFF_ATK;
                    case "BUFF_SPD" -> AbilityType.BUFF_SPD;
                    default -> AbilityType.DAMAGE;
                };
        return new AbilityDefinition(name, description, abilityType, pattern, value, range);
    }
}
