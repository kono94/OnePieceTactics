package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.data.DataLoader;
import net.lwenstrom.tft.backend.core.data.UnitDefinition;
import net.lwenstrom.tft.backend.core.impl.StandardGameUnit;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (shopLocked)
            return;
        if (gold < 2)
            return; // Cost 2 to reroll
        gold -= 2;

        // Simple random roll logic (weighted by level later)
        List<UnitDefinition> allUnits = dataLoader.getAllUnits();
        Collections.shuffle(allUnits);
        shop = allUnits.stream().limit(5).collect(Collectors.toList());
    }

    public void buyUnit(int shopIndex) {
        if (shopIndex < 0 || shopIndex >= shop.size())
            return;
        UnitDefinition def = shop.get(shopIndex);

        if (def == null)
            return; // Already bought
        if (gold < def.cost())
            return;
        if (bench.size() >= MAX_BENCH_SIZE)
            return;

        gold -= def.cost();
        bench.add(new StandardGameUnit(def));
        shop.set(shopIndex, null); // Remove from shop
    }

    public void gainGold(int amount) {
        this.gold += amount;
    }

    public void gainXp(int amount) {
        this.xp += amount;
        // Level up logic check
    }
}
