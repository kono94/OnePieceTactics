package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lwenstrom.tft.backend.core.combat.AbilityCaster;
import net.lwenstrom.tft.backend.core.combat.CombatUtils;
import net.lwenstrom.tft.backend.core.combat.TargetSelector;
import net.lwenstrom.tft.backend.core.combat.UnitMover;
import net.lwenstrom.tft.backend.core.model.GameState;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.time.Clock;

public class CombatSystem {

    private final TraitManager traitManager;
    private final Clock clock;
    private final TargetSelector targetSelector;
    private final UnitMover unitMover;
    private final AbilityCaster abilityCaster;

    private Map<String, DamageEntry> damageLog = new HashMap<>();
    private List<GameState.CombatEvent> recentEvents = new ArrayList<>();

    public record DamageEntry(String unitName, String definitionId, String ownerId, int damage) {}

    public CombatSystem(
            TraitManager traitManager,
            Clock clock,
            TargetSelector targetSelector,
            UnitMover unitMover,
            AbilityCaster abilityCaster) {
        this.traitManager = traitManager;
        this.clock = clock;
        this.targetSelector = targetSelector;
        this.unitMover = unitMover;
        this.abilityCaster = abilityCaster;
    }

    private void accumulateDamage(String unitId, String unitName, String defId, String ownerId, int damage) {
        damageLog.compute(
                unitId,
                (k, v) -> v == null
                        ? new DamageEntry(unitName, defId, ownerId, damage)
                        : new DamageEntry(unitName, defId, ownerId, v.damage() + damage));
    }

    public Map<String, DamageEntry> getDamageLog() {
        return new HashMap<>(damageLog);
    }

    public void startCombat(java.util.Collection<Player> players) {
        damageLog.clear();
        recentEvents.clear();

        var sortedPlayers = new ArrayList<Player>(players);
        sortedPlayers.sort(Comparator.comparing(Player::getId));

        if (sortedPlayers.isEmpty()) {
            return;
        }

        for (var player : players) {
            for (var unit : player.getBoardUnits()) {
                unit.savePlanningPosition();
            }
            traitManager.applyTraits(player.getBoardUnits());
        }

        if (sortedPlayers.size() > 1) {
            var p1 = sortedPlayers.get(0);
            p1.setCombatSide("TOP");
            for (var unit : p1.getBoardUnits()) {
                int newX = unit.getX();
                int newY = (Grid.PLAYER_ROWS - 1) - unit.getY();
                unit.setPosition(newX, newY);
                System.out.println("CombatPos: " + unit.getName() + " (TOP) -> " + newX + "," + newY);
            }

            var p2 = sortedPlayers.get(1);
            p2.setCombatSide("BOTTOM");
            for (var u : p2.getBoardUnits()) {
                int newY = Grid.PLAYER_ROWS + u.getY();
                u.setPosition(u.getX(), newY);
                System.out.println("CombatPos: " + u.getName() + " (BOT) -> " + u.getX() + "," + newY);
            }
        } else {
            var p1 = sortedPlayers.get(0);
            p1.setCombatSide("BOTTOM");
            for (var unit : p1.getBoardUnits()) {
                int newY = Grid.PLAYER_ROWS + unit.getY();
                unit.setPosition(unit.getX(), newY);
            }
        }
    }

    public void endCombat(java.util.Collection<Player> players) {
        System.out.println("Restoring units for " + players.size() + " players.");
        for (var player : players) {
            player.setCombatSide(null);
            for (var unit : player.getBoardUnits()) {
                unit.restorePlanningPosition();
            }
        }
    }

    public CombatResult simulateTick(List<Player> participants) {
        var currentTime = clock.currentTimeMillis();
        var allUnits = new ArrayList<GameUnit>();
        participants.forEach(p -> allUnits.addAll(p.getBoardUnits()));
        recentEvents.clear();

        var snapshot = new ArrayList<>(allUnits);

        for (var unit : snapshot) {
            if (unit.getCurrentHealth() <= 0) {
                continue;
            }

            // Handle stunned units - skip their turn and decrement stun counter
            if (unit.getStunTicksRemaining() > 0) {
                unit.setStunTicksRemaining(unit.getStunTicksRemaining() - 1);
                continue;
            }

            if (currentTime < unit.getNextAttackTime()) {
                continue;
            }

            unit.setActiveAbility(null);

            if (unit.getMaxMana() > 0 && unit.getMana() >= unit.getMaxMana()) {
                abilityCaster.castAbility(unit, allUnits, targetSelector, (uId, uName, tId, dmg) -> {
                    accumulateDamage(uId, uName, unit.getDefinitionId(), unit.getOwnerId(), dmg);
                    recentEvents.add(new GameState.CombatEvent(currentTime, "SKILL", uId, tId, dmg));
                });
                unit.setMana(0);
                unit.setNextAttackTime(currentTime + 1000);
                continue;
            }

            var target = targetSelector.findTarget(unit, allUnits);
            if (target != null) {
                var distance = CombatUtils.getDistance(unit, target);
                if (distance <= unit.getRange()) {
                    // Apply ATK buff multiplier to damage
                    int baseDamage = unit.getAttackDamage();
                    int effectiveDamage = (int) (baseDamage * unit.getAtkBuff());
                    System.out.println(unit.getName() + " attacks " + target.getName() + " for " + effectiveDamage);
                    target.takeDamage(effectiveDamage);
                    accumulateDamage(
                            unit.getId(), unit.getName(), unit.getDefinitionId(), unit.getOwnerId(), effectiveDamage);
                    recentEvents.add(new GameState.CombatEvent(
                            currentTime, "DAMAGE", unit.getId(), target.getId(), effectiveDamage));
                    unit.gainMana(10);
                    // Apply SPD buff to attack cooldown
                    float as = Math.max(0.1f, unit.getAttackSpeed());
                    float effectiveAs = as * unit.getSpdBuff();
                    long cooldownMs = (long) (1000 / effectiveAs);
                    unit.setNextAttackTime(currentTime + cooldownMs);
                } else {
                    unitMover.moveTowards(unit, target, allUnits);
                }
            }
        }

        long playersWithUnits = participants.stream()
                .filter(p -> p.getBoardUnits().stream().anyMatch(u -> u.getCurrentHealth() > 0))
                .count();

        if (playersWithUnits <= 1) {
            Player winner = participants.stream()
                    .filter(p -> p.getBoardUnits().stream().anyMatch(u -> u.getCurrentHealth() > 0))
                    .findFirst()
                    .orElse(null);

            return new CombatResult(true, winner != null ? winner.getId() : null, getDamageLog(), List.of());
        }

        return new CombatResult(false, null, Map.of(), new ArrayList<>(recentEvents));
    }

    public record CombatResult(
            boolean ended, String winnerId, Map<String, DamageEntry> damageLog, List<GameState.CombatEvent> events) {}
}
