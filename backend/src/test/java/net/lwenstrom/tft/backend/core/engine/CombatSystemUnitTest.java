package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.combat.CombatUtils;
import net.lwenstrom.tft.backend.core.combat.NearestEnemyTargetSelector;
import net.lwenstrom.tft.backend.core.combat.TargetSelector;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.test.MockUnit;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombatSystemUnitTest {

    private CombatSystem combatSystem;
    private TargetSelector targetSelector;

    @BeforeEach
    void setUp() {
        combatSystem = createTestCombatSystem();
        targetSelector = new NearestEnemyTargetSelector();
    }

    @Test
    void testFindNearestEnemy_ReturnsClosest() {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var nearEnemy = MockUnit.create("near", "P2").withPosition(1, 1);
        var farEnemy = MockUnit.create("far", "P2").withPosition(5, 5);

        List<GameUnit> candidates = List.of(source, nearEnemy, farEnemy);

        var result = targetSelector.findTarget(source, candidates);

        assertEquals("near", result.getId(), "Should return the closest enemy");
    }

    @Test
    void testFindNearestEnemy_IgnoresDead() {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var deadEnemy = MockUnit.create("dead", "P2").withPosition(1, 1).withHealth(0, 100);
        var aliveEnemy = MockUnit.create("alive", "P2").withPosition(5, 5);

        List<GameUnit> candidates = List.of(source, deadEnemy, aliveEnemy);

        var result = targetSelector.findTarget(source, candidates);

        assertEquals("alive", result.getId(), "Should ignore dead units");
    }

    @Test
    void testFindNearestEnemy_IgnoresAllies() {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var ally = MockUnit.create("ally", "P1").withPosition(1, 1);
        var enemy = MockUnit.create("enemy", "P2").withPosition(5, 5);

        List<GameUnit> candidates = List.of(source, ally, enemy);

        var result = targetSelector.findTarget(source, candidates);

        assertEquals("enemy", result.getId(), "Should ignore allies");
    }

    @Test
    void testFindNearestEnemy_ReturnsNull_WhenNoEnemies() {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var ally = MockUnit.create("ally", "P1").withPosition(1, 1);

        List<GameUnit> candidates = List.of(source, ally);

        var result = targetSelector.findTarget(source, candidates);

        assertNull(result, "Should return null when no enemies exist");
    }

    @Test
    void testGetDistance_CalculatesChebyshev() {
        var u1 = MockUnit.create("u1", "P1").withPosition(0, 0);
        var u2 = MockUnit.create("u2", "P2").withPosition(3, 4);

        var distance = CombatUtils.getDistance(u1, u2);

        assertEquals(4.0, distance, 0.001, "Distance should be 4 (max of 3 and 4)");
    }

    @Test
    void testIsEnemy_SameOwner_False() {
        var u1 = MockUnit.create("u1", "P1");
        var u2 = MockUnit.create("u2", "P1");

        var result = CombatUtils.isEnemy(u1, u2);

        assertFalse(result, "Same owner should not be enemies");
    }

    @Test
    void testIsEnemy_DifferentOwner_True() {
        var u1 = MockUnit.create("u1", "P1");
        var u2 = MockUnit.create("u2", "P2");

        var result = CombatUtils.isEnemy(u1, u2);

        assertTrue(result, "Different owners should be enemies");
    }

    @Test
    void testIsEnemy_NullOwner_True() {
        var u1 = MockUnit.create("u1", "P1");
        var u2 = MockUnit.create("u2", null);

        var result = CombatUtils.isEnemy(u1, u2);

        assertTrue(result, "Null owner should be treated as enemy (monster)");
    }

    @Test
    void testSimulateTick_UnitAttacksInRange() {
        var p1 = new Player("P1", null, createSeededRandomProvider());
        var p2 = new Player("P2", null, createSeededRandomProvider());

        var attacker = MockUnit.create("attacker", p1.getId())
                .withPosition(3, 0)
                .withAttackDamage(25)
                .withRange(1);
        var target = MockUnit.create("target", p2.getId()).withPosition(3, 0).withHealth(100, 100);

        addUnitToPlayer(p1, attacker);
        addUnitToPlayer(p2, target);

        combatSystem.startCombat(List.of(p1, p2));

        int attackerY = attacker.getY();
        int targetY = target.getY();
        int distance = Math.abs(attackerY - targetY);
        assertEquals(1, distance, "Units should be adjacent after combat positioning");

        for (int i = 0; i < 20; i++) {
            combatSystem.simulateTick(List.of(p1, p2));
        }

        assertTrue(target.getCurrentHealth() < 100, "Target should have taken damage");
    }

    @Test
    void testSimulateTick_ManaGain_OnAttack() {
        var p1 = new Player("P1", null, createSeededRandomProvider());
        var p2 = new Player("P2", null, createSeededRandomProvider());

        var attacker = MockUnit.create("attacker", p1.getId())
                .withPosition(3, 0)
                .withMana(0, 100)
                .withRange(1);
        var target = MockUnit.create("target", p2.getId()).withPosition(3, 0).withHealth(100, 100);

        addUnitToPlayer(p1, attacker);
        addUnitToPlayer(p2, target);

        combatSystem.startCombat(List.of(p1, p2));

        for (int i = 0; i < 20; i++) {
            combatSystem.simulateTick(List.of(p1, p2));
        }

        assertTrue(attacker.getMana() > 0, "Attacker should have gained mana from attacking");
    }

    private void addUnitToPlayer(Player player, GameUnit unit) {
        TestHelpers.addUnitToPlayer(player, unit);
    }
}
