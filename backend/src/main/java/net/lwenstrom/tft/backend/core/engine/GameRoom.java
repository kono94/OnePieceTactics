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
import net.lwenstrom.tft.backend.core.GameConstants;
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
        var player = new Player(name, dataLoader, randomProvider);
        players.put(player.getId(), player);

        if (hostId == null) {
            hostId = player.getId();
        }

        player.refreshShop();
        updateGameState(0);
        return player;
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        if (playerId.equals(hostId)) {
            hostId = players.isEmpty() ? null : players.keySet().iterator().next();
        }
        updateGameState(0);
    }

    public void startMatch() {
        if (phase != GamePhase.LOBBY) {
            return;
        }

        int currentCount = players.size();
        for (int i = 0; i < 8 - currentCount; i++) {
            addBot();
        }

        startPhase(GamePhase.PLANNING);
    }

    public void addBot() {
        var botId = "Bot-" + UUID.randomUUID().toString().substring(0, 4);
        var bot = new Player(botId, dataLoader, randomProvider);
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
        var p = players.get(playerId);
        if (p != null && (phase == GamePhase.PLANNING || phase == GamePhase.COMBAT)) {
            p.moveUnit(unitId, x, y);
        }
    }

    public void collectOrb(String playerId, String orbId) {
        var p = players.get(playerId);
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
            currentRoundDamageLog.putAll(combatSystem.getDamageLog());

            if (activeCombats.isEmpty()) {
                nextPhase();
            }
        }

        updateGameState(phaseEndTime - now);
    }

    private void nextPhase() {
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
            var alivePlayers =
                    players.values().stream().filter(p -> p.getHealth() > 0).count();
            if (alivePlayers <= 1) {
                log.info("Game ending: only {} player(s) remaining", alivePlayers);
                this.phase = GamePhase.END;
                updateGameState(0);
                return;
            }

            log.info("Starting PLANNING phase. Restoring units.");
            combatSystem.endCombat(players.values());

            players.values().forEach(p -> {
                p.setInCombat(false);
                p.processPendingUpgrades();
            });

            round++;
            players.values().forEach(p -> {
                p.gainGold(GameConstants.BASE_INCOME + Math.min(p.getGold() / 10, GameConstants.MAX_INTEREST));

                // Navigator trait gold
                int navGold = p.getBoardUnits().stream()
                        .filter(u -> u.getGoldBonusMax() > 0)
                        .mapToInt(u -> (int)
                                (u.getGoldBonusMin() + Math.random() * (u.getGoldBonusMax() - u.getGoldBonusMin() + 1)))
                        .sum();
                if (navGold > 0) {
                    p.gainGold(navGold);
                    log.info("Player {} gained {} bonus gold from Navigators", p.getName(), navGold);
                }

                p.gainXp(GameConstants.XP_PER_PHASE);
                p.refreshShop();
                if (p.getName().startsWith("Bot-")) {
                    refreshBotRoster(p);
                }
                if (round % 2 == 0) {
                    spawnLootOrbsForPlayer(p);
                }
            });
        }

        this.currentPhaseDuration = calculatePhaseDuration(newPhase, round);
        this.phaseEndTime = clock.currentTimeMillis() + currentPhaseDuration;

        if (phase == GamePhase.COMBAT) {
            players.values().stream().filter(p -> p.getHealth() > 0).forEach(p -> p.setInCombat(true));

            currentRoundDamageLog.clear();

            activeCombats.clear();
            currentMatchups.clear();

            var alivePlayers = players.values().stream()
                    .filter(p -> p.getHealth() > 0)
                    .collect(Collectors.toCollection(ArrayList::new));

            randomProvider.shuffle(alivePlayers);

            for (int i = 0; i < alivePlayers.size() - 1; i += 2) {
                var p1 = alivePlayers.get(i);
                var p2 = alivePlayers.get(i + 1);
                activeCombats.add(List.of(p1, p2));
                currentMatchups.put(p1.getId(), p2.getId());
                currentMatchups.put(p2.getId(), p1.getId());
            }

            if (alivePlayers.size() % 2 != 0 && alivePlayers.size() > 1) {
                var oddPlayer = alivePlayers.get(alivePlayers.size() - 1);
                var potentialDonors = alivePlayers.stream()
                        .filter(p -> !p.getId().equals(oddPlayer.getId()))
                        .toList();
                var donor = potentialDonors.get(randomProvider.nextInt(potentialDonors.size()));
                var ghost = donor.createGhost();

                activeCombats.add(List.of(oddPlayer, ghost));
                currentMatchups.put(oddPlayer.getId(), ghost.getId());
            }

            combatSystem.clearDamageLog();
            activeCombats.forEach(combatSystem::startCombat);
        }

        updateGameState(currentPhaseDuration);
    }

    private void refreshBotRoster(Player bot) {
        bot.removeAllUnits();

        var botLevel = Math.min(GameConstants.BOT_STARTING_LEVEL + (round / 2), GameConstants.BOT_MAX_LEVEL);
        bot.setLevel(botLevel);

        var unitCount = Math.min(Math.min(round + 1, botLevel), GameConstants.BOT_MAX_UNITS_PER_ROW);
        var available = dataLoader.getAllUnits();
        for (var i = 0; i < unitCount; i++) {
            var def = available.get(randomProvider.nextInt(available.size()));
            bot.addUnitToBoard(def, i, 3);
        }
    }

    private void updateGameState(long timeLeft) {
        Map<String, PlayerState> playerStates = players.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toState()));

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
        int orbCount = GameConstants.MIN_ORB_COUNT
                + randomProvider.nextInt(GameConstants.MAX_ORB_COUNT - GameConstants.MIN_ORB_COUNT + 1);
        for (int i = 0; i < orbCount; i++) {
            var orbId = UUID.randomUUID().toString();
            int x = randomProvider.nextInt(GameConstants.GRID_COLS);
            int y = randomProvider.nextInt(GameConstants.PLAYER_ROWS);

            var type =
                    randomProvider.nextInt(100) < GameConstants.ORB_GOLD_CHANCE_PERCENT ? LootType.GOLD : LootType.UNIT;
            var contentId = "";
            var amount = 0;

            if (type == LootType.GOLD) {
                amount = GameConstants.MIN_ORB_GOLD
                        + randomProvider.nextInt(GameConstants.MAX_ORB_GOLD - GameConstants.MIN_ORB_GOLD + 1);
            } else {
                var units = dataLoader.getAllUnits();
                contentId = units.get(randomProvider.nextInt(units.size())).name();
            }

            var orb = new LootOrb(orbId, x, y, type, contentId, amount);
            player.addLootOrb(orb);
        }
    }

    private long calculatePhaseDuration(GamePhase phase, int round) {
        if (phase == GamePhase.COMBAT) {
            return GameConstants.COMBAT_PHASE_MS;
        }
        return GameConstants.BASE_PLANNING_DURATION_MS + (round - 1) * GameConstants.PLANNING_DURATION_INCREMENT_MS;
    }

    // ========== REFACTORED handleCombatEnd ==========

    private void handleCombatEnd(boolean isTimeout, CombatSystem.CombatResult result, List<Player> participants) {
        var outcome = determineCombatOutcome(isTimeout, result, participants);

        if (!outcome.isDraw() && outcome.loser() != null) {
            applyDamageToLoser(outcome.winner(), outcome.loser());
        }

        checkAndTriggerGameEnd();

        notifyCombatResult(outcome, result, participants);
    }

    private record CombatOutcome(Player winner, Player loser, boolean isDraw) {}

    private CombatOutcome determineCombatOutcome(
            boolean isTimeout, CombatSystem.CombatResult result, List<Player> participants) {
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
            if (result.winnerId() != null) {
                winner = participants.stream()
                        .filter(p -> p.getId().equals(result.winnerId()))
                        .findFirst()
                        .orElse(null);
            } else {
                draw = true;
            }
        }

        Player loser = null;
        if (!draw && winner != null) {
            final var finalWinner = winner;
            loser = participants.stream()
                    .filter(p -> !p.getId().equals(finalWinner.getId()))
                    .findFirst()
                    .orElse(null);
        }

        return new CombatOutcome(winner, loser, draw);
    }

    private void applyDamageToLoser(Player winner, Player loser) {
        if (loser == null || winner == null) return;

        var damage = GameConstants.BASE_COMBAT_DAMAGE + winner.getBoardUnits().size() + (round / 3);

        if (!loser.isGhost()) {
            loser.takeDamage((int) damage);
            log.info("Combat ended: {} wins! {} takes {} damage", winner.getName(), loser.getName(), damage);

            if (loser.getHealth() <= 0) {
                var aliveCount = (int)
                        players.values().stream().filter(p -> p.getHealth() > 0).count();
                loser.setPlace(aliveCount + 1);
            }
        } else {
            log.info("Combat ended: {} wins against ghost of {}! No damage taken.", winner.getName(), loser.getName());
        }
    }

    private void checkAndTriggerGameEnd() {
        var alivePlayers =
                players.values().stream().filter(p -> p.getHealth() > 0).toList();
        if (alivePlayers.size() <= 1) {
            if (alivePlayers.size() == 1) {
                alivePlayers.get(0).setPlace(1);
            }
            this.phase = GamePhase.END;
            updateGameState(0);
        }
    }

    private void notifyCombatResult(
            CombatOutcome outcome, CombatSystem.CombatResult result, List<Player> participants) {
        if (combatResultListener == null) return;

        String winnerId = null;
        String loserId = null;

        if (outcome.winner() != null && !outcome.isDraw()) {
            winnerId = outcome.winner().getId();
            if (outcome.loser() != null) {
                loserId = outcome.loser().getId();
            }
        }

        var participantIds = participants.stream().map(Player::getId).toList();
        var damageLog = result != null ? result.damageLog() : Map.<String, CombatSystem.DamageEntry>of();
        combatResultListener.onCombatResult(id, winnerId, loserId, participantIds, damageLog);
    }

    // ========== END REFACTORED handleCombatEnd ==========
}
