package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.model.GameUnit;
import java.util.ArrayList;
import java.util.List;

public class CombatSystem {

    public void simulateTick(GameRoom room) {
        long currentTime = System.currentTimeMillis();
        List<GameUnit> allUnits = new ArrayList<>();
        room.getPlayers().forEach(p -> {
            allUnits.addAll(p.getBoardUnits());
        });

        // Loop through units (simple snapshot to allow modification during iteration)
        List<GameUnit> snapshot = new ArrayList<>(allUnits);

        for (GameUnit unit : snapshot) {
            if (unit.getCurrentHealth() <= 0)
                continue;

            // Check Attack Cooldown
            if (currentTime < unit.getNextAttackTime())
                continue;

            GameUnit target = findNearestEnemy(unit, allUnits);
            if (target != null) {
                double distance = getDistance(unit, target);
                // Simple range check
                if (distance <= unit.getRange()) {
                    // Attack
                    target.takeDamage(unit.getAttackDamage());
                    unit.gainMana(10);

                    // Set next attack time based on attack speed (attacks per second)
                    // limit attack speed to avoid infinite loop or super fast attacks (div by zero
                    // check)
                    float as = Math.max(0.1f, unit.getAttackSpeed());
                    long cooldownMs = (long) (1000 / as);
                    unit.setNextAttackTime(currentTime + cooldownMs);

                } else {
                    // Move
                    // Only move if we can move (no cooldown on movement? or shared cooldown?)
                    // For now, allow movement every tick (100ms) but maybe limit it?
                    // Let's assume movement is free for now but maybe slower?
                    moveTowards(unit, target, allUnits);
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

            // Check owner
            if (source.getOwnerId() != null && source.getOwnerId().equals(c.getOwnerId()))
                continue;

            double dst = getDistance(source, c);
            if (dst < minDst) {
                minDst = dst;
                nearest = c;
            }
        }
        return nearest;
    }

    private double getDistance(GameUnit u1, GameUnit u2) {
        // Hex logic or Grid logic? Using Chebyshev distance for 8-way movement on grid,
        // or Euclidean?
        // Let's stick to Euclidean for range check, but movement might be Grid.
        return Math.sqrt(Math.pow(u1.getX() - u2.getX(), 2) + Math.pow(u1.getY() - u2.getY(), 2));
    }

    private void moveTowards(GameUnit mover, GameUnit target, List<GameUnit> allUnits) {
        int dx = Integer.compare(target.getX(), mover.getX());
        int dy = Integer.compare(target.getY(), mover.getY());

        int newX = mover.getX() + dx;
        int newY = mover.getY() + dy;

        // Check bounds (0-7)
        if (newX < 0 || newX > 7 || newY < 0 || newY > 7)
            return;

        // Check if occupied
        boolean occupied = allUnits.stream()
                .anyMatch(u -> u.getCurrentHealth() > 0 && u.getX() == newX && u.getY() == newY);

        if (!occupied) {
            mover.setPosition(newX, newY);
        }
    }
}
