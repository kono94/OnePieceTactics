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
    private GameState currentState;

    private final DataLoader dataLoader;
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> currentMatchups = new ConcurrentHashMap<>();
    private final List<List<Player>> activeCombats = new ArrayList<>();

    private GamePhase phase = GamePhase.PLANNING;
    private long phaseEndTime;
    private int round = 0;

    private Consumer<Object> eventListener;

    // Constants
    private static final long PLANNING_DURATION_MS = 60000;
    private static final long COMBAT_DURATION_MS = 30000;

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

        this.round = 1;

        this.currentState = new GameState(
                id,
                phase,
                round,
                0,
                PLANNING_DURATION_MS,
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                gameModeRegistry.getActiveMode());

        long duration = PLANNING_DURATION_MS;
        this.phaseEndTime = System.currentTimeMillis() + duration;
        updateGameState(PLANNING_DURATION_MS);
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
        player.refreshShop();
        updateGameState(phaseEndTime - System.currentTimeMillis());
        return player;
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
        long duration = (phase == GamePhase.PLANNING) ? PLANNING_DURATION_MS : COMBAT_DURATION_MS;
        this.phaseEndTime = System.currentTimeMillis() + duration;

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
        } else if (phase == GamePhase.COMBAT) {
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
                phase,
                round,
                timeLeft,
                (phase == GamePhase.PLANNING) ? PLANNING_DURATION_MS : COMBAT_DURATION_MS,
                playerStates,
                new HashMap<>(),
                new ArrayList<>(),
                gameModeRegistry.getActiveMode());
    }
}
