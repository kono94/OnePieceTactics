package net.lwenstrom.tft.backend.game.pokemon.traits;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

public class GrassTrait implements TraitEffect {
    @Override
    public void apply(int count, List<GameUnit> units) {
        // Health Regen logic would be nice, but for static stats: Max Health.
        if (count < 1) return;
        int bonusHp = 200 * count;
        for (GameUnit unit : units) {
            if (hasTrait(unit, "grass")) {
                unit.setMaxHealth(unit.getMaxHealth() + bonusHp);
                unit.setCurrentHealth(unit.getCurrentHealth() + bonusHp);
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
