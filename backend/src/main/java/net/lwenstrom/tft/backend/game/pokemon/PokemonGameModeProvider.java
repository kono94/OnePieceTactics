package net.lwenstrom.tft.backend.game.pokemon;

import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.game.pokemon.traits.FireTrait;
import net.lwenstrom.tft.backend.game.pokemon.traits.GrassTrait;
import net.lwenstrom.tft.backend.game.pokemon.traits.WaterTrait;
import org.springframework.stereotype.Component;

@Component
public class PokemonGameModeProvider implements GameModeProvider {

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
    public void registerTraitEffects(TraitManager traitManager) {
        traitManager.registerEffect("fire", new FireTrait());
        traitManager.registerEffect("water", new WaterTrait());
        traitManager.registerEffect("grass", new GrassTrait());
    }
}
