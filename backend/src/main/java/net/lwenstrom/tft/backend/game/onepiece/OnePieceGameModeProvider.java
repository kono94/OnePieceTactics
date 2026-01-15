package net.lwenstrom.tft.backend.game.onepiece;

import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.game.onepiece.traits.FighterTrait;
import net.lwenstrom.tft.backend.game.onepiece.traits.StrawHatTrait;
import org.springframework.stereotype.Component;

@Component
public class OnePieceGameModeProvider implements GameModeProvider {

    @Override
    public GameMode getMode() {
        return GameMode.ONEPIECE;
    }

    @Override
    public String getUnitsPath() {
        return "/data/units_onepiece.json";
    }

    @Override
    public String getTraitsPath() {
        return "/data/traits_onepiece.json";
    }

    @Override
    public void registerTraitEffects(TraitManager traitManager) {
        traitManager.registerEffect("straw_hat", new StrawHatTrait());
        traitManager.registerEffect("fighter", new FighterTrait());
    }
}
