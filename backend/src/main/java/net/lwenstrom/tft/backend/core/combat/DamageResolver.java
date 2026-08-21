package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public final class DamageResolver {
    private final ElementalAffinityConfig affinityConfig;

    public DamageResolver() {
        this(ElementalAffinityConfig.neutral());
    }

    public DamageResolver(ElementalAffinityConfig affinityConfig) {
        this.affinityConfig = affinityConfig == null ? ElementalAffinityConfig.neutral() : affinityConfig;
    }

    public ElementalAffinityConfig affinityConfig() {
        return affinityConfig;
    }

    public int apply(GameUnit source, GameUnit target, int baseDamage) {
        if (baseDamage <= 0) {
            return baseDamage;
        }
        return Math.max(1, (int) Math.round(baseDamage * getMultiplier(source, target)));
    }

    public double getMultiplier(GameUnit source, GameUnit target) {
        var attackingElements = getConfiguredElements(source);
        var defendingElements = getConfiguredElements(target);
        return attackingElements.isEmpty() || defendingElements.isEmpty()
                ? 1.0
                : affinityConfig.multiplier(attackingElements, defendingElements);
    }

    private List<String> getConfiguredElements(GameUnit unit) {
        if (unit == null || unit.getTraits() == null) {
            return List.of();
        }
        return unit.getTraits().stream()
                .filter(affinityConfig::containsElement)
                .distinct()
                .toList();
    }
}
