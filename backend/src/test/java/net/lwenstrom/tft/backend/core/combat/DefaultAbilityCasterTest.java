package net.lwenstrom.tft.backend.core.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.AbilityPattern;
import net.lwenstrom.tft.backend.core.model.AbilityType;
import net.lwenstrom.tft.backend.core.model.StunModifier;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.Test;

class DefaultAbilityCasterTest {

    @Test
    void surroundDamageAbilityRespectsTargetLimit() {
        var ability = new AbilityDefinition(
                "Limited Thunder",
                "Hits limited targets.",
                AbilityType.DAMAGE,
                AbilityPattern.SURROUND,
                List.of(2, 2, 2),
                List.of(10, 10, 10),
                List.of(new StunModifier(List.of(1, 1, 1))),
                List.of(3, 3, 3));
        var source = MockUnit.create("source", "P1").withPosition(3, 3).withAbility(ability);
        var enemies = List.of(
                MockUnit.create("enemy-a", "P2").withPosition(2, 3),
                MockUnit.create("enemy-b", "P2").withPosition(3, 2),
                MockUnit.create("enemy-c", "P2").withPosition(3, 4),
                MockUnit.create("enemy-d", "P2").withPosition(4, 3),
                MockUnit.create("enemy-e", "P2").withPosition(5, 3));
        var allUnits = new java.util.ArrayList<net.lwenstrom.tft.backend.core.model.GameUnit>();
        allUnits.add(source);
        allUnits.addAll(enemies);

        new DefaultAbilityCaster()
                .castAbility(
                        source,
                        allUnits,
                        (unit, ignored) -> enemies.getFirst(),
                        new AbilityCaster.CombatStatCallback() {},
                        0L);

        assertEquals(
                3,
                enemies.stream().filter(enemy -> enemy.getCurrentHealth() == 90).count());
        assertEquals(
                3,
                enemies.stream()
                        .filter(enemy -> enemy.getStunSecondsRemaining() == 1)
                        .count());
        assertEquals(
                2,
                enemies.stream()
                        .filter(enemy -> enemy.getCurrentHealth() == 100)
                        .count());
        assertEquals(
                2,
                enemies.stream()
                        .filter(enemy -> enemy.getStunSecondsRemaining() == 0)
                        .count());
    }
}
