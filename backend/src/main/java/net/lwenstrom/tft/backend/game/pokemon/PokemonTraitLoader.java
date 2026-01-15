package net.lwenstrom.tft.backend.game.pokemon;

import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.game.pokemon.traits.FireTrait;
import net.lwenstrom.tft.backend.game.pokemon.traits.GrassTrait;
import net.lwenstrom.tft.backend.game.pokemon.traits.WaterTrait;

public class PokemonTraitLoader {
    public static void load(TraitManager traitManager) {
        traitManager.registerEffect("fire", new FireTrait());
        traitManager.registerEffect("water", new WaterTrait());
        traitManager.registerEffect("grass", new GrassTrait());
    }
}
