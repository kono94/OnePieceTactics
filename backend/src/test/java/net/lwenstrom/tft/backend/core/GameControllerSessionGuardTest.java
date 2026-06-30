package net.lwenstrom.tft.backend.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import net.lwenstrom.tft.backend.core.engine.GameEngine;
import net.lwenstrom.tft.backend.core.engine.GameRoom;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.model.ActionType;
import net.lwenstrom.tft.backend.core.model.GameAction;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class GameControllerSessionGuardTest {

    private GameController controller;
    private GameEngine gameEngine;

    @BeforeEach
    void setUp() {
        var dataLoader = TestHelpers.createMockDataLoader();
        var registry = TestHelpers.createMockRegistry();
        var clock = TestHelpers.createTestClock();
        var randomProvider = TestHelpers.createSeededRandomProvider();
        var messagingTemplate = mock(SimpMessagingTemplate.class);

        gameEngine = new GameEngine(dataLoader, registry, clock, randomProvider);
        controller = new GameController(messagingTemplate, gameEngine, dataLoader, registry);
    }

    @Test
    void handleAction_AllowsOwnSessionPlayer() {
        var room = createRoomWithHost();
        var host = findPlayer(room, "Host");

        controller.handleAction(
                room.getId(),
                new GameAction(ActionType.EXP, host.getId(), null, null, null, null, null, null),
                "host-session");

        assertEquals(
                GameConstants.STARTING_GOLD - GameConstants.REROLL_COST - GameConstants.XP_BUY_COST, host.getGold());
        assertEquals(2, host.getLevel());
        assertEquals(2, host.getXp());
    }

    @Test
    void handleAction_RejectsMismatchedPlayerId() {
        var room = createRoomWithHost();
        controller.joinRoom(new GameController.RoomRequest(room.getId(), "Guest"), "guest-session");

        var host = findPlayer(room, "Host");
        var guest = findPlayer(room, "Guest");
        var hostGold = host.getGold();
        var guestGold = guest.getGold();

        controller.handleAction(
                room.getId(),
                new GameAction(ActionType.EXP, guest.getId(), null, null, null, null, null, null),
                "host-session");

        assertEquals(hostGold, host.getGold());
        assertEquals(0, host.getXp());
        assertEquals(guestGold, guest.getGold());
        assertEquals(0, guest.getXp());
    }

    @Test
    void leaveRoom_RemovesSessionBoundPlayer() {
        var room = createRoomWithHost();
        controller.joinRoom(new GameController.RoomRequest(room.getId(), "Guest"), "guest-session");
        var guest = findPlayer(room, "Guest");

        controller.leaveRoom(new GameController.RoomRequest(room.getId(), "wrong-name"), "guest-session");

        assertNull(room.getPlayer(guest.getId()));
        assertNotNull(findPlayer(room, "Host"));
    }

    @Test
    void startRoom_DoesNotAllowNonHostToSpoofHostName() {
        var room = createRoomWithHost();
        controller.joinRoom(new GameController.RoomRequest(room.getId(), "Guest"), "guest-session");

        controller.startRoom(new GameController.RoomRequest(room.getId(), "Host"), "guest-session");

        assertEquals(GamePhase.LOBBY, room.getState().phase());
    }

    @Test
    void changeRoomMode_DoesNotAllowNonHostToSpoofHostName() {
        var room = createRoomWithHost();
        controller.joinRoom(new GameController.RoomRequest(room.getId(), "Guest"), "guest-session");

        controller.changeRoomMode(
                room.getId(), new GameController.ModeChangeRequest("Host", GameMode.POKEMON), "guest-session");

        assertEquals(GameMode.ONEPIECE, room.getState().gameMode());
    }

    @Test
    void handleAction_RejectsReadyForCombatSpoofing() {
        var room = createRoomWithHost();

        controller.startRoom(new GameController.RoomRequest(room.getId(), "Host"), "host-session");
        var host = findPlayer(room, "Host");
        var bot = room.getPlayers().stream().filter(Player::isBot).findFirst().orElseThrow();

        controller.handleAction(
                room.getId(),
                new GameAction(ActionType.READY_FOR_COMBAT, bot.getId(), null, null, null, null, null, null),
                "host-session");

        assertEquals(GamePhase.PLANNING, room.getState().phase());
        assertEquals(host.getId(), room.getState().planningReadyPlayerId());

        controller.handleAction(
                room.getId(),
                new GameAction(ActionType.READY_FOR_COMBAT, host.getId(), null, null, null, null, null, null),
                "host-session");

        assertEquals(GamePhase.COMBAT, room.getState().phase());
    }

    private GameRoom createRoomWithHost() {
        controller.createRoom(new GameController.RoomRequest("session-room", "Host"), "host-session");
        return gameEngine.getRoom("session-room");
    }

    private Player findPlayer(GameRoom room, String name) {
        return room.getPlayers().stream()
                .filter(player -> player.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
