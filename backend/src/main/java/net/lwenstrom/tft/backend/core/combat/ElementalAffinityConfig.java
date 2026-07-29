package net.lwenstrom.tft.backend.core.combat;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record ElementalAffinityConfig(
        double defaultMultiplier,
        double strongMultiplier,
        double resistedMultiplier,
        List<String> elements,
        List<ElementalRelationship> relationships) {

    public ElementalAffinityConfig {
        elements = elements == null ? List.of() : List.copyOf(elements);
        relationships = relationships == null ? List.of() : List.copyOf(relationships);
    }

    public static ElementalAffinityConfig neutral() {
        return new ElementalAffinityConfig(1.0, 1.0, 1.0, List.of(), List.of());
    }

    public boolean containsElement(String element) {
        return element != null && elements.stream().anyMatch(value -> sameId(value, element));
    }

    public double multiplier(String attackingElement, Collection<String> defendingElements) {
        if (attackingElement == null || defendingElements == null || defendingElements.isEmpty()) {
            return defaultMultiplier;
        }

        var relationship = relationshipFor(attackingElement);
        var strongAgainst =
                normalize(relationship.map(ElementalRelationship::strongAgainst).orElse(List.of()));
        var resistedBy =
                normalize(relationship.map(ElementalRelationship::resistedBy).orElse(List.of()));
        return distinctNormalized(defendingElements).stream()
                .mapToDouble(defendingElement -> strongAgainst.contains(defendingElement)
                        ? strongMultiplier
                        : resistedBy.contains(defendingElement) ? resistedMultiplier : defaultMultiplier)
                .reduce(1.0, (left, right) -> left * right);
    }

    public double getMultiplier(String attackingElement, Collection<String> defendingElements) {
        return multiplier(attackingElement, defendingElements);
    }

    public double multiplier(Collection<String> attackingElements, Collection<String> defendingElements) {
        if (attackingElements == null || attackingElements.isEmpty()) {
            return defaultMultiplier;
        }
        return distinctNormalized(attackingElements).stream()
                .mapToDouble(attackingElement -> multiplier(attackingElement, defendingElements))
                .max()
                .orElse(defaultMultiplier);
    }

    public double getMultiplier(Collection<String> attackingElements, Collection<String> defendingElements) {
        return multiplier(attackingElements, defendingElements);
    }

    public Map<String, ElementalRelationship> relationshipMap() {
        var relationshipMap = relationships.stream()
                .collect(Collectors.toMap(
                        relationship -> normalizeId(relationship.attacking()),
                        relationship -> relationship,
                        (left, right) -> left,
                        LinkedHashMap::new));
        return Map.copyOf(relationshipMap);
    }

    public Set<String> normalizedElements() {
        return elements.stream().map(ElementalAffinityConfig::normalizeId).collect(Collectors.toUnmodifiableSet());
    }

    private Optional<ElementalRelationship> relationshipFor(String attackingElement) {
        return relationships.stream()
                .filter(relationship -> sameId(relationship.attacking(), attackingElement))
                .findFirst();
    }

    private static Set<String> normalize(Collection<String> values) {
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(ElementalAffinityConfig::normalizeId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> distinctNormalized(Collection<String> values) {
        return normalize(values);
    }

    private static boolean sameId(String left, String right) {
        return left != null && right != null && normalizeId(left).equals(normalizeId(right));
    }

    private static String normalizeId(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
