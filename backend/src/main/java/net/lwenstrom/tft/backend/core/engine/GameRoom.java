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
import net.lwenstrom.tft.backend.core.model.LootOrb;
import net.lwenstrom.tft.backend.core.model.LootType;
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
    private final List<GameState.CombatEvent> lastTickEvents = new ArrayList<>();
    private final Map<String, CombatSystem.DamageEntry> currentRoundDamageLog = new ConcurrentHashMap<>();

    private CombatResultListener combatResultListener;

    @FunctionalInterface
    public interface CombatResultListener {
        void onCombatResult(
                String roomId,
                String winnerId,
                String loserId,
                List<String> participantIds,
                Map<String, CombatSystem.DamageEntry> damageLog);
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
                new HashMap<>(),
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

    public void collectOrb(String playerId, String orbId) {
        Player p = players.get(playerId);
        if (p != null) {
            p.collectOrb(orbId);
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

        lastTickEvents.clear();
        if (phase == GamePhase.COMBAT) {
            var it = activeCombats.iterator();
            while (it.hasNext()) {
                var pair = it.next();
                var result = combatSystem.simulateTick(pair);
                if (result.events() != null) {
                    lastTickEvents.addAll(result.events());
                }
                if (result.ended()) {
                    handleCombatEnd(false, result, pair);
                    it.remove();
                }
            }
            // Update live damage log
            currentRoundDamageLog.putAll(combatSystem.getDamageLog());

            // Pre-emptive end: if all combats are finished, skip to next phase
            if (activeCombats.isEmpty()) {
                nextPhase();
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

            // Exit combat mode and process pending upgrades
            players.values().forEach(p -> {
                p.setInCombat(false);
                p.processPendingUpgrades();
            });

            round++;
            players.values().forEach(p -> {
                p.gainGold(5 + Math.min(p.getGold() / 10, 5));
                p.gainXp(2);
                p.refreshShop();
                if (p.getName().startsWith("Bot-")) {
                    refreshBotRoster(p);
                }
                // Spawn Loot Orbs on even rounds
                if (round % 2 == 0) {
                    spawnLootOrbsForPlayer(p);
                }
            });
        }

        this.currentPhaseDuration = calculatePhaseDuration(newPhase, round);
        this.phaseEndTime = clock.currentTimeMillis() + currentPhaseDuration;

        if (phase == GamePhase.COMBAT) {
            // Set all active players to combat mode
            players.values().stream().filter(p -> p.getHealth() > 0).forEach(p -> p.setInCombat(true));

            // Clear damage log at the start of combat
            currentRoundDamageLog.clear();

            activeCombats.clear();
            currentMatchups.clear();

            var alivePlayers = players.values().stream()
                    .filter(p -> p.getHealth() > 0)
                    .collect(Collectors.toCollection(ArrayList::new));

            randomProvider.shuffle(alivePlayers);

            for (int i = 0; i < alivePlayers.size() - 1; i += 2) {
                Player p1 = alivePlayers.get(i);
                Player p2 = alivePlayers.get(i + 1);
                activeCombats.add(List.of(p1, p2));
                currentMatchups.put(p1.getId(), p2.getId());
                currentMatchups.put(p2.getId(), p1.getId());
            }

            // Handle odd number of players with a ghost
            if (alivePlayers.size() % 2 != 0 && alivePlayers.size() > 1) {
                Player oddPlayer = alivePlayers.get(alivePlayers.size() - 1);
                // Pick a random donor who is NOT the odd player
                List<Player> potentialDonors = alivePlayers.stream()
                        .filter(p -> !p.getId().equals(oddPlayer.getId()))
                        .toList();
                Player donor = potentialDonors.get(randomProvider.nextInt(potentialDonors.size()));
                Player ghost = donor.createGhost();

                activeCombats.add(List.of(oddPlayer, ghost));
                currentMatchups.put(oddPlayer.getId(), ghost.getId());
                // We don't put ghost in currentMatchups for others because it's not a real
                // player they can visit/see in lobby normally
            }

            // Reset combat system log before starting combat for all pairs
            combatSystem.startCombat(List.of()); // This clears the internal log
            activeCombats.forEach(combatSystem::startCombat);
        }

        updateGameState(currentPhaseDuration);
    }

    private void refreshBotRoster(Player bot) {
        bot.removeAllUnits();

        // Scale bot level with rounds: starts at 2, increases every 2 rounds, max 9
        var botLevel = Math.min(2 + (round / 2), 9);
        bot.setLevel(botLevel);

        // Unit count scales with round: round 1 → 2 units, round 2 → 3 units, etc.
        // Capped by bot level and max 7 (first row limit)
        var unitCount = Math.min(Math.min(round + 1, botLevel), 7);
        var available = dataLoader.getAllUnits();
        for (var i = 0; i < unitCount; i++) {
            var def = available.get(randomProvider.nextInt(available.size()));
            bot.addUnitToBoard(def, i, 3);
        }
    }

    private void updateGameState(long timeLeft) {
        Map<String, PlayerState> playerStates = players.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toState()));

        // Also add ghost players from activeCombats if they exist
        for (var combat : activeCombats) {
            for (var p : combat) {
                if (!playerStates.containsKey(p.getId())) {
                    playerStates.put(p.getId(), p.toState());
                }
            }
        }

        this.currentState = new GameState(
                id,
                hostId,
                phase,
                round,
                timeLeft,
                calculatePhaseDuration(phase, round),
                playerStates,
                new HashMap<>(currentMatchups),
                new ArrayList<>(lastTickEvents),
                new HashMap<>(currentRoundDamageLog),
                gameModeRegistry.getActiveMode());
    }

    private void spawnLootOrbsForPlayer(Player player) {
        int orbCount = 2 + randomProvider.nextInt(3); // 2-4 orbs
        for (int i = 0; i < orbCount; i++) {
            String orbId = UUID.randomUUID().toString();
            // Random position in the top half of the grid (visual rows 0-3)
            int x = randomProvider.nextInt(7);
            int y = randomProvider.nextInt(4);

            LootType type = randomProvider.nextInt(10) < 6 ? LootType.GOLD : LootType.UNIT; // 60% Gold, 40% Unit
            String contentId = "";
            int amount = 0;

            if (type == LootType.GOLD) {
                amount = 3 + randomProvider.nextInt(6); // 3-8 Gold
            } else {
                var units = dataLoader.getAllUnits();
                contentId = units.get(randomProvider.nextInt(units.size())).name();
            }

            LootOrb orb = new LootOrb(orbId, x, y, type, contentId, amount);
            player.addLootOrb(orb);
        }
    }

    private long calculatePhaseDuration(GamePhase phase, int round) {
        if (phase == GamePhase.COMBAT) {
            return 25000;
        }
        // Base 15s + 0.25s per round index (0-based) for PLANNING
        return 15000 + (round - 1) * 250;
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
                winner = participants.stream()
                        .filter(p -> p.getId().equals(result.winnerId()))
                        .findFirst()
                        .orElse(null);
            } else {
                draw = true;
            }
        }

        if (!draw && winner != null) {
            // Calculate Damage: base 2 + number of surviving units + round scaling
            var damage = 2 + winner.getBoardUnits().size() + (round / 3);

            final var finalWinner = winner;
            var loser = participants.stream()
                    .filter(p -> !p.getId().equals(finalWinner.getId()))
                    .findFirst()
                    .orElse(null);

            if (loser != null && !loser.isGhost()) {
                loser.takeDamage((int) damage);
                log.info("Combat ended: {} wins! {} takes {}", winner.getName(), loser.getName(), damage);

                if (loser.getHealth() <= 0) {
                    var aliveCount = (int) players.values().stream()
                            .filter(p -> p.getHealth() > 0)
                            .count();
                    loser.setPlace(aliveCount + 1);
                }
            } else if (loser != null && loser.isGhost()) {
                log.info(
                        "Combat ended: {} wins against ghost of {}! No damage taken by ghost donor.",
                        winner.getName(),
                        loser.getName());
            }
        }

        // Check for game end
        var alivePlayers =
                players.values().stream().filter(p -> p.getHealth() > 0).toList();
        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                alivePlayers.get(0).setPlace(1);
            }
            this.phase = GamePhase.END;
            updateGameState(0);
        }

        // Notify listener about combat result (even if draw)
        if (combatResultListener != null) {
            String winnerId = null;
            String loserId = null;

            if (winner != null && !draw) {
                winnerId = winner.getId();
                for (var p : participants) {
                    if (!p.getId().equals(winnerId)) {
                        loserId = p.getId();
                        break;
                    }
                }
            }

            var participantIds = participants.stream().map(Player::getId).toList();
            var damageLog = result != null ? result.damageLog() : Map.<String, CombatSystem.DamageEntry>of();
            combatResultListener.onCombatResult(id, winnerId, loserId, participantIds, damageLog);
        }
    }
}
