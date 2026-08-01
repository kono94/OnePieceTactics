package net.lwenstrom.tft.backend.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.game.onepiece.OnePieceGameModeProvider;
import net.lwenstrom.tft.backend.game.palworld.PalworldGameModeProvider;
import net.lwenstrom.tft.backend.game.pokemon.PokemonGameModeProvider;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class GameModeRegistryTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void onePieceIsTheInitialRoomModeWhenAllModesAreRegistered() {
        var registry = new GameModeRegistry(List.of(
                new PokemonGameModeProvider(jsonMapper),
                new PalworldGameModeProvider(jsonMapper),
                new OnePieceGameModeProvider(jsonMapper)));

        assertEquals(GameMode.ONEPIECE, registry.getDefaultMode());
    }

    @Test
    void focusedRegistryUsesItsOnlyAvailableMode() {
        var registry = new GameModeRegistry(List.of(new PalworldGameModeProvider(jsonMapper)));

        assertEquals(GameMode.PALWORLD, registry.getDefaultMode());
    }

    @Test
    void registryRequiresAtLeastOneModeProvider() {
        assertThrows(IllegalStateException.class, () -> new GameModeRegistry(List.of()));
    }
}
