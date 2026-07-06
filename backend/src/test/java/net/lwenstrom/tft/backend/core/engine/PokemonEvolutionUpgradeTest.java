package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class PokemonEvolutionUpgradeTest {
    @Test
    void combiningThreeBasePokemonUsesLineIdAndCreatesEvolvedForm() throws Exception {
        var charmander = loadPokemonUnit("charmander");
        var dataLoader = TestHelpers.createMockDataLoader(List.of(charmander));
        var player = createTestPlayer("Ash", dataLoader);
        player.setGold(100);
        var shop = new ArrayList<UnitDefinition>();
        shop.add(charmander);
        shop.add(charmander);
        shop.add(charmander);
        shop.add(null);
        shop.add(null);
        player.setShop(shop);

        player.buyUnit(0);
        player.buyUnit(1);
        player.buyUnit(2);

        var upgraded = player.getBench().stream()
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
        assertEquals("charmander", upgraded.getLineId());
        assertEquals(2, upgraded.getStarLevel());
        assertEquals("Charmeleon", upgraded.getName());
        assertEquals("charmeleon", upgraded.getDefinitionId());
    }

    @Test
    void combiningSixBasePokemonCreatesFinalForm() throws Exception {
        var charmander = loadPokemonUnit("charmander");
        var dataLoader = TestHelpers.createMockDataLoader(List.of(charmander));
        var player = createTestPlayer("Ash", dataLoader);
        player.setGold(100);

        for (var i = 0; i < 6; i++) {
            player.refreshShop();
            player.buyUnit(0);
        }

        var upgraded = player.getBench().stream()
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow();
        assertEquals("charmander", upgraded.getLineId());
        assertEquals(3, upgraded.getStarLevel());
        assertEquals("Charizard", upgraded.getName());
        assertEquals("charizard", upgraded.getDefinitionId());
    }

    @Test
    void shopRefreshExcludesCompletedPokemonEvolutionLine() throws Exception {
        var charmander = loadPokemonUnit("charmander");
        var squirtle = loadPokemonUnit("squirtle");
        var dataLoader = TestHelpers.createMockDataLoader(List.of(charmander, squirtle));
        var player = createTestPlayer("Ash", dataLoader);
        player.getBenchSlots().set(0, new StandardGameUnit(charmander, 3));

        player.refreshShopFree();

        assertTrue(player.hasCompletedUnitLine("charmander"));
        assertEquals("Charizard", player.getBench().get(0).getName());
        assertTrue(player.getShop().stream()
                .allMatch(unit -> unit != null && unit.lineId().equals("squirtle")));
    }

    private UnitDefinition loadPokemonUnit(String id) throws Exception {
        InputStream is = getClass().getResourceAsStream("/data/units_pokemon.json");
        assertNotNull(is);
        var units = JsonMapper.builder().build().readValue(is, new TypeReference<List<UnitDefinition>>() {});
        return units.stream()
                .filter(unit -> unit.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing unit " + id));
    }
}
