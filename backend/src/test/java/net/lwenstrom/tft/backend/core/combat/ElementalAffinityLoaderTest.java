package net.lwenstrom.tft.backend.core.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ElementalAffinityLoaderTest {
    private final ElementalAffinityLoader loader =
            new ElementalAffinityLoader(JsonMapper.builder().build());

    @Test
    void loadsThePalworldAffinityGraph() {
        var config = loader.load("/data/affinities_palworld.json");

        assertEquals(9, config.elements().size());
        assertEquals(1.2, config.strongMultiplier());
        assertEquals(0.8, config.resistedMultiplier());
        assertEquals(1.2, config.multiplier("fire", List.of("grass")));
    }

    @Test
    void rejectsDuplicateElements() {
        var config = new ElementalAffinityConfig(
                1.0,
                1.2,
                0.8,
                List.of("fire", "Fire"),
                List.of(
                        new ElementalRelationship("fire", List.of(), List.of()),
                        new ElementalRelationship("Fire", List.of(), List.of())));

        assertThrows(IllegalArgumentException.class, () -> ElementalAffinityLoader.validate(config));
    }

    @Test
    void rejectsUnknownRelationshipDefenders() {
        var config = new ElementalAffinityConfig(
                1.0,
                1.2,
                0.8,
                List.of("fire"),
                List.of(new ElementalRelationship("fire", List.of("water"), List.of())));

        assertThrows(IllegalArgumentException.class, () -> ElementalAffinityLoader.validate(config));
    }
}
