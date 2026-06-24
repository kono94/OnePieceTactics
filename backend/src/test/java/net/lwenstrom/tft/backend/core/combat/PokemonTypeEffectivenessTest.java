package net.lwenstrom.tft.backend.core.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.Test;

class PokemonTypeEffectivenessTest {

    @Test
    void returnsNeutralDamageWhenUnitsDoNotHavePokemonTypes() {
        var attacker = unit("attacker", "Trait");
        var defender = unit("defender", "OtherTrait");

        assertEquals(100, PokemonTypeEffectiveness.apply(attacker, defender, 100));
    }

    @Test
    void increasesDamageForSuperEffectiveMatchup() {
        var attacker = unit("squirtle", "Water");
        var defender = unit("charmander", "Fire");

        assertEquals(120, PokemonTypeEffectiveness.apply(attacker, defender, 100));
    }

    @Test
    void reducesDamageForResistedMatchup() {
        var attacker = unit("charmander", "Fire");
        var defender = unit("squirtle", "Water");

        assertEquals(80, PokemonTypeEffectiveness.apply(attacker, defender, 100));
    }

    @Test
    void treatsPokemonImmunitiesAsResistedDamage() {
        var attacker = unit("pikachu", "Electric");
        var defender = unit("sandshrew", "Ground");

        assertEquals(80, PokemonTypeEffectiveness.apply(attacker, defender, 100));
    }

    @Test
    void multipliesDefenderDualTypeMatchups() {
        var attacker = unit("squirtle", "Water");
        var defender = unit("geodude", "Rock", "Ground");

        assertEquals(144, PokemonTypeEffectiveness.apply(attacker, defender, 100));
    }

    @Test
    void usesBestAttackingTypeForDualTypeAttackers() {
        var attacker = unit("bulbasaur", "Grass", "Poison");
        var defender = unit("squirtle", "Water");

        assertEquals(120, PokemonTypeEffectiveness.apply(attacker, defender, 100));
    }

    private MockUnit unit(String id, String... traits) {
        return MockUnit.create(id, id + "-owner").withTraits(Set.of(traits));
    }
}
