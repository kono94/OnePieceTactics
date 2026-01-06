# Backend Context & Architecture

> **Role**: Stateful Game Server & Game Engine
> **Main Responsibility**: Managing real-time game state, simulating combat (Auto-Battler), and broadcasting updates to clients via WebSocket.

## 1. System Overview
The backend is a monolithic Spring Boot application acting as the authoritative server for the TFT-Clone. It does *not* just CRUD data; it runs a live simulation.
-   **State Management**: All active games are held in-memory within `GameEngine`.
-   **Concurrency**: Uses `ConcurrentHashMap` for rooms/players globally.
-   **Simulation**: A central "Game Loop" ticks every 100ms (10Hz), driving phase transitions and combat logic.

## 2. Tech Stack & Standards
-   **Language**: **Java 25** (Preview features enabled: Records, Pattern Matching, Virtual Threads anticipated).
-   **Framework**: **Spring Boot 3.x**
-   **Communication**: **WebSocket (STOMP)** over SockJS fallback.
-   **Data Structures**: Heavy usage of Java Records (`record`) for immutable DTOs and internal state snapshots.
-   **DI Pattern**: Constructor Injection via `lombok.@RequiredArgsConstructor`.

## 3. Architecture Map
The application resides in `net.lwenstrom.tft.backend`.

```text
src/main/java/net/lwenstrom/tft/backend/
├── BackendApplication.java       // Entry Point
├── config/
│   └── WebSocketConfig.java      // STOMP Setup (Broker: /topic, App: /app)
└── core/
    ├── GameController.java       // API Gateway: Handles WS messages & Scheduled Game Loop
    ├── DataLoader.java           // Static Data loading (Units/Traits JSON)
    ├── engine/                   // THE BRAIN (Game Logic)
    │   ├── GameEngine.java       // Singleton Manager of all GameRooms
    │   ├── GameRoom.java         // Instance of a match. Holds State + Players.
    │   ├── CombatSystem.java     // Logic for calculating damage/movement during COMBAT
    │   ├── Grid.java             // Hex/Grid logic
    │   └── Player.java           // Player entity (Gold, XP, Bench, Board)
    └── model/                    // THE DATA (DTOs/State)
        ├── GameState.java        // [Record] The "World State" sent to clients
        ├── GameAction.java       // [Record] Incoming commands (BUY, MOVE, etc.)
        └── GameUnit.java         // Mutable unit instance
```

## 4. The "Game Loop" Explained
Unlike a standard web app, this server "beats" like a heart.

1.  **Trigger**: `GameController.tick()` is `@Scheduled(fixedRate = 100)`.
2.  **Flow**: `GameController` -> `GameEngine.tick()` -> `GameRoom.tick()` -> `CombatSystem.simulate()` (if in COMBAT).
3.  **Phase Management**:
    -   `GameRoom` tracks time.
    -   **PLANNING** (30s): Shop open, moving units allowed.
    -   **COMBAT** (20s): Units move/attack automatically via `CombatSystem`.
4.  **Broadcast**: Immediately after every tick, the *entire* `GameState` is broadcast to `/topic/room/{roomId}`.
    -   *Note*: This is "Snapshot Synchronization" (sending the whole state), not just delta updates.

## 5. API & Event Intermediary
Communication is primarily asynchronous via WebSocket events.

### WebSocket Configuration
-   **Endpoint**: `/tft-websocket`
-   **Allowed Origins**: `*` (Dev mode)

### Channel Structure
| Channel / Destination | Direction | Payload Type | Description |
| :--- | :--- | :--- | :--- |
| `/tft-websocket` | Connect | - | Handshake URL |
| `/topic/room/{roomId}` | **S -> C** | `GameState` (JSON) | The full game state update (10Hz). |
| `/app/create` | **C -> S** | `RoomRequest` | Create a new room (auto-joins). |
| `/app/join` | **C -> S** | `RoomRequest` | Join an existing room. |
| `/app/room/{roomId}/action` | **C -> S** | `GameAction` | Player commands (Buy, Move, Reroll). |

### Action Payloads (JSON)
Incoming `GameAction` must match this structure:
```json
{
  "roomId": "room-123",
  "playerId": "player-abc",
  "type": "MOVE",  // Options: BUY, REROLL, EXP, MOVE
  // Optional Fields based on type:
  "shopIndex": 0,    // For BUY
  "unitId": "u-1",   // For MOVE
  "targetX": 4,      // For MOVE
  "targetY": 3       // For MOVE
}
```

### GameState Payload (JSON)
The `GameState` record is the Source of Truth for the UI.
```json
{
  "roomId": "...",
  "phase": "PLANNING", // or COMBAT
  "round": 1,
  "timeRemainingMs": 28500,
  "players": {
    "player-id": {
      "health": 100,
      "gold": 50,
      "bench": [ ... ],
      "board": [ ... ],
      "shop": [ ... ]
    }
  }
}
```

## 6. Key File Locations
-   **Main Entry**: [BackendApplication.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/BackendApplication.java)
-   **Game Loop (Scheduler)**: [GameController.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/GameController.java)
-   **Room Logic**: [GameRoom.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/engine/GameRoom.java)
-   **Data Models**: [GameState.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/model/GameState.java)
