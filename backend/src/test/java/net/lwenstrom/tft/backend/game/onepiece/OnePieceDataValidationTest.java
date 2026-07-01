package net.lwenstrom.tft.backend.game.onepiece;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class OnePieceDataValidationTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void onePieceDataReferencesOnlyDefinedTraits() throws Exception {
        var units = loadOnePieceUnits();
        var traits = loadOnePieceTraits().stream()
                .map(trait -> (String) trait.get("name"))
                .collect(Collectors.toSet());

        for (var unit : units) {
            unit.traits().forEach(trait -> assertTrue(traits.contains(trait), unit.name() + " missing trait " + trait));
            unit.forms().forEach(form -> {
                if (form.traits() != null) {
                    form.traits()
                            .forEach(trait ->
                                    assertTrue(traits.contains(trait), form.name() + " missing trait " + trait));
                }
            });
        }
    }

    @Test
    void onePieceTraitBreakpointsAreReachableByDistinctLines() throws Exception {
        var units = loadOnePieceUnits();
        var traits = loadOnePieceTraits();
        var possibleLinesByTrait = new HashMap<String, Set<String>>();

        for (var unit : units) {
            collectPossibleTraits(possibleLinesByTrait, unit.lineId(), unit.traits());
            unit.forms().forEach(form -> {
                if (form.traits() != null && !form.traits().isEmpty()) {
                    collectPossibleTraits(possibleLinesByTrait, unit.lineId(), form.traits());
                }
            });
        }

        for (var trait : traits) {
            var traitName = (String) trait.get("name");
            @SuppressWarnings("unchecked")
            var effects = (List<Map<String, Object>>) trait.get("effects");
            var maxBreakpoint = effects.stream()
                    .mapToInt(effect -> ((Number) effect.get("minUnits")).intValue())
                    .max()
                    .orElse(0);
            var possibleLines =
                    possibleLinesByTrait.getOrDefault(traitName, Set.of()).size();
            assertTrue(
                    maxBreakpoint <= possibleLines,
                    traitName + " max breakpoint " + maxBreakpoint + " exceeds possible lines " + possibleLines);
        }
    }

    @Test
    void sniperIsOneUnitSelfTrait() throws Exception {
        var traits = loadOnePieceTraits();

        var sniper = traits.stream()
                .filter(trait -> "Sniper".equals(trait.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing Sniper trait"));

        assertEquals("SELF", sniper.get("targetScope"));
        assertEquals("DISTANCE_DAMAGE", sniper.get("effectType"));

        @SuppressWarnings("unchecked")
        var effects = (List<Map<String, Object>>) sniper.get("effects");
        assertEquals(1, effects.size());
        assertEquals(1, effects.getFirst().get("minUnits"));

        @SuppressWarnings("unchecked")
        var values = (Map<String, Object>) effects.getFirst().get("values");
        assertEquals(0.1, ((Number) values.get("damagePerCell")).doubleValue());
    }

    private List<UnitDefinition> loadOnePieceUnits() throws Exception {
        InputStream is = getClass().getResourceAsStream("/data/units_onepiece.json");
        assertNotNull(is);
        return jsonMapper.readValue(is, new TypeReference<>() {});
    }

    private List<Map<String, Object>> loadOnePieceTraits() throws Exception {
        InputStream is = getClass().getResourceAsStream("/data/traits_onepiece.json");
        assertNotNull(is);
        return jsonMapper.readValue(is, new TypeReference<>() {});
    }

    private void collectPossibleTraits(
            Map<String, Set<String>> possibleLinesByTrait, String lineId, List<String> traits) {
        traits.forEach(trait -> possibleLinesByTrait
                .computeIfAbsent(trait, ignored -> new HashSet<>())
                .add(lineId));
    }
}
