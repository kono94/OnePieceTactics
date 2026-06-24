package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import net.lwenstrom.tft.backend.core.combat.BfsUnitMover;
import net.lwenstrom.tft.backend.core.combat.DefaultAbilityCaster;
import net.lwenstrom.tft.backend.core.combat.NearestEnemyTargetSelector;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.AbilityPattern;
import net.lwenstrom.tft.backend.core.model.AbilityType;
import net.lwenstrom.tft.backend.core.model.DotModifier;
import net.lwenstrom.tft.backend.core.model.EffectType;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.TraitTargetScope;
import net.lwenstrom.tft.backend.test.MockUnit;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ShieldAndDotCombatTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

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

    @Test
    void onHitDotRefreshesInsteadOfStackingForSameSourceAndTarget() throws Exception {
        var traitManager = new TraitManager();
        traitManager.registerEffect(
                "poison",
                new GenericTraitApplier("poison", EffectType.ON_HIT_DOT, TraitTargetScope.TEAM, effects("""
                                [{"minUnits":1,"values":{"damageRatio":0.10,"durationMs":2000,"tickIntervalMs":1000}}]
                                """)));

        var clock = createTestClock();
        var combatSystem = new CombatSystem(
                traitManager,
                clock,
                new NearestEnemyTargetSelector(),
                new BfsUnitMover(clock),
                new DefaultAbilityCaster());
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var p2 = new Player("P2", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var attacker = MockUnit.create("attacker", p1.getId())
                .withTraits(Set.of("Poison"))
                .withAttackDamage(100)
                .withRange(10);
        var target =
                MockUnit.create("target", p2.getId()).withHealth(1000, 1000).withRange(1);
        target.setNextAttackTime(10_000);
        TestHelpers.addUnitToPlayer(p1, attacker);
        TestHelpers.addUnitToPlayer(p2, target);

        combatSystem.startCombat(List.of(p1, p2));
        combatSystem.simulateTick(List.of(p1, p2));

        assertEquals(1, target.getDotEffects().size());
        var firstExpiresAt = target.getDotEffects().getFirst().expiresAt();

        attacker.setNextAttackTime(0);
        clock.advance(500);
        combatSystem.simulateTick(List.of(p1, p2));

        assertEquals(1, target.getDotEffects().size());
        assertTrue(target.getDotEffects().getFirst().expiresAt() > firstExpiresAt);
    }

    @Test
    void traitStatBonusesDoNotStackAcrossRepeatedCombats() throws Exception {
        var traitManager = new TraitManager();
        traitManager.registerEffect(
                "ground",
                new GenericTraitApplier("ground", EffectType.ARMOR_AND_MR, TraitTargetScope.TEAM, effects("""
                                [{"minUnits":1,"values":{"armor":18,"mr":18}}]
                                """)));

        var clock = createTestClock();
        var combatSystem = new CombatSystem(
                traitManager,
                clock,
                new NearestEnemyTargetSelector(),
                new BfsUnitMover(clock),
                new DefaultAbilityCaster());
        var p1 = new Player("P1", GameMode.ONEPIECE, createMockDataLoader(), createSeededRandomProvider());
        var ground = MockUnit.create("ground", p1.getId()).withTraits(Set.of("Ground"));
        var normal = MockUnit.create("normal", p1.getId()).withTraits(Set.of("Normal"));
        TestHelpers.addUnitToPlayer(p1, ground);
        TestHelpers.addUnitToPlayer(p1, normal);

        combatSystem.startCombat(List.of(p1));
        assertEquals(18, ground.getArmor());
        assertEquals(18, normal.getArmor());
        combatSystem.endCombat(List.of(p1));
        assertEquals(0, ground.getArmor());
        assertEquals(0, normal.getArmor());

        combatSystem.startCombat(List.of(p1));
        assertEquals(18, ground.getArmor());
        assertEquals(18, normal.getArmor());
    }

    private List<JsonNode> effects(String json) throws Exception {
        var result = new java.util.ArrayList<JsonNode>();
        var array = jsonMapper.readTree(json);
        for (var effect : array) {
            result.add(effect);
        }
        return result;
    }
}
