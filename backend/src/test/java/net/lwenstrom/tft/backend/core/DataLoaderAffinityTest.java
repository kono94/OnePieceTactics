package net.lwenstrom.tft.backend.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.game.onepiece.OnePieceGameModeProvider;
import net.lwenstrom.tft.backend.game.palworld.PalworldGameModeProvider;
import net.lwenstrom.tft.backend.game.pokemon.PokemonGameModeProvider;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class DataLoaderAffinityTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void providersExposeGenericAffinityResources() {
        var onePiece = new OnePieceGameModeProvider(jsonMapper);
        var pokemon = new PokemonGameModeProvider(jsonMapper);
        var palworld = new PalworldGameModeProvider(jsonMapper);

        assertTrue(onePiece.getAffinitiesPath().isEmpty());
        assertEquals(
                "/data/affinities_pokemon.json", pokemon.getAffinitiesPath().orElseThrow());
        assertEquals(
                "/data/affinities_palworld.json", palworld.getAffinitiesPath().orElseThrow());
        assertEquals("palworld", GameMode.PALWORLD.getValue());
    }

    @Test
    void dataLoaderCachesPokemonAndPalworldAffinityConfigs() {
        var registry = new GameModeRegistry(
                List.of(
                        new OnePieceGameModeProvider(jsonMapper),
                        new PokemonGameModeProvider(jsonMapper),
                        new PalworldGameModeProvider(jsonMapper)),
                "pokemon");
        var dataLoader = new DataLoader(registry, jsonMapper);

        assertEquals(
                18, dataLoader.getAffinityConfig(GameMode.POKEMON).elements().size());
        assertEquals(
                9, dataLoader.getAffinityConfig(GameMode.PALWORLD).elements().size());
        assertEquals(1.0, dataLoader.getAffinityConfig(GameMode.ONEPIECE).defaultMultiplier());
        assertEquals(
                0, dataLoader.getAffinityConfig(GameMode.ONEPIECE).elements().size());
    }
}
