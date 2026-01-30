package net.lwenstrom.tft.backend.core.model;

import java.util.Collections;
import java.util.List;

public record AbilityDefinition(
        String name,
        String description,
        AbilityType type,
        String pattern,
        int range,
        List<Integer> values, // Exactly 3 values [lvl1, lvl2, lvl3]
        List<AbilityModifier> modifiers) {

    public AbilityDefinition {
        if (modifiers == null) modifiers = Collections.emptyList();
        if (values == null) values = Collections.emptyList();
    }

    // Get value for a specific star level (1-indexed)
    public int getValueForLevel(int starLevel) {
        if (values.isEmpty()) return 0;
        int index = Math.min(starLevel - 1, values.size() - 1);
        return values.get(index);
    }

    public List<AbilityModifier> modifiers() {
        return modifiers;
    }

    // Generate formatted description using template placeholders
    // Supported placeholders: $value, $range, $pattern, $type
    // $value is formatted as "v1/v2/v3" with the current level highlighted
    public String getFormattedDescription(int starLevel) {
        if (description == null) return "";

        var formatted = description;

        // Format $value as "v1/v2/v3" with highlighting
        if (values != null && !values.isEmpty()) {
            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) valueBuilder.append("/");

                int level = i + 1;
                if (level == starLevel) {
                    valueBuilder
                            .append("<span class=\"active\">")
                            .append(values.get(i))
                            .append("</span>");
                } else {
                    valueBuilder
                            .append("<span class=\"inactive\">")
                            .append(values.get(i))
                            .append("</span>");
                }
            }
            formatted = formatted.replace("$value", valueBuilder.toString());
        }

        // Replace other placeholders
        formatted = formatted.replace("$range", String.valueOf(range));
        formatted = formatted.replace("$pattern", pattern != null ? pattern : "SINGLE");
        formatted = formatted.replace("$type", type.toString());

        return formatted;
    }
}
