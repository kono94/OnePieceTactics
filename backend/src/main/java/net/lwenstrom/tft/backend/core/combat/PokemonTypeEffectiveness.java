package net.lwenstrom.tft.backend.core.combat;

import net.lwenstrom.tft.backend.core.model.GameUnit;

public final class PokemonTypeEffectiveness {
    private static final ElementalAffinityConfig CONFIG =
            new ElementalAffinityLoader().load("/data/affinities_pokemon.json");

    private PokemonTypeEffectiveness() {}

    public static int apply(GameUnit attacker, GameUnit defender, int damage) {
        if (damage <= 0) {
            return damage;
        }
        return Math.max(1, Math.round(damage * getMultiplier(attacker, defender)));
    }

    public static float getMultiplier(GameUnit attacker, GameUnit defender) {
        var attackingElements = attacker == null || attacker.getTraits() == null
                ? java.util.List.<String>of()
                : attacker.getTraits().stream()
                        .filter(CONFIG::containsElement)
                        .distinct()
                        .toList();
        var defendingElements = defender == null || defender.getTraits() == null
                ? java.util.List.<String>of()
                : defender.getTraits().stream()
                        .filter(CONFIG::containsElement)
                        .distinct()
                        .toList();
        return (float) CONFIG.multiplier(attackingElements, defendingElements);
    }
}
