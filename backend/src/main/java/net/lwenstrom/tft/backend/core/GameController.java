package net.lwenstrom.tft.backend.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.tft.backend.core.engine.CombatSystem;
import net.lwenstrom.tft.backend.core.engine.GameEngine;
import net.lwenstrom.tft.backend.core.engine.GameRoom;
import net.lwenstrom.tft.backend.core.engine.Player;
import net.lwenstrom.tft.backend.core.model.GameAction;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.core.model.TraitMetadata;
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

    @Scheduled(fixedRate = GameConstants.TICK_RATE_MS)
    public void tick() {
        gameEngine.tick();
        gameEngine.getActiveRooms().forEach(room -> {
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        });
    }

    @GetMapping("/api/traits")
    public List<TraitMetadata> getTraits() {
        return dataLoader.getTraitMetadata();
    }

    @GetMapping("/api/mode")
    public GameMode getMode() {
        return gameModeRegistry.getActiveMode();
    }

    @MessageMapping("/create")
    public void createRoom(@Payload RoomRequest request) {
        var room = gameEngine.createRoom(request.roomId());
        configureCombatResultListener(room);
        joinRoom(new RoomRequest(room.getId(), request.playerName()));
    }

    private void configureCombatResultListener(GameRoom room) {
        room.setCombatResultListener(this::handleCombatResult);
    }

    private void handleCombatResult(
            String roomId,
            String winnerId,
            String loserId,
            List<String> participantIds,
            Map<String, CombatSystem.DamageEntry> damageLog) {
        var damageMap = buildDamageMap(damageLog);
        var payload = buildCombatResultPayload(winnerId, loserId, participantIds, damageMap);
        var event = Map.of("type", "COMBAT_RESULT", "payload", payload);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/event", (Object) event);
    }

    private Map<String, Map<String, Object>> buildDamageMap(Map<String, CombatSystem.DamageEntry> damageLog) {
        return damageLog.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "name",
                                e.getValue().unitName(),
                                "damage",
                                e.getValue().damage())));
    }

    private Map<String, Object> buildCombatResultPayload(
            String winnerId, String loserId, List<String> participantIds, Map<String, Map<String, Object>> damageMap) {
        return Map.of(
                "winnerId",
                winnerId != null ? winnerId : "",
                "loserId",
                loserId != null ? loserId : "",
                "participantIds",
                participantIds,
                "damageLog",
                damageMap);
    }

    @MessageMapping("/join")
    public void joinRoom(@Payload RoomRequest request) {
        var room = gameEngine.getRoom(request.roomId());
        if (room != null) {
            room.addPlayer(request.playerName());
            broadcastRoomState(room);
        }
    }

    @MessageMapping("/leave")
    public void leaveRoom(@Payload RoomRequest request) {
        var room = gameEngine.getRoom(request.roomId());
        if (room != null) {
            room.removePlayer(request.playerName());
            broadcastRoomState(room);
        }
    }

    @MessageMapping("/start")
    public void startRoom(@Payload RoomRequest request) {
        log.info("Received start request for room: {} from player: {}", request.roomId(), request.playerName());
        var room = gameEngine.getRoom(request.roomId());
        if (room == null) {
            log.info("Room not found.");
            return;
        }

        var player = findPlayerByName(room, request.playerName());
        if (player == null) {
            log.info("Player not found in room.");
            return;
        }

        log.info(
                "Found player: {} ID: {} Host ID: {}",
                player.getName(),
                player.getId(),
                room.getState().hostId());

        if (room.getState().hostId().equals(player.getId())) {
            log.info("Host verified. Starting match.");
            room.startMatch();
            broadcastRoomState(room);
        } else {
            log.info("Player is not host.");
        }
    }

    private Player findPlayerByName(GameRoom room, String playerName) {
        return room.getPlayers().stream()
                .filter(p -> p.getName().equals(playerName))
                .findFirst()
                .orElse(null);
    }

    @MessageMapping("/room/{id}/add-bot")
    public void addBot(@DestinationVariable String id) {
        var room = gameEngine.getRoom(id);
        if (room != null) {
            room.addBot();
            broadcastRoomState(room);
        }
    }

    @MessageMapping("/room/{id}/action")
    public void handleAction(@DestinationVariable String id, @Payload GameAction action) {
        var room = gameEngine.getRoom(id);
        if (room == null) return;

        var player = room.getPlayer(action.playerId());
        if (player == null) {
            log.warn("Player not found in room.");
            return;
        }

        processAction(room, player, action);
        broadcastRoomState(room);
    }

    private void processAction(GameRoom room, Player player, GameAction action) {
        switch (action.type()) {
            case BUY -> player.buyUnit(action.shopIndex());
            case REROLL -> player.refreshShop();
            case EXP -> handleExpPurchase(player);
            case MOVE -> handleMove(room, action);
            case SELL -> handleSell(room, player, action);
            case LOCK -> player.setShopLocked(!player.isShopLocked());
            case COLLECT_ORB -> room.collectOrb(action.playerId(), action.orbId());
        }
    }

    private void handleExpPurchase(Player player) {
        if (player.getGold() >= GameConstants.XP_BUY_COST) {
            player.gainGold(-GameConstants.XP_BUY_COST);
            player.gainXp(GameConstants.XP_BUY_AMOUNT);
        }
    }

    private void handleMove(GameRoom room, GameAction action) {
        var phase = room.getState().phase();
        if (phase == GamePhase.PLANNING || phase == GamePhase.COMBAT) {
            room.moveUnit(action.playerId(), action.unitId(), action.targetX(), action.targetY());
        }
    }

    private void handleSell(GameRoom room, Player player, GameAction action) {
        // Allow selling bench units anytime, but board units only during PLANNING
        player.sellUnit(action.unitId(), room.getState().phase() == GamePhase.PLANNING);
    }

    private void broadcastRoomState(GameRoom room) {
        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
    }

    public record RoomRequest(String roomId, String playerName) {}
}
