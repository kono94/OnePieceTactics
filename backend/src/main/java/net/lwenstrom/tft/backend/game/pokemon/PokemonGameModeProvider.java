package net.lwenstrom.tft.backend.game.pokemon;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.GameConstants;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.engine.BotRosterProfile;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameMode;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class PokemonGameModeProvider implements GameModeProvider {
    private final JsonMapper jsonMapper;

    @Override
    public GameMode getMode() {
        return GameMode.POKEMON;
    }

    @Override
    public String getUnitsPath() {
        return "/data/units_pokemon.json";
    }

    @Override
    public String getTraitsPath() {
        return "/data/traits_pokemon.json";
    }

    @Override
    public String getAugmentsPath() {
        return "/data/augments_pokemon.json";
    }

    @Override
    public Optional<String> getAffinitiesPath() {
        return Optional.of("/data/affinities_pokemon.json");
    }

    @Override
    public BotRosterProfile getBotRosterProfile(int round) {
        if (round <= 2) {
            return new BotRosterProfile(GameConstants.BOT_MAX_UNITS_PER_ROW, 0, 0, 0, 5, 1, 5, 0, 5);
        }
        if (round == 3) {
            return new BotRosterProfile(GameConstants.BOT_MAX_UNITS_PER_ROW, 1, 0, 0, 30, 2, 30, 2, 10);
        }
        if (round <= 6) {
            return new BotRosterProfile(GameConstants.BOT_MAX_UNITS_PER_ROW, 1, 0, 0, 40, 4, 35, 3, 18);
        }
        if (round == 7) {
            return new BotRosterProfile(7, 1, 0, 0, 50, 12, 42, 8, 25);
        }
        if (round <= 9) {
            return new BotRosterProfile(5, 1, 0, 0, 50, 12, 42, 8, 25);
        }
        if (round <= 14) {
            return new BotRosterProfile(6, 1, 2, 0, 25, 55, 50, 18, 35);
        }
        if (round == 15) {
            return new BotRosterProfile(6, 0, 2, 2, 15, 75, 35, 45, 45);
        }
        return new BotRosterProfile(7, 0, 2, 2, 15, 75, 35, 45, 45);
    }

    @Override
    public void registerTraitEffects(TraitManager traitManager) {
        PokemonTraitLoader.load(traitManager, jsonMapper);
    }
}
