package net.lwenstrom.tft.backend.core.model;

public record DotEffect(
        String sourceId,
        String sourceName,
        String sourceDefinitionId,
        String sourceOwnerId,
        int damagePerTick,
        long nextTickTime,
        long expiresAt,
        long tickIntervalMs,
        String skillName) {
    public DotEffect withNextTickTime(long nextTickTime) {
        return new DotEffect(
                sourceId,
                sourceName,
                sourceDefinitionId,
                sourceOwnerId,
                damagePerTick,
                nextTickTime,
                expiresAt,
                tickIntervalMs,
                skillName);
    }
}
