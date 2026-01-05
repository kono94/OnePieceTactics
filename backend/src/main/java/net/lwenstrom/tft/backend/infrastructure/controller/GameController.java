package net.lwenstrom.tft.backend.infrastructure.controller;

import net.lwenstrom.tft.backend.core.model.GameAction;
import net.lwenstrom.tft.backend.core.model.GameState;
import net.lwenstrom.tft.backend.engine.GameEngine;
import net.lwenstrom.tft.backend.engine.GameRoom;
import net.lwenstrom.tft.backend.engine.Player;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin
public class GameController {

    private final GameEngine gameEngine;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameEngine gameEngine, SimpMessagingTemplate messagingTemplate) {
        this.gameEngine = gameEngine;
        this.messagingTemplate = messagingTemplate;
    }

    // Ticking the game engine every 100ms
    @Scheduled(fixedRate = 100)
    public void tick() {
        gameEngine.tick();
        // Broadcast state updates
        gameEngine.getAllRooms().forEach(room -> {
            // For now, simplify and assume we broadcast 'default' room to the main topic
            if (room.getId().equals("default")) {
                messagingTemplate.convertAndSend("/topic/gamestate", room.getState());
            }
            // Also broadcast to specific room topic if we were fully multi-room
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        });
    }

    @MessageMapping("/join")
    public void joinRoom(@Payload String playerName) {
        // Simplified: Force usage of "default" room
        GameRoom room = gameEngine.getRoom("default");
        if (room == null) {
            room = gameEngine.createRoom("default");
        }
        Player p = room.addPlayer(playerName);

        // DEBUG: Add starter units to the board so the user sees something happening
        java.util.List<net.lwenstrom.tft.backend.core.data.UnitDefinition> allUnits = gameEngine.getDataLoader()
                .getAllUnits();
        if (!allUnits.isEmpty()) {
            // Unit 1
            var u1 = new net.lwenstrom.tft.backend.core.impl.StandardGameUnit(allUnits.get(0));
            u1.setPosition(2, 2);
            // u1.setOwnerId(p.getId()); // Need to set owner if we track it on unit
            p.getBoardUnits().add(u1);

            // Unit 2 (Enemy? or same team? For now same team)
            if (allUnits.size() > 1) {
                var u2 = new net.lwenstrom.tft.backend.core.impl.StandardGameUnit(allUnits.get(1));
                u2.setPosition(5, 5);
                // u2.setOwnerId(p.getId());
                p.getBoardUnits().add(u2);
            }
        }

        // Send back the room ID / private info?
        // For now, just broadcast state to the room topic
        messagingTemplate.convertAndSend("/topic/gamestate", room.getState());
    }

    @MessageMapping("/room/{roomId}/action")
    public void handleAction(@DestinationVariable String roomId, @Payload GameAction action) {
        GameRoom room = gameEngine.getRoom(roomId);
        if (room == null)
            return;

        Player p = room.getPlayer(action.playerId());
        if (p == null)
            return;

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
                // Handle move logic (complex, depends on if on bench or board)
            }
        }

        // Broadcast new state immediately after action
        messagingTemplate.convertAndSend("/topic/room/" + roomId, room.getState());
    }
}
