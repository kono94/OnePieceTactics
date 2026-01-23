package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public interface AbilityCaster {
    void castAbility(GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector);

    void castAbility(GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector, DamageCallback callback);

    @FunctionalInterface
    interface DamageCallback {
        void onDamage(String unitId, String unitName, String targetId, int damage);
    }
}
