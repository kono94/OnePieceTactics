package net.lwenstrom.tft.backend.core.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record AbilityDefinition(
        String name,
        String description,
        AbilityType type,
        AbilityPattern pattern,
        List<Integer> range,
        List<Integer> values, // Exactly 3 values [lvl1, lvl2, lvl3]
        List<AbilityModifier> modifiers,
        List<Integer> targetLimit,
        List<Float> stunDurationSeconds) {
    public AbilityDefinition {
        if (modifiers == null) {
            modifiers = Collections.emptyList();
        }
        if (values == null) {
            values = Collections.emptyList();
        }
        if (range == null) {
            range = Collections.emptyList();
        }
        if (targetLimit == null) {
            targetLimit = Collections.emptyList();
        }
        if (stunDurationSeconds == null) {
            stunDurationSeconds = Collections.emptyList();
        }
    }

    public AbilityDefinition(
            String name,
            String description,
            AbilityType type,
            AbilityPattern pattern,
            List<Integer> range,
            List<Integer> values,
            List<AbilityModifier> modifiers) {
        this(
                name,
                description,
                type,
                pattern,
                range,
                values,
                modifiers,
                Collections.emptyList(),
                Collections.emptyList());
    }

    public AbilityDefinition(
            String name,
            String description,
            AbilityType type,
            AbilityPattern pattern,
            List<Integer> range,
            List<Integer> values,
            List<AbilityModifier> modifiers,
            List<Integer> targetLimit) {
        this(name, description, type, pattern, range, values, modifiers, targetLimit, Collections.emptyList());
    }

    // Get value for a specific star level (1-indexed)
    public int getValueForLevel(int starLevel) {
        if (values.isEmpty()) {
            return 0;
        }
        int index = Math.min(starLevel - 1, values.size() - 1);
        return values.get(index);
    }

    public float getStunDurationForLevel(int starLevel) {
        if (stunDurationSeconds.isEmpty()) {
            return getValueForLevel(starLevel);
        }
        var index = Math.min(starLevel - 1, stunDurationSeconds.size() - 1);
        return stunDurationSeconds.get(index);
    }

    // Get range for a specific star level (1-indexed)
    public int getRangeForLevel(int starLevel) {
        if (range.isEmpty()) {
            return 0;
        }
        int index = Math.min(starLevel - 1, range.size() - 1);
        return range.get(index);
    }

    public int getTargetLimitForLevel(int starLevel) {
        if (targetLimit.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int index = Math.min(starLevel - 1, targetLimit.size() - 1);
        return targetLimit.get(index);
    }

    public List<AbilityModifier> modifiers() {
        return modifiers;
    }

    // Generate formatted description using template placeholders
    // Supported placeholders: $value, $range, $pattern, $type
    public String getFormattedDescription(int starLevel) {
        if (description == null) {
            return "";
        }

        var formatted = description;

        // Format $value as "v1/v2/v3" with highlighting
        var descriptionValues =
                type == AbilityType.STUN && !stunDurationSeconds.isEmpty() ? stunDurationSeconds : values;
        if (descriptionValues != null && !descriptionValues.isEmpty()) {
            formatted = formatted.replace("$value", formatList(descriptionValues, starLevel));
        }

        // Format $range as "r1/r2/r3" with highlighting
        if (range != null && !range.isEmpty()) {
            formatted = formatted.replace("$range", formatList(range, starLevel));
        }

        // Replace other placeholders
        formatted = formatted.replace("$pattern", pattern != null ? pattern.name() : "SINGLE");
        formatted = formatted.replace("$type", type.toString());

        return formatted;
    }

    private String formatList(List<? extends Number> list, int starLevel) {
        var builder = new StringBuilder();
        for (var i = 0; i < list.size(); i++) {
            if (i > 0) builder.append("/");

            var level = i + 1;
            var value = BigDecimal.valueOf(list.get(i).doubleValue())
                    .stripTrailingZeros()
                    .toPlainString();
            if (level == starLevel) {
                builder.append("<span class=\"active\">").append(value).append("</span>");
            } else {
                builder.append("<span class=\"inactive\">").append(value).append("</span>");
            }
        }
        return builder.toString();
    }
}
