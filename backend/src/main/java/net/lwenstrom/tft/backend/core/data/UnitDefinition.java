package net.lwenstrom.tft.backend.core.data;

import java.util.List;

public record UnitDefinition(
        String id,
        String name,
        int cost,
        int maxHealth,
        int maxMana,
        int attackDamage,
        int abilityPower,
        int armor,
        int magicResist,
        float attackSpeed,
        int range,
        List<String> traits) {
}
