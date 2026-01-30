package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.ConditionalModifier;
import net.lwenstrom.tft.backend.core.model.ExecuteModifier;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.LifestealModifier;
import net.lwenstrom.tft.backend.core.model.ScalingModifier;
import org.springframework.stereotype.Component;

@Component
public class DefaultAbilityCaster implements AbilityCaster {

    @Override
    public void castAbility(GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector) {
        castAbility(source, allUnits, targetSelector, (id, name, tId, dmg) -> {});
    }

    @Override
    public void castAbility(
            GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector, DamageCallback callback) {
        AbilityDefinition ability = source.getAbility();
        if (ability == null) return;

        source.setActiveAbility(ability.name());

        var abilityType = ability.type();
        int value = ability.getValueForLevel(source.getStarLevel());

        switch (abilityType) {
            case DAMAGE -> castDamageAbility(source, allUnits, targetSelector, ability, value, callback);
            case STUN -> castStunAbility(source, allUnits, targetSelector, ability, value);
            case HEAL -> castHealAbility(source, allUnits, ability, value, callback);
            case BUFF_ATK -> castBuffAtkAbility(source, allUnits, ability, value);
            case BUFF_SPD -> castBuffSpdAbility(source, allUnits, ability, value);
        }
    }

    private void castDamageAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            TargetSelector targetSelector,
            AbilityDefinition ability,
            int damage,
            DamageCallback callback) {
        var target = targetSelector.findTarget(source, allUnits);
        if (target == null) return;

        // Check conditional modifiers before applying damage
        if (!checkConditionalModifiers(source, target, ability)) {
            return;
        }

        // Apply scaling modifiers
        int scaledDamage = applyScalingModifiers(source, target, ability, damage);

        // Apply execute modifier bonus damage
        int finalDamage = applyExecuteModifier(target, ability, scaledDamage);

        // Track total damage dealt for lifesteal
        var totalDamageDealt = new int[] {0};

        applyToTargets(source, allUnits, target, ability, u -> {
            u.takeDamage(finalDamage);
            totalDamageDealt[0] += finalDamage;
            callback.onDamage(source.getId(), source.getName(), u.getId(), finalDamage);
        });

        // Apply lifesteal modifier
        applyLifestealModifier(source, ability, totalDamageDealt[0], callback);
    }

    private void castStunAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            TargetSelector targetSelector,
            AbilityDefinition ability,
            int stunTicks) {
        var target = targetSelector.findTarget(source, allUnits);
        if (target == null) return;

        applyToTargets(source, allUnits, target, ability, u -> {
            u.setStunTicksRemaining(u.getStunTicksRemaining() + stunTicks);
        });
    }

    private void castHealAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            AbilityDefinition ability,
            int healAmount,
            DamageCallback callback) {
        // HEAL targets allies (including self)
        switch (ability.pattern()) {
            case "SINGLE" -> {
                // Heal lowest-health ally
                var target = findLowestHealthAlly(source, allUnits);
                if (target != null) {
                    healUnit(target, healAmount);
                    callback.onDamage(source.getId(), source.getName(), target.getId(), -healAmount); // Negative for
                    // heal
                }
            }
            case "SURROUND" -> {
                // Heal all allies in range
                int r = ability.range();
                allUnits.stream()
                        .filter(u -> u.getCurrentHealth() > 0)
                        .filter(u -> CombatUtils.isAlly(source, u))
                        .filter(u -> Math.abs(u.getX() - source.getX()) <= r && Math.abs(u.getY() - source.getY()) <= r)
                        .forEach(u -> {
                            healUnit(u, healAmount);
                            callback.onDamage(source.getId(), source.getName(), u.getId(), -healAmount);
                        });
            }
            default -> {
                // Default: heal self
                healUnit(source, healAmount);
                callback.onDamage(source.getId(), source.getName(), source.getId(), -healAmount);
            }
        }
    }

    private void castBuffAtkAbility(
            GameUnit source, List<GameUnit> allUnits, AbilityDefinition ability, int buffPercent) {
        // Buff all allies' ATK by value percent
        float multiplier = 1.0f + (buffPercent / 100.0f);
        allUnits.stream()
                .filter(u -> u.getCurrentHealth() > 0)
                .filter(u -> CombatUtils.isAlly(source, u))
                .forEach(u -> u.setAtkBuff(u.getAtkBuff() * multiplier));
    }

    private void castBuffSpdAbility(
            GameUnit source, List<GameUnit> allUnits, AbilityDefinition ability, int buffPercent) {
        // Buff all allies' attack speed by value percent
        float multiplier = 1.0f + (buffPercent / 100.0f);
        allUnits.stream()
                .filter(u -> u.getCurrentHealth() > 0)
                .filter(u -> CombatUtils.isAlly(source, u))
                .forEach(u -> u.setSpdBuff(u.getSpdBuff() * multiplier));
    }

    private void applyToTargets(
            GameUnit source,
            List<GameUnit> allUnits,
            GameUnit target,
            AbilityDefinition ability,
            java.util.function.Consumer<GameUnit> effect) {
        switch (ability.pattern()) {
            case "SINGLE" -> effect.accept(target);
            case "LINE" -> {
                int dx = Integer.compare(target.getX(), source.getX());
                int dy = Integer.compare(target.getY(), source.getY());
                for (int i = 1; i <= ability.range(); i++) {
                    int tx = source.getX() + dx * i;
                    int ty = source.getY() + dy * i;
                    final int fX = tx;
                    final int fY = ty;

                    allUnits.stream()
                            .filter(u -> u.getX() == fX && u.getY() == fY && u.getCurrentHealth() > 0)
                            .filter(u -> CombatUtils.isEnemy(source, u))
                            .forEach(effect);
                }
            }
            case "SURROUND" -> {
                int r = ability.range();
                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int tx = source.getX() + dx;
                        int ty = source.getY() + dy;
                        final int fX = tx;
                        final int fY = ty;

                        allUnits.stream()
                                .filter(u -> u.getX() == fX && u.getY() == fY && u.getCurrentHealth() > 0)
                                .filter(u -> CombatUtils.isEnemy(source, u))
                                .forEach(effect);
                    }
                }
            }
        }
    }

    private GameUnit findLowestHealthAlly(GameUnit source, List<GameUnit> allUnits) {
        return allUnits.stream()
                .filter(u -> u.getCurrentHealth() > 0)
                .filter(u -> CombatUtils.isAlly(source, u))
                .min((a, b) -> Float.compare(
                        (float) a.getCurrentHealth() / a.getMaxHealth(),
                        (float) b.getCurrentHealth() / b.getMaxHealth()))
                .orElse(null);
    }

    private void healUnit(GameUnit unit, int amount) {
        int newHealth = Math.min(unit.getMaxHealth(), unit.getCurrentHealth() + amount);
        unit.setCurrentHealth(newHealth);
    }

    // Check all conditional modifiers. Returns false if any condition is not met.
    private boolean checkConditionalModifiers(GameUnit source, GameUnit target, AbilityDefinition ability) {
        for (var modifier : ability.modifiers()) {
            if (modifier instanceof ConditionalModifier conditionalModifier) {
                if (!conditionalModifier.isMet(source, target)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Apply scaling modifiers to the base damage/heal value.
    private int applyScalingModifiers(GameUnit source, GameUnit target, AbilityDefinition ability, int baseValue) {
        var scaledValue = (float) baseValue;

        for (var modifier : ability.modifiers()) {
            if (modifier instanceof ScalingModifier scalingModifier) {
                var multiplier = scalingModifier.calculateMultiplier(source, target);
                scaledValue *= multiplier;
            }
        }

        return (int) scaledValue;
    }

    // Apply execute modifier bonus damage if target is below HP threshold.
    private int applyExecuteModifier(GameUnit target, AbilityDefinition ability, int baseDamage) {
        var totalDamage = baseDamage;

        for (var modifier : ability.modifiers()) {
            if (modifier instanceof ExecuteModifier executeModifier) {
                var bonusDamage = executeModifier.calculateBonusDamage(target, baseDamage);
                totalDamage += bonusDamage;
            }
        }

        return totalDamage;
    }

    // Apply lifesteal modifier healing to the caster.
    private void applyLifestealModifier(
            GameUnit source, AbilityDefinition ability, int damageDealt, DamageCallback callback) {
        for (var modifier : ability.modifiers()) {
            if (modifier instanceof LifestealModifier lifestealModifier) {
                var healAmount = lifestealModifier.calculateHealing(damageDealt);
                if (healAmount > 0) {
                    healUnit(source, healAmount);
                    // Report healing as negative damage
                    callback.onDamage(source.getId(), source.getName(), source.getId(), -healAmount);
                }
            }
        }
    }
}
