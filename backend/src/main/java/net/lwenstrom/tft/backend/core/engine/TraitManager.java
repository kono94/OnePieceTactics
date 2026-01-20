package net.lwenstrom.tft.backend.core.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraitManager {
    private static final Logger log = LoggerFactory.getLogger(TraitManager.class);

    private final Map<String, TraitEffect> effects = new HashMap<>();

    public void registerEffect(String traitId, TraitEffect effect) {
        effects.put(normalizeTraitId(traitId), effect);
    }

    public void applyTraits(List<GameUnit> units) {
        // Map Trait ID -> Count (Unique Units)
        var traitCounts = new HashMap<String, Set<String>>();

        for (var unit : units) {
            for (var traitName : unit.getTraits()) {
                var id = normalizeTraitId(traitName);
                traitCounts.computeIfAbsent(id, k -> new HashSet<String>()).add(unit.getName());
            }
        }

        // Apply effects
        traitCounts.forEach((traitId, uniqueUnits) -> {
            var count = uniqueUnits.size();
            if (effects.containsKey(traitId)) {
                effects.get(traitId).apply(count, units);
            } else {
                log.warn("No effect registered for trait: {}", traitId);
            }
        });
    }

    public static String normalizeTraitId(String name) {
        return name.toLowerCase().replace(" ", "_");
    }
}
