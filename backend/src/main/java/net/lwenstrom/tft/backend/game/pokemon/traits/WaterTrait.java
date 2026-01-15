package net.lwenstrom.tft.backend.game.pokemon.traits;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

public class WaterTrait implements TraitEffect {
    @Override
    public void apply(int count, List<GameUnit> units) {
        if (count < 1) return;
        // Mana regen?
        for (GameUnit unit : units) {
            if (hasTrait(unit, "water")) {
                // Give starting mana?
                unit.setMana(20 * count);
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
