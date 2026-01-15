package net.lwenstrom.tft.backend.game.onepiece;

import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.game.onepiece.traits.FighterTrait;
import net.lwenstrom.tft.backend.game.onepiece.traits.StrawHatTrait;

public class OnePieceTraitLoader {
    /**
     * Registers all One Piece traits to the given TraitManager.
     */
    public static void load(TraitManager traitManager) {
        traitManager.registerEffect("straw_hat", new StrawHatTrait());
        traitManager.registerEffect("fighter", new FighterTrait());
    }
}
