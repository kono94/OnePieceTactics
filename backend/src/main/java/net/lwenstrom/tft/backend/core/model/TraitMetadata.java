package net.lwenstrom.tft.backend.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TraitMetadata(
        String id,
        String name,
        String description,
        String type,
        String iconColor,
        List<TraitEffect> effects,
        Map<String, Object> extras) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TraitEffect(int minUnits, String description, String style) {}
}
