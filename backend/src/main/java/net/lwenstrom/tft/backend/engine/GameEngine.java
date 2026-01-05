package net.lwenstrom.tft.backend.engine;

import net.lwenstrom.tft.backend.core.data.DataLoader;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class GameEngine {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final DataLoader dataLoader;

    public GameEngine(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }

    public GameRoom createRoom() {
        var room = new GameRoom(dataLoader);
        rooms.put(room.getId(), room);
        return room;
    }

    public GameRoom createRoom(String id) {
        var room = new GameRoom(id, dataLoader);
        rooms.put(room.getId(), room);
        return room;
    }

    public java.util.Collection<GameRoom> getAllRooms() {
        return rooms.values();
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    // Engine tick method called by scheduler
    public void tick() {
        rooms.values().forEach(GameRoom::tick);
    }
}
