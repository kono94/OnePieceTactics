package net.lwenstrom.tft.backend.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.model.AugmentEffectType;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.game.onepiece.OnePieceGameModeProvider;
import net.lwenstrom.tft.backend.game.pokemon.PokemonGameModeProvider;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class DataLoaderAugmentTest {

    @Test
    void loadsAndValidatesModeAugments() {
        var jsonMapper = JsonMapper.builder().build();
        var registry = new GameModeRegistry(
                List.of(new OnePieceGameModeProvider(jsonMapper), new PokemonGameModeProvider(jsonMapper)), "onepiece");
        var dataLoader = new DataLoader(registry, jsonMapper);

        validate(dataLoader, GameMode.ONEPIECE);
        validate(dataLoader, GameMode.POKEMON);
    }

    private void validate(DataLoader dataLoader, GameMode mode) {
        var augments = dataLoader.getAugments(mode);
        assertEquals(15, augments.size());
        assertEquals(
                Set.of(
                        AugmentEffectType.TEAM_ATTACK_SPEED_PER_RANGED_UNIT,
                        AugmentEffectType.TEAM_DAMAGE_REDUCTION,
                        AugmentEffectType.TEAM_ATTACK_DAMAGE_ON_KILL,
                        AugmentEffectType.TEAM_MAX_HEALTH,
                        AugmentEffectType.TEAM_ATTACK_DAMAGE,
                        AugmentEffectType.TEAM_ABILITY_POWER,
                        AugmentEffectType.TEAM_ARMOR_AND_MAGIC_RESIST,
                        AugmentEffectType.MELEE_LIFESTEAL,
                        AugmentEffectType.RANGED_ATTACK_DAMAGE,
                        AugmentEffectType.TEAM_MANA_GAIN,
                        AugmentEffectType.TEAM_STARTING_MANA,
                        AugmentEffectType.GOLD_PER_EMPTY_BENCH_SLOT,
                        AugmentEffectType.TEAM_STARTING_SHIELD,
                        AugmentEffectType.GOLD,
                        AugmentEffectType.XP),
                augments.stream().map(augment -> augment.effectType()).collect(Collectors.toSet()));
        assertEquals(
                15,
                augments.stream()
                        .map(augment -> augment.id())
                        .collect(Collectors.toSet())
                        .size());
        assertTrue(augments.stream().allMatch(augment -> augment.values().size() == 3));
        assertTrue(augments.stream().allMatch(augment -> augment.descriptions().size() == 3));
        assertEquals(
                Map.ofEntries(
                        Map.entry(AugmentEffectType.TEAM_ATTACK_SPEED_PER_RANGED_UNIT, List.of(3, 5, 8)),
                        Map.entry(AugmentEffectType.TEAM_DAMAGE_REDUCTION, List.of(5, 10, 15)),
                        Map.entry(AugmentEffectType.TEAM_ATTACK_DAMAGE_ON_KILL, List.of(5, 8, 12)),
                        Map.entry(AugmentEffectType.TEAM_MAX_HEALTH, List.of(120, 220, 360)),
                        Map.entry(AugmentEffectType.TEAM_ATTACK_DAMAGE, List.of(4, 7, 10)),
                        Map.entry(AugmentEffectType.TEAM_ABILITY_POWER, List.of(10, 18, 30)),
                        Map.entry(AugmentEffectType.TEAM_ARMOR_AND_MAGIC_RESIST, List.of(8, 14, 24)),
                        Map.entry(AugmentEffectType.MELEE_LIFESTEAL, List.of(8, 12, 18)),
                        Map.entry(AugmentEffectType.RANGED_ATTACK_DAMAGE, List.of(5, 8, 12)),
                        Map.entry(AugmentEffectType.TEAM_MANA_GAIN, List.of(12, 20, 30)),
                        Map.entry(AugmentEffectType.TEAM_STARTING_MANA, List.of(10, 20, 35)),
                        Map.entry(AugmentEffectType.GOLD_PER_EMPTY_BENCH_SLOT, List.of(3, 5, 8)),
                        Map.entry(AugmentEffectType.TEAM_STARTING_SHIELD, List.of(100, 180, 300)),
                        Map.entry(AugmentEffectType.GOLD, List.of(20, 35, 50)),
                        Map.entry(AugmentEffectType.XP, List.of(8, 16, 24))),
                augments.stream()
                        .collect(Collectors.toMap(augment -> augment.effectType(), augment -> augment.values())));
    }
}
