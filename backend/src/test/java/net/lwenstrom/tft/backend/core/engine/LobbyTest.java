package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.createSeededRandomProvider;
import static net.lwenstrom.tft.backend.test.TestHelpers.createTestClock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LobbyTest {

    @Mock
    private DataLoader dataLoader;

    @Mock
    private GameModeRegistry gameModeRegistry;

    @Mock
    private GameModeProvider gameModeProvider;

    private GameRoom gameRoom;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(gameModeRegistry.getActiveMode()).thenReturn(GameMode.ONEPIECE);
        when(gameModeRegistry.getActiveProvider()).thenReturn(gameModeProvider);

        gameRoom =
                new GameRoom("room-1", dataLoader, gameModeRegistry, createTestClock(), createSeededRandomProvider());
    }

    @Test
    public void testRoomStartsWithLobbyPhase() {
        assertEquals(GamePhase.LOBBY, gameRoom.getState().phase());
        assertNull(gameRoom.getState().hostId());
    }

    @Test
    public void testFirstPlayerIsHost() {
        Player p1 = gameRoom.addPlayer("Player1");
        assertEquals(p1.getId(), gameRoom.getState().hostId());

        gameRoom.addPlayer("Player2");
        assertEquals(p1.getId(), gameRoom.getState().hostId()); // Still Player1
    }

    @Test
    public void testHostMigration() {
        Player p1 = gameRoom.addPlayer("Player1");
        Player p2 = gameRoom.addPlayer("Player2");

        gameRoom.removePlayer(p1.getId());
        assertEquals(p2.getId(), gameRoom.getState().hostId());

        gameRoom.removePlayer(p2.getId());
        assertNull(gameRoom.getState().hostId());
    }

    @Test
    public void testStartMatch() {
        // Mock data loader for bots
        UnitDefinition dummyUnit =
                new UnitDefinition("unit-1", "Luffy", 1, 100, 0, 10, 0, 0, 0, 1.0f, 1, List.of("Pirate"), null);
        when(dataLoader.getAllUnits()).thenReturn(List.of(dummyUnit));

        gameRoom.addPlayer("Host");
        gameRoom.startMatch();

        assertEquals(GamePhase.PLANNING, gameRoom.getState().phase());
        assertEquals(1, gameRoom.getState().round());
        assertEquals(8, gameRoom.getState().players().size()); // 1 Human + 7 Bots
    }
}
