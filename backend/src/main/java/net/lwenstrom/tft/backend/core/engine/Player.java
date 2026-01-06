package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.model.GameUnit;

@Getter
@Setter
public class Player {
    private final String id;
    private String name;

    private int health = 100;
    private int gold = 10; // Starting gold
    private int level = 1;
    private int xp = 0;

    // Limits
    private static final int MAX_BENCH_SIZE = 9;

    private final List<GameUnit> bench = new ArrayList<>();
    // The board is managed by the shared Grid?, or each player keeps track of their
    // units on the board?
    // Usually Player has a list of units "on board", mapped to Grid coords.
    private final List<GameUnit> boardUnits = new ArrayList<>();

    private List<UnitDefinition> shop = new ArrayList<>();
    private boolean shopLocked = false;

    private final DataLoader dataLoader;

    public Player(String name, DataLoader dataLoader) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dataLoader = dataLoader;
    }

    public void refreshShop() {
        if (shopLocked) return;
        if (gold < 2) return; // Cost 2 to reroll
        gold -= 2;

        // Simple random roll logic (weighted by level later)
        List<UnitDefinition> allUnits = new ArrayList<>(dataLoader.getAllUnits());
        Collections.shuffle(allUnits);
        shop = allUnits.stream().limit(5).collect(Collectors.toList());
    }

    public void buyUnit(int shopIndex) {
        if (shopIndex < 0 || shopIndex >= shop.size()) return;
        UnitDefinition def = shop.get(shopIndex);

        if (def == null) return; // Already bought
        if (gold < def.cost()) return;
        if (bench.size() >= MAX_BENCH_SIZE) return;

        gold -= def.cost();
        StandardGameUnit newUnit = new StandardGameUnit(def);
        newUnit.setOwnerId(this.id);
        bench.add(newUnit);
        shop.set(shopIndex, null); // Remove from shop
        checkUpgrade(def.name(), 1);
    }

    private void checkUpgrade(String unitName, int starLevel) {
        List<GameUnit> candidates = new ArrayList<>();
        candidates.addAll(bench.stream()
                .filter(u -> u.getName().equals(unitName) && u.getStarLevel() == starLevel)
                .toList());
        candidates.addAll(boardUnits.stream()
                .filter(u -> u.getName().equals(unitName) && u.getStarLevel() == starLevel)
                .toList());

        if (candidates.size() >= 3) {
            List<GameUnit> toRemove = candidates.subList(0, 3);
            GameUnit targetPosUnit = toRemove.stream()
                    .filter(u -> boardUnits.contains(u))
                    .findFirst()
                    .orElse(toRemove.get(0));

            int x = targetPosUnit.getX();
            int y = targetPosUnit.getY();

            bench.removeAll(toRemove);
            boardUnits.removeAll(toRemove);

            UnitDefinition def = dataLoader.getAllUnits().stream()
                    .filter(d -> d.name().equals(unitName))
                    .findFirst()
                    .orElse(null);

            if (def != null) {
                // Scale stats
                double scale = 1.8;
                UnitDefinition upgradedDef = new UnitDefinition(
                        def.id(),
                        def.name(),
                        def.cost(),
                        (int) (def.maxHealth() * scale),
                        def.maxMana(),
                        (int) (def.attackDamage() * scale),
                        def.abilityPower(),
                        def.armor(),
                        def.magicResist(),
                        def.attackSpeed(),
                        def.range(),
                        def.traits());

                StandardGameUnit upgraded = new StandardGameUnit(upgradedDef);
                upgraded.setOwnerId(this.id);
                upgraded.setStarLevel(starLevel + 1);
                upgraded.setPosition(x, y);

                if (y < 0) {
                    bench.add(upgraded);
                } else {
                    boardUnits.add(upgraded);
                }

                checkUpgrade(unitName, starLevel + 1);
            }
        }
    }

    public void gainGold(int amount) {
        this.gold += amount;
    }

    public void gainXp(int amount) {
        this.xp += amount;
    }

    public void moveUnit(String unitId, int x, int y) {
        // Search bench
        var benchUnit =
                bench.stream().filter(u -> u.getId().equals(unitId)).findFirst().orElse(null);
        if (benchUnit != null) {
            // Moving from Bench
            if (y >= 0) { // To Board
                // Check board limit (standard TFT is level based, for now unlimited or max 10)
                if (boardUnits.size() >= level + 10) return; // Simplified limit

                bench.remove(benchUnit);
                benchUnit.setPosition(x, y);
                boardUnits.add(benchUnit);
            } else {
                // To Bench (Reorder - ignored for now)
            }
        } else {
            // Search board
            var boardUnit = boardUnits.stream()
                    .filter(u -> u.getId().equals(unitId))
                    .findFirst()
                    .orElse(null);
            if (boardUnit != null) {
                // Moving from Board
                if (y < 0) { // To Bench
                    if (bench.size() >= MAX_BENCH_SIZE) return;
                    boardUnits.remove(boardUnit);
                    bench.add(boardUnit);
                } else { // To Board (Move)
                    boardUnit.setPosition(x, y);
                }
            }
        }
    }
}
