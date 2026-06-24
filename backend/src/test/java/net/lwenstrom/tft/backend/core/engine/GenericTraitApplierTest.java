package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.lwenstrom.tft.backend.core.model.EffectType;
import net.lwenstrom.tft.backend.core.model.TraitTargetScope;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class GenericTraitApplierTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void defaultScopeAppliesOnlyToTraitHolders() throws Exception {
        var effects = effects("""
                [{"minUnits":1,"values":{"manaGain":0.30}}]
                """);
        var applier = new GenericTraitApplier("water", EffectType.MANA_GAIN, effects);
        var water = MockUnit.create("water", "P1").withTraits(Set.of("Water"));
        var normal = MockUnit.create("normal", "P1").withTraits(Set.of("Normal"));

        applier.apply(1, List.of(water, normal));

        assertEquals(1.3f, water.getManaGainMultiplier(), 0.001f);
        assertEquals(1.0f, normal.getManaGainMultiplier(), 0.001f);
    }

    @Test
    void teamScopeAppliesToEveryUnitOnTeam() throws Exception {
        var effects = effects("""
                [{"minUnits":1,"values":{"manaGain":0.30}}]
                """);
        var applier = new GenericTraitApplier("water", EffectType.MANA_GAIN, TraitTargetScope.TEAM, effects);
        var water = MockUnit.create("water", "P1").withTraits(Set.of("Water"));
        var normal = MockUnit.create("normal", "P1").withTraits(Set.of("Normal"));

        applier.apply(1, List.of(water, normal));

        assertEquals(1.3f, water.getManaGainMultiplier(), 0.001f);
        assertEquals(1.3f, normal.getManaGainMultiplier(), 0.001f);
    }

    @Test
    void startManaPercentScalesWithEachUnitsMaxMana() throws Exception {
        var effects = effects("""
                [{"minUnits":1,"values":{"manaPercent":0.25}}]
                """);
        var applier = new GenericTraitApplier("psychic", EffectType.START_MANA_PERCENT, TraitTargetScope.TEAM, effects);
        var lowMana = MockUnit.create("low", "P1").withTraits(Set.of("Psychic")).withMana(0, 60);
        var highMana =
                MockUnit.create("high", "P1").withTraits(Set.of("Normal")).withMana(0, 100);

        applier.apply(1, List.of(lowMana, highMana));

        assertEquals(15, lowMana.getMana());
        assertEquals(25, highMana.getMana());
    }

    private List<JsonNode> effects(String json) throws Exception {
        var result = new ArrayList<JsonNode>();
        var array = jsonMapper.readTree(json);
        for (var effect : array) {
            result.add(effect);
        }
        return result;
    }
}
