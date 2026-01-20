package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.combat.BfsUnitMover;
import net.lwenstrom.tft.backend.core.combat.DefaultAbilityCaster;
import net.lwenstrom.tft.backend.core.combat.NearestEnemyTargetSelector;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import net.lwenstrom.tft.backend.core.model.GameState;
import net.lwenstrom.tft.backend.core.model.GameState.PlayerState;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import net.lwenstrom.tft.backend.core.time.Clock;

@Slf4j
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

    private long currentPhaseDuration;

    private final GameModeRegistry gameModeRegistry;
    private final Clock clock;
    private final RandomProvider randomProvider;
    private final TraitManager traitManager;
    private final CombatSystem combatSystem;

    private CombatResultListener combatResultListener;

    @FunctionalInterface
    public interface CombatResultListener {
        void onCombatResult(String roomId, String winnerId, String loserId);
    }

    public void setCombatResultListener(CombatResultListener listener) {
        this.combatResultListener = listener;
    }

    public GameRoom(
            String id,
            DataLoader dataLoader,
            GameModeRegistry gameModeRegistry,
            Clock clock,
            RandomProvider randomProvider) {
        this.id = id;
        this.dataLoader = dataLoader;
        this.gameModeRegistry = gameModeRegistry;
        this.clock = clock;
        this.randomProvider = randomProvider;

        this.traitManager = new TraitManager();
        gameModeRegistry.getActiveProvider().registerTraitEffects(this.traitManager);
        this.combatSystem = new CombatSystem(
                traitManager,
                clock,
                new NearestEnemyTargetSelector(),
                new BfsUnitMover(clock),
                new DefaultAbilityCaster());

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

    public boolean isEnded() {
        return phase == GamePhase.END;
    }

    public Player addPlayer(String name) {
        Player player = new Player(name, dataLoader, randomProvider);
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
        if (phase != GamePhase.LOBBY) {
            return;
        }

        // Fill with bots if needed (up to 8)
        int currentCount = players.size();
        for (int i = 0; i < 8 - currentCount; i++) {
            addBot();
        }

        startPhase(GamePhase.PLANNING);
    }

    public void addBot() {
        String botId = "Bot-" + UUID.randomUUID().toString().substring(0, 4);
        Player bot = new Player(botId, dataLoader, randomProvider);
        players.put(bot.getId(), bot);
        bot.refreshShop();
        refreshBotRoster(bot);
        updateGameState(phaseEndTime - clock.currentTimeMillis());
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
        if (phase == GamePhase.LOBBY) {
            return;
        }

        long now = clock.currentTimeMillis();
        if (now >= phaseEndTime) {
            nextPhase();
        }

        if (phase == GamePhase.COMBAT) {
            var it = activeCombats.iterator();
            while (it.hasNext()) {
                var pair = it.next();
                var result = combatSystem.simulateTick(pair);
                if (result.ended()) {
                    handleCombatEnd(false, result, pair);
                    it.remove();
                }
            }
        }

        updateGameState(phaseEndTime - now);
    }

    private void nextPhase() {
        // Handle timeout: force-end any remaining combats before phase change
        if (phase == GamePhase.COMBAT && !activeCombats.isEmpty()) {
            for (var pair : activeCombats) {
                handleCombatEnd(true, null, pair);
            }
            activeCombats.clear();
        }

        if (phase == GamePhase.PLANNING) {
            startPhase(GamePhase.COMBAT);
        } else {
            startPhase(GamePhase.PLANNING);
        }
    }

    private void startPhase(GamePhase newPhase) {
        this.phase = newPhase;
        log.info("Starting phase: {}", newPhase);

        if (phase == GamePhase.PLANNING) {
            // Check if game should end (only one player with health > 0)
            var alivePlayers =
                    players.values().stream().filter(p -> p.getHealth() > 0).count();
            if (alivePlayers <= 1) {
                log.info("Game ending: only {} player(s) remaining", alivePlayers);
                this.phase = GamePhase.END;
                updateGameState(0);
                return;
            }

            log.info("Starting PLANNING phase. Restoring units.");
            // Restore units from combat positions
            combatSystem.endCombat(players.values());

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
        this.phaseEndTime = clock.currentTimeMillis() + currentPhaseDuration;

        if (phase == GamePhase.COMBAT) {
            activeCombats.clear();
            currentMatchups.clear();
            var shuffled = new ArrayList<Player>(players.values());
            randomProvider.shuffle(shuffled);
            for (int i = 0; i < shuffled.size() - 1; i += 2) {
                Player p1 = shuffled.get(i);
                Player p2 = shuffled.get(i + 1);
                activeCombats.add(List.of(p1, p2));
                currentMatchups.put(p1.getId(), p2.getId());
                currentMatchups.put(p2.getId(), p1.getId());
            }
            activeCombats.forEach(combatSystem::startCombat);
        }

        updateGameState(currentPhaseDuration);
    }

    private void refreshBotRoster(Player bot) {
        bot.removeAllUnits();

        int unitCount = Math.min((round / 2) + 1, 7); // Max 7 for first row
        List<UnitDefinition> available = dataLoader.getAllUnits();
        for (int i = 0; i < unitCount; i++) {
            UnitDefinition def = available.get(randomProvider.nextInt(available.size()));
            bot.addUnitToBoard(def, i, 3);
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
                new HashMap<>(currentMatchups),
                new ArrayList<>(),
                gameModeRegistry.getActiveMode());
    }

    private long calculatePhaseDuration(GamePhase phase, int round) {
        // Base 10s + 2s per round index (0-based)
        // Round 1: 10 + 0 = 10s
        // Round 2: 10 + 2 = 12s
        return 10000 + (round - 1) * 2000;
    }

    private void handleCombatEnd(boolean isTimeout, CombatSystem.CombatResult result, List<Player> participants) {
        Player winner = null;
        var draw = false;

        if (isTimeout || result == null) {
            // Timeout: Winner is player with highest total HP on board
            var maxHp = -1;
            for (var p : participants) {
                var totalHp = p.getBoardUnits().stream()
                        .mapToInt(GameUnit::getCurrentHealth)
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
            // Calculate Damage: base 2 + number of surviving units
            var damage = 2 + winner.getBoardUnits().size();

            final var finalWinner = winner;
            var loser = participants.stream()
                    .filter(p -> !p.getId().equals(finalWinner.getId()))
                    .findFirst()
                    .orElse(null);

            if (loser != null) {
                loser.takeDamage(damage);
                log.info("Combat ended: {} wins! {} takes {}", winner.getName(), loser.getName(), damage);

                // Notify listener about combat result
                if (combatResultListener != null) {
                    combatResultListener.onCombatResult(id, winner.getId(), loser.getId());
                }
            }
        }
    }
}
