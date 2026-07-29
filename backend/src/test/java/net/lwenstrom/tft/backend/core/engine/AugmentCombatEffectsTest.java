package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.lwenstrom.tft.backend.core.combat.DefaultAbilityCaster;
import net.lwenstrom.tft.backend.core.combat.NearestEnemyTargetSelector;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.AbilityPattern;
import net.lwenstrom.tft.backend.core.model.AbilityType;
import net.lwenstrom.tft.backend.core.model.AugmentEffectType;
import net.lwenstrom.tft.backend.core.model.AugmentTier;
import net.lwenstrom.tft.backend.core.model.SelectedAugment;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class AugmentCombatEffectsTest {

    @Test
    void combatOnlyEffectsResetAfterCombatEnds() {
        var player = TestHelpers.createTestPlayer("Player");
        var rangedDef = new UnitDefinition(
                "ranged",
                "Ranged",
                1,
                net.lwenstrom.tft.backend.core.model.UnitRole.DAMAGE,
                List.of(100, 100, 100),
                List.of(100, 100, 100),
                List.of(10, 10, 10),
                List.of(0, 0, 0),
                List.of(0, 0, 0),
                List.of(1.0f, 1.0f, 1.0f),
                List.of(3, 3, 3),
                List.of(),
                null);
        var meleeDef = TestHelpers.createUnitDef("melee", "Melee", 1, 100, 10);
        player.setLevel(2);
        player.addUnitToBoard(rangedDef, 0, 0);
        player.addUnitToBoard(meleeDef, 1, 0);
        player.addSelectedAugment(selected("ranged-tempo", AugmentEffectType.TEAM_ATTACK_SPEED_PER_RANGED_UNIT, 5));
        player.addSelectedAugment(selected("guarded-formation", AugmentEffectType.TEAM_DAMAGE_REDUCTION, 10));
        player.addSelectedAugment(selected("snowball-strike", AugmentEffectType.TEAM_ATTACK_DAMAGE_ON_KILL, 4));

        var combatSystem = TestHelpers.createTestCombatSystem();
        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());

        combatSystem.startCombat(List.of(player));
        manager.applyCombatEffects(List.of(player));

        var ranged = player.getBoardUnits().get(0);
        assertTrue(ranged.getAttackSpeed() > 1.0f);
        assertEquals(10, ranged.getDamageReduction());
        assertEquals(4, ranged.getTeamAttackDamageOnKill());

        combatSystem.endCombat(List.of(player));

        assertEquals(1.0f, ranged.getAttackSpeed());
        assertEquals(0, ranged.getDamageReduction());
        assertEquals(0, ranged.getTeamAttackDamageOnKill());
    }

    @Test
    void directAttackKillStacksTeamAttackDamage() {
        var attacker = TestHelpers.createTestPlayer("Attacker");
        var defender = TestHelpers.createTestPlayer("Defender");
        attacker.setLevel(2);
        defender.setLevel(1);
        attacker.addUnitToBoard(TestHelpers.createUnitDef("carry", "Carry", 1, 100, 20), 0, 0);
        attacker.addUnitToBoard(TestHelpers.createUnitDef("ally", "Ally", 1, 100, 10), 1, 0);
        defender.addUnitToBoard(TestHelpers.createUnitDef("target", "Target", 1, 1, 1), 0, 0);
        attacker.addSelectedAugment(selected("snowball-strike", AugmentEffectType.TEAM_ATTACK_DAMAGE_ON_KILL, 4));

        var clock = TestHelpers.createTestClock();
        var combatSystem = TestHelpers.createTestCombatSystem(clock);
        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());

        combatSystem.startCombat(List.of(attacker, defender));
        manager.applyCombatEffects(List.of(attacker, defender));
        combatSystem.simulateTick(List.of(attacker, defender));

        assertEquals(24, attacker.getBoardUnits().get(0).getAttackDamage());
        assertEquals(14, attacker.getBoardUnits().get(1).getAttackDamage());
    }

    @Test
    void abilityPowerAugmentIncreasesDamagingAbilityOutput() {
        var attacker = TestHelpers.createTestPlayer("Attacker");
        var defender = TestHelpers.createTestPlayer("Defender");
        attacker.addUnitToBoard(createDamageCaster(), 0, 0);
        defender.addUnitToBoard(TestHelpers.createUnitDef("target", "Target", 1, 200, 1), 0, 0);
        attacker.addSelectedAugment(selected("focused-haki", AugmentEffectType.TEAM_ABILITY_POWER, 20));

        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());
        manager.applyCombatEffects(List.of(attacker));

        var caster = new DefaultAbilityCaster();
        caster.castAbility(
                attacker.getBoardUnits().get(0),
                List.of(
                        attacker.getBoardUnits().get(0),
                        defender.getBoardUnits().get(0)),
                new NearestEnemyTargetSelector());

        assertEquals(80, defender.getBoardUnits().get(0).getCurrentHealth());
    }

    @Test
    void defenseAugmentMitigatesAbilityAndBasicDamage() {
        var player = TestHelpers.createTestPlayer("Player");
        player.addUnitToBoard(TestHelpers.createUnitDef("unit", "Unit", 1, 200, 10), 0, 0);
        player.addSelectedAugment(selected("iron-line", AugmentEffectType.TEAM_DEFENSE, 16));
        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());

        manager.applyCombatEffects(List.of(player));
        var unit = player.getBoardUnits().getFirst();
        assertEquals(16, unit.getDefense());
        unit.takeAbilityDamage(100);
        assertEquals(114, unit.getCurrentHealth());

        unit.setCurrentHealth(200);
        unit.takeDamage(100);
        assertEquals(114, unit.getCurrentHealth());
    }

    @Test
    void startingShieldAugmentUsesEffectiveShieldCap() {
        var player = TestHelpers.createTestPlayer("Player");
        player.addUnitToBoard(TestHelpers.createUnitDef("unit", "Unit", 1, 100, 10), 0, 0);
        player.addSelectedAugment(selected("first-guard", AugmentEffectType.TEAM_STARTING_SHIELD, 125));
        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());

        manager.applyCombatEffects(List.of(player));

        assertEquals(50, player.getBoardUnits().getFirst().getShield());
    }

    private UnitDefinition createDamageCaster() {
        var ability = new AbilityDefinition(
                "Strike",
                "Deal damage.",
                AbilityType.DAMAGE,
                AbilityPattern.SINGLE,
                List.of(1, 1, 1),
                List.of(100, 100, 100),
                List.of());
        return new UnitDefinition(
                "caster",
                "Caster",
                1,
                net.lwenstrom.tft.backend.core.model.UnitRole.DAMAGE,
                List.of(100, 100, 100),
                List.of(100, 100, 100),
                List.of(10, 10, 10),
                List.of(0, 0, 0),
                List.of(0, 0, 0),
                List.of(1.0f, 1.0f, 1.0f),
                List.of(1, 1, 1),
                List.of(),
                ability);
    }

    private SelectedAugment selected(String id, AugmentEffectType effectType, int value) {
        return new SelectedAugment(id, id, id, AugmentTier.SILVER, effectType, value, 3, null);
    }
}
