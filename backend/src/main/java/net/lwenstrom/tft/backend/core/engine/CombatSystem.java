package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.lwenstrom.tft.backend.core.combat.AbilityCaster;
import net.lwenstrom.tft.backend.core.combat.CombatUtils;
import net.lwenstrom.tft.backend.core.combat.TargetSelector;
import net.lwenstrom.tft.backend.core.combat.UnitMover;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.time.Clock;

public class CombatSystem {

    private final TraitManager traitManager;
    private final Clock clock;
    private final TargetSelector targetSelector;
    private final UnitMover unitMover;
    private final AbilityCaster abilityCaster;

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

    public void startCombat(java.util.Collection<Player> players) {
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

        var snapshot = new ArrayList<>(allUnits);

        for (var unit : snapshot) {
            if (unit.getCurrentHealth() <= 0) {
                continue;
            }

            if (currentTime < unit.getNextAttackTime()) {
                continue;
            }

            unit.setActiveAbility(null);

            if (unit.getMaxMana() > 0 && unit.getMana() >= unit.getMaxMana()) {
                abilityCaster.castAbility(unit, allUnits, targetSelector);
                unit.setMana(0);
                unit.setNextAttackTime(currentTime + 1000);
                continue;
            }

            var target = targetSelector.findTarget(unit, allUnits);
            if (target != null) {
                var distance = CombatUtils.getDistance(unit, target);
                if (distance <= unit.getRange()) {
                    System.out.println(
                            unit.getName() + " attacks " + target.getName() + " for " + unit.getAttackDamage());
                    target.takeDamage(unit.getAttackDamage());
                    unit.gainMana(10);
                    float as = Math.max(0.1f, unit.getAttackSpeed());
                    long cooldownMs = (long) (1000 / as);
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

            return new CombatResult(true, winner != null ? winner.getId() : null);
        }

        return new CombatResult(false, null);
    }

    public record CombatResult(boolean ended, String winnerId) {}
}
