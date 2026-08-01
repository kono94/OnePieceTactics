package net.lwenstrom.tft.backend.core.engine;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.analytics.GameplayAnalyticsRecorder;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import net.lwenstrom.tft.backend.core.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameEngine {

    private final DataLoader dataLoader;
    private final GameModeRegistry gameModeRegistry;
    private final Clock clock;
    private final RandomProvider randomProvider;
    private final GameplayAnalyticsRecorder analyticsRecorder;
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameEngine(
            DataLoader dataLoader, GameModeRegistry gameModeRegistry, Clock clock, RandomProvider randomProvider) {
        this(dataLoader, gameModeRegistry, clock, randomProvider, GameplayAnalyticsRecorder.NO_OP);
    }

    @Autowired
    public GameEngine(
            DataLoader dataLoader,
            GameModeRegistry gameModeRegistry,
            Clock clock,
            RandomProvider randomProvider,
            GameplayAnalyticsRecorder analyticsRecorder) {
        this.dataLoader = dataLoader;
        this.gameModeRegistry = gameModeRegistry;
        this.clock = clock;
        this.randomProvider = randomProvider;
        this.analyticsRecorder = analyticsRecorder;
    }

    public GameRoom createRoom() {
        return createRoom(UUID.randomUUID().toString());
    }

    public GameRoom createRoom(String id) {
        return tryCreateRoom(id).orElseThrow(() -> new IllegalStateException("Room already exists: " + id));
    }

    public Optional<GameRoom> tryCreateRoom(String id) {
        var room = new GameRoom(
                id,
                dataLoader,
                gameModeRegistry,
                clock,
                randomProvider,
                gameModeRegistry.getDefaultMode(),
                analyticsRecorder);
        return rooms.putIfAbsent(room.getId(), room) == null ? Optional.of(room) : Optional.empty();
    }

    public GameRoom getRoom(String id) {
        return rooms.get(id);
    }

    public Collection<GameRoom> getActiveRooms() {
        return rooms.values();
    }

    public void removeRoom(String id) {
        rooms.remove(id);
    }

    public void tick() {
        rooms.values().forEach(GameRoom::tick);
        // Remove ended games
        rooms.entrySet().removeIf(entry -> entry.getValue().isEnded());
    }
}
