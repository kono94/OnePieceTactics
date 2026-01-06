package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.model.GameState;

public class GameRoom {
    private final String id;
    private GameState currentState;

    private final DataLoader dataLoader;
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> currentMatchups = new ConcurrentHashMap<>();
    private final List<List<Player>> activeCombats = new ArrayList<>();

    private GamePhase phase = GamePhase.PLANNING;
    private long phaseEndTime;
    private int round = 0;

    // Constants
    private static final long PLANNING_DURATION_MS = 30000;
    private static final long COMBAT_DURATION_MS = 60000;

    private final CombatSystem combatSystem = new CombatSystem();

    public GameRoom(DataLoader dataLoader) {
        this(UUID.randomUUID().toString(), dataLoader);
    }

    public GameRoom(String id, DataLoader dataLoader) {
        this.id = id;
        this.dataLoader = dataLoader;
        // Initial dummy state (will be updated)
        this.currentState = new GameState(id, phase.name(), round, 0, new HashMap<>(), new HashMap<>(),
                new ArrayList<>());
        startPhase(GamePhase.PLANNING);
        updateGameState(PLANNING_DURATION_MS); // Ensure state reflects initial phase
    }

    public String getId() {
        return id;
    }

    public Player addPlayer(String name) {
        var p = new Player(name, dataLoader);
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
        var now = System.currentTimeMillis();
        var timeLeft = phaseEndTime - now;

        if (timeLeft <= 0) {
            // Force end any remaining combats
            if (phase == GamePhase.COMBAT && !activeCombats.isEmpty()) {
                for (List<Player> pair : activeCombats) {
                    handleCombatEnd(true, null, pair);
                }
                activeCombats.clear();
            }
            nextPhase();
        }

        if (phase == GamePhase.COMBAT) {
            var it = activeCombats.iterator();
            while (it.hasNext()) {
                var pair = it.next();
                CombatSystem.CombatResult result = combatSystem.simulateTick(pair);
                if (result.ended()) {
                    handleCombatEnd(false, result, pair);
                    it.remove();
                }
            }

            if (activeCombats.isEmpty()) {
                // All combats finished early
                this.phaseEndTime = System.currentTimeMillis(); // End phase immediately
                nextPhase();
                return;
            }
        }

        // Update GameState object for sync
        updateGameState(timeLeft);
    }

    private void startPhase(GamePhase newPhase) {
        // Handle transitions
        if (this.phase == GamePhase.COMBAT && newPhase != GamePhase.COMBAT) {
            combatSystem.endCombat(players.values());
            activeCombats.clear();
            currentMatchups.clear();
            players.values().forEach(p -> p.setBoardLocked(false));
        }

        this.phase = newPhase;
        var duration = (newPhase == GamePhase.PLANNING) ? PLANNING_DURATION_MS : COMBAT_DURATION_MS;
        this.phaseEndTime = System.currentTimeMillis() + duration;

        if (newPhase == GamePhase.PLANNING) {
            round++;
            players.values().forEach(p -> {
                p.gainGold(5);
                p.gainXp(2);
                p.refreshShop();
            });
        } else if (newPhase == GamePhase.COMBAT) {
            players.values().forEach(p -> p.setBoardLocked(true));

            // Create Pairings
            List<Player> pList = new ArrayList<>(players.values());
            java.util.Collections.shuffle(pList);

            for (int i = 0; i < pList.size(); i += 2) {
                if (i + 1 < pList.size()) {
                    Player p1 = pList.get(i);
                    Player p2 = pList.get(i + 1);
                    List<Player> pair = java.util.List.of(p1, p2);

                    currentMatchups.put(p1.getId(), p2.getId());
                    currentMatchups.put(p2.getId(), p1.getId());

                    activeCombats.add(pair);
                    combatSystem.startCombat(pair);
                    System.out.println("Started combat between " + p1.getName() + " and " + p2.getName());
                } else {
                    // Odd player out (Ghost? For now just do nothing or clone)
                    // Leaving sitting out
                    System.out.println("Player sitting out: " + pList.get(i).getName());
                }
            }
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
        var bot = addPlayer("Bot-" + UUID.randomUUID().toString().substring(0, 4));
        var allUnits = dataLoader.getAllUnits();
        if (!allUnits.isEmpty()) {
            var unitDef = allUnits.get((int) (Math.random() * allUnits.size()));
            var unit = new net.lwenstrom.tft.backend.core.engine.StandardGameUnit(unitDef);
            unit.setOwnerId(bot.getId());
            // Simple random position for bot (on their board half?)
            // Just putting them randomly for now to ensure they are seen
            unit.setPosition((int) (Math.random() * 7), (int) (Math.random() * 4));
            bot.getBoardUnits().add(unit);
        }
    }

    private void updateGameState(long timeLeft) {
        var playerStates = new HashMap<String, GameState.PlayerState>();
        for (var p : players.values()) {
            playerStates.put(
                    p.getId(),
                    new GameState.PlayerState(
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
                new HashMap<>(currentMatchups), new ArrayList<>());
    }

    public GameState getState() {
        return currentState;
    }

    private void handleCombatEnd(boolean isTimeout, CombatSystem.CombatResult result, List<Player> participants) {
        Player winner = null;
        boolean draw = false;

        if (isTimeout || result == null) {
            // Timeout: Winner is player with highest total HP on board
            int maxHp = -1;

            for (Player p : participants) {
                int totalHp = p.getBoardUnits().stream()
                        .mapToInt(net.lwenstrom.tft.backend.core.model.GameUnit::getCurrentHealth)
                        .sum();
                if (totalHp > maxHp) {
                    maxHp = totalHp;
                    winner = p;
                    draw = false;
                } else if (totalHp == maxHp) {
                    draw = true;
                }
            }
        } else {
            // Elimination: Use result
            if (result.winnerId() != null) {
                winner = players.get(result.winnerId());
            } else {
                draw = true;
            }
        }

        if (!draw && winner != null) {
            final Player finalWinner = winner;
            // Calculate Damage
            int damage = 2 + finalWinner.getBoardUnits().size();

            participants.stream()
                    .filter(p -> !p.getId().equals(finalWinner.getId()))
                    .forEach(p -> p.takeDamage(damage));
        }
    }

    public enum GamePhase {
        PLANNING,
        COMBAT,
        END
    }
}
