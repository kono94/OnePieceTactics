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
    private int round = 0;

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
        // Initial dummy state (will be updated)
        this.currentState = new GameState(id, phase.name(), round, 0, new HashMap<>(), new ArrayList<>());
        startPhase(GamePhase.PLANNING);
        updateGameState(PLANNING_DURATION_MS); // Ensure state reflects initial phase
    }

    public String getId() {
        return id;
    }

    public Player addPlayer(String name) {
        Player p = new Player(name, dataLoader);
        // Initial setup
        p.refreshShop();
        players.put(p.getId(), p);
        updateGameState(phaseEndTime - System.currentTimeMillis());
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
                p.gainXp(2);
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

    public void addBot() {
        Player bot = addPlayer("Bot-" + UUID.randomUUID().toString().substring(0, 4));
        var allUnits = dataLoader.getAllUnits();
        if (!allUnits.isEmpty()) {
            var unitDef = allUnits.get((int) (Math.random() * allUnits.size()));
            var unit = new net.lwenstrom.tft.backend.core.impl.StandardGameUnit(unitDef);
            unit.setOwnerId(bot.getId());
            // Simple random position for bot (on their board half?)
            // Just putting them randomly for now to ensure they are seen
            unit.setPosition((int) (Math.random() * 8), (int) (Math.random() * 8));
            bot.getBoardUnits().add(unit);
        }
    }

    private void updateGameState(long timeLeft) {
        Map<String, GameState.PlayerState> playerStates = new HashMap<>();
        for (Player p : players.values()) {
            playerStates.put(p.getId(), new GameState.PlayerState(
                    p.getId(),
                    p.getName(),
                    p.getHealth(),
                    p.getGold(),
                    p.getLevel(),
                    p.getXp(),
                    new ArrayList<>(p.getBench()),
                    new ArrayList<>(p.getBoardUnits()),
                    new ArrayList<>(), // Active traits not yet implemented
                    new ArrayList<>(p.getShop())));
        }
        this.currentState = new GameState(id, phase.name(), round, Math.max(0, timeLeft), playerStates,
                new ArrayList<>());
    }

    public GameState getState() {
        return currentState;
    }

    public enum GamePhase {
        PLANNING, COMBAT, END
    }
}
