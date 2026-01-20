package net.lwenstrom.tft.backend.core;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.tft.backend.core.engine.GameEngine;
import net.lwenstrom.tft.backend.core.engine.GameRoom;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.model.GameAction;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameEngine gameEngine;
    private final DataLoader dataLoader;
    private final GameModeRegistry gameModeRegistry;

    @Scheduled(fixedRate = 100)
    public void tick() {
        gameEngine.tick();
        gameEngine.getActiveRooms().forEach(room -> {
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        });
    }

    @GetMapping("/api/traits")
    public List<Object> getTraits() {
        return dataLoader.getTraitMetadata();
    }

    @GetMapping("/api/mode")
    public GameMode getMode() {
        return gameModeRegistry.getActiveMode();
    }

    @MessageMapping("/create")
    public void createRoom(@Payload RoomRequest request) {
        GameRoom room = gameEngine.createRoom(request.roomId());
        // Do not add bots initially

        joinRoom(new RoomRequest(room.getId(), request.playerName()));
    }

    @MessageMapping("/join")
    public void joinRoom(@Payload RoomRequest request) {
        GameRoom room = gameEngine.getRoom(request.roomId());
        if (room != null) {
            room.addPlayer(request.playerName());
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        }
    }

    @MessageMapping("/leave")
    public void leaveRoom(@Payload RoomRequest request) {
        GameRoom room = gameEngine.getRoom(request.roomId());
        if (room != null) {
            room.removePlayer(request.playerName()); // Assuming playerName is used as ID or we have ID mapping
            // Ideally request should send playerId if possible, or we assume name is unique
            // per room for now.
            // Using player name as ID for simplicity in this MVP as seen in addPlayer
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        }
    }

    @MessageMapping("/start")
    public void startRoom(@Payload RoomRequest request) {
        log.info("Received start request for room: {} from player: {}", request.roomId(), request.playerName());
        GameRoom room = gameEngine.getRoom(request.roomId());
        if (room != null) {
            // Find player by name to get their ID
            Player player = room.getPlayers().stream()
                    .filter(p -> p.getName().equals(request.playerName()))
                    .findFirst()
                    .orElse(null);

            if (player != null) {
                log.info("Found player: {} ID: {} Host ID: {}", player.getName(), player.getId(),
                        room.getState().hostId());
                if (room.getState().hostId().equals(player.getId())) {
                    log.info("Host verified. Starting match.");
                    room.startMatch();
                    messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
                } else {
                    log.info("Player is not host.");
                }
            } else {
                log.info("Player not found in room.");
            }
        } else {
            log.info("Room not found.");
        }
    }

    @MessageMapping("/room/{id}/add-bot")
    public void addBot(@DestinationVariable String id) {
        GameRoom room = gameEngine.getRoom(id);
        if (room != null) {
            room.addBot();
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        }
    }

    @MessageMapping("/room/{id}/action")
    public void handleAction(@DestinationVariable String id, @Payload GameAction action) {
        GameRoom room = gameEngine.getRoom(id);
        if (room != null) {
            Player p = room.getPlayer(action.playerId());
            if (p == null)
                return;

            switch (action.type()) {
                case BUY -> {
                    p.buyUnit(action.shopIndex());
                }
                case REROLL -> {
                    p.refreshShop();
                }
                case EXP -> {
                    if (p.getGold() >= 4) {
                        p.gainGold(-4);
                        p.gainXp(4);
                    }
                }
                case MOVE -> {
                    if (room.getState().phase() == GamePhase.PLANNING) {
                        room.moveUnit(action.playerId(), action.unitId(), action.targetX(), action.targetY());
                    }
                }
                case SELL -> {
                    // TODO: Implement
                }
                case LOCK -> {
                    // TODO: Implement
                }
            }
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        }
    }

    public record RoomRequest(String roomId, String playerName) {
    }
}
