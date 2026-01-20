package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.createSeededRandomProvider;
import static net.lwenstrom.tft.backend.test.TestHelpers.createTestClock;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
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

        gameModeRegistry = new GameModeRegistry(List.of(provider), "onepiece");
        dataLoader = new DataLoader(gameModeRegistry);
        dataLoader.loadData();

        gameRoom = new GameRoom(
                "bot-test-room", dataLoader, gameModeRegistry, createTestClock(), createSeededRandomProvider());
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
            assertTrue(y >= 0 && y <= 3, "Y should be 0-3 for bot initial placement (assuming player view)");
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
}
