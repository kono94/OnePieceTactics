package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private static final long PLANNING_DURATION_MS = 8000;
    private static final long COMBAT_DURATION_MS = 20000;

    private final CombatSystem combatSystem = new CombatSystem();

    public GameRoom(DataLoader dataLoader) {
        this(UUID.randomUUID().toString(), dataLoader);
    }

    public GameRoom(String id, DataLoader dataLoader) {
        this.id = id;
        this.dataLoader = dataLoader;
        // Initial dummy state (will be updated)
        this.currentState = new GameState(
                id, phase.name(), round, 0, PLANNING_DURATION_MS, new HashMap<>(), new HashMap<>(), new ArrayList<>());
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
                            p.getNextLevelXp(),
                            p.getPlace(), // Added
                            p.getCombatSide(),
                            new ArrayList<>(p.getBench()),
                            new ArrayList<>(p.getBoardUnits()),
                            new ArrayList<>(), // Active traits not yet implemented
                            new ArrayList<>(p.getShop())));
        }

        long totalDuration = (phase == GamePhase.PLANNING) ? PLANNING_DURATION_MS : COMBAT_DURATION_MS;

        this.currentState = new GameState(
                id,
                phase.name(),
                round,
                Math.max(0, timeLeft),
                totalDuration,
                playerStates,
                new HashMap<>(currentMatchups),
                new ArrayList<>());
    }

    // ... startPhase ...

    private void startPhase(GamePhase newPhase) {
        // Handle transitions
        if (this.phase == GamePhase.COMBAT && newPhase != GamePhase.COMBAT) {
            combatSystem.endCombat(players.values());
            activeCombats.clear();
            currentMatchups.clear();
            players.values().forEach(p -> p.setBoardLocked(false));

            // Check if game ended in the previous tick (handleCombatEnd sets phase to END
            // if applicable)
            // But here we are forcefully transitioning.
            // If we are already in END phase (set by handleCombatEnd), we should probably
            // stay there or stop.
            // However, nextPhase() calls startPhase().
        }

        // If game is over, ensure we don't restart cycles unless reset
        if (this.phase == GamePhase.END) {
            // Logic to stop? For now allow re-entry if needed, or just return.
        }

        this.phase = newPhase;
        var duration = (newPhase == GamePhase.PLANNING) ? PLANNING_DURATION_MS : COMBAT_DURATION_MS;
        this.phaseEndTime = System.currentTimeMillis() + duration;

        if (newPhase == GamePhase.PLANNING) {
            round++;
            players.values().stream()
                    .filter(p -> p.getHealth() > 0)
                    .forEach(
                            p -> { // Only alive players gain resources
                                p.gainGold(5);
                                p.gainXp(2);
                                p.refreshShop();

                                if (isBot(p)) {
                                    refreshBotRoster(p);
                                }
                            });
        } else if (newPhase == GamePhase.COMBAT) {
            players.values().forEach(p -> p.setBoardLocked(true));

            // Create Pairings ONLY for ALIVE players
            List<Player> alivePlayers = players.values().stream()
                    .filter(p -> p.getHealth() > 0)
                    .collect(java.util.stream.Collectors.toList());

            java.util.Collections.shuffle(alivePlayers);

            // If 0 or 1 player alive, we probably shouldn't even start combat, or handled
            // in prev phase?
            // If 1 player, they fight a clone or just wait? Standard TFT: ghost or wait.
            // If < 2 players, combat ends immediately in tick().

            for (int i = 0; i < alivePlayers.size(); i += 2) {
                if (i + 1 < alivePlayers.size()) {
                    Player p1 = alivePlayers.get(i);
                    Player p2 = alivePlayers.get(i + 1);
                    List<Player> pair = java.util.List.of(p1, p2);

                    currentMatchups.put(p1.getId(), p2.getId());
                    currentMatchups.put(p2.getId(), p1.getId());

                    activeCombats.add(pair);
                    combatSystem.startCombat(pair);
                    System.out.println("Started combat between " + p1.getName() + " and " + p2.getName());
                } else {
                    // Odd player out
                    System.out.println(
                            "Player sitting out: " + alivePlayers.get(i).getName());
                }
            }
        }
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
            // Calculate Damage - FIXED 5 HP
            int damage = 5;

            Player loser = participants.stream()
                    .filter(p -> !p.getId().equals(finalWinner.getId()))
                    .findFirst()
                    .orElse(null);

            if (loser != null) {
                loser.takeDamage(damage);

                // Check Death
                if (loser.getHealth() <= 0 && loser.getPlace() == null) {
                    long livingCount = players.values().stream()
                            .filter(p -> p.getHealth() > 0)
                            .count();
                    // If I just died, I am at place (livingCount + 1).
                    // Wait, livingCount does NOT include me if I am <= 0.
                    // Example: 8 players. 1 dies. Living=7. Place = 8.
                    // Example: 2 players. 1 dies. Living=1. Place = 2.
                    loser.setPlace((int) livingCount + 1);
                }

                if (eventListener != null) {
                    var payload = Map.of(
                            "winnerId", finalWinner.getId(),
                            "loserId", loser.getId(),
                            "damageDealt", damage);
                    // System.out.println("Dispatching COMBAT_RESULT: " + payload);
                    eventListener.accept(new GameEvent("COMBAT_RESULT", payload));
                }
            }
        }

        // Check Game Over
        long livingCount = players.values().stream().filter(p -> p.getHealth() > 0).count();
        if (livingCount <= 1 && players.size() > 1) { // Ensure >1 start so single-player testing doesn't instant-end
            Player survivor = players.values().stream()
                    .filter(p -> p.getHealth() > 0)
                    .findFirst()
                    .orElse(null);
            if (survivor != null && survivor.getPlace() == null) {
                survivor.setPlace(1);
            }
            this.phase = GamePhase.END;
            this.phaseEndTime = System.currentTimeMillis() + 60000; // Stay in END for a while
        }
    }

    private void nextPhase() {
        if (phase == GamePhase.PLANNING) {
            startPhase(GamePhase.COMBAT);
        } else {
            startPhase(GamePhase.PLANNING);
        }
    }

    private boolean isBot(Player p) {
        return p.getId().startsWith("Bot-") || p.getName().startsWith("Bot-");
    }

    private void refreshBotRoster(Player bot) {
        System.out.println("Refreshing roster for " + bot.getName() + " (Round " + round + ")");
        // 1. Clear current units
        // Remove from grid
        for (var u : bot.getBoardUnits()) {
            bot.getGrid().removeUnit(u);
        }
        bot.getBoardUnits().clear();
        bot.getBench().clear();

        // 2. Determine target unit count based on round (max 7)
        int targetCount = Math.min(7, (round / 2) + 1);
        targetCount = Math.max(1, targetCount); // At least 1 unit

        // 3. Select random units
        var allUnits = dataLoader.getAllUnits();
        if (allUnits.isEmpty())
            return;

        for (int i = 0; i < targetCount; i++) {
            var unitDef = allUnits.get((int) (Math.random() * allUnits.size()));

            // 20% chance for 2-star
            int starLevel = (Math.random() < 0.2) ? 2 : 1;

            // Adjust stats for star level (simplified logic, similar to Player upgrade)
            net.lwenstrom.tft.backend.core.engine.UnitDefinition actualDef = unitDef;
            if (starLevel == 2) {
                double scale = 1.8;
                actualDef = new net.lwenstrom.tft.backend.core.engine.UnitDefinition(
                        unitDef.id(),
                        unitDef.name(),
                        unitDef.cost(),
                        (int) (unitDef.maxHealth() * scale),
                        unitDef.maxMana(),
                        (int) (unitDef.attackDamage() * scale),
                        unitDef.abilityPower(),
                        unitDef.armor(),
                        unitDef.magicResist(),
                        unitDef.attackSpeed(),
                        unitDef.range(),
                        unitDef.traits(),
                        unitDef.ability());
            }

            var unit = new net.lwenstrom.tft.backend.core.engine.StandardGameUnit(actualDef);
            unit.setOwnerId(bot.getId());
            unit.setStarLevel(starLevel);

            // 4. Place on grid (Rows 0-3, Cols 0-6)
            // Try random positions until valid
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 20) {
                int x = (int) (Math.random() * Grid.COLS);
                int y = (int) (Math.random() * Grid.PLAYER_ROWS); // Player area is rows 0-3

                if (bot.getGrid().isValid(x, y) && bot.getGrid().isEmpty(x, y)) {
                    bot.getGrid().placeUnit(unit, x, y);
                    bot.getBoardUnits().add(unit);
                    placed = true;
                }
                attempts++;
            }
            if (!placed) {
                bot.getBench().add(unit);
            }
        }

        // Scale bot level
        bot.setLevel(targetCount); // Sync level with unit count for simplicity
    }

    public void addBot() {
        var bot = addPlayer("Bot-" + UUID.randomUUID().toString().substring(0, 4));
        refreshBotRoster(bot);
    }

    public GameState getState() {
        return currentState;
    }

    private java.util.function.Consumer<GameEvent> eventListener;

    public void setEventListener(java.util.function.Consumer<GameEvent> listener) {
        this.eventListener = listener;
    }

    public record GameEvent(String type, Object payload) {
    }

    public enum GamePhase {
        PLANNING,
        COMBAT,
        END
    }
}
