package net.lwenstrom.tft.backend.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AugmentTier;
import net.lwenstrom.tft.backend.core.model.GameMode;
import org.junit.jupiter.api.Test;

class CombatSimulatorTest {

    @Test
    void sameSeedProducesSamePokemonCombatSummary() {
        var fixture = SimulationTestSupport.createFixture();
        var request = new CombatSimulationRequest(
                GameMode.POKEMON, board("Charmander", "charmander", 1), board("Bulbasaur", "bulbasaur", 1), 10, 123L);

        var first = fixture.simulator().simulate(request);
        var second = fixture.simulator().simulate(request);

        assertEquals(first.boardOneWins(), second.boardOneWins());
        assertEquals(first.boardTwoWins(), second.boardTwoWins());
        assertEquals(first.draws(), second.draws());
        assertEquals(first.averageDurationMs(), second.averageDurationMs());
        assertEquals(first.unitStats(), second.unitStats());
    }

    @Test
    void differentSeedsCanChangeStochasticCombatSummary() {
        var fixture = SimulationTestSupport.createFixture();
        var left = new BoardSpec(
                "Swordsmen",
                4,
                List.of(
                        new UnitSpec("vista_v1", 2, 3, 2),
                        new UnitSpec("zoro_v1", 2, 4, 2),
                        new UnitSpec("brook_v1", 2, 5, 2)),
                List.of(),
                AugmentTier.GOLD);
        var right = new BoardSpec(
                "Targets",
                4,
                List.of(new UnitSpec("luffy_v1", 2, 3, 0), new UnitSpec("sanji_v1", 2, 4, 0)),
                List.of(),
                AugmentTier.GOLD);

        var first = fixture.simulator().simulate(new CombatSimulationRequest(GameMode.ONEPIECE, left, right, 40, 1L));
        var second = fixture.simulator().simulate(new CombatSimulationRequest(GameMode.ONEPIECE, left, right, 40, 99L));

        assertNotEquals(
                first.averageDurationMs() + ":" + first.boardOneWins() + ":" + first.boardTwoWins(),
                second.averageDurationMs() + ":" + second.boardOneWins() + ":" + second.boardTwoWins());
    }

    @Test
    void timeoutFallsBackToRemainingHealthOutcome() {
        var fixture = SimulationTestSupport.createFixture();
        var request = new CombatSimulationRequest(
                GameMode.POKEMON, board("Tank", "snorlax", 1), board("Small", "rattata", 1), 1, 42L, 100L, 0L);

        var result = fixture.simulator().simulate(request);

        assertEquals(1, result.runs());
        assertEquals(1, result.boardOneWins());
        assertEquals(0, result.boardTwoWins());
    }

    @Test
    void pokemonTypeEffectivenessIsVisibleInMirrorCostDuel() {
        var fixture = SimulationTestSupport.createFixture();
        var fireIntoGrass = fixture.simulator()
                .simulate(new CombatSimulationRequest(
                        GameMode.POKEMON, board("Fire", "charmander", 1), board("Grass", "bulbasaur", 1), 12, 42L));
        var grassIntoFire = fixture.simulator()
                .simulate(new CombatSimulationRequest(
                        GameMode.POKEMON, board("Grass", "bulbasaur", 1), board("Fire", "charmander", 1), 12, 42L));

        assertTrue(fireIntoGrass.boardOneWins() > fireIntoGrass.boardTwoWins());
        assertTrue(grassIntoFire.boardTwoWins() > grassIntoFire.boardOneWins());
    }

    @Test
    void traitsDotsStunsShieldsAndTemporaryBuffsProduceUnitStats() {
        var fixture = SimulationTestSupport.createFixture();
        var left = new BoardSpec(
                "Pokemon Effects",
                4,
                List.of(
                        new UnitSpec("charmander", 2, 3, 2),
                        new UnitSpec("bulbasaur", 2, 4, 2),
                        new UnitSpec("jigglypuff", 2, 5, 2)),
                List.of("first-guard"),
                AugmentTier.GOLD);
        var right = new BoardSpec(
                "Targets",
                4,
                List.of(new UnitSpec("squirtle", 2, 3, 0), new UnitSpec("jigglypuff", 2, 4, 0)),
                List.of(),
                AugmentTier.GOLD);

        var result = fixture.simulator().simulate(new CombatSimulationRequest(GameMode.POKEMON, left, right, 6, 7L));

        assertFalse(result.unitStats().isEmpty());
        assertTrue(result.unitStats().values().stream().anyMatch(stats -> stats.averageDamage() > 0));
        assertTrue(result.averageBoardOneRemainingHealth() >= 0);
        assertTrue(result.averageBoardTwoRemainingHealth() >= 0);
    }

    private BoardSpec board(String name, String unitId, int starLevel) {
        return new BoardSpec(name, List.of(new UnitSpec(unitId, starLevel, 4, 2)));
    }
}
