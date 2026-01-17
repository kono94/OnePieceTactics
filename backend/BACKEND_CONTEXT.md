# Backend Architecture & Context

## 1. System Overview
The backend is a **Stateful Real-Time Game Server** built with **Spring Boot 4+** and **Java 25**. It serves as the authoritative source of truth for the game, managing all logic, state, and simulation in-memory.

- **Role**: Validates actions, simulates combat, maintains game consistency, and broadcasts state updates.
- **Persistence**: Short-term in-memory storage (RAM). No database is currently used for match state.
- **Communication**: Event-driven architecture using **STOMP over WebSockets**.

## 2. Technology Stack
| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Java | 25 (Preview Features) |
| **Framework** | Spring Boot | 4.x |
| **Build Tool** | Maven | Latest |
| **Communication** | WebSocket (STOMP) | Spring Messaging |
| **Concurrency** | Virtual Threads | Implied readiness |
| **Utilities** | Lombok | Setup |

## 3. Architecture Map
The codebase is organized in `net.lwenstrom.tft.backend`:

- **`config`**: Configuration classes (e.g., `WebSocketConfig`).
- **`core`**: The heart of the game engine.
    - **`GameController`**: The bridge between WebSockets and the Engine. Ticks the loop.
    - **`DataLoader`**: Loads JSON resources (Units, Traits).
    - **`engine`**: Contains the simulation logic.
        - `GameEngine`: Manager of all `GameRoom` instances.
        - `GameRoom`: Represents a single match execution unit.
        - `CombatSystem`: Logic for pathfinding, attacking, and damage.
        - `Player`, `Grid`, `TraitManager`: Core entities.
    - **`model`**: Immutable Data Transfer Objects (DTOs) and Records.
        - `GameState`: The specific snapshot sent to clients.
        - `GameAction`, `GameEvent`: Messages.
- **`game`**: Theme-specific implementations (e.g., `onepiece`, `pokemon`) that load traits and factories.
- **`api`**: REST controllers for meta-info (minimal usage).

## 4. The Game Loop ("The Pulse")
The system follows a **Server-Authoritative Tick-Based** model.

1.  **The Beat**: `GameController` runs a `@Scheduled` task every **100ms**.
2.  **Engine Tick**: It calls `gameEngine.tick()`, which iterates over every active `GameRoom`.
3.  **Room Simulation**:
    -   Inside `GameRoom.tick()`, the engine checks the **Phase Timer**.
    -   If in **COMBAT Phase**, it calls `combatSystem.simulateTick()`:
        -   Units regain mana, move (using BFS/Euclidean heuristic), attack, or cast abilities.
        -   Damage is applied immediately.
    -   If Timer expires, it transitions phases (PLANNING <-> COMBAT).
4.  **Broadcast**: Immediately after the tick, `GameController` pushes the updated `GameState` to the specific room's WebSocket topic.

## 5. API & Event Intermediary
Communication is strictly **WebSocket-first**. REST is rarely used.

### WebSocket Configuration
- **Broker Endpoint**: `/tft-websocket`
- **Application Prefix**: `/app` (Client -> Server)
- **Topic Prefix**: `/topic` (Server -> Client)

### Message Protocols

| Direction | Destination | Type | Payload Structure (JSON) | Note |
| :--- | :--- | :--- | :--- | :--- |
| **C -> S** | `/app/create` | Room Setup | `{"roomId": "...", "playerName": "..."}` | Creates or Gets room |
| **C -> S** | `/app/join` | Room Setup | `{"roomId": "...", "playerName": "..."}` | Joins existing room |
| **C -> S** | `/app/room/{id}/action` | Gameplay | `{"type": "MOVE|BUY|REROLL|EXP", "playerId": "...", ...}` | See `GameAction` record |
| **S -> C** | `/topic/room/{id}` | State Sync | `GameState` (Compete JSON Snapshot) | Sent every ~100ms |
| **S -> C** | `/topic/room/{id}/event` | One-off | `{"type": "COMBAT_RESULT", "payload": {...}}` | For animations/toasts |

### Key Data Structures (Java Records)
- **GameAction**: `type`, `playerId`, `unitId`, `targetX`, `targetY`, `shopIndex`.
- **GameState**: `phase`, `round`, `timeRemainingMs`, `players` (Map), `matchups`.

## 6. Key File Locations
- **Main Entry**: `BackendApplication.java`
- **Game Loop Driver**: `core/GameController.java`
- **Room Logic**: `core/engine/GameRoom.java`
- **Combat Logic**: `core/engine/CombatSystem.java`
- **State Definition**: `core/model/GameState.java`

## 7. Development Guidelines
- **Zero Database Dependency**: All state is transient.
- **Constructor Injection**: Use `@RequiredArgsConstructor` and `final` fields.
- **Logic Separation**:
    - **Controllers** handle I/O.
    - **Rooms** handle Rules.
    - **Systems** (Combat) handle Math/Simulation.
