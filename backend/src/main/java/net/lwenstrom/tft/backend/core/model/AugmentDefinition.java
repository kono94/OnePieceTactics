package net.lwenstrom.tft.backend.core.model;

import java.util.List;

public record AugmentDefinition(
        String id,
        String name,
        AugmentEffectType effectType,
        List<Integer> values,
        List<String> descriptions,
        String image) {}
