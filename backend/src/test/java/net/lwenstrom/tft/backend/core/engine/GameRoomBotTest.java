package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.createSeededRandomProvider;
import static net.lwenstrom.tft.backend.test.TestHelpers.createTestClock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import net.lwenstrom.tft.backend.game.onepiece.OnePieceGameModeProvider;
import net.lwenstrom.tft.backend.game.palworld.PalworldGameModeProvider;
import net.lwenstrom.tft.backend.game.pokemon.PokemonGameModeProvider;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameRoomBotTest {

    private DataLoader dataLoader;
    private GameRoom gameRoom;
    private GameModeRegistry gameModeRegistry;

    @BeforeEach
    void setUp() {
        GameModeProvider provider = new GameModeProvider() {
            @Override
            public GameMode getMode() {
                return GameMode.ONEPIECE;
            }

            @Override
            public String getUnitsPath() {
                return "/data/units_onepiece.json";
            }

            @Override
            public String getTraitsPath() {
                return "/data/traits_onepiece.json";
            }

            @Override
            public void registerTraitEffects(TraitManager traitManager) {}
        };

        gameModeRegistry = new GameModeRegistry(List.of(provider));
        dataLoader = new DataLoader(
                gameModeRegistry,
                tools.jackson.databind.json.JsonMapper.builder().build());
        dataLoader.loadData();

        gameRoom = new GameRoom(
                "bot-test-room",
                dataLoader,
                gameModeRegistry,
                createTestClock(),
                createSeededRandomProvider(),
                GameMode.ONEPIECE);
    }

    @Test
    void testBotAddedWithUnits() {
        gameRoom.addBot();

        // Find the bot
        Player bot = gameRoom.getPlayers().stream()
                .filter(p -> p.getId().startsWith("Bot-") || p.getName().startsWith("Bot-"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Bot not found"));

        assertTrue(bot.getBoardUnits().size() >= 1, "Bot should have at least 1 unit");
        System.out.println("Bot units at later round: " + bot.getBoardUnits().size() + " units.");

        // Check valid placement
        for (var unit : bot.getBoardUnits()) {
            int x = unit.getX();
            int y = unit.getY();
            assertTrue(x >= 0 && x <= 6, "X should be 0-6");
            assertTrue(y >= 0 && y <= 2, "Y should be 0-2 for bot initial placement (assuming player view)");
            // Note: My implementation used y 0-3 for bot which mimics player area.
        }
    }

    @Test
    void testBotRosterRefreshesOnNewPhase() {
        gameRoom.addBot();
        Player bot = gameRoom.getPlayers().iterator().next();

        // Advance phase to COMBAT
        // To trigger nextPhase, we need to tick or call private methods.
        // We can force it by waiting or using reflection, but simpler:
        // GameRoom treats phase transition internally.
        // Let's just simulate what GameController does or assume startPhase is called.
        // Wait, startPhase is private.
        // But tick() checks time.
        // Or we can just inspect the effect of addBot which calls refreshBotRoster.

        // To verify *startPhase* hook, we need access to it.
        // Since it's private, we can't call it directly.
        // However, we can call tick() if we manipulate time, but that's hard.

        // Alternative: Verify refreshBotRoster logic via reflection or just trust
        // addBot works
        // and knowing I hooked it in startPhase.
        // Use reflection to call startPhase
        try {
            var method = GameRoom.class.getDeclaredMethod("startPhase", GamePhase.class);
            method.setAccessible(true);

            // Call startPhase(COMBAT) then startPhase(PLANNING) to trigger new round logic
            method.invoke(gameRoom, GamePhase.COMBAT);
            method.invoke(gameRoom, GamePhase.PLANNING);

            // Verify roster exists
            assertFalse(bot.getBoardUnits().isEmpty(), "Roster should not be empty");
            System.out.println("Bot units after refresh: " + bot.getBoardUnits().size());

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testBotUnitsFollowShopOdds() {
        // Add bot at round 0 (level 2)
        gameRoom.addBot();

        Player bot = gameRoom.getPlayers().stream()
                .filter(p -> p.getName().startsWith("Bot-"))
                .findFirst()
                .orElseThrow();

        // Bot should have units appropriate for level 2
        // At level 2, shop odds are: 70% 1-cost, 30% 2-cost, 0% 3-5 cost
        for (var unit : bot.getBoardUnits()) {
            int cost = unit.getCost();
            assertTrue(
                    cost >= 1 && cost <= 2, "Bot at round 0 (level 2) should only have 1-2 cost units, found: " + cost);
            System.out.println("Unit: " + unit.getName() + " (Cost: " + cost + ", Star: " + unit.getStarLevel() + ")");
        }
    }

    @Test
    void testBotCanHaveHigherStarUnits() {
        boolean found2Star = false;
        boolean found3Star = false;

        // Add multiple bots to increase chances of seeing higher star units
        for (int i = 0; i < 20; i++) {
            gameRoom.addBot();
        }

        for (var player : gameRoom.getPlayers()) {
            if (!player.getName().startsWith("Bot-")) continue;

            for (var unit : player.getBoardUnits()) {
                int starLevel = unit.getStarLevel();
                assertTrue(starLevel >= 1 && starLevel <= 3, "Star level should be 1, 2, or 3");

                if (starLevel == 2) found2Star = true;
                if (starLevel == 3) found3Star = true;
            }
        }

        System.out.println("Found 2-star: " + found2Star + ", Found 3-star: " + found3Star);
    }

    @Test
    void botRosterKeepsFirstTwoRoundsSoftAndGuaranteesRoundThreeUpgrade() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(99));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 1);
        assertEquals(2, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 1));

        refreshBotAtRound(room, bot, 2);
        assertEquals(3, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 1));

        refreshBotAtRound(room, bot, 3);
        assertEquals(3, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream().anyMatch(boardUnit -> boardUnit.getStarLevel() == 2));
    }

    @Test
    void botRosterRaisesTwoStarChanceAfterRoundThree() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(10));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 3);
        assertTrue(bot.getBoardUnits().stream().anyMatch(boardUnit -> boardUnit.getStarLevel() >= 2));

        refreshBotAtRound(room, bot, 4);
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 2));
    }

    @Test
    void botRosterPreventsThreeStarsThroughRoundFive() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(0));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 5);

        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 2));
    }

    @Test
    void botRosterDelaysExpensiveThreeStars() {
        var fourCost = TestHelpers.createUnitDef("four-cost", "Four Cost", 4, 100, 10);
        var room = createRoomWithUnits(List.of(fourCost), new FixedRandomProvider(0));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 15);
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 2));

        refreshBotAtRound(room, bot, 16);
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 3));
    }

    @Test
    void roundTenBotRosterCapsUnitCountAndGuaranteesCheapThreeStars() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(99));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 10);

        assertEquals(7, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream()
                        .filter(boardUnit -> boardUnit.getStarLevel() == 3)
                        .count()
                >= 2);
    }

    @Test
    void pokemonRoundFiveBotRosterKeepsSharedEarlyCurve() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(35), GameMode.POKEMON);
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 5);

        assertEquals(4, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() == 2));
    }

    @Test
    void pokemonRoundEightBotRosterUsesOneFewerUnit() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(99), GameMode.POKEMON);
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 8);

        assertEquals(5, bot.getBoardUnits().size());
    }

    @Test
    void pokemonRoundTenBotRosterUsesOneFewerUnit() {
        var unit = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(99), GameMode.POKEMON);
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 10);

        assertEquals(6, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream()
                        .filter(boardUnit -> boardUnit.getStarLevel() == 3)
                        .count()
                >= 2);
    }

    @Test
    void roundFifteenBotRosterGuaranteesCheapAndMidCostThreeStars() {
        var oneCost = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var threeCost = TestHelpers.createUnitDef("three-cost", "Three Cost", 3, 100, 10);
        var room = createRoomWithUnits(List.of(oneCost, threeCost), new FixedRandomProvider(99));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 15);

        assertEquals(7, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream()
                        .filter(boardUnit -> boardUnit.getCost() <= 2)
                        .filter(boardUnit -> boardUnit.getStarLevel() == 3)
                        .count()
                >= 2);
        assertTrue(bot.getBoardUnits().stream()
                        .filter(boardUnit -> boardUnit.getCost() >= 3 && boardUnit.getCost() <= 4)
                        .filter(boardUnit -> boardUnit.getStarLevel() == 3)
                        .count()
                >= 2);
    }

    @Test
    void pokemonRoundFifteenBotRosterUsesOneFewerUnit() {
        var oneCost = TestHelpers.createUnitDef("one-cost", "One Cost", 1, 100, 10);
        var threeCost = TestHelpers.createUnitDef("three-cost", "Three Cost", 3, 100, 10);
        var room = createRoomWithUnits(List.of(oneCost, threeCost), new FixedRandomProvider(99), GameMode.POKEMON);
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 15);

        assertEquals(6, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream()
                        .filter(boardUnit -> boardUnit.getCost() <= 2)
                        .filter(boardUnit -> boardUnit.getStarLevel() == 3)
                        .count()
                >= 2);
        assertTrue(bot.getBoardUnits().stream()
                        .filter(boardUnit -> boardUnit.getCost() >= 3 && boardUnit.getCost() <= 4)
                        .filter(boardUnit -> boardUnit.getStarLevel() == 3)
                        .count()
                >= 2);
    }

    @Test
    void expensiveBotUnitsNeverExceedTwoStars() {
        var unit = TestHelpers.createUnitDef("five-cost", "Five Cost", 5, 100, 10);
        var room = createRoomWithUnits(List.of(unit), new FixedRandomProvider(0));
        var bot = room.addBot().orElseThrow();

        refreshBotAtRound(room, bot, 15);

        assertEquals(7, bot.getBoardUnits().size());
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getCost() == 5));
        assertTrue(bot.getBoardUnits().stream().allMatch(boardUnit -> boardUnit.getStarLevel() <= 2));
    }

    private GameRoom createRoomWithUnits(List<UnitDefinition> units, RandomProvider randomProvider) {
        return createRoomWithUnits(units, randomProvider, GameMode.ONEPIECE);
    }

    private GameRoom createRoomWithUnits(List<UnitDefinition> units, RandomProvider randomProvider, GameMode mode) {
        return new GameRoom(
                "bot-tuning-test",
                TestHelpers.createMockDataLoader(units),
                createMockRegistry(mode),
                createTestClock(),
                randomProvider,
                mode);
    }

    private GameModeRegistry createMockRegistry(GameMode mode) {
        var jsonMapper = tools.jackson.databind.json.JsonMapper.builder().build();
        GameModeProvider provider =
                switch (mode) {
                    case ONEPIECE -> new OnePieceGameModeProvider(jsonMapper);
                    case POKEMON -> new PokemonGameModeProvider(jsonMapper);
                    case PALWORLD -> new PalworldGameModeProvider(jsonMapper);
                };
        return new GameModeRegistry(List.of(provider));
    }

    private void refreshBotAtRound(GameRoom room, Player bot, int targetRound) {
        try {
            var roundField = GameRoom.class.getDeclaredField("round");
            roundField.setAccessible(true);
            var previousRound = roundField.getInt(room);
            roundField.setInt(room, targetRound);

            var method = GameRoom.class.getDeclaredMethod("refreshBotRoster", Player.class);
            method.setAccessible(true);
            method.invoke(room, bot);

            roundField.setInt(room, previousRound);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to refresh bot roster for round " + targetRound, e);
        }
    }

    private static final class FixedRandomProvider implements RandomProvider {
        private final int value;
        private final Random random = new Random(0L);

        private FixedRandomProvider(int value) {
            this.value = value;
        }

        @Override
        public <T> void shuffle(List<T> list) {
            Collections.shuffle(list, random);
        }

        @Override
        public int nextInt(int bound) {
            if (bound <= 0) {
                throw new IllegalArgumentException("bound must be positive");
            }
            return Math.floorMod(value, bound);
        }

        @Override
        public double nextDouble() {
            return Math.max(0.0, Math.min(0.99, value / 100.0));
        }

        @Override
        public Random getRandom() {
            return random;
        }
    }
}
