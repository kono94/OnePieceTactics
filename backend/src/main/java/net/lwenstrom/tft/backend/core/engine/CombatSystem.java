package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public class CombatSystem {

    private final TraitManager traitManager = new TraitManager();

    public void startCombat(java.util.Collection<Player> players) {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(java.util.Comparator.comparing(Player::getId));

        if (sortedPlayers.isEmpty())
            return;

        // Apply Traits per player BEFORE saving/mirroring
        for (Player p : players) {
            // Save state (positions + base stats)
            for (GameUnit u : p.getBoardUnits()) {
                u.savePlanningPosition();
            }
            // Apply traits (modifies stats)
            traitManager.applyTraits(p.getBoardUnits());
        }

        if (sortedPlayers.size() > 1) {
            // Player 2 (Top): Mirror coordinates
            Player p2 = sortedPlayers.get(1);
            for (GameUnit u : p2.getBoardUnits()) {
                // Mirror logic: 4 rows (0-3) become rows 4-7.
                int newX = 6 - u.getX(); // Mirror horizontal (Cols 0-6)
                int newY = 7 - u.getY(); // Mirror vertical (Rows 0-3 -> 7-4)
                u.setPosition(newX, newY);
            }
        }
    }

    public void endCombat(java.util.Collection<Player> players) {
        for (Player p : players) {
            for (GameUnit u : p.getBoardUnits()) {
                u.restorePlanningPosition();
            }
        }
    }

    public CombatResult simulateTick(List<Player> participants) {
        var currentTime = System.currentTimeMillis();
        var allUnits = new ArrayList<GameUnit>();
        participants.forEach(p -> {
            allUnits.addAll(p.getBoardUnits());
        });

        // Debug
        // System.out.println("Simulating tick for " + participants.size() + " players.
        // Total units: " + allUnits.size());

        var snapshot = new ArrayList<>(allUnits);

        for (var unit : snapshot) {
            if (unit.getCurrentHealth() <= 0)
                continue;

            if (currentTime < unit.getNextAttackTime())
                continue;

            var target = findNearestEnemy(unit, allUnits);
            if (target != null) {
                var distance = getDistance(unit, target);
                // Range check
                if (distance <= unit.getRange()) {
                    target.takeDamage(unit.getAttackDamage());
                    unit.gainMana(10);
                    float as = Math.max(0.1f, unit.getAttackSpeed());
                    long cooldownMs = (long) (1000 / as);
                    unit.setNextAttackTime(currentTime + cooldownMs);
                } else {
                    moveTowards(unit, target, allUnits);
                }
            }
        }
        // Cleanup dead units from board <-- REMOVED PERMANENT DELETION
        // We leave them in the list so they can be revived next round.
        // Combat logic ignores them via getCurrentHealth() <= 0 check.

        long playersWithUnits = participants.stream()
                .filter(p -> !p.getBoardUnits().isEmpty())
                .count();

        if (playersWithUnits <= 1) {
            // Determine winner
            Player winner = participants.stream()
                    .filter(p -> !p.getBoardUnits().isEmpty())
                    .findFirst()
                    .orElse(null); // Null implies draw (0 players left)

            return new CombatResult(true, winner != null ? winner.getId() : null);
        }

        return new CombatResult(false, null);
    }

    private GameUnit findNearestEnemy(GameUnit source, List<GameUnit> candidates) {
        GameUnit nearest = null;
        var minDst = Double.MAX_VALUE;

        for (var c : candidates) {
            if (c == source || c.getCurrentHealth() <= 0)
                continue;

            // Check owner
            if (source.getOwnerId() != null && source.getOwnerId().equals(c.getOwnerId()))
                continue;

            var dst = getDistance(source, c);
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

    private void moveTowards(GameUnit mover, GameUnit target, List<GameUnit> allUnits) {
        var dx = Integer.compare(target.getX(), mover.getX());
        var dy = Integer.compare(target.getY(), mover.getY());

        var newX = mover.getX() + dx;
        var newY = mover.getY() + dy;

        // Check bounds (0-7)
        if (newX < 0 || newX > 7 || newY < 0 || newY > 7)
            return;

        // Check if occupied
        var occupied = allUnits.stream()
                .anyMatch(u -> u.getCurrentHealth() > 0 && u.getX() == newX && u.getY() == newY);

        if (!occupied) {
            mover.setPosition(newX, newY);
        }
    }

    public record CombatResult(boolean ended, String winnerId) {
    }
}
