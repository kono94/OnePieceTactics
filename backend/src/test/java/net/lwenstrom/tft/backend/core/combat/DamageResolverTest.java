package net.lwenstrom.tft.backend.core.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.Test;

class DamageResolverTest {
    private static final ElementalAffinityConfig AFFINITY = new ElementalAffinityConfig(
            1.0,
            1.2,
            0.8,
            List.of("fire", "water", "grass", "ice"),
            List.of(
                    new ElementalRelationship("fire", List.of("grass"), List.of("fire", "water")),
                    new ElementalRelationship("water", List.of("fire"), List.of("water", "grass")),
                    new ElementalRelationship("grass", List.of("water"), List.of("fire", "grass", "ice")),
                    new ElementalRelationship("ice", List.of("grass"), List.of("fire"))));

    private final DamageResolver resolver = new DamageResolver(AFFINITY);

    @Test
    void derivesAttackerElementFromTraits() {
        var attacker = MockUnit.create("attacker", "one").withTraits(Set.of("fire"));
        var grass = MockUnit.create("grass", "two").withTraits(Set.of("grass"));
        var water = MockUnit.create("water", "two").withTraits(Set.of("water"));

        assertEquals(120, resolver.apply(attacker, grass, 100));
        assertEquals(80, resolver.apply(attacker, water, 100));
    }

    @Test
    void multipliesDefenderTraitsAndChoosesTheBestAttackerTrait() {
        var attacker = MockUnit.create("attacker", "one").withTraits(Set.of("fire", "water"));
        var defender = MockUnit.create("defender", "two").withTraits(Set.of("grass", "fire"));

        assertEquals(96, resolver.apply(attacker, defender, 100));
    }

    @Test
    void damageUsesSourceTraitsRegardlessOfDamageOrigin() {
        var attacker = MockUnit.create("attacker", "one").withTraits(Set.of("fire"));
        var defender = MockUnit.create("defender", "two").withTraits(Set.of("water"));

        assertEquals(80, resolver.apply(attacker, defender, 100));
    }

    @Test
    void neutralModesRemainUntyped() {
        var attacker = MockUnit.create("attacker", "one").withTraits(Set.of("fire"));
        var defender = MockUnit.create("defender", "two").withTraits(Set.of("water"));
        var neutral = new DamageResolver(ElementalAffinityConfig.neutral());

        assertEquals(100, neutral.apply(attacker, defender, 100));
    }
}
