package net.lwenstrom.tft.backend.core.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.lwenstrom.tft.backend.core.GameConstants;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public class Bench {

    private final GameUnit[] slots = new GameUnit[GameConstants.MAX_BENCH_SIZE];

    public record BenchEntry(int index, GameUnit unit) {}

    public Optional<Integer> findFirstEmptySlot() {
        return IntStream.range(0, GameConstants.MAX_BENCH_SIZE)
                .filter(i -> slots[i] == null)
                .boxed()
                .findFirst();
    }

    public Optional<GameUnit> get(int slot) {
        if (slot < 0 || slot >= GameConstants.MAX_BENCH_SIZE) {
            return Optional.empty();
        }
        return Optional.ofNullable(slots[slot]);
    }

    public GameUnit getOrNull(int slot) {
        if (slot < 0 || slot >= GameConstants.MAX_BENCH_SIZE) {
            return null;
        }
        return slots[slot];
    }

    public void set(int slot, GameUnit unit) {
        if (slot >= 0 && slot < GameConstants.MAX_BENCH_SIZE) {
            slots[slot] = unit;
        }
    }

    public void clear(int slot) {
        set(slot, null);
    }

    public void swap(int slotA, int slotB) {
        if (slotA < 0 || slotA >= GameConstants.MAX_BENCH_SIZE || slotB < 0 || slotB >= GameConstants.MAX_BENCH_SIZE) {
            return;
        }
        var temp = slots[slotA];
        slots[slotA] = slots[slotB];
        slots[slotB] = temp;
    }

    public Optional<BenchEntry> findUnit(String unitId) {
        return IntStream.range(0, GameConstants.MAX_BENCH_SIZE)
                .filter(i -> slots[i] != null && slots[i].getId().equals(unitId))
                .mapToObj(i -> new BenchEntry(i, slots[i]))
                .findFirst();
    }

    public Stream<GameUnit> units() {
        return Arrays.stream(slots).filter(Objects::nonNull);
    }

    public int count() {
        return (int) units().count();
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    public boolean isFull() {
        return count() >= GameConstants.MAX_BENCH_SIZE;
    }

    public List<GameUnit> toList() {
        return Arrays.asList(slots.clone());
    }

    public void removeUnit(GameUnit unit) {
        for (int i = 0; i < GameConstants.MAX_BENCH_SIZE; i++) {
            if (slots[i] != null && slots[i].getId().equals(unit.getId())) {
                slots[i] = null;
                return;
            }
        }
    }

    public int indexOf(GameUnit unit) {
        for (int i = 0; i < GameConstants.MAX_BENCH_SIZE; i++) {
            if (slots[i] != null && slots[i].getId().equals(unit.getId())) {
                return i;
            }
        }
        return -1;
    }
}
