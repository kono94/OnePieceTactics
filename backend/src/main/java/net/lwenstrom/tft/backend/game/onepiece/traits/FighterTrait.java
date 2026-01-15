package net.lwenstrom.tft.backend.game.onepiece.traits;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

public class FighterTrait implements TraitEffect {

    @Override
    public void apply(int count, List<GameUnit> units) {
        int bonusHp = 0;
        if (count >= 6) bonusHp = 700;
        else if (count >= 4) bonusHp = 350;
        else if (count >= 2) bonusHp = 150;

        if (bonusHp > 0) {
            for (GameUnit unit : units) {
                if (hasTrait(unit, "fighter")) {
                    unit.setMaxHealth(unit.getMaxHealth() + bonusHp);
                    unit.setCurrentHealth(unit.getCurrentHealth() + bonusHp);
                }
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
