package net.lwenstrom.tft.backend.game.onepiece.traits;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

public class SupportTrait implements TraitEffect {

    @Override
    public void apply(int count, List<GameUnit> units) {
        int bonusStartMana = 0;

        if (count >= 4) {
            bonusStartMana = 50;
        } else if (count >= 2) {
            bonusStartMana = 20;
        }

        if (bonusStartMana > 0) {
            for (GameUnit unit : units) {
                if (hasTrait(unit, "support")) {
                    unit.setMana(Math.min(unit.getMaxMana(), unit.getMana() + bonusStartMana));
                }
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
