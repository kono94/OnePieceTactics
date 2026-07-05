package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.test.MockUnit;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraitImplementationTests {

    private CombatSystem combatSystem;

    @BeforeEach
    void setUp() {
        combatSystem = createTestCombatSystem();
    }

    @Test
    void testBeastPirates_LowHpDamageBonus() {
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());

        var unit = MockUnit.create("beast", p1.getId())
                .withName("Beast")
                .withPosition(0, 0)
                .withAttackDamage(100)
                .withRange(5);
        unit.setLowHpDamageBonus(0.5f);
        unit.setLowHpDamageThreshold(0.5f);
        unit.setCurrentHealth(40); // Below threshold
        unit.setMaxHealth(100);

        var target = MockUnit.create("target", p2.getId())
                .withName("Target")
                .withPosition(1, 0)
                .withHealth(1000, 1000);

        TestHelpers.addUnitToPlayer(p1, unit);
        TestHelpers.addUnitToPlayer(p2, target);

        combatSystem.simulateTick(List.of(p1, p2));

        // Damage should be 100 * (1.0 + 0.5) = 150
        assertEquals(850, p2.getBoardUnits().get(0).getCurrentHealth(), "Target should have taken 150 damage");
    }

    @Test
    void testSniper_DistanceDamage() {
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());

        var unit = MockUnit.create("sniper", p1.getId())
                .withName("Sniper")
                .withPosition(0, 0)
                .withAttackDamage(100)
                .withRange(10);
        unit.setDamagePerCell(0.1f);

        var target = MockUnit.create("target", p2.getId())
                .withName("Target")
                .withPosition(5, 0)
                .withHealth(1000, 1000);

        TestHelpers.addUnitToPlayer(p1, unit);
        TestHelpers.addUnitToPlayer(p2, target);

        combatSystem.simulateTick(List.of(p1, p2));

        // Distance is 5 cells. Bonus = 5 * 0.1 = 0.5. Total = 100 * 1.5 = 150
        assertEquals(850, p2.getBoardUnits().get(0).getCurrentHealth(), "Target should have taken 150 damage");
    }

    @Test
    void testBigMomPirates_Revive() {
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());

        var unit = MockUnit.create("bm", p1.getId())
                .withName("BigMom")
                .withPosition(0, 0)
                .withAttackDamage(100)
                .withRange(1);
        unit.setHasRevive(true);
        unit.setMaxHealth(100);
        unit.setCurrentHealth(10);

        var attacker = MockUnit.create("attacker", p2.getId())
                .withName("Attacker")
                .withPosition(1, 0)
                .withAttackDamage(50)
                .withRange(1)
                .withHealth(1000, 1000); // Give enough HP so it doesn't die instantly

        TestHelpers.addUnitToPlayer(p1, unit);
        TestHelpers.addUnitToPlayer(p2, attacker);

        // Run two ticks:
        // 1. BM attacks Attacker (BM goes first because p1 is first)
        // 2. Attacker attacks BM (BM revives)
        combatSystem.simulateTick(List.of(p1, p2));

        var bmInPlayer = p1.getBoardUnits().get(0);
        assertTrue(bmInPlayer.isReviveUsed(), "Revive should be triggered");
        assertEquals(40, bmInPlayer.getCurrentHealth(), "Should revive with 40% HP");
    }

    @Test
    void testMage_ManaGainMultiplier() {
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());

        var unit = MockUnit.create("mage", p1.getId())
                .withName("Mage")
                .withPosition(0, 0)
                .withMana(0, 100)
                .withRange(5);
        unit.setManaGainMultiplier(1.5f);

        var target = MockUnit.create("target", p2.getId())
                .withName("Target")
                .withPosition(1, 0)
                .withHealth(1000, 1000);
        target.setNextAttackTime(10_000);

        TestHelpers.addUnitToPlayer(p1, unit);
        TestHelpers.addUnitToPlayer(p2, target);

        combatSystem.simulateTick(List.of(p1, p2));

        // Base mana is 10. Bonus 1.5x -> 15
        assertEquals(15, p1.getBoardUnits().get(0).getMana(), "Mage should have gained 15 mana");
    }

    @Test
    void testBerserker_LowHpAsBonus() {
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());

        var unit = MockUnit.create("berserker", p1.getId())
                .withName("Berserker")
                .withPosition(0, 0)
                .withAttackSpeed(1.0f)
                .withRange(5);
        unit.setLowHpAsBonus(0.5f);
        unit.setLowHpAsThreshold(0.5f);
        unit.setCurrentHealth(40);
        unit.setMaxHealth(100);

        var target = MockUnit.create("target", p2.getId())
                .withName("Target")
                .withPosition(1, 0)
                .withHealth(100, 100);

        TestHelpers.addUnitToPlayer(p1, unit);
        TestHelpers.addUnitToPlayer(p2, target);

        long start = 0; // Use 0 to match TestClock
        unit.setNextAttackTime(start);

        combatSystem.simulateTick(List.of(p1, p2));

        // Multiplier is 0.5 bonus + 1.0 base = 1.5. AS = 1.0 * 1.5 = 1.5.
        // Cooldown = 1000 / 1.5 = 666ms.
        long expectedNext = start + 666;
        assertEquals(expectedNext, p1.getBoardUnits().get(0).getNextAttackTime(), 5);
    }
}
