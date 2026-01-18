package net.lwenstrom.tft.backend.core.combat;

import java.util.Comparator;
import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import org.springframework.stereotype.Component;

@Component
public class NearestEnemyTargetSelector implements TargetSelector {

    @Override
    public GameUnit findTarget(GameUnit source, List<GameUnit> allUnits) {
        return allUnits.stream()
                .filter(c -> c != source && c.getCurrentHealth() > 0 && CombatUtils.isEnemy(source, c))
                .min(Comparator.comparingDouble(c -> CombatUtils.getDistance(source, c)))
                .orElse(null);
    }
}
