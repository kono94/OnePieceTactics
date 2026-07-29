package net.lwenstrom.tft.backend.core.combat;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import tools.jackson.databind.json.JsonMapper;

public final class ElementalAffinityLoader {
    private final JsonMapper jsonMapper;

    public ElementalAffinityLoader() {
        this(JsonMapper.builder().build());
    }

    public ElementalAffinityLoader(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public ElementalAffinityConfig load(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Affinity resource stream is required");
        }
        var config = jsonMapper.readValue(inputStream, ElementalAffinityConfig.class);
        validate(config);
        return config;
    }

    public ElementalAffinityConfig load(String resourcePath) {
        try (var inputStream = ElementalAffinityLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Could not find affinity data at " + resourcePath);
            }
            return load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to close elemental affinity data", e);
        }
    }

    public static void validate(ElementalAffinityConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Affinity config is required");
        }
        validateMultiplier(config.defaultMultiplier(), "defaultMultiplier");
        validateMultiplier(config.strongMultiplier(), "strongMultiplier");
        validateMultiplier(config.resistedMultiplier(), "resistedMultiplier");

        var elements = normalizeUnique(config.elements(), "elements");
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Affinity config must define at least one element");
        }
        if (config.relationships().size() != elements.size()) {
            throw new IllegalArgumentException("Affinity config must define one relationship per element");
        }

        var attackingElements = new HashSet<String>();
        for (var relationship : config.relationships()) {
            if (relationship == null || relationship.attacking() == null) {
                throw new IllegalArgumentException("Affinity relationship must define an attacking element");
            }
            var attacking = normalizeId(relationship.attacking());
            if (!elements.contains(attacking) || !attackingElements.add(attacking)) {
                throw new IllegalArgumentException(
                        "Affinity relationship has an unknown or duplicate attacker: " + attacking);
            }

            var strongAgainst = normalizeUnique(relationship.strongAgainst(), "strongAgainst");
            var resistedBy = normalizeUnique(relationship.resistedBy(), "resistedBy");
            if (!elements.containsAll(strongAgainst) || !elements.containsAll(resistedBy)) {
                throw new IllegalArgumentException("Affinity relationship references an unknown defender");
            }
            var overlap = new HashSet<>(strongAgainst);
            overlap.retainAll(resistedBy);
            if (!overlap.isEmpty()) {
                throw new IllegalArgumentException("Affinity relationship cannot both affect a defender: " + overlap);
            }
        }
        if (!attackingElements.equals(elements)) {
            throw new IllegalArgumentException("Affinity config must cover every attacking element");
        }
    }

    private static Set<String> normalizeUnique(List<String> values, String field) {
        if (values == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        var normalized = new HashSet<String>();
        for (var value : values) {
            if (value == null || value.isBlank() || !normalized.add(normalizeId(value))) {
                throw new IllegalArgumentException(field + " must contain unique nonblank ids");
            }
        }
        return normalized;
    }

    private static void validateMultiplier(double value, String field) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(field + " must be a positive finite number");
        }
    }

    private static String normalizeId(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
