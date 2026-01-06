package net.lwenstrom.tft.backend.core.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraitManager {
    private static final Logger log = LoggerFactory.getLogger(TraitManager.class);

    public void applyTraits(List<GameUnit> units) {
        // Map Trait ID -> Count (Unique Units)
        Map<String, Set<String>> traitCounts = new HashMap<>();

        for (GameUnit unit : units) {
            for (String traitName : unit.getTraits()) {
                String id = normalizeTraitId(traitName);
                traitCounts.computeIfAbsent(id, k -> new HashSet<>()).add(unit.getName());
            }
        }

        // Apply effects
        traitCounts.forEach((traitId, uniqueUnits) -> {
            int count = uniqueUnits.size();
            applyEffect(traitId, count, units);
        });
    }

    private void applyEffect(String traitId, int count, List<GameUnit> units) {
        // Simple switch for now - in production this should be data-driven
        switch (traitId) {
            case "straw_hat" -> applyStrawHat(count, units);
            case "fighter" -> applyFighter(count, units);
            // Swordsman and Navigator logic might be handled elsewhere (Combat Runtime or
            // Round End)
        }
    }

    private void applyStrawHat(int count, List<GameUnit> units) {
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
                    // Assuming interface has setters/modifiers. If directly manipulating DTO, use
                    // careful casting or add methods.
                    // UnitDefinition is record, so we are modifying the Runtime GameUnit
                    // (StandardGameUnit).
                    if (unit instanceof StandardGameUnit sgu) {
                        sgu.setMaxHealth(sgu.getMaxHealth() + bonusHp);
                        sgu.setCurrentHealth(sgu.getCurrentHealth() + bonusHp);
                        sgu.setAttackSpeed(sgu.getAttackSpeed() + bonusAs);
                    }
                }
            }
        }
    }

    private void applyFighter(int count, List<GameUnit> units) {
        int bonusHp = 0;
        if (count >= 6) bonusHp = 700;
        else if (count >= 4) bonusHp = 350;
        else if (count >= 2) bonusHp = 150;

        if (bonusHp > 0) {
            for (GameUnit unit : units) {
                if (hasTrait(unit, "fighter")) {
                    if (unit instanceof StandardGameUnit sgu) {
                        sgu.setMaxHealth(sgu.getMaxHealth() + bonusHp);
                        sgu.setCurrentHealth(sgu.getCurrentHealth() + bonusHp);
                    }
                }
            }
        }
    }

    private boolean hasTrait(GameUnit unit, String traitId) {
        return unit.getTraits().stream().anyMatch(t -> normalizeTraitId(t).equals(traitId));
    }

    public static String normalizeTraitId(String name) {
        return name.toLowerCase().replace(" ", "_");
    }
}
