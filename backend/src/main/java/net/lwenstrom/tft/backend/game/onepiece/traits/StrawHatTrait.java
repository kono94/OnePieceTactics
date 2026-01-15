package net.lwenstrom.tft.backend.game.onepiece.traits;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

public class StrawHatTrait implements TraitEffect {

    @Override
    public void apply(int count, List<GameUnit> units) {
        int bonusHp = 0;
        float bonusAs = 0f;

        if (count >= 6) {
            bonusHp = 700;
            bonusAs = 0.50f;
        } else if (count >= 4) {
            bonusHp = 400;
            bonusAs = 0.25f;
        } else if (count >= 2) {
            bonusHp = 200;
            bonusAs = 0.10f;
        }

        if (bonusHp > 0) {
            for (GameUnit unit : units) {
                if (hasTrait(unit, "straw_hat")) {
                    unit.setMaxHealth(unit.getMaxHealth() + bonusHp);
                    unit.setCurrentHealth(unit.getCurrentHealth() + bonusHp);
                    unit.setAttackSpeed(unit.getAttackSpeed() + bonusAs);
                }
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
