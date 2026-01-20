package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import org.springframework.stereotype.Component;

@Component
public class DefaultAbilityCaster implements AbilityCaster {

    @Override
    public void castAbility(GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector) {
        AbilityDefinition ability = source.getAbility();
        if (ability == null) return;

        GameUnit target = targetSelector.findTarget(source, allUnits);
        if (target == null && "DMG".equals(ability.type())) return;

        source.setActiveAbility(ability.name());

        int damage = ability.value() * source.getStarLevel();

        switch (ability.pattern()) {
            case "SINGLE" -> {
                if (target != null) {
                    target.takeDamage(damage);
                }
            }
            case "LINE" -> {
                if (target != null) {
                    int dx = Integer.compare(target.getX(), source.getX());
                    int dy = Integer.compare(target.getY(), source.getY());
                    for (int i = 1; i <= ability.range(); i++) {
                        int tx = source.getX() + dx * i;
                        int ty = source.getY() + dy * i;
                        final int fX = tx;
                        final int fY = ty;

                        allUnits.stream()
                                .filter(u -> u.getX() == fX && u.getY() == fY && u.getCurrentHealth() > 0)
                                .filter(u -> CombatUtils.isEnemy(source, u))
                                .forEach(u -> u.takeDamage(damage));
                    }
                }
            }
            case "SURROUND" -> {
                int r = ability.range();
                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int tx = source.getX() + dx;
                        int ty = source.getY() + dy;
                        final int fX = tx;
                        final int fY = ty;

                        allUnits.stream()
                                .filter(u -> u.getX() == fX && u.getY() == fY && u.getCurrentHealth() > 0)
                                .filter(u -> CombatUtils.isEnemy(source, u))
                                .forEach(u -> u.takeDamage(damage));
                    }
                }
            }
        }
    }
}
