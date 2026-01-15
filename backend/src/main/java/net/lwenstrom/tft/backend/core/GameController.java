package net.lwenstrom.tft.backend.core;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
        room.setEventListener(event -> {
            messagingTemplate.convertAndSend("/topic/room/" + room.getId() + "/event", event);
        });

        // Add 7 bots
        for (int i = 0; i < 7; i++) {
            room.addBot();
        }

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

    @MessageMapping("/room/{id}/add-bot")
    public void addBot(@DestinationVariable String id) {
        GameRoom room = gameEngine.getRoom(id);
        if (room != null) {
            room.addBot();
        }
    }

    @MessageMapping("/room/{id}/action")
    public void handleAction(@DestinationVariable String id, @Payload GameAction action) {
        GameRoom room = gameEngine.getRoom(id);
        if (room != null) {
            Player p = room.getPlayer(action.playerId());
            if (p == null) return;

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

    public record RoomRequest(String roomId, String playerName) {}
}
