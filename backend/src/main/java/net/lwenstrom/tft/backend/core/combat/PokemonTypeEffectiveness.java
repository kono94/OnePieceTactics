package net.lwenstrom.tft.backend.core.combat;

import java.util.Map;
import java.util.Set;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public final class PokemonTypeEffectiveness {
    private static final float SUPER_EFFECTIVE_MULTIPLIER = 1.2f;
    private static final float RESISTED_MULTIPLIER = 0.8f;
    private static final Set<String> POKEMON_TYPES = Set.of(
            "Normal",
            "Fire",
            "Water",
            "Grass",
            "Electric",
            "Ice",
            "Fighting",
            "Poison",
            "Ground",
            "Flying",
            "Psychic",
            "Bug",
            "Rock",
            "Ghost",
            "Dragon",
            "Dark",
            "Steel",
            "Fairy");

    private static final Map<String, Set<String>> SUPER_EFFECTIVE = Map.ofEntries(
            Map.entry("Fire", Set.of("Grass", "Ice", "Bug", "Steel")),
            Map.entry("Water", Set.of("Fire", "Ground", "Rock")),
            Map.entry("Grass", Set.of("Water", "Ground", "Rock")),
            Map.entry("Electric", Set.of("Water", "Flying")),
            Map.entry("Ice", Set.of("Grass", "Ground", "Flying", "Dragon")),
            Map.entry("Fighting", Set.of("Normal", "Ice", "Rock", "Dark", "Steel")),
            Map.entry("Poison", Set.of("Grass", "Fairy")),
            Map.entry("Ground", Set.of("Fire", "Electric", "Poison", "Rock", "Steel")),
            Map.entry("Flying", Set.of("Grass", "Fighting", "Bug")),
            Map.entry("Psychic", Set.of("Fighting", "Poison")),
            Map.entry("Bug", Set.of("Grass", "Psychic", "Dark")),
            Map.entry("Rock", Set.of("Fire", "Ice", "Flying", "Bug")),
            Map.entry("Ghost", Set.of("Psychic", "Ghost")),
            Map.entry("Dragon", Set.of("Dragon")),
            Map.entry("Dark", Set.of("Psychic", "Ghost")),
            Map.entry("Steel", Set.of("Ice", "Rock", "Fairy")),
            Map.entry("Fairy", Set.of("Fighting", "Dragon", "Dark")));

    private static final Map<String, Set<String>> RESISTED = Map.ofEntries(
            Map.entry("Normal", Set.of("Rock", "Steel", "Ghost")),
            Map.entry("Fire", Set.of("Fire", "Water", "Rock", "Dragon")),
            Map.entry("Water", Set.of("Water", "Grass", "Dragon")),
            Map.entry("Grass", Set.of("Fire", "Grass", "Poison", "Flying", "Bug", "Dragon", "Steel")),
            Map.entry("Electric", Set.of("Electric", "Grass", "Dragon", "Ground")),
            Map.entry("Ice", Set.of("Fire", "Water", "Ice", "Steel")),
            Map.entry("Fighting", Set.of("Poison", "Flying", "Psychic", "Bug", "Ghost", "Fairy")),
            Map.entry("Poison", Set.of("Poison", "Ground", "Rock", "Ghost", "Steel")),
            Map.entry("Ground", Set.of("Grass", "Bug", "Flying")),
            Map.entry("Flying", Set.of("Electric", "Rock", "Steel")),
            Map.entry("Psychic", Set.of("Psychic", "Steel", "Dark")),
            Map.entry("Bug", Set.of("Fire", "Fighting", "Poison", "Flying", "Ghost", "Steel", "Fairy")),
            Map.entry("Rock", Set.of("Fighting", "Ground", "Steel")),
            Map.entry("Ghost", Set.of("Normal", "Dark")),
            Map.entry("Dragon", Set.of("Steel", "Fairy")),
            Map.entry("Dark", Set.of("Fighting", "Dark", "Fairy")),
            Map.entry("Steel", Set.of("Fire", "Water", "Electric", "Steel")),
            Map.entry("Fairy", Set.of("Fire", "Poison", "Steel")));

    private PokemonTypeEffectiveness() {}

    public static int apply(GameUnit attacker, GameUnit defender, int damage) {
        if (damage <= 0) {
            return damage;
        }
        return Math.max(1, Math.round(damage * getMultiplier(attacker, defender)));
    }

    public static float getMultiplier(GameUnit attacker, GameUnit defender) {
        var attackerTypes = getPokemonTypes(attacker);
        var defenderTypes = getPokemonTypes(defender);
        if (attackerTypes.isEmpty() || defenderTypes.isEmpty()) {
            return 1.0f;
        }

        float bestMultiplier = 0.0f;
        for (var attackerType : attackerTypes) {
            float multiplier = 1.0f;
            for (var defenderType : defenderTypes) {
                multiplier *= getSingleTypeMultiplier(attackerType, defenderType);
            }
            bestMultiplier = Math.max(bestMultiplier, multiplier);
        }
        return bestMultiplier;
    }

    private static Set<String> getPokemonTypes(GameUnit unit) {
        if (unit == null || unit.getTraits() == null) {
            return Set.of();
        }
        var types = new java.util.HashSet<String>();
        for (var trait : unit.getTraits()) {
            if (POKEMON_TYPES.contains(trait)) {
                types.add(trait);
            }
        }
        return types;
    }

    private static float getSingleTypeMultiplier(String attackerType, String defenderType) {
        if (SUPER_EFFECTIVE.getOrDefault(attackerType, Set.of()).contains(defenderType)) {
            return SUPER_EFFECTIVE_MULTIPLIER;
        }
        if (RESISTED.getOrDefault(attackerType, Set.of()).contains(defenderType)) {
            return RESISTED_MULTIPLIER;
        }
        return 1.0f;
    }
}
