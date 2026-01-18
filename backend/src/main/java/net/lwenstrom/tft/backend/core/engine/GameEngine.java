package net.lwenstrom.tft.backend.core.engine;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.random.RandomProvider;
import net.lwenstrom.tft.backend.core.time.Clock;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameEngine {

    private final DataLoader dataLoader;
    private final GameModeRegistry gameModeRegistry;
    private final Clock clock;
    private final RandomProvider randomProvider;
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameRoom createRoom() {
        return createRoom(UUID.randomUUID().toString());
    }

    public GameRoom createRoom(String id) {
        var room = new GameRoom(id, dataLoader, gameModeRegistry, clock, randomProvider);
        rooms.put(room.getId(), room);
        return room;
    }

    public GameRoom getRoom(String id) {
        return rooms.get(id);
    }

    public Collection<GameRoom> getActiveRooms() {
        return rooms.values();
    }

    public void tick() {
        rooms.values().forEach(GameRoom::tick);
    }
}
