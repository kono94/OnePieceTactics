package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.AbilityPattern;
import net.lwenstrom.tft.backend.core.model.AbilityType;
import net.lwenstrom.tft.backend.core.model.DotModifier;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class ShieldAndDotCombatTest {
    @Test
    void shieldAbilityGrantsShieldAndAbsorbsDamage() {
        var ability = new AbilityDefinition(
                "Iron Defense",
                "Gain shield",
                AbilityType.SHIELD,
                AbilityPattern.SINGLE,
                List.of(1, 1, 1),
                List.of(120, 240, 480),
                List.of());
        var shieldUnitDef = TestHelpers.createUnitDefWithAbility("shield", "Shieldmon", 1, 500, 10, ability);
        var enemyDef = TestHelpers.createUnitDef("enemy", "Enemy", 1, 500, 40);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(shieldUnitDef, enemyDef));
        var p1 = createTestPlayer("P1", dataLoader);
        var p2 = createTestPlayer("P2", dataLoader);
        p1.setLevel(2);
        p2.setLevel(2);
        p1.addUnitToBoard(shieldUnitDef, 0, 0);
        p2.addUnitToBoard(enemyDef, 0, 0);
        var shieldUnit = p1.getBoardUnits().getFirst();
        shieldUnit.setMana(10);
        p2.getBoardUnits().getFirst().setNextAttackTime(10_000);

        var clock = createTestClock();
        var combatSystem = TestHelpers.createTestCombatSystem(clock);
        combatSystem.startCombat(List.of(p1, p2));
        combatSystem.simulateTick(List.of(p1, p2));

        assertEquals(120, shieldUnit.getShield());
        shieldUnit.takeDamage(80);
        assertEquals(40, shieldUnit.getShield());
        assertEquals(500, shieldUnit.getCurrentHealth());
    }

    @Test
    void dotModifierTicksWithCasterAttribution() {
        var dot = new DotModifier(
                DotModifier.DotType.BURN, List.of(25, 50, 100), List.of(3, 3, 3), List.of(1000, 1000, 1000));
        var ability = new AbilityDefinition(
                "Ember",
                "Burn target",
                AbilityType.DAMAGE,
                AbilityPattern.SINGLE,
                List.of(3, 3, 3),
                List.of(40, 90, 200),
                List.of(dot));
        var casterDef = TestHelpers.createUnitDefWithAbility("caster", "Caster", 1, 500, 10, ability);
        var targetDef = TestHelpers.createUnitDef("target", "Target", 1, 500, 5);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(casterDef, targetDef));
        var p1 = createTestPlayer("P1", dataLoader);
        var p2 = createTestPlayer("P2", dataLoader);
        p1.setLevel(2);
        p2.setLevel(2);
        p1.addUnitToBoard(casterDef, 0, 0);
        p2.addUnitToBoard(targetDef, 0, 0);
        GameUnit caster = p1.getBoardUnits().getFirst();
        GameUnit target = p2.getBoardUnits().getFirst();
        caster.setMana(10);

        var clock = createTestClock();
        var combatSystem = TestHelpers.createTestCombatSystem(clock);
        combatSystem.startCombat(List.of(p1, p2));
        combatSystem.simulateTick(List.of(p1, p2));
        assertEquals(460, target.getCurrentHealth());
        caster.setNextAttackTime(10_000);

        clock.advance(1000);
        combatSystem.simulateTick(List.of(p1, p2));

        assertEquals(435, target.getCurrentHealth());
        var damageEntry = combatSystem.getDamageLog().get(caster.getId());
        assertNotNull(damageEntry);
        assertEquals("caster", damageEntry.definitionId());
        assertTrue(damageEntry.damage() >= 65);
    }
}
