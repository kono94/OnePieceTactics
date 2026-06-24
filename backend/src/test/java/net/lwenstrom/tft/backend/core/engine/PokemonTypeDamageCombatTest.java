package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.addUnitToPlayer;
import static net.lwenstrom.tft.backend.test.TestHelpers.createMockDataLoader;
import static net.lwenstrom.tft.backend.test.TestHelpers.createSeededRandomProvider;
import static net.lwenstrom.tft.backend.test.TestHelpers.createTestCombatSystem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.AbilityPattern;
import net.lwenstrom.tft.backend.core.model.AbilityType;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.Test;

class PokemonTypeDamageCombatTest {

    @Test
    void autoAttacksApplyPokemonTypeEffectiveness() {
        var combatSystem = createTestCombatSystem();
        var p1 = new Player("P1", GameMode.POKEMON, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.POKEMON, createMockDataLoader(), createSeededRandomProvider());
        var attacker = MockUnit.create("squirtle", p1.getId())
                .withPosition(3, 3)
                .withAttackDamage(100)
                .withRange(1)
                .withTraits(Set.of("Water"));
        var target = MockUnit.create("charmander", p2.getId())
                .withPosition(3, 4)
                .withHealth(1000, 1000)
                .withTraits(Set.of("Fire"));
        target.setNextAttackTime(10_000);
        addUnitToPlayer(p1, attacker);
        addUnitToPlayer(p2, target);

        combatSystem.simulateTick(List.of(p1, p2));

        assertEquals(880, target.getCurrentHealth());
    }

    @Test
    void damageAbilitiesApplyPokemonTypeEffectiveness() {
        var ability = new AbilityDefinition(
                "Water Gun", "Damage", AbilityType.DAMAGE, AbilityPattern.SINGLE, List.of(1), List.of(100), List.of());
        var combatSystem = createTestCombatSystem();
        var p1 = new Player("P1", GameMode.POKEMON, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.POKEMON, createMockDataLoader(), createSeededRandomProvider());
        var caster = MockUnit.create("squirtle", p1.getId())
                .withPosition(3, 3)
                .withMana(50, 50)
                .withAbility(ability)
                .withTraits(Set.of("Water"));
        var target = MockUnit.create("charmander", p2.getId())
                .withPosition(3, 4)
                .withHealth(1000, 1000)
                .withTraits(Set.of("Fire"));
        target.setNextAttackTime(10_000);
        addUnitToPlayer(p1, caster);
        addUnitToPlayer(p2, target);

        combatSystem.simulateTick(List.of(p1, p2));

        assertEquals(880, target.getCurrentHealth());
    }
}
