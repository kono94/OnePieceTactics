package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.model.GameState.PlayerState;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.random.RandomProvider;

@Getter
@Setter
public class Player {
    private final String id;
    private String name;
    private Integer place; // Null if still playing, 1-8 if finished
    private String combatSide; // "TOP" or "BOTTOM" during combat, null otherwise

    private int health = 100;
    private int gold = 10; // Starting gold
    private int level = 1;
    private int xp = 0;

    // Limits
    private static final int MAX_BENCH_SIZE = 9;

    private final RandomProvider randomProvider;
    private final Grid grid = new Grid();

    private final List<GameUnit> bench = new ArrayList<>();
    private final List<GameUnit> boardUnits = new ArrayList<>();

    private List<UnitDefinition> shop = new ArrayList<>();
    private boolean shopLocked = false;
    private boolean boardLocked = false;
    private boolean inCombat = false;
    private final List<PendingUpgrade> pendingUpgrades = new ArrayList<>();

    private record PendingUpgrade(String unitName, int starLevel) {}

    private final DataLoader dataLoader;

    public Player(String name, DataLoader dataLoader, RandomProvider randomProvider) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.dataLoader = dataLoader;
        this.randomProvider = randomProvider;
    }

    private static final int SHOP_SIZE = 5;

    public void refreshShop() {
        if (shopLocked || gold < 2) {
            return;
        }
        gold -= 2;

        var allUnits = dataLoader.getAllUnits();
        shop = new ArrayList<>();
        for (var i = 0; i < SHOP_SIZE; i++) {
            var randomIndex = randomProvider.nextInt(allUnits.size());
            shop.add(allUnits.get(randomIndex));
        }
    }

    public void buyUnit(int shopIndex) {
        if (shopIndex < 0 || shopIndex >= shop.size()) return;
        UnitDefinition def = shop.get(shopIndex);

        if (def == null) return;
        if (gold < def.cost()) return;
        if (bench.size() >= MAX_BENCH_SIZE) return;

        gold -= def.cost();
        StandardGameUnit newUnit = new StandardGameUnit(def);
        newUnit.setOwnerId(this.id);
        bench.add(newUnit);
        shop.set(shopIndex, null);
        if (inCombat) {
            // Defer upgrade check until combat ends
            pendingUpgrades.add(new PendingUpgrade(def.name(), 1));
        } else {
            checkUpgrade(def.name(), 1);
        }
    }

    private void checkUpgrade(String unitName, int starLevel) {
        var candidates = new ArrayList<GameUnit>();
        candidates.addAll(bench.stream()
                .filter(u -> u.getName().equals(unitName) && u.getStarLevel() == starLevel)
                .toList());
        candidates.addAll(boardUnits.stream()
                .filter(u -> u.getName().equals(unitName) && u.getStarLevel() == starLevel)
                .toList());

        if (candidates.size() >= 3) {
            var unitsToRemove = candidates.subList(0, 3);
            var targetPosUnit = unitsToRemove.stream()
                    .filter(u -> boardUnits.contains(u))
                    .findFirst()
                    .orElse(unitsToRemove.get(0));

            int x = targetPosUnit.getX();
            int y = targetPosUnit.getY();

            // Clear from grid if necessary
            for (var u : unitsToRemove) {
                if (boardUnits.contains(u)) {
                    grid.removeUnit(u);
                }
            }
            bench.removeAll(unitsToRemove);
            boardUnits.removeAll(unitsToRemove);

            UnitDefinition def = dataLoader.getAllUnits().stream()
                    .filter(d -> d.name().equals(unitName))
                    .findFirst()
                    .orElse(null);

            if (def != null) {
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
                        def.traits(),
                        def.ability());

                var upgraded = new StandardGameUnit(upgradedDef);
                upgraded.setOwnerId(this.id);
                upgraded.setStarLevel(starLevel + 1);

                // Place back logic
                if (boardUnits.contains(targetPosUnit) || (y >= 0 && grid.isEmpty(x, y))) {
                    // Try placing on board at target position
                    if (grid.isValid(x, y) && grid.isEmpty(x, y)) {
                        grid.placeUnit(upgraded, x, y);
                        boardUnits.add(upgraded);
                    } else {
                        // Fallback to bench if grid somehow full/invalid
                        bench.add(upgraded);
                    }
                } else {
                    bench.add(upgraded);
                }

                checkUpgrade(unitName, starLevel + 1);
            }
        }
    }

    public void gainGold(int amount) {
        this.gold += amount;
    }

    public void sellUnit(String unitId, boolean allowBoardSell) {
        // Try to find unit in bench first (always allowed)
        var benchUnit =
                bench.stream().filter(u -> u.getId().equals(unitId)).findFirst().orElse(null);
        if (benchUnit != null) {
            var refund = calculateSellValue(benchUnit);
            bench.remove(benchUnit);
            gold += refund;
            return;
        }

        // Try to find unit on board (only if allowed, e.g., during PLANNING phase)
        if (!allowBoardSell) {
            return;
        }
        var boardUnit = boardUnits.stream()
                .filter(u -> u.getId().equals(unitId))
                .findFirst()
                .orElse(null);
        if (boardUnit != null) {
            var refund = calculateSellValue(boardUnit);
            grid.removeUnit(boardUnit);
            boardUnits.remove(boardUnit);
            gold += refund;
        }
    }

    public int calculateSellValue(GameUnit unit) {
        // Formula: cost × 3^(starLevel - 1)
        // 1-star, 1-cost → 1 gold
        // 2-star, 1-cost → 3 gold
        // 3-star, 1-cost → 9 gold
        // 3-star, 2-cost → 18 gold
        var cost = unit.getCost();
        var starLevel = unit.getStarLevel();
        return cost * (int) Math.pow(3, starLevel - 1);
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public void processPendingUpgrades() {
        // Process all pending upgrades that were deferred during combat
        var upgradesToProcess = new ArrayList<>(pendingUpgrades);
        pendingUpgrades.clear();
        for (var pending : upgradesToProcess) {
            checkUpgrade(pending.unitName(), pending.starLevel());
        }
    }

    public void gainXp(int amount) {
        this.xp += amount;
        checkLevelUp();
    }

    public void takeDamage(int amount) {
        this.health = Math.max(0, this.health - amount);
    }

    private void checkLevelUp() {
        while (true) {
            int xpNeeded = getXpNeededForLevel(this.level);
            if (this.xp >= xpNeeded) {
                this.xp -= xpNeeded;
                this.level++;
            } else {
                break;
            }
        }
    }

    private int getXpNeededForLevel(int currentLevel) {
        return switch (currentLevel) {
            case 1 -> 2;
            case 2 -> 6;
            case 3 -> 10;
            case 4 -> 20;
            case 5 -> 36;
            case 6 -> 56;
            case 7 -> 80;
            default -> 100;
        };
    }

    public int getNextLevelXp() {
        return getXpNeededForLevel(this.level);
    }

    public void moveUnit(String unitId, int x, int y) {
        if (boardLocked) return;
        // Validation: Player grid check
        if (y >= 0 && !grid.isValid(x, y)) return;

        var benchUnit =
                bench.stream().filter(u -> u.getId().equals(unitId)).findFirst().orElse(null);
        if (benchUnit != null) {
            // Bench -> Board
            if (y >= 0) {
                var targetUnit = grid.getUnitAt(x, y).orElse(null);
                if (targetUnit != null) {
                    // Swap: Board unit goes to bench, bench unit goes to board
                    grid.removeUnit(targetUnit);
                    boardUnits.remove(targetUnit);
                    targetUnit.setPosition(-1, -1);
                    bench.add(targetUnit);

                    bench.remove(benchUnit);
                    grid.placeUnit(benchUnit, x, y);
                    boardUnits.add(benchUnit);
                } else {
                    // Empty cell - standard move
                    if (boardUnits.size() >= level) return; // Cap

                    bench.remove(benchUnit);
                    grid.placeUnit(benchUnit, x, y);
                    boardUnits.add(benchUnit);
                }
            }
        } else {
            var boardUnit = boardUnits.stream()
                    .filter(u -> u.getId().equals(unitId))
                    .findFirst()
                    .orElse(null);
            if (boardUnit != null) {
                // Board -> Bench
                if (y < 0) {
                    if (bench.size() >= MAX_BENCH_SIZE) return;
                    grid.removeUnit(boardUnit); // Remove from grid
                    boardUnits.remove(boardUnit);
                    boardUnit.setPosition(-1, -1);
                    bench.add(boardUnit);
                } else if (grid.isValid(x, y)) {
                    // Board -> Board (Move or Swap)
                    int oldX = boardUnit.getX();
                    int oldY = boardUnit.getY();

                    var targetUnit = grid.getUnitAt(x, y).orElse(null);

                    grid.removeUnit(boardUnit);

                    if (targetUnit != null) {
                        // Swap: Move target to old position
                        grid.removeUnit(targetUnit);
                        grid.placeUnit(targetUnit, oldX, oldY);
                    }

                    // Place moving unit to new position
                    grid.placeUnit(boardUnit, x, y);
                }
            }
        }
    }

    public void removeAllUnits() {
        new ArrayList<>(boardUnits).forEach(u -> {
            grid.removeUnit(u);
            boardUnits.remove(u);
        });
    }

    public void addUnitToBoard(UnitDefinition def, int x, int y) {
        if (boardUnits.size() >= level) return;
        var unit = new StandardGameUnit(def);
        unit.setOwnerId(this.id);
        if (grid.isValid(x, y) && grid.isEmpty(x, y)) {
            grid.placeUnit(unit, x, y);
            boardUnits.add(unit);
        }
    }

    public PlayerState toState() {
        return new PlayerState(
                id,
                name,
                health,
                gold,
                level,
                xp,
                getNextLevelXp(),
                place,
                combatSide,
                new ArrayList<>(bench),
                new ArrayList<>(boardUnits),
                new ArrayList<>(), // TODO: Calculate active traits
                new ArrayList<>(shop));
    }
}
