package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public class CombatSystem {

    private final TraitManager traitManager = new TraitManager();

    public void startCombat(java.util.Collection<Player> players) {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(java.util.Comparator.comparing(Player::getId));

        if (sortedPlayers.isEmpty()) return;

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
            // Player 1 (Top)
            Player p1 = sortedPlayers.get(0);
            p1.setCombatSide("TOP");
            for (GameUnit u : p1.getBoardUnits()) {
                // Top Player: Rotate 180 locally (Front 0->3 Mid, Back 3->0 Top)
                int newX = (Grid.COLS - 1) - u.getX(); // Mirror Horizontal
                int newY = (Grid.PLAYER_ROWS - 1) - u.getY(); // Invert Vertical
                u.setPosition(newX, newY);
            }

            // Player 2 (Bottom): Just offset
            Player p2 = sortedPlayers.get(1);
            p2.setCombatSide("BOTTOM");
            for (GameUnit u : p2.getBoardUnits()) {
                // Bottom Player: Front 0->4 Mid, Back 3->7 Bottom
                int newY = Grid.PLAYER_ROWS + u.getY();
                u.setPosition(u.getX(), newY);
            }
        } else {
            // Single player testing? Treat as Bottom.
            Player p1 = sortedPlayers.get(0);
            p1.setCombatSide("BOTTOM");
            for (GameUnit u : p1.getBoardUnits()) {
                // Bottom Player: Front 0->4 Mid, Back 3->7 Bottom
                int newY = Grid.PLAYER_ROWS + u.getY();
                u.setPosition(u.getX(), newY);
            }
        }
    }

    public void endCombat(java.util.Collection<Player> players) {
        for (Player p : players) {
            p.setCombatSide(null);
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
            if (unit.getCurrentHealth() <= 0) continue;

            if (currentTime < unit.getNextAttackTime()) continue;

            // Reset active ability flag
            unit.setActiveAbility(null);

            // Cast Check
            if (unit.getMaxMana() > 0 && unit.getMana() >= unit.getMaxMana()) {
                castAbility(unit, allUnits);
                unit.setMana(0);
                // Cast Time / Global Cooldown (e.g. 1 second)
                unit.setNextAttackTime(currentTime + 1000);
                continue;
            }

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

        long playersWithUnits =
                participants.stream().filter(p -> !p.getBoardUnits().isEmpty()).count();

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
            if (c == source || c.getCurrentHealth() <= 0) continue;

            // Check owner
            if (source.getOwnerId() != null && source.getOwnerId().equals(c.getOwnerId())) continue;

            var dst = getDistance(source, c);
            if (dst < minDst) {
                minDst = dst;
                nearest = c;
            }
        }
        return nearest;
    }

    private double getDistance(GameUnit u1, GameUnit u2) {
        // Use Euclidean for check, but BFS for path
        return Math.sqrt(Math.pow(u1.getX() - u2.getX(), 2) + Math.pow(u1.getY() - u2.getY(), 2));
    }

    private void moveTowards(GameUnit mover, GameUnit target, List<GameUnit> allUnits) {
        // Staggered Movement: Check if enough time passed
        if (System.currentTimeMillis() < mover.getNextMoveTime()) {
            return;
        }

        // BFS Pathfinding
        Point nextStep = findNextStep(mover, target, allUnits);

        if (nextStep != null) {
            mover.setPosition(nextStep.x, nextStep.y);
            // 800ms delay for staggered movement
            mover.setNextMoveTime(System.currentTimeMillis() + 800);
        }
    }

    private Point findNextStep(GameUnit start, GameUnit target, List<GameUnit> allUnits) {
        // Grid size
        int rows = Grid.COMBAT_ROWS;
        int cols = Grid.COLS;

        boolean[][] occupied = new boolean[rows][cols];
        for (GameUnit u : allUnits) {
            if (u.getCurrentHealth() > 0 && u != start) { // Treat self as not occupied (obviously)
                if (u.getX() >= 0 && u.getX() < cols && u.getY() >= 0 && u.getY() < rows) {
                    occupied[u.getY()][u.getX()] = true;
                }
            }
        }
        // Target position is occupied, but it is our destination.
        // We want to reach a neighbor of target.
        // BFS to find shortest path to "Attack Range" of target.
        // For simplicity, let's path to any neighbor of target (Range 1).
        // If unit has range > 1, we could stop earlier, but let's stick to simple
        // movement.
        // Actually, if range > 1, pathing to neighbor is "too close" but safe.

        // BFS State
        var queue = new java.util.ArrayDeque<Point>();
        var parent = new java.util.HashMap<Point, Point>();
        var visited = new java.util.HashSet<Point>();

        Point startPt = new Point(start.getX(), start.getY());
        queue.add(startPt);
        visited.add(startPt);

        Point foundDest = null;

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            // Check if this point is in range of target
            double dist = Math.sqrt(Math.pow(current.x - target.getX(), 2) + Math.pow(current.y - target.getY(), 2));
            if (dist <= start.getRange()) {
                // Found a valid position in range!
                // BUT: We can't actually stop ON an occupied tile unless it's the start tile.
                // We need to check if 'current' is occupied.
                // If 'current' is occupied, we can't stand there.
                // EXCEPTION: If we are already there (startPt).
                if (current.equals(startPt) || !occupied[current.y][current.x]) {
                    foundDest = current;
                    break;
                }
            }

            // Neighbors
            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                    // Start point is always valid. Other points must be empty.
                    // Actually, for BFS exploration, we can only walk through empty tiles.
                    if (!occupied[ny][nx]) {
                        Point next = new Point(nx, ny);
                        if (!visited.contains(next)) {
                            visited.add(next);
                            parent.put(next, current);
                            queue.add(next);
                        }
                    }
                }
            }
        }

        if (foundDest != null) {
            // Reconstruct path to find the first step (the one after startPt)
            Point curr = foundDest;
            while (curr != null && parent.containsKey(curr) && !parent.get(curr).equals(startPt)) {
                curr = parent.get(curr);
            }
            // If curr is foundDest and parent[curr] is startPt, then curr is the step.
            // If curr is startPt (already in range), return null (don't move).
            if (curr.equals(startPt)) return null;
            return curr;
        }

        return null; // No path
    }

    private record Point(int x, int y) {}

    private void castAbility(GameUnit source, List<GameUnit> allUnits) {
        AbilityDefinition ability = source.getAbility();
        if (ability == null) return;

        source.setActiveAbility(ability.name());
        // System.out.println(source.getName() + " casts " + ability.name() + "!");

        // Scaling: Value * StarLevel
        int damage = ability.value() * source.getStarLevel(); // Simple scaling
        // If abilityPower is used, could be: damage * (1 + ap/100.0)

        GameUnit target = findNearestEnemy(source, allUnits);
        if (target == null && "DMG".equals(ability.type())) return;
        // Heals might target allies, but assume DMG for now based on requirements.

        String pattern = ability.pattern();
        switch (pattern) {
            case "SINGLE" -> {
                if (target != null) {
                    target.takeDamage(damage);
                }
            }
            case "LINE_3" -> {
                if (target != null) {
                    // Line towards target
                    int dx = Integer.compare(target.getX(), source.getX());
                    int dy = Integer.compare(target.getY(), source.getY());
                    // 3 cells in that direction
                    for (int i = 1; i <= 3; i++) {
                        int tx = source.getX() + dx * i;
                        int ty = source.getY() + dy * i;
                        final int fX = tx; // effectively final for lambda
                        final int fY = ty;

                        allUnits.stream()
                                .filter(u -> u.getX() == fX && u.getY() == fY && u.getCurrentHealth() > 0)
                                .filter(u -> isEnemy(source, u))
                                .forEach(u -> u.takeDamage(damage));
                    }
                }
            }
            case "SURROUND_8" -> {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int tx = source.getX() + dx;
                        int ty = source.getY() + dy;
                        final int fX = tx;
                        final int fY = ty;

                        allUnits.stream()
                                .filter(u -> u.getX() == fX && u.getY() == fY && u.getCurrentHealth() > 0)
                                .filter(u -> isEnemy(source, u))
                                .forEach(u -> u.takeDamage(damage));
                    }
                }
            }
        }
    }

    private boolean isEnemy(GameUnit u1, GameUnit u2) {
        if (u1.getOwnerId() == null || u2.getOwnerId() == null) return true; // Monster?
        return !u1.getOwnerId().equals(u2.getOwnerId());
    }

    public record CombatResult(boolean ended, String winnerId) {}
}
