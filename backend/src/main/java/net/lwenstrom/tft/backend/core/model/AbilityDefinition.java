package net.lwenstrom.tft.backend.core.model;

import java.util.Collections;
import java.util.List;

public record AbilityDefinition(
        String name,
        String description,
        AbilityType type,
        String pattern,
        List<Integer> range,
        List<Integer> values, // Exactly 3 values [lvl1, lvl2, lvl3]
        List<AbilityModifier> modifiers) {
    public AbilityDefinition {
        if (modifiers == null)
            modifiers = Collections.emptyList();
        if (values == null)
            values = Collections.emptyList();
        if (range == null)
            range = Collections.emptyList();
    }

    // Get value for a specific star level (1-indexed)
    public int getValueForLevel(int starLevel) {
        if (values.isEmpty())
            return 0;
        int index = Math.min(starLevel - 1, values.size() - 1);
        return values.get(index);
    }

    // Get range for a specific star level (1-indexed)
    public int getRangeForLevel(int starLevel) {
        if (range.isEmpty())
            return 0;
        int index = Math.min(starLevel - 1, range.size() - 1);
        return range.get(index);
    }

    public List<AbilityModifier> modifiers() {
        return modifiers;
    }

    // Generate formatted description using template placeholders
    // Supported placeholders: $value, $range, $pattern, $type
    public String getFormattedDescription(int starLevel) {
        if (description == null)
            return "";

        var formatted = description;

        // Format $value as "v1/v2/v3" with highlighting
        if (values != null && !values.isEmpty()) {
            formatted = formatted.replace("$value", formatList(values, starLevel));
        }

        // Format $range as "r1/r2/r3" with highlighting
        if (range != null && !range.isEmpty()) {
            formatted = formatted.replace("$range", formatList(range, starLevel));
        }

        // Replace other placeholders
        formatted = formatted.replace("$pattern", pattern != null ? pattern : "SINGLE");
        formatted = formatted.replace("$type", type.toString());

        return formatted;
    }

    private String formatList(List<Integer> list, int starLevel) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0)
                builder.append("/");

            int level = i + 1;
            if (level == starLevel) {
                builder.append("<span class=\"active\">").append(list.get(i)).append("</span>");
            } else {
                builder.append("<span class=\"inactive\">").append(list.get(i)).append("</span>");
            }
        }
        return builder.toString();
    }
}
