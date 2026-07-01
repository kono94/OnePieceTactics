package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.tft.backend.core.engine.AbstractGameUnit;
import net.lwenstrom.tft.backend.core.engine.AugmentManager;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.ConditionalModifier;
import net.lwenstrom.tft.backend.core.model.DotEffect;
import net.lwenstrom.tft.backend.core.model.DotModifier;
import net.lwenstrom.tft.backend.core.model.ExecuteModifier;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.KnockbackModifier;
import net.lwenstrom.tft.backend.core.model.LifestealModifier;
import net.lwenstrom.tft.backend.core.model.ScalingModifier;
import net.lwenstrom.tft.backend.core.model.StunModifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultAbilityCaster implements AbilityCaster {

    @Override
    public void castAbility(GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector) {
        castAbility(source, allUnits, targetSelector, new CombatStatCallback() {}, System.currentTimeMillis());
    }

    @Override
    public void castAbility(
            GameUnit source, List<GameUnit> allUnits, TargetSelector targetSelector, CombatStatCallback callback) {
        castAbility(source, allUnits, targetSelector, callback, System.currentTimeMillis());
    }

    @Override
    public void castAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            TargetSelector targetSelector,
            CombatStatCallback callback,
            long currentTime) {
        AbilityDefinition ability = source.getAbility();
        if (ability == null) {
            return;
        }

        source.setActiveAbility(ability.name());

        var abilityType = ability.type();
        int value = ability.getValueForLevel(source.getStarLevel());

        switch (abilityType) {
            case DAMAGE -> castDamageAbility(source, allUnits, targetSelector, ability, value, callback, currentTime);
            case STUN -> castStunAbility(source, allUnits, targetSelector, ability, value, callback);
            case HEAL -> castHealAbility(source, allUnits, ability, value, callback);
            case BUFF_ATK -> castBuffAtkAbility(source, allUnits, ability, value, callback);
            case BUFF_SPD -> castBuffSpdAbility(source, allUnits, ability, value, callback);
            case SHIELD -> castShieldAbility(source, allUnits, ability, value, callback);
        }
    }

    private void castDamageAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            TargetSelector targetSelector,
            AbilityDefinition ability,
            int damage,
            CombatStatCallback callback,
            long currentTime) {
        var target = targetSelector.findTarget(source, allUnits);
        if (target == null) {
            return;
        }

        // Check conditional modifiers before applying damage
        if (!checkConditionalModifiers(source, target, ability)) {
            return;
        }

        // Apply scaling modifiers
        int scaledDamage = applyScalingModifiers(source, target, ability, damage);

        // Apply Warlord ability damage multiplier
        scaledDamage = (int) (scaledDamage * source.getAbilityDamageMultiplier());

        // Apply execute modifier bonus damage
        int finalDamage = applyExecuteModifier(source, target, ability, scaledDamage);

        // Track total damage dealt for lifesteal
        var totalDamageDealt = new int[] {0};

        applyToTargets(source, allUnits, target, ability, u -> {
            int effectiveDamage = PokemonTypeEffectiveness.apply(source, u, finalDamage);
            u.takeDamage(effectiveDamage);
            if (isFinalKill(u)) {
                AugmentManager.applyTeamAttackDamageOnKill(source, allUnits);
            }
            // Apply secondary effects from modifiers (stun, knockback)
            applyStunAndKnockbackModifiers(source, u, ability);
            applyDotModifiers(source, u, ability, currentTime);
            totalDamageDealt[0] += effectiveDamage;
            callback.onDamage(source.getId(), source.getName(), u.getId(), effectiveDamage);
        });

        // Apply lifesteal modifier
        applyLifestealModifier(source, ability, totalDamageDealt[0], callback);
    }

    private boolean isFinalKill(GameUnit target) {
        return target.getCurrentHealth() <= 0 && (!target.hasRevive() || target.isReviveUsed());
    }

    private void castStunAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            TargetSelector targetSelector,
            AbilityDefinition ability,
            int stunSeconds,
            CombatStatCallback callback) {
        var target = targetSelector.findTarget(source, allUnits);
        if (target == null) {
            return;
        }

        applyToTargets(source, allUnits, target, ability, u -> {
            u.setStunSecondsRemaining(u.getStunSecondsRemaining() + stunSeconds);
            callback.onSkill(source.getId(), source.getName(), u.getId(), 0);
        });
    }

    private void castHealAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            AbilityDefinition ability,
            int healAmount,
            CombatStatCallback callback) {
        // Apply Doctor heal amplification
        int amplifiedHeal = (int) (healAmount * source.getHealAmplification());
        final int finalHeal = amplifiedHeal;

        // HEAL targets allies board-wide (including self)
        switch (ability.pattern()) {
            case SINGLE -> {
                // Heal lowest-health ally on board
                GameUnit target = findLowestHealthAlly(allUnits, source);
                if (target != null) {
                    var effectiveHeal = healUnit(target, finalHeal);
                    if (effectiveHeal > 0) {
                        callback.onHealing(source.getId(), source.getName(), target.getId(), effectiveHeal);
                    }
                }
            }
            case SURROUND, LINE -> {
                // Heal all allies on board
                allUnits.stream()
                        .filter(u -> u.getCurrentHealth() > 0)
                        .filter(u -> CombatUtils.isAlly(source, u))
                        .forEach(u -> {
                            var effectiveHeal = healUnit(u, finalHeal);
                            if (effectiveHeal > 0) {
                                callback.onHealing(source.getId(), source.getName(), u.getId(), effectiveHeal);
                            }
                        });
            }
            default -> {
                // Default: heal self
                var effectiveHeal = healUnit(source, finalHeal);
                if (effectiveHeal > 0) {
                    callback.onHealing(source.getId(), source.getName(), source.getId(), effectiveHeal);
                }
            }
        }
    }

    private void castBuffAtkAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            AbilityDefinition ability,
            int buffPercent,
            CombatStatCallback callback) {
        // Buff all allies' ATK board-wide
        float multiplier = 1.0f + (buffPercent / 100.0f);
        allUnits.stream()
                .filter(u -> u.getCurrentHealth() > 0)
                .filter(u -> CombatUtils.isAlly(source, u))
                .forEach(u -> {
                    u.setAtkBuff(u.getAtkBuff() * multiplier);
                    callback.onSkill(source.getId(), source.getName(), u.getId(), 0);
                });

        // Musician check
        if (source.getAsOnCast() > 0) {
            float musAs = source.getAsOnCast();
            int duration = source.getAsOnCastDuration();
            long now = System.currentTimeMillis();
            allUnits.stream()
                    .filter(u -> u.getCurrentHealth() > 0)
                    .filter(u -> CombatUtils.isAlly(source, u))
                    .forEach(u -> {
                        if (u instanceof AbstractGameUnit agu) {
                            agu.applyTemporaryAsBuff(musAs, duration, now);
                        }
                    });
            log.info("Musician {} buffs allies with +{} AS for {}s", source.getName(), musAs, duration);
        }
    }

    private void castBuffSpdAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            AbilityDefinition ability,
            int buffPercent,
            CombatStatCallback callback) {
        // Buff all allies' attack speed board-wide
        float multiplier = 1.0f + (buffPercent / 100.0f);
        allUnits.stream()
                .filter(u -> u.getCurrentHealth() > 0)
                .filter(u -> CombatUtils.isAlly(source, u))
                .forEach(u -> {
                    u.setSpdBuff(u.getSpdBuff() * multiplier);
                    callback.onSkill(source.getId(), source.getName(), u.getId(), 0);
                });
    }

    private void castShieldAbility(
            GameUnit source,
            List<GameUnit> allUnits,
            AbilityDefinition ability,
            int shieldAmount,
            CombatStatCallback callback) {
        switch (ability.pattern()) {
            case SINGLE -> {
                source.setShield(source.getShield() + shieldAmount);
                callback.onShielding(source.getId(), source.getName(), source.getId(), shieldAmount);
            }
            case SURROUND, LINE ->
                allUnits.stream()
                        .filter(u -> u.getCurrentHealth() > 0)
                        .filter(u -> CombatUtils.isAlly(source, u))
                        .forEach(u -> {
                            u.setShield(u.getShield() + shieldAmount);
                            callback.onShielding(source.getId(), source.getName(), u.getId(), shieldAmount);
                        });
        }
    }

    private void applyToTargets(
            GameUnit source,
            List<GameUnit> allUnits,
            GameUnit target,
            AbilityDefinition ability,
            java.util.function.Consumer<GameUnit> effect) {
        int starLevel = source.getStarLevel();
        int range = ability.getRangeForLevel(starLevel);

        switch (ability.pattern()) {
            case SINGLE -> effect.accept(target);
            case LINE -> {
                int dx = Integer.compare(target.getX(), source.getX());
                int dy = Integer.compare(target.getY(), source.getY());
                for (int i = 1; i <= range; i++) {
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
            case SURROUND -> {
                int r = range;
                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }
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

    private GameUnit findLowestHealthAlly(List<GameUnit> allUnits, GameUnit source) {
        return allUnits.stream()
                .filter(u -> u.getCurrentHealth() > 0)
                .filter(u -> CombatUtils.isAlly(source, u))
                .min((a, b) -> Float.compare(
                        (float) a.getCurrentHealth() / a.getMaxHealth(),
                        (float) b.getCurrentHealth() / b.getMaxHealth()))
                .orElse(null);
    }

    private int healUnit(GameUnit unit, int amount) {
        int previousHealth = unit.getCurrentHealth();
        int newHealth = Math.min(unit.getMaxHealth(), unit.getCurrentHealth() + amount);
        unit.setCurrentHealth(newHealth);
        return newHealth - previousHealth;
    }

    // Check all conditional modifiers. Returns false if any condition is not met.
    private boolean checkConditionalModifiers(GameUnit source, GameUnit target, AbilityDefinition ability) {
        for (var modifier : ability.modifiers()) {
            if (modifier instanceof ConditionalModifier conditionalModifier) {
                if (!conditionalModifier.isMet(source, target, source.getStarLevel())) {
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
                var multiplier = scalingModifier.calculateMultiplier(source, target, source.getStarLevel());
                scaledValue *= multiplier;
            }
        }

        return (int) scaledValue;
    }

    // Apply execute modifier bonus damage if target is below HP threshold.
    private int applyExecuteModifier(GameUnit source, GameUnit target, AbilityDefinition ability, int baseDamage) {
        var totalDamage = baseDamage;

        for (var modifier : ability.modifiers()) {
            if (modifier instanceof ExecuteModifier executeModifier) {
                var bonusDamage = executeModifier.calculateBonusDamage(target, baseDamage, source.getStarLevel());
                totalDamage += bonusDamage;
            }
        }

        return totalDamage;
    }

    // Apply lifesteal modifier healing to the caster.
    private void applyLifestealModifier(
            GameUnit source, AbilityDefinition ability, int damageDealt, CombatStatCallback callback) {
        for (var modifier : ability.modifiers()) {
            if (modifier instanceof LifestealModifier lifestealModifier) {
                var healAmount = lifestealModifier.calculateHealing(damageDealt, source.getStarLevel());
                if (healAmount > 0) {
                    var effectiveHeal = healUnit(source, healAmount);
                    if (effectiveHeal > 0) {
                        callback.onHealing(source.getId(), source.getName(), source.getId(), effectiveHeal);
                    }
                }
            }
        }
    }

    private void applyStunAndKnockbackModifiers(GameUnit source, GameUnit target, AbilityDefinition ability) {
        if (target == null) return;
        int starLevel = source.getStarLevel();

        for (var modifier : ability.modifiers()) {
            if (modifier instanceof StunModifier stunModifier) {
                int seconds = stunModifier.getStunSeconds(starLevel);
                target.setStunSecondsRemaining(target.getStunSecondsRemaining() + seconds);
            } else if (modifier instanceof KnockbackModifier knockbackModifier) {
                int cells = knockbackModifier.getCells(starLevel);
                applyKnockback(source, target, cells);
            }
        }
    }

    private void applyDotModifiers(GameUnit source, GameUnit target, AbilityDefinition ability, long currentTime) {
        if (target == null) return;
        int starLevel = source.getStarLevel();

        for (var modifier : ability.modifiers()) {
            if (modifier instanceof DotModifier dotModifier) {
                int damagePerTick = dotModifier.getDamagePerTick(starLevel);
                int durationSeconds = dotModifier.getDurationSeconds(starLevel);
                int tickIntervalMs = dotModifier.getTickIntervalMs(starLevel);
                if (damagePerTick <= 0 || durationSeconds <= 0 || tickIntervalMs <= 0) {
                    continue;
                }
                target.addDotEffect(new DotEffect(
                        source.getId(),
                        source.getName(),
                        source.getDefinitionId(),
                        source.getOwnerId(),
                        damagePerTick,
                        currentTime + tickIntervalMs,
                        currentTime + durationSeconds * 1000L,
                        tickIntervalMs,
                        dotModifier.dotType().name()));
            }
        }
    }

    private void applyKnockback(GameUnit source, GameUnit target, int cells) {
        if (target == null || cells <= 0) return;
        int dx = Integer.compare(target.getX(), source.getX());
        int dy = Integer.compare(target.getY(), source.getY());

        int newX = target.getX() + dx * cells;
        int newY = target.getY() + dy * cells;

        // Clamp to board boundaries
        newX = Math.max(0, Math.min(6, newX));
        newY = Math.max(0, Math.min(7, newY));

        target.setPosition(newX, newY);
    }
}
