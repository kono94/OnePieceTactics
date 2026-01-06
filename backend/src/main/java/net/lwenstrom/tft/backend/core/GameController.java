package net.lwenstrom.tft.backend.core;

import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.engine.GameEngine;
import net.lwenstrom.tft.backend.core.model.GameAction;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin
@RequiredArgsConstructor
public class GameController {

    private final GameEngine gameEngine;
    private final SimpMessagingTemplate messagingTemplate;

    // Ticking the game engine every 100ms
    @Scheduled(fixedRate = 100)
    public void tick() {
        gameEngine.tick();
        // Broadcast state updates to each room's topic
        gameEngine.getAllRooms().forEach(room -> {
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        });
    }

    public record RoomRequest(String roomId, String playerName) {}

    @MessageMapping("/create")
    public void createRoom(@Payload RoomRequest request) {
        // Prevent overwriting if exists, or just return existing?
        // For simplicity, if exists, just join it (or we could error)
        var room = gameEngine.getRoom(request.roomId());
        if (room == null) {
            room = gameEngine.createRoom(request.roomId());
            // Add bots only on creation
            for (int i = 0; i < 7; i++) room.addBot();
        }

        room.addPlayer(request.playerName());
        // Immediate update to the room
        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
    }

    @MessageMapping("/join")
    public void joinRoom(@Payload RoomRequest request) {
        var room = gameEngine.getRoom(request.roomId());
        if (room != null) {
            room.addPlayer(request.playerName());
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        }
    }

    @MessageMapping("/room/{roomId}/action")
    public void handleAction(@DestinationVariable String roomId, @Payload GameAction action) {
        var room = gameEngine.getRoom(roomId);
        if (room == null) return;

        var p = room.getPlayer(action.playerId());
        if (p == null) return;

        switch (action.type()) {
            case "BUY" -> p.buyUnit(action.shopIndex());
            case "REROLL" -> p.refreshShop();
            case "EXP" -> {
                if (p.getGold() >= 4) {
                    p.gainGold(-4);
                    p.gainXp(4);
                }
            }
            case "MOVE" -> {
                if ("PLANNING".equals(room.getState().phase())) {
                    p.moveUnit(action.unitId(), action.targetX(), action.targetY());
                }
            }
        }

        // Broadcast new state immediately after action
        messagingTemplate.convertAndSend("/topic/room/" + roomId, room.getState());
    }
}
