package net.lwenstrom.tft.backend.game.palworld;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.combat.ElementalAffinityLoader;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import net.lwenstrom.tft.backend.core.model.AugmentDefinition;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.UnitRole;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class PalworldDataValidationTest {
    private static final Set<String> ELEMENTS =
            Set.of("Neutral", "Fire", "Water", "Electric", "Grass", "Ice", "Ground", "Dark", "Dragon");

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void rosterMatchesCanonicalLinesCostsRolesAndStats() throws Exception {
        var units = loadUnits();

        assertEquals(55, units.size());
        assertEquals(55, units.stream().map(UnitDefinition::id).distinct().count());
        assertEquals(
                Set.of(
                        "lamball",
                        "cattiva",
                        "chikipi",
                        "foxparks",
                        "lifmunk",
                        "pengullet",
                        "daedream",
                        "depresso",
                        "gumoss",
                        "vixy",
                        "sparkit",
                        "tanzee",
                        "fuack",
                        "tocotoco",
                        "direhowl",
                        "celaray",
                        "dumud",
                        "dazzi",
                        "flambelle",
                        "mimog",
                        "cremis",
                        "melpaca",
                        "galeclaw",
                        "lovander",
                        "hoodle",
                        "chillet",
                        "penking",
                        "katress",
                        "lunaris",
                        "quivern",
                        "petallia",
                        "mossanda",
                        "grizzbolt",
                        "tarantriss",
                        "relaxaurus",
                        "tetroise",
                        "anubis",
                        "shadowbeak",
                        "lyleen",
                        "orserk",
                        "selyne",
                        "jormuntide-ignis",
                        "bellanoir",
                        "aegidron",
                        "renjishi",
                        "silvance",
                        "dandilord",
                        "shaolong",
                        "jetragon",
                        "frostallion",
                        "paladius",
                        "necromus",
                        "neptilius",
                        "xenolord",
                        "panthalus"),
                units.stream().map(UnitDefinition::id).collect(Collectors.toSet()));
        assertEquals(Map.of(1, 12L, 2, 13L, 3, 11L, 4, 12L, 5, 7L), distribution(units, UnitDefinition::cost));
        assertEquals(
                Map.of(UnitRole.DAMAGE, 23L, UnitRole.TANK, 16L, UnitRole.SUPPORT, 16L),
                distribution(units, UnitDefinition::role));
        assertEquals(List.of(900, 1620, 2916), find(units, "lamball").maxHealth());
        assertEquals(List.of(140, 252, 454), find(units, "jetragon").attackDamage());
        assertEquals(List.of(2400, 4320, 7776), find(units, "panthalus").maxHealth());
        assertEquals(List.of(0.60f, 0.60f, 0.60f), find(units, "panthalus").attackSpeed());

        units.forEach(unit -> {
            assertEquals(unit.id(), unit.lineId());
            assertTrue(unit.forms().isEmpty(), unit.id() + " must not have star-specific forms");
            assertEquals(3, unit.maxHealth().size());
            assertEquals(3, unit.maxMana().size());
            assertEquals(3, unit.attackDamage().size());
            assertEquals(3, unit.abilityPower().size());
            assertEquals(3, unit.defense().size());
            assertEquals(3, unit.attackSpeed().size());
            assertEquals(3, unit.range().size());
            assertTrue(unit.traits().size() >= 1 && unit.traits().size() <= 2);
            assertTrue(unit.traits().stream().allMatch(ELEMENTS::contains));
            assertNotNull(unit.ability());
            assertEquals(3, unit.ability().values().size());
            assertSame(unit.ability(), unit.getAbility(1));
            assertSame(unit.ability(), unit.getAbility(2));
            assertSame(unit.ability(), unit.getAbility(3));
        });
    }

    @Test
    void revisedSchemaDoesNotContainExplicitElementsKeysOrAnimationFields() throws Exception {
        var root = loadRaw("/data/units_palworld.json");

        for (var unit : root) {
            assertFalse(unit.has("basicElement"), unit.get("id").asString());
            assertFalse(unit.has("attackAnimationKey"), unit.get("id").asString());
            assertFalse(unit.has("forms"), unit.get("id").asString());
            var ability = unit.get("ability");
            assertFalse(ability.has("element"), unit.get("id").asString());
            assertFalse(ability.has("key"), unit.get("id").asString());
            assertFalse(ability.has("animationKey"), unit.get("id").asString());
        }
    }

    @Test
    void traitsUseTheNinePalworldElementsAndReachAllBreakpoints() throws Exception {
        var traits = loadRawList("/data/traits_palworld.json");

        assertEquals(9, traits.size());
        assertEquals(
                ELEMENTS.stream().map(String::toLowerCase).collect(Collectors.toSet()),
                traits.stream().map(trait -> trait.get("id").asString()).collect(Collectors.toSet()));
        traits.forEach(trait -> {
            assertEquals("element", trait.get("type").asString());
            assertEquals("TEAM", trait.get("targetScope").asString());
            var minUnits = new java.util.ArrayList<Integer>();
            trait.get("effects")
                    .forEach(effect -> minUnits.add(effect.get("minUnits").asInt()));
            assertEquals(List.of(1, 2, 3, 4), minUnits);
        });
    }

    @Test
    void augmentsAndAffinityGraphMatchCanonicalResources() throws Exception {
        var augments = loadAugments();
        assertEquals(15, augments.size());
        assertTrue(augments.stream().allMatch(augment -> augment.values().size() == 3));
        assertTrue(augments.stream().allMatch(augment -> augment.descriptions().size() == 3));

        try (var inputStream = getClass().getResourceAsStream("/data/affinities_palworld.json")) {
            assertNotNull(inputStream);
            var config = new ElementalAffinityLoader(jsonMapper).load(inputStream);
            assertEquals(9, config.elements().size());
            assertEquals(9, config.relationships().size());
            assertEquals(1.2, config.strongMultiplier());
            assertEquals(0.8, config.resistedMultiplier());
        }
    }

    @Test
    void providerLoadsAllRevisedPalworldResources() {
        var provider = new PalworldGameModeProvider(jsonMapper);
        var registry = new GameModeRegistry(List.of(provider), "palworld");
        var dataLoader = new DataLoader(registry, jsonMapper);

        assertEquals(GameMode.PALWORLD, provider.getMode());
        assertEquals(55, dataLoader.getAllUnits(GameMode.PALWORLD).size());
        assertEquals(9, dataLoader.getTraitMetadata(GameMode.PALWORLD).size());
        assertEquals(15, dataLoader.getAugments(GameMode.PALWORLD).size());
        assertEquals(
                9, dataLoader.getAffinityConfig(GameMode.PALWORLD).elements().size());
    }

    private List<UnitDefinition> loadUnits() throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/data/units_palworld.json")) {
            assertNotNull(inputStream);
            return jsonMapper.readValue(inputStream, new TypeReference<>() {});
        }
    }

    private List<AugmentDefinition> loadAugments() throws Exception {
        try (var inputStream = getClass().getResourceAsStream("/data/augments_palworld.json")) {
            assertNotNull(inputStream);
            return jsonMapper.readValue(inputStream, new TypeReference<>() {});
        }
    }

    private JsonNode loadRaw(String resource) throws Exception {
        try (var inputStream = getClass().getResourceAsStream(resource)) {
            assertNotNull(inputStream);
            return jsonMapper.readTree(inputStream);
        }
    }

    private List<JsonNode> loadRawList(String resource) throws Exception {
        var root = loadRaw(resource);
        var values = new java.util.ArrayList<JsonNode>();
        root.forEach(values::add);
        return values;
    }

    private UnitDefinition find(List<UnitDefinition> units, String id) {
        return units.stream().filter(unit -> unit.id().equals(id)).findFirst().orElseThrow();
    }

    private <T> Map<T, Long> distribution(List<UnitDefinition> units, Function<UnitDefinition, T> key) {
        return units.stream().collect(Collectors.groupingBy(key, Collectors.counting()));
    }
}
