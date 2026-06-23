package net.lwenstrom.tft.backend.game.pokemon;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.engine.StandardGameUnit;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class PokemonDataValidationTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

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
}
