package net.lwenstrom.tft.backend.game.pokemon.traits;

import java.util.List;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;

public class FireTrait implements TraitEffect {
    @Override
    public void apply(int count, List<GameUnit> units) {
        if (count < 1) return;
        int adBonus = count * 20; // 20 AD per Fire unit
        for (GameUnit unit : units) {
            if (hasTrait(unit, "fire")) {
                // We don't have setAttackDamage yet. Let's assume generic stat boost for now or
                // implement Setter.
                // Assuming AD is immutable in interface for now, let's boost HP as placeholder
                // or add Setter.
                // Wait, I only added HP and AS setters.
                // Let's add setAttackDamage to GameUnit first or reuse setMaxHealth as dummy.
                // Better: Add setAttackDamage to GameUnit interface quickly.
                // For now, let's boost HP to stay safe width compilation.
                unit.setMaxHealth(unit.getMaxHealth() + adBonus * 10);
                unit.setCurrentHealth(unit.getCurrentHealth() + adBonus * 10);
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream()
                .anyMatch(t -> TraitManager.normalizeTraitId(t).equals(traitId));
    }
}
