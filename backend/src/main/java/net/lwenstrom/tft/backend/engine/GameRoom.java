package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.data.DataLoader;
import net.lwenstrom.tft.backend.core.model.GameState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    private final String id;
    private GameState currentState;

    private final DataLoader dataLoader;
    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private GamePhase phase = GamePhase.PLANNING;
    private long phaseEndTime;
    private int round = 1;

    // Constants
    private static final long PLANNING_DURATION_MS = 30000;
    private static final long COMBAT_DURATION_MS = 20000;

    private final CombatSystem combatSystem = new CombatSystem();

    public GameRoom(DataLoader dataLoader) {
        this(UUID.randomUUID().toString(), dataLoader);
    }

    public GameRoom(String id, DataLoader dataLoader) {
        this.id = id;
        this.dataLoader = dataLoader;
        // Initial dummy state
        this.currentState = new GameState(id, phase.name(), round, 0, new HashMap<>(), new ArrayList<>());
        startPhase(GamePhase.PLANNING);
    }

    public String getId() {
        return id;
    }

    public Player addPlayer(String name) {
        Player p = new Player(name, dataLoader);
        players.put(p.getId(), p);
        return p;
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public java.util.Collection<Player> getPlayers() {
        return players.values();
    }

    public void tick() {
        long now = System.currentTimeMillis();
        long timeLeft = phaseEndTime - now;

        if (timeLeft <= 0) {
            nextPhase();
        }

        if (phase == GamePhase.COMBAT) {
            combatSystem.simulateTick(this);
        }

        // Update GameState object for sync
        updateGameState(timeLeft);
    }

    private void startPhase(GamePhase newPhase) {
        this.phase = newPhase;
        long duration = (newPhase == GamePhase.PLANNING) ? PLANNING_DURATION_MS : COMBAT_DURATION_MS;
        this.phaseEndTime = System.currentTimeMillis() + duration;

        if (newPhase == GamePhase.PLANNING) {
            round++;
            players.values().forEach(p -> {
                p.gainGold(5);
                p.refreshShop();
            });
        }
    }

    private void nextPhase() {
        if (phase == GamePhase.PLANNING) {
            startPhase(GamePhase.COMBAT);
        } else {
            startPhase(GamePhase.PLANNING);
        }
    }

    private void updateGameState(long timeLeft) {
        // Create DTOs from internal state
        // This is where we would map Player -> PlayerState
        this.currentState = new GameState(id, phase.name(), round, Math.max(0, timeLeft), new HashMap<>(),
                new ArrayList<>());
    }

    public GameState getState() {
        return currentState;
    }

    public enum GamePhase {
        PLANNING, COMBAT, END
    }
}
