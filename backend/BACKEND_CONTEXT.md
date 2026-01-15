# Backend Architectural Context

## 1. System Overview
The backend is a **Stateful Game Server** built with **Spring Boot 4** and **Java 25**. It acts as the authoritative source of truth for the "One Piece Tactics" Auto Battler. It manages the game loop, combat simulation, and state synchronization entirely in-memory, without a persistent database for match state.

**Core Responsibilities:**
*   **Game Loop Authority**: Runs a deterministic 10Hz tick loop to drive phases and combat.
*   **State Management**: Holds the `GameRoom` and `GameState` in memory.
*   **Real-time Sync**: Pushes full state updates to clients via STOMP WebSockets.
*   **Combat Simulation**: Resolves auto-battler logic (pathing, attacking, damage) on the server.

---

## 2. Tech Stack & Standards
| Component | Technology | Version | Notes |
| :--- | :--- | :--- | :--- |
| **Language** | Java | **25** | Uses Preview Features (Records, Pattern Matching) |
| **Framework** | Spring Boot | **4.0.1** | Latest Spring ecosystem |
| **Build Tool** | Maven | Latest | Uses `spotless` for formatting |
| **Communication** | WebSocket | STOMP | `spring-boot-starter-websocket` |
| **Data Mapping** | Jackson | Latest | JSON serialization |
| **Boilerplate** | Lombok | Latest | `@RequiredArgsConstructor` for IoC |

**Coding Standards:**
*   **DTOs**: Use Java `record` exclusively for data transfer (GameState, GameAction).
*   **Injection**: Constructor Injection (`final` fields + `@RequiredArgsConstructor`).
*   **State**: In-memory `ConcurrentHashMap` for active rooms.
*   **Formatting**: Enforced via `mvn spotless:apply`.

---

## 3. Architecture Map
The codebase is structured within `net.lwenstrom.tft.backend`:

```text
backend/
├── config/
│   └── WebSocketConfig.java       # STOMP Endpoint configuration
├── core/
│   ├── DataLoader.java            # Loads Units/Traits from JSON/Static data
│   ├── GameController.java        # WebSocket Entrypoint & Main Tick Host
│   ├── engine/                    # CORE DISPATCHER & LOGIC
│   │   ├── GameEngine.java        # Service managing multiple GameRooms
│   │   ├── GameRoom.java          # State Machine for a single match (Phases, Loop)
│   │   ├── CombatSystem.java      # Combat simulation logic (Pathing, Damage)
│   │   ├── Player.java            # Player state (Gold, XP, Board logic)
│   │   └── Grid.java              # 7x8 Hex/Grid logic
│   └── model/                     # DATA TRANSFER OBJECTS (Records)
│       ├── GameState.java         # The "Snapshot" sent to frontend
│       └── GameAction.java        # Incoming commands (Move, Buy, etc.)
```

---

## 4. The "Game Loop" Explained
The game does NOT rely on client-side updating for logic. It is a **Server-Authoritative** loop.

### A. The Tick
1.  **Driver**: `GameController.java` has a `@Scheduled(fixedRate = 100)` method.
2.  **Flow**:
    *   `GameController` -> `GameEngine.tick()` -> `GameRoom.tick()`
    *   **Planning Phase**: Updates timers. Checks for phase transitions.
    *   **Combat Phase**: Calls `CombatSystem.simulateTick()` for every active pair of players.
3.  **Broadcasting**: After *every* 100ms tick, the new `GameState` is broadcast to `/topic/room/{id}`.

### B. State Logic (`GameRoom.java`)
*   **Phases**: `PLANNING` (Buy/Move units) -> `COMBAT` (Simulation) -> `END`.
*   **Units**: Stored in `Player.boardUnits` (List). Backend calculates positions.
*   **Combat**:
    *   Pairs players randomly.
    *   Runs simulation ticks (Move -> Attack -> Mana -> Spell).
    *   If a fight ends, it emits `COMBAT_RESULT` event but waits for all fights to end or timeout before next phase.

---

## 5. API & Event Intermediary
Communication is exclusively **STOMP over WebSockets**.

### Connection Details
*   **Endpoint**: `/tft-websocket`
*   **Allowed Origins**: `*` (configured for dev)

### Client -> Server (Commands)
Sent to `/app/...`.
| Destination | Payload (JSON) | Description |
| :--- | :--- | :--- |
| `/app/create` | `RoomRequest { roomId, playerName }` | Creates a room and adds user |
| `/app/join` | `RoomRequest { roomId, playerName }` | Joins an existing room |
| `/app/room/{id}/action` | `GameAction` | Gameplay commands |

**`GameAction` Structure:**
```json
{
  "type": "MOVE",       // BUY, SELL, REROLL, EXP, MOVE
  "playerId": "...",
  "unitId": "...",      // For MOVE/SELL
  "targetX": 0,         // For MOVE
  "targetY": 0,         // For MOVE
  "shopIndex": 0        // For BUY
}
```

### Server -> Client (Updates)
Subscribed to `/topic/...`.
| Topic | Payload | Frequency | Description |
| :--- | :--- | :--- | :--- |
| `/topic/room/{id}` | `GameState` (JSON) | **10Hz** | Full snapshot of the game. Contains Players, Board, Timer, Phase. |
| `/topic/room/{id}/event` | `GameEvent` (JSON) | On Occurence | One-off events (e.g., Damage dealt, Match end). |

**`GameEvent` Structure:**
```json
{
  "type": "COMBAT_RESULT",
  "payload": {
    "winnerId": "...",
    "loserId": "...",
    "damageDealt": 5
  }
}
```

---

## 6. Key File Locations
*   **Main Application**: `src/main/java/net/lwenstrom/tft/backend/BackendApplication.java`
*   **Game Loop Driver**: `src/main/java/net/lwenstrom/tft/backend/core/GameController.java`
*   **State Machine**: `src/main/java/net/lwenstrom/tft/backend/core/engine/GameRoom.java`
*   **Data Models**: `src/main/java/net/lwenstrom/tft/backend/core/model/GameState.java`
