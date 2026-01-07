package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lwenstrom.tft.backend.core.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameRoomBotTest {

    private DataLoader dataLoader;
    private GameRoom gameRoom;

    @BeforeEach
    void setUp() {
        // Manually instantiate DataLoader and load data
        // We assume /data/units.json is in src/main/resources which is on classpath
        dataLoader = new DataLoader(new ObjectMapper());
        dataLoader.loadData();

        gameRoom = new GameRoom(dataLoader);
    }

    @Test
    void testBotAddedWithUnits() {
        gameRoom.addBot();

        // Find the bot
        Player bot = gameRoom.getPlayers().stream()
                .filter(p -> p.getId().startsWith("Bot-") || p.getName().startsWith("Bot-"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Bot not found"));

        assertFalse(bot.getBoardUnits().isEmpty(), "Bot should have units initially");
        System.out.println("Bot has " + bot.getBoardUnits().size() + " units.");

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

        // Initial units
        int initialCount = bot.getBoardUnits().size();
        var initialUnitIds = bot.getBoardUnits().stream().map(u -> u.getId()).toList();

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
        // Actually, I can use reflection to call startPhase(GamePhase.PLANNING) if I
        // really want.

        try {
            var method = GameRoom.class.getDeclaredMethod("startPhase", GameRoom.GamePhase.class);
            method.setAccessible(true);

            // Advance round
            // Round is private, but increments in PLANNING.
            // Let's call it multiple times to ensure round count goes up and unit count
            // increases

            // Call startPhase(COMBAT) then startPhase(PLANNING) to trigger new round logic
            method.invoke(gameRoom, GameRoom.GamePhase.COMBAT);
            method.invoke(gameRoom, GameRoom.GamePhase.PLANNING); // Round should be 1 now?

            // Check if units changed
            var newUnitIds = bot.getBoardUnits().stream().map(u -> u.getId()).toList();
            assertNotEquals(initialUnitIds, newUnitIds, "Units should strictly change/refresh");

            // Check scaling
            // Round 0 -> 1 unit? Round 1 -> 1 unit?
            // (round/2) + 1.
            // R0: 1. R1: 1. R2: 2. R3: 2.

            // Go to round 4
            for (int i = 0; i < 6; i++) {
                method.invoke(gameRoom, GameRoom.GamePhase.COMBAT);
                method.invoke(gameRoom, GameRoom.GamePhase.PLANNING);
            }

            int newCount = bot.getBoardUnits().size();
            System.out.println("Bot units at later round: " + newCount);
            assertTrue(newCount > 1, "Bot should have more units at later rounds");

        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
