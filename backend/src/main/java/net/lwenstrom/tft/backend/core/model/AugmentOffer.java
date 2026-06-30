package net.lwenstrom.tft.backend.core.model;

public record AugmentOffer(
        String id,
        String name,
        String description,
        AugmentTier tier,
        AugmentEffectType effectType,
        int value,
        String image) {}
