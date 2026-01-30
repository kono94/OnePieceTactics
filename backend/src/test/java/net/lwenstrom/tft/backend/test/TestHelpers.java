package net.lwenstrom.tft.backend.test;

import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.combat.BfsUnitMover;
import net.lwenstrom.tft.backend.core.combat.DefaultAbilityCaster;
import net.lwenstrom.tft.backend.core.combat.NearestEnemyTargetSelector;
import net.lwenstrom.tft.backend.core.engine.CombatSystem;
import net.lwenstrom.tft.backend.core.engine.GameRoom;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import net.lwenstrom.tft.backend.core.time.Clock;

public final class TestHelpers {

    private TestHelpers() {}

    public static final long TEST_SEED = 42L;

    public static GameModeRegistry createMockRegistry() {
        GameModeProvider provider = new GameModeProvider() {
            @Override
            public GameMode getMode() {
                return GameMode.ONEPIECE;
            }

            @Override
            public String getUnitsPath() {
                return "";
            }

            @Override
            public String getTraitsPath() {
                return "";
            }

            @Override
            public void registerTraitEffects(TraitManager traitManager) {}
        };
        return new GameModeRegistry(List.of(provider), "onepiece");
    }

    public static DataLoader createMockDataLoader() {
        return createMockDataLoader(List.of(createDefaultUnitDef()));
    }

    public static DataLoader createMockDataLoader(List<UnitDefinition> units) {
        var registry = createMockRegistry();
        return new DataLoader(registry) {
            @Override
            public List<UnitDefinition> getAllUnits() {
                return units;
            }

            @Override
            public UnitDefinition getUnitDefinition(String id) {
                return units.stream()
                        .filter(u -> u.id().equals(id))
                        .findFirst()
                        .orElse(units.isEmpty() ? null : units.get(0));
            }

            @Override
            public GameMode getGameMode() {
                return GameMode.ONEPIECE;
            }
        };
    }

    public static UnitDefinition createDefaultUnitDef() {
        return new UnitDefinition(
                "test-unit-1",
                "TestUnit",
                1,
                l(100),
                l(100),
                l(10),
                l(0),
                l(0),
                l(0),
                lf(1.0f),
                l(1),
                List.of("Trait"),
                null);
    }

    public static UnitDefinition createUnitDef(String id, String name, int cost, int health, int attackDamage) {
        return new UnitDefinition(
                id, name, cost, l(health), l(100), l(attackDamage), l(0), l(0), l(0), lf(1.0f), l(1), List.of(), null);
    }

    public static UnitDefinition createUnitDefWithAbility(
            String id, String name, int cost, int health, int maxMana, AbilityDefinition ability) {
        return new UnitDefinition(
                id, name, cost, l(health), l(maxMana), l(10), l(0), l(0), l(0), lf(1.0f), l(1), List.of(), ability);
    }

    private static List<Integer> l(int val) {
        return List.of(val, val, val);
    }

    private static List<Float> lf(float val) {
        return List.of(val, val, val);
    }

    public static SeededRandomProvider createSeededRandomProvider() {
        return new SeededRandomProvider(TEST_SEED);
    }

    public static SeededRandomProvider createSeededRandomProvider(long seed) {
        return new SeededRandomProvider(seed);
    }

    public static Player createTestPlayer(String name) {
        return createTestPlayer(name, createMockDataLoader());
    }

    public static Player createTestPlayer(String name, DataLoader dataLoader) {
        return new Player(name, dataLoader, createSeededRandomProvider());
    }

    public static Player createTestPlayer(String name, DataLoader dataLoader, RandomProvider randomProvider) {
        return new Player(name, dataLoader, randomProvider);
    }

    public static TestClock createTestClock() {
        return new TestClock();
    }

    public static CombatSystem createTestCombatSystem() {
        return createTestCombatSystem(createTestClock());
    }

    public static CombatSystem createTestCombatSystem(Clock clock) {
        return new CombatSystem(
                new TraitManager(),
                clock,
                new NearestEnemyTargetSelector(),
                new BfsUnitMover(clock),
                new DefaultAbilityCaster());
    }

    public static GameRoom createTestGameRoom() {
        return createTestGameRoom("test-room");
    }

    public static GameRoom createTestGameRoom(String roomId) {
        var registry = createMockRegistry();
        var dataLoader = createMockDataLoader();
        var clock = createTestClock();
        var randomProvider = createSeededRandomProvider();
        return new GameRoom(roomId, dataLoader, registry, clock, randomProvider);
    }

    public static GameRoom createTestGameRoom(DataLoader dataLoader) {
        var registry = createMockRegistry();
        var clock = createTestClock();
        var randomProvider = createSeededRandomProvider();
        return new GameRoom("test-room", dataLoader, registry, clock, randomProvider);
    }

    public static GameRoom createTestGameRoom(DataLoader dataLoader, Clock clock) {
        var registry = createMockRegistry();
        var randomProvider = createSeededRandomProvider();
        return new GameRoom("test-room", dataLoader, registry, clock, randomProvider);
    }

    public static void fastForwardPhase(GameRoom room) {
        try {
            var field = GameRoom.class.getDeclaredField("phaseEndTime");
            field.setAccessible(true);
            field.set(room, System.currentTimeMillis() - 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fast forward phase", e);
        }
    }

    public static void setPhase(GameRoom room, net.lwenstrom.tft.backend.core.model.GamePhase phase) {
        try {
            var method = GameRoom.class.getDeclaredMethod(
                    "startPhase", net.lwenstrom.tft.backend.core.model.GamePhase.class);
            method.setAccessible(true);
            method.invoke(room, phase);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set phase", e);
        }
    }

    public static void addUnitToPlayer(Player player, net.lwenstrom.tft.backend.core.model.GameUnit unit) {
        try {
            var field = Player.class.getDeclaredField("boardUnits");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var boardUnits = (java.util.List<net.lwenstrom.tft.backend.core.model.GameUnit>) field.get(player);
            boardUnits.add(unit);
            unit.setOwnerId(player.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to add unit to player", e);
        }
    }
}
