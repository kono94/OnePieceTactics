package net.lwenstrom.tft.backend.core.engine;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;

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
                List<String> traits,
                AbilityDefinition ability) {
}
