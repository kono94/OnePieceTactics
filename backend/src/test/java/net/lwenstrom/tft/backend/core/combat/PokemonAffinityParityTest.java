package net.lwenstrom.tft.backend.core.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.Test;

class PokemonAffinityParityTest {
    private final DamageResolver resolver =
            new DamageResolver(new ElementalAffinityLoader().load("/data/affinities_pokemon.json"));

    @Test
    void preservesSingleTypeTraitDerivedParity() {
        var water = MockUnit.create("squirtle", "one").withTraits(Set.of("Water"));
        var fire = MockUnit.create("charmander", "two").withTraits(Set.of("Fire"));

        assertEquals(120, resolver.apply(water, fire, 100));
        assertEquals(80, resolver.apply(fire, water, 100));
    }

    @Test
    void preservesDualDefenderAndBestAttackingTypeParity() {
        var water = MockUnit.create("squirtle", "one").withTraits(Set.of("Water"));
        var rockGround = MockUnit.create("geodude", "two").withTraits(Set.of("Rock", "Ground"));
        var grassPoison = MockUnit.create("bulbasaur", "one").withTraits(Set.of("Grass", "Poison"));

        assertEquals(144, resolver.apply(water, rockGround, 100));
        assertEquals(120, resolver.apply(grassPoison, water, 100));
    }

    @Test
    void missingPokemonTypesRemainNeutral() {
        var attacker = MockUnit.create("attacker", "one").withTraits(Set.of("Trait"));
        var defender = MockUnit.create("defender", "two").withTraits(Set.of("OtherTrait"));

        assertEquals(100, resolver.apply(attacker, defender, 100));
    }
}
