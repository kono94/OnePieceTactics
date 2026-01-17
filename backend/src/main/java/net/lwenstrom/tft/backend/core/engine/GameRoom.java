package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.core.model.GameState;
import net.lwenstrom.tft.backend.core.model.GameState.PlayerState;

public class GameRoom {
    private final String id;
    private String hostId;
    private GameState currentState;

    private final DataLoader dataLoader;
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> currentMatchups = new ConcurrentHashMap<>();
    private final List<List<Player>> activeCombats = new ArrayList<>();

    private GamePhase phase = GamePhase.LOBBY;
    private long phaseEndTime;
    private int round = 0;

    private Consumer<Object> eventListener;

    // Constants
    // Constants
    private long currentPhaseDuration;

    private final GameModeRegistry gameModeRegistry;
    private final TraitManager traitManager;
    private final CombatSystem combatSystem;

    public GameRoom(String id, DataLoader dataLoader, GameModeRegistry gameModeRegistry) {
        this.id = id;
        this.dataLoader = dataLoader;
        this.gameModeRegistry = gameModeRegistry;

        this.traitManager = new TraitManager();
        gameModeRegistry.getActiveProvider().registerTraitEffects(this.traitManager);
        this.combatSystem = new CombatSystem(traitManager);

        this.round = 0;

        this.currentState = new GameState(
                id,
                null,
                phase,
                round,
                0,
                0,
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                gameModeRegistry.getActiveMode());

        // In LOBBY, no timer runs until startMatch is called
        this.phaseEndTime = Long.MAX_VALUE;
    }

    public String getId() {
        return id;
    }

    public GameState getState() {
        return currentState;
    }

    public void setEventListener(Consumer<Object> eventListener) {
        this.eventListener = eventListener;
    }

    public Player addPlayer(String name) {
        Player player = new Player(name, dataLoader);
        players.put(player.getId(), player);

        if (hostId == null) {
            hostId = player.getId();
        }

        player.refreshShop();
        updateGameState(0); // Time remaining generic for lobby
        return player;
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        if (playerId.equals(hostId)) {
            // Assign new host
            hostId = players.isEmpty() ? null : players.keySet().iterator().next();
        }
        updateGameState(0);
    }

    public void startMatch() {
        if (phase != GamePhase.LOBBY) return;

        // Fill with bots if needed (up to 8)
        int currentCount = players.size();
        for (int i = 0; i < 8 - currentCount; i++) {
            addBot();
        }

        startPhase(GamePhase.PLANNING);
    }

    public void addBot() {
        String botId = "Bot-" + UUID.randomUUID().toString().substring(0, 4);
        Player bot = new Player(botId, dataLoader);
        players.put(bot.getId(), bot);
        bot.refreshShop();
        refreshBotRoster(bot);
        updateGameState(phaseEndTime - System.currentTimeMillis());
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public void moveUnit(String playerId, String unitId, int x, int y) {
        Player p = players.get(playerId);
        if (p != null && phase == GamePhase.PLANNING) {
            p.moveUnit(unitId, x, y);
        }
    }

    public void tick() {
        if (phase == GamePhase.LOBBY) return;

        long now = System.currentTimeMillis();
        if (now >= phaseEndTime) {
            nextPhase();
        }

        if (phase == GamePhase.COMBAT) {
            combatSystem.simulateTick(new ArrayList<>(players.values()));
        }

        updateGameState(phaseEndTime - now);
    }

    private void nextPhase() {
        if (phase == GamePhase.PLANNING) {
            startPhase(GamePhase.COMBAT);
        } else {
            startPhase(GamePhase.PLANNING);
        }
    }

    private void startPhase(GamePhase newPhase) {
        this.phase = newPhase;

        if (phase == GamePhase.PLANNING) {
            round++;
            players.values().forEach(p -> {
                p.gainGold(5 + Math.min(p.getGold() / 10, 5));
                p.gainXp(2);
                p.refreshShop();
                if (p.getId().startsWith("Bot-")) {
                    refreshBotRoster(p);
                }
            });
        }

        this.currentPhaseDuration = calculatePhaseDuration(newPhase, round);
        this.phaseEndTime = System.currentTimeMillis() + currentPhaseDuration;
        updateGameState(currentPhaseDuration);

        if (phase == GamePhase.COMBAT) {
            activeCombats.clear();
            List<Player> shuffled = new ArrayList<>(players.values());
            Collections.shuffle(shuffled);
            for (int i = 0; i < shuffled.size() - 1; i += 2) {
                activeCombats.add(List.of(shuffled.get(i), shuffled.get(i + 1)));
            }
            activeCombats.forEach(combatSystem::startCombat);
        }
    }

    private void refreshBotRoster(Player bot) {
        bot.removeAllUnits();

        int unitCount = Math.min((round / 2) + 1, 7); // Max 7 for first row
        List<UnitDefinition> available = dataLoader.getAllUnits();
        for (int i = 0; i < unitCount; i++) {
            UnitDefinition def = available.get((int) (Math.random() * available.size()));
            bot.addUnitToBoard(def, i, 0);
        }
    }

    private void updateGameState(long timeLeft) {
        Map<String, PlayerState> playerStates = players.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toState()));

        this.currentState = new GameState(
                id,
                hostId,
                phase,
                round,
                timeLeft,
                calculatePhaseDuration(phase, round),
                playerStates,
                new HashMap<>(),
                new ArrayList<>(),
                gameModeRegistry.getActiveMode());
    }

    private long calculatePhaseDuration(GamePhase phase, int round) {
        // Base 10s + 2s per round index (0-based)
        // Round 1: 10 + 0 = 10s
        // Round 2: 10 + 2 = 12s
        return 10000 + (round - 1) * 2000;
    }
}
