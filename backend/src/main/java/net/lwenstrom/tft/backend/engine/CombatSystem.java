package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.model.GameUnit;
import java.util.ArrayList;
import java.util.List;

public class CombatSystem {

    public void simulateTick(GameRoom room) {
        List<GameUnit> allUnits = new ArrayList<>();
        room.getPlayers().forEach(p -> {
            allUnits.addAll(p.getBoardUnits());
        });

        // Loop through units (simple snapshot to allow modification during iteration)
        List<GameUnit> snapshot = new ArrayList<>(allUnits);

        for (GameUnit unit : snapshot) {
            if (unit.getCurrentHealth() <= 0)
                continue;

            GameUnit target = findNearestEnemy(unit, allUnits);
            if (target != null) {
                double distance = getDistance(unit, target);
                // Simple range check
                if (distance <= unit.getRange()) {
                    // Attack
                    target.takeDamage(unit.getAttackDamage());
                    unit.gainMana(10);
                } else {
                    // Move
                    moveTowards(unit, target);
                }
            }
        }

        // Cleanup dead units from board
        room.getPlayers().forEach(p -> {
            p.getBoardUnits().removeIf(u -> u.getCurrentHealth() <= 0);
        });
    }

    private GameUnit findNearestEnemy(GameUnit source, List<GameUnit> candidates) {
        GameUnit nearest = null;
        double minDst = Double.MAX_VALUE;

        for (GameUnit c : candidates) {
            if (c == source || c.getCurrentHealth() <= 0)
                continue;
            // In a real game, check if 'c' belongs to a different team.
            // For this FFA / Clone, we assume everyone else is enemy for now.

            double dst = getDistance(source, c);
            if (dst < minDst) {
                minDst = dst;
                nearest = c;
            }
        }
        return nearest;
    }

    private double getDistance(GameUnit u1, GameUnit u2) {
        return Math.sqrt(Math.pow(u1.getX() - u2.getX(), 2) + Math.pow(u1.getY() - u2.getY(), 2));
    }

    private void moveTowards(GameUnit mover, GameUnit target) {
        int dx = Integer.compare(target.getX(), mover.getX());
        int dy = Integer.compare(target.getY(), mover.getY());

        // Simple grid movement
        if (dx != 0)
            mover.setPosition(mover.getX() + dx, mover.getY());
        else if (dy != 0)
            mover.setPosition(mover.getX(), mover.getY() + dy);
    }
}
