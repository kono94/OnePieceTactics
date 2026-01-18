package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.test.MockUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CombatSystemUnitTest {

    private CombatSystem combatSystem;

    @BeforeEach
    void setUp() {
        combatSystem = new CombatSystem();
    }

    @Test
    void testFindNearestEnemy_ReturnsClosest() throws Exception {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var nearEnemy = MockUnit.create("near", "P2").withPosition(1, 1);
        var farEnemy = MockUnit.create("far", "P2").withPosition(5, 5);

        List<GameUnit> candidates = List.of(source, nearEnemy, farEnemy);

        var result = invokeFindNearestEnemy(source, candidates);

        assertEquals("near", result.getId(), "Should return the closest enemy");
    }

    @Test
    void testFindNearestEnemy_IgnoresDead() throws Exception {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var deadEnemy = MockUnit.create("dead", "P2").withPosition(1, 1).withHealth(0, 100);
        var aliveEnemy = MockUnit.create("alive", "P2").withPosition(5, 5);

        List<GameUnit> candidates = List.of(source, deadEnemy, aliveEnemy);

        var result = invokeFindNearestEnemy(source, candidates);

        assertEquals("alive", result.getId(), "Should ignore dead units");
    }

    @Test
    void testFindNearestEnemy_IgnoresAllies() throws Exception {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var ally = MockUnit.create("ally", "P1").withPosition(1, 1);
        var enemy = MockUnit.create("enemy", "P2").withPosition(5, 5);

        List<GameUnit> candidates = List.of(source, ally, enemy);

        var result = invokeFindNearestEnemy(source, candidates);

        assertEquals("enemy", result.getId(), "Should ignore allies");
    }

    @Test
    void testFindNearestEnemy_ReturnsNull_WhenNoEnemies() throws Exception {
        var source = MockUnit.create("source", "P1").withPosition(0, 0);
        var ally = MockUnit.create("ally", "P1").withPosition(1, 1);

        List<GameUnit> candidates = List.of(source, ally);

        var result = invokeFindNearestEnemy(source, candidates);

        assertNull(result, "Should return null when no enemies exist");
    }

    @Test
    void testGetDistance_CalculatesEuclidean() throws Exception {
        var u1 = MockUnit.create("u1", "P1").withPosition(0, 0);
        var u2 = MockUnit.create("u2", "P2").withPosition(3, 4);

        var distance = invokeGetDistance(u1, u2);

        assertEquals(5.0, distance, 0.001, "Distance should be 5 (3-4-5 triangle)");
    }

    @Test
    void testIsEnemy_SameOwner_False() throws Exception {
        var u1 = MockUnit.create("u1", "P1");
        var u2 = MockUnit.create("u2", "P1");

        var result = invokeIsEnemy(u1, u2);

        assertFalse(result, "Same owner should not be enemies");
    }

    @Test
    void testIsEnemy_DifferentOwner_True() throws Exception {
        var u1 = MockUnit.create("u1", "P1");
        var u2 = MockUnit.create("u2", "P2");

        var result = invokeIsEnemy(u1, u2);

        assertTrue(result, "Different owners should be enemies");
    }

    @Test
    void testIsEnemy_NullOwner_True() throws Exception {
        var u1 = MockUnit.create("u1", "P1");
        var u2 = MockUnit.create("u2", null);

        var result = invokeIsEnemy(u1, u2);

        assertTrue(result, "Null owner should be treated as enemy (monster)");
    }

    @Test
    void testSimulateTick_UnitAttacksInRange() {
        var p1 = new Player("P1", null);
        var p2 = new Player("P2", null);

        // Position units at y=0 so they're adjacent AFTER combat positioning
        var attacker = MockUnit.create("attacker", p1.getId())
                .withPosition(3, 0)
                .withAttackDamage(25)
                .withRange(1);
        var target = MockUnit.create("target", p2.getId()).withPosition(3, 0).withHealth(100, 100);

        addUnitToPlayer(p1, attacker);
        addUnitToPlayer(p2, target);

        combatSystem.startCombat(List.of(p1, p2));

        // After startCombat, units should be at y=3 and y=4 (adjacent)
        // The exact TOP/BOTTOM depends on ID sorting, but they'll be adjacent
        int attackerY = attacker.getY();
        int targetY = target.getY();
        int distance = Math.abs(attackerY - targetY);
        assertEquals(1, distance, "Units should be adjacent after combat positioning");

        // Run multiple ticks to ensure attack happens
        for (int i = 0; i < 20; i++) {
            combatSystem.simulateTick(List.of(p1, p2));
        }

        assertTrue(target.getCurrentHealth() < 100, "Target should have taken damage");
    }

    @Test
    void testSimulateTick_ManaGain_OnAttack() {
        var p1 = new Player("P1", null);
        var p2 = new Player("P2", null);

        var attacker = MockUnit.create("attacker", p1.getId())
                .withPosition(3, 0)
                .withMana(0, 100)
                .withRange(1);
        var target = MockUnit.create("target", p2.getId()).withPosition(3, 0).withHealth(100, 100);

        addUnitToPlayer(p1, attacker);
        addUnitToPlayer(p2, target);

        combatSystem.startCombat(List.of(p1, p2));

        // Run ticks
        for (int i = 0; i < 20; i++) {
            combatSystem.simulateTick(List.of(p1, p2));
        }

        assertTrue(attacker.getMana() > 0, "Attacker should have gained mana from attacking");
    }

    // Helper methods using reflection to access private methods for unit testing

    private GameUnit invokeFindNearestEnemy(GameUnit source, List<GameUnit> candidates) throws Exception {
        Method method = CombatSystem.class.getDeclaredMethod("findNearestEnemy", GameUnit.class, List.class);
        method.setAccessible(true);
        return (GameUnit) method.invoke(combatSystem, source, candidates);
    }

    private double invokeGetDistance(GameUnit u1, GameUnit u2) throws Exception {
        Method method = CombatSystem.class.getDeclaredMethod("getDistance", GameUnit.class, GameUnit.class);
        method.setAccessible(true);
        return (double) method.invoke(combatSystem, u1, u2);
    }

    private boolean invokeIsEnemy(GameUnit u1, GameUnit u2) throws Exception {
        Method method = CombatSystem.class.getDeclaredMethod("isEnemy", GameUnit.class, GameUnit.class);
        method.setAccessible(true);
        return (boolean) method.invoke(combatSystem, u1, u2);
    }

    private void addUnitToPlayer(Player player, GameUnit unit) {
        try {
            var field = Player.class.getDeclaredField("boardUnits");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<GameUnit> boardUnits = (List<GameUnit>) field.get(player);
            boardUnits.add(unit);
            unit.setOwnerId(player.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to add unit to player", e);
        }
    }
}
