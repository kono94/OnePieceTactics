package net.lwenstrom.tft.backend.core.combat;

import net.lwenstrom.tft.backend.core.model.GameUnit;

public final class CombatUtils {

    private CombatUtils() {}

    public static double getDistance(GameUnit u1, GameUnit u2) {
        return Math.max(Math.abs(u1.getX() - u2.getX()), Math.abs(u1.getY() - u2.getY()));
    }

    public static boolean isEnemy(GameUnit u1, GameUnit u2) {
        if (u1.getOwnerId() == null || u2.getOwnerId() == null) return true;
        return !u1.getOwnerId().equals(u2.getOwnerId());
    }

    public static boolean isAlly(GameUnit u1, GameUnit u2) {
        if (u1.getOwnerId() == null || u2.getOwnerId() == null) return false;
        return u1.getOwnerId().equals(u2.getOwnerId());
    }
}
