package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BenchTest {

    private Bench bench;

    @BeforeEach
    void setUp() {
        bench = new Bench();
    }

    @Test
    void testNewBench_IsEmpty() {
        assertTrue(bench.isEmpty());
        assertEquals(0, bench.count());
    }

    @Test
    void testSet_AddsUnitToSlot() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit);

        assertFalse(bench.isEmpty());
        assertEquals(1, bench.count());
        assertTrue(bench.get(0).isPresent());
        assertEquals(unit.getId(), bench.get(0).get().getId());
    }

    @Test
    void testClear_RemovesUnitFromSlot() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit);

        bench.clear(0);

        assertTrue(bench.isEmpty());
        assertFalse(bench.get(0).isPresent());
    }

    @Test
    void testSwap_ExchangesUnits() {
        var unit1 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        var unit2 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit1);
        bench.set(1, unit2);

        bench.swap(0, 1);

        assertEquals(unit2.getId(), bench.get(0).get().getId());
        assertEquals(unit1.getId(), bench.get(1).get().getId());
    }

    @Test
    void testSwap_WithEmptySlot() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit);

        bench.swap(0, 5);

        assertFalse(bench.get(0).isPresent());
        assertTrue(bench.get(5).isPresent());
        assertEquals(unit.getId(), bench.get(5).get().getId());
    }

    @Test
    void testFindFirstEmptySlot_ReturnsFirstEmpty() {
        var unit1 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        var unit2 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit1);
        bench.set(2, unit2);

        var emptySlot = bench.findFirstEmptySlot();

        assertTrue(emptySlot.isPresent());
        assertEquals(1, emptySlot.get());
    }

    @Test
    void testFindFirstEmptySlot_EmptyWhenFull() {
        for (int i = 0; i < 9; i++) {
            bench.set(i, new StandardGameUnit(TestHelpers.createDefaultUnitDef()));
        }

        var emptySlot = bench.findFirstEmptySlot();

        assertFalse(emptySlot.isPresent());
        assertTrue(bench.isFull());
    }

    @Test
    void testFindUnit_ReturnsCorrectEntry() {
        var unit1 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        var unit2 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(3, unit1);
        bench.set(7, unit2);

        var entry = bench.findUnit(unit2.getId());

        assertTrue(entry.isPresent());
        assertEquals(7, entry.get().index());
        assertEquals(unit2, entry.get().unit());
    }

    @Test
    void testFindUnit_NotFound() {
        var entry = bench.findUnit("nonexistent-id");
        assertFalse(entry.isPresent());
    }

    @Test
    void testRemoveUnit_RemovesCorrectUnit() {
        var unit1 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        var unit2 = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit1);
        bench.set(1, unit2);

        bench.removeUnit(unit1);

        assertEquals(1, bench.count());
        assertFalse(bench.get(0).isPresent());
        assertTrue(bench.get(1).isPresent());
    }

    @Test
    void testIndexOf_ReturnsCorrectIndex() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(4, unit);

        assertEquals(4, bench.indexOf(unit));
    }

    @Test
    void testIndexOf_NotFound_ReturnsMinusOne() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        assertEquals(-1, bench.indexOf(unit));
    }

    @Test
    void testGetOrNull_ReturnsNullForEmptySlot() {
        assertNull(bench.getOrNull(0));
    }

    @Test
    void testGetOrNull_ReturnsUnitForOccupiedSlot() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit);

        assertEquals(unit, bench.getOrNull(0));
    }

    @Test
    void testToList_ReturnsCopy() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());
        bench.set(0, unit);

        var list = bench.toList();

        assertEquals(9, list.size());
        assertEquals(unit, list.get(0));
        for (int i = 1; i < 9; i++) {
            assertNull(list.get(i));
        }
    }

    @Test
    void testUnits_StreamsAllUnits() {
        bench.set(0, new StandardGameUnit(TestHelpers.createDefaultUnitDef()));
        bench.set(4, new StandardGameUnit(TestHelpers.createDefaultUnitDef()));
        bench.set(8, new StandardGameUnit(TestHelpers.createDefaultUnitDef()));

        var count = bench.units().count();

        assertEquals(3, count);
    }

    @Test
    void testSet_InvalidSlot_NoOp() {
        var unit = new StandardGameUnit(TestHelpers.createDefaultUnitDef());

        bench.set(-1, unit);
        bench.set(10, unit);

        assertTrue(bench.isEmpty());
    }

    @Test
    void testGet_InvalidSlot_ReturnsEmpty() {
        assertFalse(bench.get(-1).isPresent());
        assertFalse(bench.get(10).isPresent());
    }
}
