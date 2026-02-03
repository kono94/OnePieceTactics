package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.model.GameState.PlayerState;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.LootOrb;
import net.lwenstrom.tft.backend.core.model.LootType;
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

    private final List<GameUnit> bench = new ArrayList<>(java.util.Arrays.asList(new GameUnit[MAX_BENCH_SIZE]));
    private final List<GameUnit> boardUnits = new ArrayList<>();
    private final List<LootOrb> lootOrbs = new ArrayList<>();

    private List<UnitDefinition> shop = new ArrayList<>();
    private boolean shopLocked = false;
    private boolean boardLocked = false;
    private boolean inCombat = false;
    private boolean ghost = false;
    private final List<PendingUpgrade> pendingUpgrades = new ArrayList<>();

    private record PendingUpgrade(String unitName, int starLevel) {
    }

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
        if (shopIndex < 0 || shopIndex >= shop.size())
            return;
        UnitDefinition def = shop.get(shopIndex);

        if (def == null)
            return;
        if (gold < def.cost())
            return;

        // Find first empty slot on bench
        int emptySlot = -1;
        for (int i = 0; i < MAX_BENCH_SIZE; i++) {
            if (bench.get(i) == null) {
                emptySlot = i;
                break;
            }
        }
        if (emptySlot == -1)
            return;

        gold -= def.cost();
        StandardGameUnit newUnit = new StandardGameUnit(def);
        newUnit.setOwnerId(this.id);
        bench.set(emptySlot, newUnit);
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
                .filter(u -> u != null && u.getName().equals(unitName) && u.getStarLevel() == starLevel)
                .toList());
        candidates.addAll(boardUnits.stream()
                .filter(u -> u != null && u.getName().equals(unitName) && u.getStarLevel() == starLevel)
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
            // Remove from bench by setting slot to null
            for (var u : unitsToRemove) {
                int benchIdx = bench.indexOf(u);
                if (benchIdx != -1) {
                    bench.set(benchIdx, null);
                }
            }
            boardUnits.removeAll(unitsToRemove);

            UnitDefinition def = dataLoader.getAllUnits().stream()
                    .filter(d -> d.name().equals(unitName))
                    .findFirst()
                    .orElse(null);

            if (def != null) {
                var upgraded = new StandardGameUnit(def, starLevel + 1);
                upgraded.setOwnerId(this.id);

                // Place back logic
                if (boardUnits.contains(targetPosUnit) || (y >= 0 && grid.isEmpty(x, y))) {
                    // Try placing on board at target position
                    if (grid.isValid(x, y) && grid.isEmpty(x, y)) {
                        grid.placeUnit(upgraded, x, y);
                        boardUnits.add(upgraded);
                    } else {
                        // Fallback to bench if grid somehow full/invalid
                        addToBench(upgraded);
                    }
                } else {
                    addToBench(upgraded);
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
        for (int i = 0; i < MAX_BENCH_SIZE; i++) {
            var unit = bench.get(i);
            if (unit != null && unit.getId().equals(unitId)) {
                var refund = calculateSellValue(unit);
                bench.set(i, null);
                gold += refund;
                return;
            }
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

    public void addLootOrb(LootOrb orb) {
        this.lootOrbs.add(orb);
    }

    public void collectOrb(String orbId) {
        var orb = lootOrbs.stream().filter(o -> o.id().equals(orbId)).findFirst().orElse(null);

        if (orb != null) {
            lootOrbs.remove(orb);
            if (orb.type() == LootType.GOLD) {
                gainGold(orb.amount());
            } else if (orb.type() == LootType.UNIT) {
                var units = dataLoader.getAllUnits();
                var def = units.stream()
                        .filter(u -> u.name().equals(orb.contentId()))
                        .findFirst()
                        .orElse(null);
                if (def != null) {
                    var unit = new StandardGameUnit(def);
                    unit.setOwnerId(this.id);
                    addToBenchOrRefund(unit, def.cost());
                }
            }
        }
    }

    private void addToBenchOrRefund(GameUnit unit, int refundAmount) {
        int emptySlot = -1;
        for (int i = 0; i < MAX_BENCH_SIZE; i++) {
            if (bench.get(i) == null) {
                emptySlot = i;
                break;
            }
        }
        if (emptySlot != -1) {
            bench.set(emptySlot, unit);
            checkUpgrade(unit.getName(), 1);
        } else {
            gainGold(refundAmount);
        }
    }

    private void addToBench(GameUnit unit) {
        for (int i = 0; i < MAX_BENCH_SIZE; i++) {
            if (bench.get(i) == null) {
                bench.set(i, unit);
                return;
            }
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
        if (boardLocked)
            return;
        // Validation: Player grid check
        if (y >= 0 && !grid.isValid(x, y))
            return;

        // Try to find if moving from bench
        int fromBenchIdx = -1;
        GameUnit benchUnit = null;
        for (int i = 0; i < MAX_BENCH_SIZE; i++) {
            var u = bench.get(i);
            if (u != null && u.getId().equals(unitId)) {
                fromBenchIdx = i;
                benchUnit = u;
                break;
            }
        }

        if (benchUnit != null) {
            // Bench -> Board
            if (y >= 0) {
                if (inCombat)
                    return;

                var targetUnit = grid.getUnitAt(x, y).orElse(null);
                if (targetUnit != null) {
                    // Swap: Board unit goes to bench (at the index we moved from), bench unit goes
                    // to board
                    grid.removeUnit(targetUnit);
                    boardUnits.remove(targetUnit);
                    targetUnit.setPosition(-1, -1);
                    bench.set(fromBenchIdx, targetUnit);

                    grid.placeUnit(benchUnit, x, y);
                    boardUnits.add(benchUnit);
                } else {
                    // Empty cell - standard move
                    if (boardUnits.size() >= level)
                        return; // Cap

                    bench.set(fromBenchIdx, null);
                    grid.placeUnit(benchUnit, x, y);
                    boardUnits.add(benchUnit);
                }
            } else if (y < 0) {
                // Bench -> Bench (Reorder)
                int toBenchIdx = x; // Frontend sends slot index in targetX
                if (toBenchIdx >= 0 && toBenchIdx < MAX_BENCH_SIZE && toBenchIdx != fromBenchIdx) {
                    var targetBenchUnit = bench.get(toBenchIdx);
                    bench.set(toBenchIdx, benchUnit);
                    bench.set(fromBenchIdx, targetBenchUnit);
                }
            }
        } else {
            var boardUnit = boardUnits.stream()
                    .filter(u -> u.getId().equals(unitId))
                    .findFirst()
                    .orElse(null);
            if (boardUnit != null) {
                if (inCombat)
                    return;

                // Board -> Bench
                if (y < 0) {
                    int targetBenchIdx = x;
                    if (targetBenchIdx < 0 || targetBenchIdx >= MAX_BENCH_SIZE) {
                        // Fallback: find first empty
                        targetBenchIdx = -1;
                        for (int i = 0; i < MAX_BENCH_SIZE; i++) {
                            if (bench.get(i) == null) {
                                targetBenchIdx = i;
                                break;
                            }
                        }
                    }

                    if (targetBenchIdx != -1) {
                        var targetBenchUnit = bench.get(targetBenchIdx);
                        if (targetBenchUnit != null) {
                            // Swap: Bench unit goes to board (where board unit was)
                            int oldX = boardUnit.getX();
                            int oldY = boardUnit.getY();

                            grid.removeUnit(boardUnit);
                            boardUnits.remove(boardUnit);
                            boardUnit.setPosition(-1, -1);

                            bench.set(targetBenchIdx, boardUnit);

                            grid.placeUnit(targetBenchUnit, oldX, oldY);
                            boardUnits.add(targetBenchUnit);
                        } else {
                            // Place in empty bench slot
                            grid.removeUnit(boardUnit);
                            boardUnits.remove(boardUnit);
                            boardUnit.setPosition(-1, -1);
                            bench.set(targetBenchIdx, boardUnit);
                        }
                    }
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
        if (boardUnits.size() >= level)
            return;
        var unit = new StandardGameUnit(def);
        unit.setOwnerId(this.id);
        if (grid.isValid(x, y) && grid.isEmpty(x, y)) {
            grid.placeUnit(unit, x, y);
            boardUnits.add(unit);
        }
    }

    public Player createGhost() {
        Player ghostPlayer = new Player(this.name, this.dataLoader, this.randomProvider);
        ghostPlayer.setGhost(true);
        ghostPlayer.setHealth(this.health);
        ghostPlayer.setLevel(this.level);

        for (GameUnit unit : this.boardUnits) {
            GameUnit cloned = unit.cloneUnit();
            ghostPlayer.boardUnits.add(cloned);
            ghostPlayer.grid.placeUnit(cloned, cloned.getX(), cloned.getY());
        }
        return ghostPlayer;
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
                new ArrayList<>(shop),
                new ArrayList<>(lootOrbs),
                ghost);
    }
}
