package net.lwenstrom.tft.backend.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameEngine gameEngine;
    private final DataLoader dataLoader;
    private final GameModeRegistry gameModeRegistry;
    private final Map<String, SessionPlayer> sessionPlayers = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = GameConstants.TICK_RATE_MS)
    public void tick() {
        gameEngine.tick();
        gameEngine.getActiveRooms().forEach(room -> {
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
        });
    }

    @GetMapping("/api/traits")
    public List<TraitMetadata> getTraits(@RequestParam(required = false) String mode) {
        var resolvedMode = mode != null ? GameMode.fromString(mode) : gameModeRegistry.getDefaultMode();
        return dataLoader.getTraitMetadata(resolvedMode);
    }

    @GetMapping("/api/mode")
    public GameMode getMode() {
        return gameModeRegistry.getDefaultMode();
    }

    @MessageMapping("/create")
    public void createRoom(@Payload RoomRequest request, @Header("simpSessionId") String sessionId) {
        var room = gameEngine.createRoom(request.roomId());
        configureCombatResultListener(room);
        addPlayerForSession(
                room, request.playerName(), request.analyticsClientId(), request.reconnectToken(), sessionId);
        broadcastRoomState(room);
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
                                "unitName",
                                e.getValue().unitName(),
                                "definitionId",
                                e.getValue().definitionId(),
                                "ownerId",
                                e.getValue().ownerId(),
                                "damage",
                                e.getValue().damage(),
                                "healing",
                                e.getValue().healing(),
                                "shielding",
                                e.getValue().shielding())));
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
    public void joinRoom(@Payload RoomRequest request, @Header("simpSessionId") String sessionId) {
        var room = gameEngine.getRoom(request.roomId());
        if (room == null) {
            return;
        }

        var existingSessionPlayer = resolveSessionPlayer(room.getId(), sessionId);
        if (existingSessionPlayer != null) {
            broadcastRoomState(room);
            return;
        }

        var rejoiningPlayer = room.reconnectPlayer(request.reconnectToken());
        if (rejoiningPlayer.isPresent()) {
            bindSession(room.getId(), rejoiningPlayer.get().getId(), sessionId);
            broadcastRoomState(room);
            return;
        }

        if (!addPlayerForSession(
                room, request.playerName(), request.analyticsClientId(), request.reconnectToken(), sessionId)) {
            log.info("Rejected join for room {} because it is not accepting players.", room.getId());
            return;
        }

        broadcastRoomState(room);
    }

    @MessageMapping("/leave")
    public void leaveRoom(@Payload RoomRequest request, @Header("simpSessionId") String sessionId) {
        var sessionPlayer = resolveSessionPlayer(request.roomId(), sessionId);
        if (sessionPlayer == null) return;

        var room = gameEngine.getRoom(sessionPlayer.roomId());
        if (room != null) {
            sessionPlayers.remove(sessionId);
            room.disconnectPlayer(sessionPlayer.playerId());
            broadcastRoomState(room);
        }
    }

    @MessageMapping("/abandon")
    public void abandonRoom(@Payload RoomRequest request, @Header("simpSessionId") String sessionId) {
        var sessionPlayer = resolveSessionPlayer(request.roomId(), sessionId);
        if (sessionPlayer == null) return;

        sessionPlayers.remove(sessionId);
        var room = gameEngine.getRoom(sessionPlayer.roomId());
        if (room != null && room.abandonPlayer(sessionPlayer.playerId())) {
            broadcastRoomState(room);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        disconnectSession(event.getSessionId());
    }

    @MessageMapping("/start")
    public void startRoom(@Payload RoomRequest request, @Header("simpSessionId") String sessionId) {
        log.info("Received start request for room: {} from player: {}", request.roomId(), request.playerName());
        var room = gameEngine.getRoom(request.roomId());
        if (room == null) {
            log.info("Room not found.");
            return;
        }

        var player = resolveBoundPlayer(room, sessionId);
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

    @MessageMapping("/room/{id}/add-bot")
    public void addBot(@DestinationVariable String id, @Header("simpSessionId") String sessionId) {
        var room = gameEngine.getRoom(id);
        if (room == null) return;

        var player = resolveBoundPlayer(room, sessionId);
        if (player == null) return;

        if (!room.getState().hostId().equals(player.getId())) {
            log.info("Player is not host. Add bot denied.");
            return;
        }

        if (room.addBot().isPresent()) {
            broadcastRoomState(room);
        } else {
            log.info("Rejected add bot for room {} because it is not accepting players.", room.getId());
        }
    }

    @MessageMapping("/room/{id}/action")
    public void handleAction(
            @DestinationVariable String id, @Payload GameAction action, @Header("simpSessionId") String sessionId) {
        var room = gameEngine.getRoom(id);
        if (room == null) return;

        var sessionPlayer = resolveSessionPlayer(id, sessionId);
        if (sessionPlayer == null || !sessionPlayer.playerId().equals(action.playerId())) {
            log.warn("Rejected action for unbound or mismatched player.");
            return;
        }

        var player = room.getPlayer(sessionPlayer.playerId());
        if (player == null) {
            log.warn("Player not found in room.");
            return;
        }

        processAction(room, player, action);
        room.refreshState();
        broadcastRoomState(room);
    }

    @MessageMapping("/room/{id}/mode")
    public void changeRoomMode(
            @DestinationVariable String id,
            @Payload ModeChangeRequest request,
            @Header("simpSessionId") String sessionId) {
        var room = gameEngine.getRoom(id);
        if (room == null) return;

        var player = resolveBoundPlayer(room, sessionId);
        if (player == null) return;

        if (!room.getState().hostId().equals(player.getId())) {
            log.info("Player is not host. Mode change denied.");
            return;
        }

        if (room.setGameMode(request.gameMode())) {
            broadcastRoomState(room);
        }
    }

    private void processAction(GameRoom room, Player player, GameAction action) {
        switch (action.type()) {
            case BUY -> player.buyUnit(action.shopIndex());
            case REROLL -> player.refreshShop();
            case EXP -> handleExpPurchase(player);
            case MOVE -> handleMove(room, player, action);
            case SELL -> handleSell(room, player, action);
            case LOCK -> player.setShopLocked(!player.isShopLocked());
            case COLLECT_ORB -> room.collectOrb(player.getId(), action.orbId());
            case READY_FOR_COMBAT -> room.readyForCombat(player.getId());
            case SELECT_AUGMENT -> room.selectAugment(player.getId(), action.augmentId());
        }
    }

    private void handleExpPurchase(Player player) {
        if (player.getLevel() < GameConstants.MAX_PLAYER_LEVEL && player.getGold() >= GameConstants.XP_BUY_COST) {
            player.gainGold(-GameConstants.XP_BUY_COST);
            player.gainXp(GameConstants.XP_BUY_AMOUNT);
        }
    }

    private void handleMove(GameRoom room, Player player, GameAction action) {
        var phase = room.getState().phase();
        if (phase == GamePhase.PLANNING || phase == GamePhase.COMBAT) {
            room.moveUnit(player.getId(), action.unitId(), action.targetX(), action.targetY());
        }
    }

    private void handleSell(GameRoom room, Player player, GameAction action) {
        // Allow selling bench units anytime, but board units only during PLANNING
        player.sellUnit(action.unitId(), room.getState().phase() == GamePhase.PLANNING);
    }

    private void broadcastRoomState(GameRoom room) {
        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getState());
    }

    private boolean addPlayerForSession(
            GameRoom room, String playerName, String analyticsClientId, String reconnectToken, String sessionId) {
        var player = room.tryAddPlayer(playerName, analyticsClientId, reconnectToken);
        player.ifPresent(p -> bindSession(room.getId(), p.getId(), sessionId));
        return player.isPresent();
    }

    private void bindSession(String roomId, String playerId, String sessionId) {
        sessionPlayers
                .entrySet()
                .removeIf(entry -> entry.getValue().roomId().equals(roomId)
                        && entry.getValue().playerId().equals(playerId));
        sessionPlayers.put(sessionId, new SessionPlayer(roomId, playerId));
    }

    private void disconnectSession(String sessionId) {
        var sessionPlayer = sessionPlayers.remove(sessionId);
        if (sessionPlayer == null) {
            return;
        }
        var room = gameEngine.getRoom(sessionPlayer.roomId());
        if (room != null) {
            room.disconnectPlayer(sessionPlayer.playerId());
            broadcastRoomState(room);
        }
    }

    private SessionPlayer resolveSessionPlayer(String roomId, String sessionId) {
        var sessionPlayer = sessionPlayers.get(sessionId);
        if (sessionPlayer == null || !sessionPlayer.roomId().equals(roomId)) {
            return null;
        }
        return sessionPlayer;
    }

    private Player resolveBoundPlayer(GameRoom room, String sessionId) {
        var sessionPlayer = resolveSessionPlayer(room.getId(), sessionId);
        if (sessionPlayer == null) {
            return null;
        }
        return room.getPlayer(sessionPlayer.playerId());
    }

    public record RoomRequest(String roomId, String playerName, String analyticsClientId, String reconnectToken) {
        public RoomRequest(String roomId, String playerName) {
            this(roomId, playerName, null, null);
        }
    }

    public record ModeChangeRequest(String playerName, GameMode gameMode) {}

    private record SessionPlayer(String roomId, String playerId) {}
}
