package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public interface AbilityCaster {
    void castAbility(GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector);

    void castAbility(
            GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector, CombatStatCallback callback);

    default void castAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            TargetSelector targetSelector,
            CombatStatCallback callback,
            long currentTime) {
        castAbility(source, allUnits, targetSelector, callback);
    }

    interface CombatStatCallback {
        default void onDamage(String unitId, String unitName, String targetId, int damage) {}

        default void onHealing(String unitId, String unitName, String targetId, int healing) {}

        default void onShielding(String unitId, String unitName, String targetId, int shielding) {}

        default void onSkill(String unitId, String unitName, String targetId, int value) {}
    }
}
