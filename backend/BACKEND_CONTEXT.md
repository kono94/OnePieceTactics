# Backend Context - Architectural Blueprint

> **Last Updated**: 2026-01-25
> **Purpose**: Comprehensive technical reference for AI developers and engineers to understand the game server's architecture, game loop, and communication patterns.

---

## 1. System Overview

This backend is a **Stateful Game Server** for an Auto-Battler (Teamfight Tactics-style) game. It is the **single source of truth** for all game state. The frontend is purely a rendering layer that sends player actions and receives authoritative state updates.

**Key Characteristics**:
- **In-Memory State**: All game state (`GameRoom`, `Player`, `GameUnit`) is held in memory; no database persistence for match state.
- **Theme-Agnostic Core**: The engine core is generic (`GameUnit`, `Trait`, `Origin`). "One Piece" and "Pokemon" are themes loaded via configuration and `GameModeProvider` implementations.
- **WebSocket-First Communication**: Real-time state synchronization via STOMP over WebSocket.
- **Server-Side Authority**: All game logic (gold, XP, combat calculations) executes on the backend. The frontend cannot cheat.

---

## 2. Tech Stack & Standards

| Category | Value |
|----------|-------|
| **Java Version** | 25 (with preview features) |
| **Spring Boot Version** | 4.0.1 |
| **Build Tool** | Maven |
| **Dependency Injection** | Constructor Injection only (`@RequiredArgsConstructor` via Lombok) |
| **Code Formatting** | Spotless + Palantir Java Format (`mvn spotless:apply`) |
| **Key Libraries** | `spring-boot-starter-websocket`, `spring-boot-starter-web`, `lombok`, `jackson-databind` |

**Coding Standards** (Enforced by Project Guidelines):
- **`var` keyword**: Use explicitly for local variables.
- **Stream API**: Preferred over imperative loops for collection processing.
- **Records**: Used for DTOs and immutable data (`GameState`, `UnitDefinition`, `GameAction`).
- **No Field Injection**: `@Autowired` on fields is forbidden.
- **camelCase for Acronyms**: `userId`, `aiPlayer`, not `userID`, `AIPlayer`.

---

## 3. Architecture Map

```
src/main/java/net/lwenstrom/tft/backend/
├── BackendApplication.java         # Spring Boot entry point, enables @Scheduling
├── api/
│   └── InfoController.java         # REST: /api/config, /api/traits
├── config/
│   └── WebSocketConfig.java        # STOMP WebSocket configuration
├── core/                           # Theme-Agnostic Game Engine
│   ├── DataLoader.java             # Loads units/traits JSON based on active GameMode
│   ├── GameController.java         # WebSocket/REST handler, central dispatcher, @Scheduled tick
│   ├── GameModeProvider.java       # Interface for theme-specific data paths & trait effects
│   ├── GameModeRegistry.java       # Holds active GameModeProvider, configured via `game.mode` property
│   ├── combat/                     # Combat sub-system (Strategy Pattern)
│   │   ├── TargetSelector.java     # Interface: finds attack target
│   │   ├── NearestEnemyTargetSelector.java  # Implementation: nearest enemy by distance
│   │   ├── UnitMover.java          # Interface: moves unit towards target
│   │   ├── BfsUnitMover.java       # Implementation: BFS pathfinding movement
│   │   ├── AbilityCaster.java      # Interface: casts unit ability
│   │   ├── DefaultAbilityCaster.java  # Implementation: handles all ability types (DMG, STUN, HEAL, BUFF)
│   │   └── CombatUtils.java        # Static helpers (getDistance, isEnemy, isAlly)
│   ├── engine/                     # Core game loop & entities
│   │   ├── GameEngine.java         # Spring Service: manages GameRoom instances
│   │   ├── GameRoom.java           # Per-room state: players, phase, matchups, combat lifecycle
│   │   ├── Player.java             # Player entity: health, gold, level, board, bench, shop
│   │   ├── Grid.java               # 7x4 (planning) / 7x8 (combat) grid management
│   │   ├── CombatSystem.java       # Combat simulation per tick
│   │   ├── TraitManager.java       # Applies cumulative trait bonuses to units
│   │   ├── AbstractGameUnit.java   # Base unit with stats, position, mana, items
│   │   ├── StandardGameUnit.java   # Concrete GameUnit implementation
│   │   └── UnitDefinition.java     # Record: immutable unit template from JSON
│   ├── model/                      # Data Transfer Objects (Records)
│   │   ├── GameState.java          # Full room snapshot sent to frontend
│   │   ├── PlayerState.java        # Nested record inside GameState
│   │   ├── GameAction.java         # Incoming player action (BUY, MOVE, REROLL, etc.)
│   │   ├── ActionType.java         # Enum: BUY, SELL, MOVE, REROLL, EXP, LOCK
│   │   ├── GameUnit.java           # Interface: unit contract
│   │   ├── GameMode.java           # Enum: ONEPIECE, POKEMON
│   │   ├── GamePhase.java          # Enum: LOBBY, PLANNING, COMBAT
│   │   └── Trait.java, TraitEffect.java, AbilityDefinition.java, AbilityType.java, GameItem.java, LootOrb.java, LootType.java
│   ├── random/                     # Randomness abstraction for testability
│   │   ├── RandomProvider.java     # Interface: shuffle, nextInt, nextDouble
│   │   └── DefaultRandomProvider.java  # Production implementation (java.util.Random)
│   └── time/                       # Time abstraction for testability
│       ├── Clock.java              # Interface: currentTimeMillis()
│       └── SystemClock.java        # Production implementation (System.currentTimeMillis)
└── game/                           # Theme-Specific Implementations
    ├── onepiece/
    │   ├── OnePieceGameModeProvider.java  # Provides paths to One Piece data, registers traits
    │   ├── OnePieceTraitLoader.java
    │   └── traits/                 # One Piece trait effect implementations
    │       ├── StrawHatTrait.java
    │       └── FighterTrait.java
    └── pokemon/
        ├── PokemonGameModeProvider.java
        ├── PokemonTraitLoader.java
        └── traits/                 # Pokemon trait effect implementations
```

---

## 4. The "Game Loop" Explained

The backend operates on a **100ms tick loop** driven by Spring's `@Scheduled` annotation.

### 4.1 Tick Execution Flow

```
@Scheduled(fixedRate = 100)
GameController.tick()
    └── GameEngine.tick()
            └── for each GameRoom:
                    GameRoom.tick()
                        ├── [LOBBY phase] → No-op
                        ├── [PLANNING phase] → Check phase timeout → transition to COMBAT
                        └── [COMBAT phase] → CombatSystem.simulateTick() for each active matchup
                                             └── Check combat end → handleCombatEnd() → deduct HP
    └── Broadcast GameState to /topic/room/{id}
    └── Remove ended games from active rooms
```

### 4.2 Phase Transitions

| Phase | Duration | Trigger to Next Phase |
|-------|----------|----------------------|
| `LOBBY` | Infinite | Host sends `/app/start` message |
| `PLANNING` | 15s + (round-1) × 2s | Timer expiry |
| `COMBAT` | Same formula | Timer expiry OR all combats resolved |

**PLANNING Phase**:
1. Restore all units to their saved planning positions (post-combat).
2. Increment round counter.
3. Award passive income: `5 gold + min(currentGold/10, 5)` (interest).
4. Award XP: `2 XP`.
5. Refresh shop for all players (including bots).

**COMBAT Phase**:
1. Pair players randomly using `RandomProvider.shuffle()`.
2. For each pair, call `CombatSystem.startCombat()`:
   - Save unit planning positions.
   - Apply trait bonuses via `TraitManager.applyTraits()`.
   - Mirror one player's units to the "TOP" grid half (rows 0-3), opponent to "BOTTOM" (rows 4-7).
3. Each tick, `CombatSystem.simulateTick()` runs:
   - Units find targets, attack (if in range), or move closer.
   - Mana is gained on attack; abilities cast at full mana.
4. Combat ends when only one player has surviving units → loser takes `2 + survivingUnits` damage.
5. `CombatResultListener` emits `COMBAT_RESULT` event to `/topic/room/{id}/event` with winner/loser IDs and damage log.

### 4.3 Combat Simulation (`CombatSystem.simulateTick`)

```java
for each unit (not dead):
    if stunned (stunTicksRemaining > 0): decrement stun counter, skip turn
    if cooldown active: skip
    if mana full: cast ability, reset mana, set attack cooldown
    else:
        find target (NearestEnemyTargetSelector)
        if in range:
            attack with atkBuff multiplier
            deal damage, gain mana
            apply spdBuff to attack cooldown
        else: move towards target (BfsUnitMover using pathfinding)

check if only one player has living units → return CombatResult(ended=true, winnerId, damageLog)
```

**Key Interfaces (Strategy Pattern)**:
- `TargetSelector`: How to pick an enemy target.
- `UnitMover`: How to pathfind/move.
- `AbilityCaster`: How to execute ability logic.

These are injected into `CombatSystem`, enabling easy unit testing and swapping logic.

---

## 5. State Management

### 5.1 Where State Lives

| Entity | Scope | Lifecycle |
|--------|-------|-----------|
| `GameEngine` | Singleton (Spring `@Service`) | Application lifespan |
| `GameRoom` | One per active room | Created on `/app/create`, auto-removed when game ends |
| `Player` | One per player in a room | Lives inside `GameRoom.players` map |
| `GameUnit` | Per unit | Lives in `Player.boardUnits` or `Player.bench` |

### 5.2 Key State Objects

**`GameRoom`** holds:
- `phase: GamePhase` (LOBBY, PLANNING, COMBAT)
- `round: int`
- `players: Map<String, Player>`
- `activeCombats: List<List<Player>>` (pairs fighting)
- `currentMatchups: Map<String, String>` (player ID → opponent ID)
- `phaseEndTime: long`

**`Player`** holds:
- `health`, `gold`, `level`, `xp`
- `grid: Grid` (7×4 grid for planning)
- `boardUnits: List<GameUnit>`, `bench: List<GameUnit>`
- `shop: List<UnitDefinition>` (5 purchasable units)

### 5.3 Serialized State (`GameState` Record)

The entire room state is serialized as a `GameState` record and sent to all clients every tick:

```java
public record GameState(
    String roomId,
    String hostId,
    GamePhase phase,
    long round,
    long timeRemainingMs,
    long totalPhaseDuration,
    Map<String, PlayerState> players,
    Map<String, String> matchups,
    List<CombatEvent> recentEvents,
    GameMode gameMode
) { ... }
```

---

## 6. API & Event Contract

### 6.1 WebSocket Endpoints

**Connection**: `ws://<host>:8080/tft-websocket` (STOMP protocol)

| Destination | Direction | Payload | Description |
|-------------|-----------|---------|-------------|
| `/app/create` | Client → Server | `{ roomId, playerName }` | Create room, auto-join host |
| `/app/join` | Client → Server | `{ roomId, playerName }` | Join existing room |
| `/app/leave` | Client → Server | `{ roomId, playerName }` | Leave room |
| `/app/start` | Client → Server | `{ roomId, playerName }` | Host starts match (fills bots to 8) |
| `/app/room/{id}/action` | Client → Server | `GameAction` | Player action (BUY, MOVE, REROLL, EXP) |
| `/app/room/{id}/add-bot` | Client → Server | (none) | Add a bot to the room |
| `/topic/room/{id}` | Server → Client | `GameState` | Full state broadcast every 100ms |
| `/topic/room/{id}/event` | Server → Client | `{ type, payload }` | Combat result events (`COMBAT_RESULT`) |

### 6.2 `GameAction` Payload Structure

```json
{
  "type": "BUY" | "SELL" | "MOVE" | "REROLL" | "EXP" | "LOCK" | "COLLECT_ORB",
  "playerId": "uuid-string",
  "unitId": "uuid-string",       // For MOVE, SELL
  "targetX": 0-6,                // For MOVE
  "targetY": -1 to 7,            // For MOVE (-1 = bench)
  "shopIndex": 0-4,              // For BUY
  "orbId": "uuid-string"         // For COLLECT_ORB
}
```

### 6.3 REST Endpoints

| Endpoint | Method | Response | Description |
|----------|--------|----------|-------------|
| `/api/config` | GET | `{ "gameMode": "onepiece" }` | Current game mode |
| `/api/mode` | GET | `"onepiece"` | Active game mode enum value |
| `/api/traits` | GET | `[{...trait metadata}]` | Trait definitions for UI |

---

## 7. Testability Architecture

The backend was refactored for high testability using dependency injection for side-effects:

### 7.1 Time Abstraction

```java
public interface Clock {
    long currentTimeMillis();
}
// Production: SystemClock (uses System.currentTimeMillis())
// Test: MockClock (controllable time)
```

### 7.2 Random Abstraction

```java
public interface RandomProvider {
    <T> void shuffle(List<T> list);
    int nextInt(int bound);
    double nextDouble();
    Random getRandom();
}
// Production: DefaultRandomProvider (java.util.Random)
// Test: SeededRandomProvider (deterministic via seed)
```

### 7.3 Combat Strategy Interfaces

All combat behaviors are injectable:
- `TargetSelector`
- `UnitMover`
- `AbilityCaster`

This allows unit tests to mock specific behaviors without running full combat simulations.

---

## 8. Game Mode System

Themes are hot-swappable via the `game.mode` property (default: `onepiece`).

### 8.1 How It Works

1. `GameModeRegistry` collects all `GameModeProvider` beans.
2. On startup, selects provider matching `game.mode` property.
3. `DataLoader` uses provider to load correct JSON files.
4. `TraitManager` receives mode-specific trait effect registrations.

### 8.2 Implementing a New Theme

1. Create package under `game/<themename>/`.
2. Implement `GameModeProvider`:
   ```java
   @Component
   public class MyThemeProvider implements GameModeProvider {
       public GameMode getMode() { return GameMode.MY_THEME; }
       public String getUnitsPath() { return "/data/units_mytheme.json"; }
       public String getTraitsPath() { return "/data/traits_mytheme.json"; }
       public void registerTraitEffects(TraitManager tm) { /* register */ }
   }
   ```
3. Add `MY_THEME` to `GameMode` enum.
4. Create JSON data files in `src/main/resources/data/`.

---

## 9. Key File Locations Quick Reference

| Purpose | Path |
|---------|------|
| **Main Application** | `src/main/java/.../BackendApplication.java` |
| **Game Engine Service** | `src/main/java/.../core/engine/GameEngine.java` |
| **Game Room (State Holder)** | `src/main/java/.../core/engine/GameRoom.java` |
| **Combat System** | `src/main/java/.../core/engine/CombatSystem.java` |
| **WebSocket Config** | `src/main/java/.../config/WebSocketConfig.java` |
| **WebSocket/REST Controller** | `src/main/java/.../core/GameController.java` |
| **Data Loader** | `src/main/java/.../core/DataLoader.java` |
| **Game State DTO** | `src/main/java/.../core/model/GameState.java` |
| **Ability Type Enum** | `src/main/java/.../core/model/AbilityType.java` |
| **Ability Caster** | `src/main/java/.../core/combat/DefaultAbilityCaster.java` |
| **Loot Orb/Type** | `src/main/java/.../core/model/LootOrb.java`, `LootType.java` |
| **Unit Data (One Piece)** | `src/main/resources/data/units_onepiece.json` |
| **Trait Data (One Piece)** | `src/main/resources/data/traits_onepiece.json` |

---

## 10. Grid System

| Constant | Value | Notes |
|----------|-------|-------|
| `Grid.COLS` | 7 | Board width |
| `Grid.PLAYER_ROWS` | 4 | Each player's half |
| `Grid.COMBAT_ROWS` | 8 | Full combat board (4 + 4) |

**Planning Phase**: Each player sees their own 7×4 grid.

**Combat Phase**: Grids are merged:
- Player 1 (TOP): Units mirrored to rows 0-3.
- Player 2 (BOTTOM): Units placed on rows 4-7.

---

## 11. Dependency Injection Graph

```
BackendApplication
    └── ObjectMapper (Bean)

GameController (Singleton)
    ├── SimpMessagingTemplate (WebSocket sender)
    ├── GameEngine
    ├── DataLoader
    └── GameModeRegistry

GameEngine (Singleton)
    ├── DataLoader
    ├── GameModeRegistry
    ├── Clock
    └── RandomProvider

GameRoom (Per-Room Instance)
    ├── DataLoader
    ├── GameModeRegistry
    ├── Clock
    ├── RandomProvider
    ├── TraitManager (created per room)
    └── CombatSystem
            ├── TraitManager
            ├── Clock
            ├── TargetSelector
            ├── UnitMover
            └── AbilityCaster
```

---

## 12. Common Operations Cheat Sheet

| Task | Location | Method |
|------|----------|--------|
| Create a room | `GameEngine` | `createRoom()` |
| Add a player | `GameRoom` | `addPlayer(name)` |
| Buy a unit | `Player` | `buyUnit(shopIndex)` |
| Move a unit | `GameRoom` → `Player` | `moveUnit(unitId, x, y)` |
| Start match | `GameRoom` | `startMatch()` |
| Simulate combat tick | `CombatSystem` | `simulateTick(participants)` |
| Apply traits | `TraitManager` | `applyTraits(units)` |
| Refresh shop | `Player` | `refreshShop()` |
| Deal damage to player | `Player` | `takeDamage(amount)` |
| Sell unit | `Player` | `sellUnit(unitId, allowBoardSell)` |
| Collect loot orb | `Player` | `collectOrb(orbId)` |
| Process deferred upgrades | `Player` | `processPendingUpgrades()` |

---

## 13. Ability System

### 13.1 Ability Types (`AbilityType` Enum)

| Type | Effect | Value Meaning |
|------|--------|---------------|
| `DAMAGE` | Deal damage to enemies | Damage amount × star level |
| `STUN` | Target skips N combat ticks | Stun duration in ticks |
| `HEAL` | Restore HP to self or allies | Heal amount |
| `BUFF_ATK` | Increase ATK for all allied units | % increase (e.g., 20 = +20%) |
| `BUFF_SPD` | Decrease attack cooldown for allies | % increase to attack speed |

### 13.2 `AbilityDefinition` Record

```java
public record AbilityDefinition(
    String name,
    String description,  // NEW: Human-readable description for UI
    AbilityType type,    // NEW: Enum instead of implicit "DMG"
    String pattern,      // SINGLE, LINE, SURROUND
    int value,
    int range
) {
    // Factory for backward-compatible JSON parsing
    public static AbilityDefinition fromJson(String name, String description, String type, ...);
}
```

### 13.3 Ability Targeting Patterns (`DefaultAbilityCaster`)

| Pattern | DAMAGE/STUN Behavior | HEAL Behavior |
|---------|---------------------|---------------|
| `SINGLE` | Target nearest enemy | Heal lowest-health ally |
| `LINE` | All enemies in a line to range | N/A |
| `SURROUND` | All enemies within range radius | All allies within range |

### 13.4 Unit Status Effects

Units have temporary combat buffs that reset after each combat:

| Field | Type | Default | Purpose |
|-------|------|---------|---------|
| `stunTicksRemaining` | int | 0 | Skips turn while > 0, decrements each tick |
| `atkBuff` | float | 1.0f | Multiplier for attack damage |
| `spdBuff` | float | 1.0f | Multiplier for attack speed (affects cooldown) |

---

## 14. Damage Tracking System

### 14.1 How It Works

`CombatSystem` tracks all damage dealt during combat via `damageLog`:

```java
public record DamageEntry(String unitName, String definitionId, String ownerId, int damage) {}

// Accumulated per unit ID:
private Map<String, DamageEntry> damageLog;
```

- Damage accumulates from both **auto-attacks** and **abilities**.
- Negative damage values represent **healing** (for display purposes).
- Log is cleared at `startCombat()` and included in `CombatResult`.

### 14.2 Data Flow

1. `CombatSystem.simulateTick()` calls `accumulateDamage()` on each hit.
2. On combat end, `CombatResult.damageLog()` is passed to `GameRoom.handleCombatEnd()`.
3. `CombatResultListener.onCombatResult()` emits damageLog to frontend via WebSocket.
4. Live damage is also synced in `GameState.damageLog` every tick during combat.

---

## 15. Loot Orb System

### 15.1 Records

```java
public enum LootType { GOLD, UNIT }
public record LootOrb(String id, int x, int y, LootType type, String contentId, int amount) {}
```

### 15.2 Spawning Logic (`GameRoom.spawnLootOrbsForPlayer`)

- Orbs spawn on **even rounds** (round 2, 4, 6, ...) at the start of PLANNING phase.
- Each player receives **1-3 orbs** randomly placed on their grid (top half, rows 0-3).
- **70% chance**: Gold orb (3-8 gold).
- **30% chance**: Unit orb (random unit from pool).

### 15.3 Collection (`Player.collectOrb`)

- Triggered via `COLLECT_ORB` action from frontend.
- **Gold orbs**: Add gold to player.
- **Unit orbs**: Add unit to bench (if space), otherwise refund as gold.

---

## 16. Combat Phase Restrictions

### 16.1 Sell Restrictions

```java
// GameController.handleAction()
case SELL -> {
    // Allow selling bench units anytime, but board units only during PLANNING
    p.sellUnit(action.unitId(), room.getState().phase() == GamePhase.PLANNING);
}
```

| Unit Location | PLANNING Phase | COMBAT Phase |
|---------------|----------------|--------------|
| Bench | ✅ Can sell | ✅ Can sell |
| Board | ✅ Can sell | ❌ Cannot sell |

### 16.2 Deferred Star-Up

When a unit would upgrade during COMBAT phase, the upgrade is queued:

```java
public void processPendingUpgrades()  // Called at start of PLANNING phase
```

- Units don't visually upgrade mid-combat (avoids confusion).
- Pending upgrades are processed when PLANNING phase begins.

---

*This document is auto-generated by AI analysis. For implementation details, refer to the actual source code.*
