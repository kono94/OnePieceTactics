package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import net.lwenstrom.tft.backend.core.model.ActionType;
import net.lwenstrom.tft.backend.core.model.GameAction;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class GameRoomConcurrencyTest {

    @Test
    void duplicateRoomCreationIsAtomic() throws Exception {
        var engine = new GameEngine(
                TestHelpers.createMockDataLoader(),
                TestHelpers.createMockRegistry(),
                TestHelpers.createTestClock(),
                TestHelpers.createSeededRandomProvider());

        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = IntStream.range(0, 32)
                    .mapToObj(index -> (Callable<Boolean>)
                            () -> engine.tryCreateRoom("shared-room").isPresent())
                    .toList();
            var created = executor.invokeAll(tasks).stream()
                    .filter(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .count();

            assertEquals(1, created);
            assertNotNull(engine.getRoom("shared-room"));
        }
    }

    @Test
    void tickAndCommandsPreservePlayerInvariants() throws Exception {
        var room = new GameRoom(
                "concurrent-room",
                TestHelpers.createMockDataLoader(),
                TestHelpers.createMockRegistry(),
                TestHelpers.createTestClock(),
                TestHelpers.createSeededRandomProvider(),
                net.lwenstrom.tft.backend.core.model.GameMode.ONEPIECE);
        var player = room.addPlayer("Player");
        room.startMatch();

        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = IntStream.range(0, 100)
                    .mapToObj(index -> (Callable<Void>) () -> {
                        if (index % 2 == 0) {
                            room.tick();
                        } else {
                            room.applyAction(
                                    player.getId(),
                                    new GameAction(
                                            ActionType.REROLL, player.getId(), null, null, null, null, null, null));
                        }
                        return null;
                    })
                    .toList();
            executor.invokeAll(tasks).forEach(future -> {
                try {
                    future.get();
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            });
        }

        assertTrue(player.getGold() >= 0);
        assertEquals(player.getId(), room.getPlayer(player.getId()).getId());
        assertNotNull(room.getState().players().get(player.getId()));
    }
}
