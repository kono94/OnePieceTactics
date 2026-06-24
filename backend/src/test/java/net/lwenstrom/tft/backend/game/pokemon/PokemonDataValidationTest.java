package net.lwenstrom.tft.backend.game.pokemon;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.engine.StandardGameUnit;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class PokemonDataValidationTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private static final Set<String> REMOVED_CLASS_TRAITS =
            Set.of("Starter", "Striker", "Defender", "Speedster", "Caster", "Support", "Ranger", "Legendary");

    @Test
    void pokemonRosterHasExpectedCostDistributionAndForms() throws Exception {
        var units = loadPokemonUnits();

        assertEquals(55, units.size());
        assertEquals(Map.of(1, 12L, 2, 13L, 3, 11L, 4, 12L, 5, 7L), costDistribution(units));

        var dratini = find(units, "dratini");
        assertEquals("Dragonair", new StandardGameUnit(dratini, 2).getName());
        assertEquals("dragonair", new StandardGameUnit(dratini, 2).getDefinitionId());
        assertEquals("Dragonite", new StandardGameUnit(dratini, 3).getName());
        assertEquals("dragonite", new StandardGameUnit(dratini, 3).getDefinitionId());

        var zubat = find(units, "zubat");
        assertEquals("Crobat", new StandardGameUnit(zubat, 3).getName());
        assertEquals("crobat", new StandardGameUnit(zubat, 3).getDefinitionId());

        units.stream().filter(unit -> unit.cost() >= 4).forEach(unit -> {
            assertTrue(unit.forms().isEmpty(), unit.name() + " should not evolve");
            assertEquals(unit.id(), new StandardGameUnit(unit, 3).getDefinitionId());
        });
    }

    @Test
    void pokemonDataReferencesOnlyDefinedTraits() throws Exception {
        var units = loadPokemonUnits();
        var traits = loadPokemonTraits().stream()
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
    void pokemonSetUsesOnlyTeamScopedTypeTraits() throws Exception {
        var traits = loadPokemonTraits();

        assertEquals(16, traits.size());
        for (var trait : traits) {
            assertEquals("type", trait.get("type"), trait.get("name") + " should be a Pokemon type trait");
            assertEquals("TEAM", trait.get("targetScope"), trait.get("name") + " should buff the whole team");
            assertFalse(REMOVED_CLASS_TRAITS.contains(trait.get("name")), trait.get("name") + " should be removed");
        }
    }

    @Test
    void pokemonUnitsDoNotReferenceRemovedClassTraits() throws Exception {
        var units = loadPokemonUnits();

        for (var unit : units) {
            assertTrue(
                    REMOVED_CLASS_TRAITS.stream().noneMatch(unit.traits()::contains),
                    unit.name() + " still has a removed class trait");
            unit.forms().forEach(form -> {
                if (form.traits() != null) {
                    assertTrue(
                            REMOVED_CLASS_TRAITS.stream().noneMatch(form.traits()::contains),
                            form.name() + " still has a removed class trait");
                }
            });
        }
    }

    @Test
    void pokemonTraitBreakpointsAreReachableByDistinctLines() throws Exception {
        var units = loadPokemonUnits();
        var traits = loadPokemonTraits();
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

    private List<UnitDefinition> loadPokemonUnits() throws Exception {
        InputStream is = getClass().getResourceAsStream("/data/units_pokemon.json");
        assertNotNull(is);
        return jsonMapper.readValue(is, new TypeReference<>() {});
    }

    private List<Map<String, Object>> loadPokemonTraits() throws Exception {
        InputStream is = getClass().getResourceAsStream("/data/traits_pokemon.json");
        assertNotNull(is);
        return jsonMapper.readValue(is, new TypeReference<>() {});
    }

    private UnitDefinition find(List<UnitDefinition> units, String id) {
        return units.stream()
                .filter(unit -> unit.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing unit " + id));
    }

    private Map<Integer, Long> costDistribution(List<UnitDefinition> units) {
        return units.stream().collect(Collectors.groupingBy(UnitDefinition::cost, Collectors.counting()));
    }

    private void collectPossibleTraits(
            Map<String, Set<String>> possibleLinesByTrait, String lineId, List<String> traits) {
        traits.forEach(trait -> possibleLinesByTrait
                .computeIfAbsent(trait, ignored -> new HashSet<>())
                .add(lineId));
    }
}
