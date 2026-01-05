package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.grid.Grid;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import java.util.List;

public class CombatEngine {

    // Simulate one tick of combat between two players
    // This would be called by GameRoom during COMBAT phase
    public void simulateTick(Player p1, Player p2) {
        // 1. Identify targets (Nearest neighbor)
        // 2. Attack (Deal damage)
        // 3. Generate Mana
        // 4. Check deaths
        // 5. Cast skills if mana full

        // Placeholder for the "Simplified Combat" required by user
        // We will just have them find nearest valid target and hit it
    }

    private GameUnit findTarget(GameUnit source, List<GameUnit> enemies) {
        // Simple distance check
        return enemies.stream()
                .min((u1, u2) -> Double.compare(dist(source, u1), dist(source, u2)))
                .orElse(null);
    }

    private double dist(GameUnit u1, GameUnit u2) {
        return Math.sqrt(Math.pow(u1.getX() - u2.getX(), 2) + Math.pow(u1.getY() - u2.getY(), 2));
    }
}
