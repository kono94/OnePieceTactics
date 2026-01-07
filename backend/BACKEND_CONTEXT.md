# Backend Context & Architecture

> **Role**: Stateful Game Server & Game Engine
> **Main Responsibility**: Managing real-time game state, simulating combat (Auto-Battler), and broadcasting updates to clients via WebSocket.

## 1. System Overview
The backend is a monolithic Spring Boot application acting as the authoritative server for the One Piece TFT-Clone.
- **State Management**: All active games are held in-memory within the `GameEngine`.
- **Concurrency**: Uses `ConcurrentHashMap` for rooms/players globally.
- **Simulation**: A central "Game Loop" ticks every 100ms (10Hz).
- **Separation of Concerns**: The core engine is theme-agnostic, loading specific data (Luffy, Pirate) from `units.json`.

## 2. Tech Stack & Standards
- **Language**: **Java 25** (Heavy use of `record`, `var`, and Modern Stream API).
- **Framework**: **Spring Boot 4+** (Latest features).
- **Communication**: **STOMP WebSockets**.
- **Build**: **Maven** (with `mvn spotless:apply` for formatting).
- **DI Pattern**: **Constructor Injection** only, using Lombok `@RequiredArgsConstructor` and `final` fields.

## 3. Architecture Map
The application resides in `net.lwenstrom.tft.backend`.

```text
src/main/java/net/lwenstrom/tft/backend/
├── BackendApplication.java       // Entry Point
├── config/
│   └── WebSocketConfig.java      // STOMP Setup (Broker: /topic, App: /app)
└── core/
    ├── GameController.java       // API Gateway: Handles WS messages & Scheduled Game Loop
    ├── DataLoader.java           // Static Data loading (units.json)
    ├── engine/                   // THE BRAIN (Game Logic)
    │   ├── GameEngine.java       // Singleton Manager of all GameRooms
    │   ├── GameRoom.java         // Instance of a match. Holds State + Players.
    │   ├── CombatSystem.java     // Logic for combat simulation (Ticks, Elimination, Damage)
    │   ├── Grid.java             // 4x7 grid logic & distance calculations
    │   ├── TraitManager.java     // Logic for calculating tiered trait bonuses
    │   └── Player.java           // Player entity (Gold, XP, Health, Hand/Board)
    └── model/                    // THE DATA (DTOs/State)
        ├── GameState.java        // [Record] The world state snapshot sent to clients
        ├── GameAction.java       // [Record] Incoming commands (BUY, MOVE, etc.)
        ├── GameUnit.java         // [Interface] Core unit model
        └── Trait.java            // [Record] Definition of a trait / origin
```

## 4. The "Game Loop" Explained
The engine runs on a fixed-rate heartbeat:

1. **Trigger**: `GameController.tick()` is `@Scheduled(fixedRate = 100)`.
2. **Flow**: `GameController` -> `GameEngine.tick()` -> `GameRoom.tick()`.
3. **Phases**:
   - **PLANNING** (30s): Board units are moveable. Players gain gold/XP and roll shops.
   - **COMBAT** (60s): Board units move/attack autonomously. `CombatSystem` simulates each tick.
4. **Broadcast**: After every tick, the entire `GameState` record is broadcast to `/topic/room/{roomId}` for immediate UI synchronization.

## 5. API & Event Intermediary

### WebSocket Configuration
- **STOMP Endpoint**: `/tft-websocket`
- **Application Prefix**: `/app`
- **Broker Prefix**: `/topic`

### System Events
| Destination | Direction | Payload Type | Description |
| :--- | :--- | :--- | :--- |
| `/topic/room/{roomId}` | **S -> C** | `GameState` | 10Hz "World State" broadcast. |
| `/app/create` | **C -> S** | `RoomRequest` | Initialize a new room + match. |
| `/app/join` | **C -> S** | `RoomRequest` | Join an existing room via ID. |
| `/app/room/{roomId}/action`| **C -> S** | `GameAction` | Commands: `BUY`, `REROLL`, `EXP`, `MOVE`. |

### Action Specification
```json
{
  "type": "MOVE",
  "playerId": "...",
  "unitId": "...",
  "targetX": 0,
  "targetY": 0
}
```

## 6. Key File Locations
- **Main Entry**: [BackendApplication.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/BackendApplication.java)
- **Scheduler**: [GameController.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/GameController.java)
- **State Definition**: [GameState.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/model/GameState.java)
- **Game Logic**: [GameRoom.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/engine/GameRoom.java)
