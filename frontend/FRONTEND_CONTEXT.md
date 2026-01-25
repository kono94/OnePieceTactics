# FRONTEND_CONTEXT.md

## High-Level Summary

**OnePieceTactics** is a real-time auto-battler game client built with Vue.js 3. Players manage units on a grid-based board, purchase champions from a shop, and watch automated combat unfold. The frontend is a thin client that renders authoritative game state received from the backend via STOMP WebSockets. All game logic (gold, combat, shop) is computed server-side; the frontend's responsibilities are purely display and user input relay.

The application supports **theme-swapping** between game modes (e.g., "One Piece" and "Pokemon") via configuration fetched from the backend at startup.

---

## Tech Stack

| Category            | Technology                   | Version   | Notes                                           |
|---------------------|------------------------------|-----------|-------------------------------------------------|
| **Framework**       | Vue.js                       | ^3.4.0    | Composition API with `<script setup>`           |
| **Build Tool**      | Vite                         | ^5.0.0    | Dev server with WebSocket proxy                 |
| **Language**        | TypeScript                   | ^5.2.0    | Used in components and data modules             |
| **State Management**| Pinia                        | ^2.1.7    | Installed but **not actively used**—state is prop-drilled from `App.vue` |
| **Real-Time Comm**  | @stomp/stompjs               | ^7.0.0    | WebSocket client for game state sync            |
| **Styling**         | Vanilla CSS (Scoped)         | -         | Component-scoped `<style scoped>` blocks        |
| **Linting**         | ESLint + Prettier            | -         | Configured via `eslint.config.ts`, `.prettierrc.json` |

---

## Folder Structure

```
frontend/
├── index.html                # HTML entry point
├── package.json              # Dependencies and scripts
├── vite.config.ts            # Vite config with /ws proxy to backend:8080
├── env.d.ts                  # TypeScript env declarations
├── eslint.config.ts          # ESLint configuration
├── .prettierrc.json          # Prettier formatting rules
│
├── public/
│   ├── favicon.svg           # Default favicon (One Piece)
│   ├── pokeball.png          # Pokemon mode favicon
│   └── assets/
│       └── units/            # Unit sprite images (e.g., luffy.png)
│
└── src/
    ├── main.ts               # Vue app bootstrap (createApp, mount)
    ├── App.vue               # Root component: WebSocket, routing, global state
    ├── style.css             # Global base styles (font, colors)
    │
    ├── components/           # All UI components
    │   ├── Lobby.vue         # Room create/join screen
    │   ├── WaitingRoom.vue   # Pre-game lobby with player list
    │   ├── GameInterface.vue # Main game wrapper (stats, bench, shop)
    │   ├── GameCanvas.vue    # Grid rendering, drag-and-drop, combat visuals
    │   ├── TraitSidebar.vue  # Active synergy/trait display
    │   ├── PlayerList.vue    # Scoreboard overlay (all players' HP/level)
    │   ├── UnitTooltip.vue   # Hover tooltip for unit stats
    │   ├── PhaseAnnouncement.vue  # Animated PLANNING/COMBAT banners
    │   ├── EndScreen.vue     # Game-over leaderboard and "Play Again"
    │   └── game/
    │       ├── AttackAnimation.vue  # Renders attack & ability visual effects
    │       ├── DamageReport.vue     # Collapsible damage tracking panel (post-combat stats)
    │       └── OutcomeOverlay.vue   # "ROUND WON/LOST" splash after combat
    │
    ├── types/
    │   ├── index.ts              # Central export for all game types
    │   └── game.ts               # TypeScript DTOs mirroring backend Java models
    │
    └── data/
        ├── animationConfig.ts  # Per-unit attack/ability animation config (type, color)
        └── traitData.ts        # Trait definitions & helpers (loaded from backend)
```

---

## Key Architectural Decisions

### 1. Component Style: `<script setup lang="ts">`

All components use Vue 3's **Composition API** with the `<script setup>` syntactic sugar. This provides:
- Automatic variable exposure to the template
- Cleaner, less boilerplate code
- TypeScript type inference with `defineProps<T>()` and `defineEmits()`

### 2. State Management: Centralized in `App.vue` (Props Down, Events Up)

Despite Pinia being installed, the application **does not use a Pinia store**. Instead:

- **`App.vue`** holds the single source of frontend truth:
  - `gameState` (reactive ref containing the full server state)
  - `currentRoomId`, `currentView`, `isConnected`
  - WebSocket `Client` instance and subscriptions

- **Data flows downward** via props:
  ```
  App.vue (gameState) 
    → GameInterface (:state, :current-player-name)
      → GameCanvas (:state, :my-player-id)
      → PlayerList (:players, :my-player-id)
      → TraitSidebar (:units)
  ```

- **Actions flow upward** via events:
  ```
  GameInterface (@action) 
    → App.vue (handleGameAction)
      → WebSocket publish to /app/room/{id}/action
  ```

**Rationale**: The entire game state is a single JSON blob pushed by the server. There's no need for granular client-side state updates; the server is authoritative.

### 3. Real-Time Communication: STOMP over WebSocket

| Aspect           | Implementation                                                    |
|------------------|-------------------------------------------------------------------|
| Connection       | `new Client({ brokerURL: 'ws://localhost:8080/tft-websocket' })` |
| Subscriptions    | `/topic/room/{roomId}` (state), `/topic/room/{roomId}/event` (events) |
| Actions          | Publish to `/app/room/{roomId}/action` with `{ type, playerId, ... }` |
| Lifecycle Events | `/app/create`, `/app/join`, `/app/start`, `/app/leave`           |

The `Client` lifecycle is managed in `App.vue`'s `onMounted` and `onUnmounted` hooks.

### 4. View Routing: Boolean State Machine

There is **no Vue Router**. Navigation is controlled by reactive refs:

```typescript
const currentView = ref<'lobby' | 'game'>('lobby')
```

Within the `game` view, conditional rendering switches between:
- `WaitingRoom` (when `gameState.phase === 'LOBBY'`)
- `GameInterface` (during PLANNING, COMBAT, END phases)

### 5. Styling: Scoped Vanilla CSS

- Each component uses `<style scoped>` for encapsulation.
- Color palette is consistent (slate/blue/amber tones):
  - Background: `#0f172a`, `#1e293b`
  - Accent: `#3b82f6` (blue), `#ef4444` (red), `#eab308` (gold)
- No CSS framework (Tailwind classes appear in `EndScreen.vue` as class names but are implemented as custom CSS, not Tailwind).

### 6. TypeScript Types: Strongly Typed DTOs

All game state and action types are defined in `src/types/game.ts`:

**Core Interfaces**:
- `GameState`: Root state object received from backend
- `PlayerState`: Individual player data (gold, health, board, bench, shop)
- `GameUnit`: Unit instance with stats, position, and combat effects
- `UnitDefinition`: Template for unit creation (shop display)
- `AbilityDefinition`: Ability metadata (name, description, type, pattern, value)
- `ActiveTrait`: Synergy state with breakpoint tracking
- `CombatEvent`: Real-time combat events (DAMAGE, SKILL, DEATH, MOVE)
- `DamageEntry`: Post-combat damage tracking per unit
- `LootOrb`: Collectible rewards on the grid

**Action Types**:
- `GameAction`: Union type for all player actions sent to backend
- `ActionType`: Enum of action types (BUY, SELL, MOVE, REROLL, EXP, COLLECT_ORB)

**Type Safety**:
- Components use `defineProps<T>()` for compile-time type checking
- DTOs mirror backend Java models for consistency
- Enums prevent invalid state values

### 7. Trait Data: Dynamic Loading from Backend

Trait definitions (synergy breakpoints, descriptions) are **not hardcoded**. On mount, `App.vue` fetches `/api/traits` and populates a global object:

```typescript
// data/traitData.ts
export const TRAIT_DATA: Record<string, TraitDefinition> = {};
export const setTraitData = (traits: TraitDefinition[]) => { ... };
```

This enables **theme-swapping** (One Piece → Pokemon) without frontend code changes.

### 8. Drag-and-Drop: Native HTML5 API

Unit placement uses the HTML5 Drag and Drop API:
- `draggable="true"` on units and bench slots
- `@dragstart`, `@dragover.prevent`, `@drop` handlers
- Visual feedback via `isDragging` and `dragOverCellIndex` reactive refs
- Pointer events disabled on non-dragged units during drag to enable grid cell drops

**Coordinate Mapping**:
- During PLANNING, player units (backend Y: 0–3) are visually rendered at Y: 4–7 (bottom half).
- On drop, visual Y is translated back: `backendY = dropY - 4`

**Sell Mechanic**:
- Dedicated sell zone below the bench
- Units can be sold from bench during combat phase
- Board units cannot be sold during combat (enforced by backend)

### 9. Game Grid Rendering

The grid is a CSS Grid with constants defined in `GameCanvas.vue`:

```typescript
const GRID_ROWS = 8
const GRID_COLS = 7
const PLAYER_ROWS = 4  // One player's half
const CELL_SIZE = 60   // px
```

Units are positioned absolutely within the grid container using inline styles.

### 10. Animation System: Per-Unit Attack & Ability Effects

Combat visuals are powered by a comprehensive animation system:

| File                        | Role                                                                  |
|-----------------------------|-----------------------------------------------------------------------|
| `data/animationConfig.ts`   | Defines per-unit attack types (`punch`, `slash`, `projectile`) and colors |
| `game/AttackAnimation.vue`  | Renders visual effects based on attack/ability type and pattern        |
| `GameCanvas.vue`            | Manages animation lifecycle, death effects, star-up celebrations       |

**Attack Types**:
- **punch**: Expanding impact ring (e.g., Luffy)
- **slash**: Rotating arc effect (e.g., Zoro)
- **projectile**: Flying orb from source to target (e.g., Nami)

**Ability Patterns** (determined by backend):
- **SINGLE**: Burst effect on target
- **LINE**: Beam from caster to target
- **SURROUND**: AoE ring centered on target

**Status Effect Visuals**:
- **Stun**: Grayscale filter with "STUNNED" badge overlay
- **Attack Buff**: Orange glow (`atkBuff > 1.01`)
- **Speed Buff**: Blue glow (`spdBuff > 1.01`)
- Multiple buffs stack additively with team border colors

**Unit Lifecycle Animations**:
- **Death Animation**: 600ms fade-out effect when units die during combat
- **Star-Up Celebration**: 1200ms particle burst when units level up (2★ or 3★)
- Animations tracked via reactive Sets and Maps to prevent duplicates

**Event-Driven System**:
- Watches `gameState.recentEvents` for DAMAGE, SKILL, DEATH events
- Deduplicates events using timestamps
- Animations auto-cleanup via `setTimeout` and emit `complete` event

### 11. Damage Report System

Post-combat damage tracking is displayed in a collapsible side panel:

**Features**:
- Automatically populated with `damageLog` from backend after each combat round
- Displays only the current player's units that participated in combat
- Shows unit icon, name, and total damage dealt
- Sorted by damage (highest to lowest)
- Visual damage bars scaled relative to highest damage dealer
- Collapsed by default to minimize screen clutter
- Fixed position on right side of screen with slide-in/out animation

**Implementation** (`DamageReport.vue`):
- Filters damage entries by `ownerId === myPlayerId`
- Reactive sorting and max damage calculation
- Unit icons loaded from `/assets/units/{definitionId}.png`
- Custom scrollbar styling for overflow content

### 12. Loot Orbs System

Loot orbs spawn on the grid after combat and provide rewards when collected:

**Types**:
- **GOLD**: Provides gold currency
- **UNIT**: Provides a random unit

**Visual Design**:
- Rendered as clickable orbs on the grid (top half, visual rows 0-3)
- Animated glow effect with emoji icons (🪙 for gold, 🎁 for items)
- Position calculated using `CELL_SIZE` grid constants

**Interaction**:
- Click to collect via `COLLECT_ORB` action
- Orbs stored in `PlayerState.lootOrbs` array
- Removed from state after collection

---

## Important File Paths

| Purpose                      | Path                                          |
|------------------------------|-----------------------------------------------|
| Vue App Entry                | `src/main.ts`                                 |
| Root Component               | `src/App.vue`                                 |
| Global Styles                | `src/style.css`                               |
| Main Game UI                 | `src/components/GameInterface.vue`            |
| Grid/Board Renderer          | `src/components/GameCanvas.vue`               |
| Damage Tracking Panel        | `src/components/game/DamageReport.vue`        |
| TypeScript Types             | `src/types/game.ts` (exported via `types/index.ts`) |
| Trait Definitions            | `src/data/traitData.ts`                       |
| Animation Config             | `src/data/animationConfig.ts`                 |
| Vite Config                  | `vite.config.ts`                              |
| Unit Assets                  | `public/assets/units/{definitionId}.png`      |

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                  BACKEND                                     │
│  (GameRoom, GameEngine, Player)                                              │
│                                                                              │
│   ┌──────────────────┐       ┌──────────────────┐                            │
│   │  REST API        │       │  WebSocket       │                            │
│   │  /api/config     │       │  /tft-websocket  │                            │
│   │  /api/traits     │       │                  │                            │
│   └────────┬─────────┘       └────────┬─────────┘                            │
└────────────┼─────────────────────────┼──────────────────────────────────────┘
             │                         │
             │ (on mount)              │ (continuous)
             ▼                         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                App.vue                                       │
│                                                                              │
│   • Fetches config/traits on mount                                           │
│   • Creates STOMP Client                                                     │
│   • Subscribes to /topic/room/{id} → updates gameState ref                   │
│   • Publishes actions to /app/room/{id}/action                               │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐    │
│   │                         REACTIVE STATE                               │    │
│   │  gameState: ref<any>     currentView: ref<'lobby'|'game'>            │    │
│   │  isConnected: ref        currentRoomId: ref                          │    │
│   └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────────────┐   │
│   │    Lobby     │  │ WaitingRoom  │  │        GameInterface             │   │
│   │              │  │              │  │  ┌────────┐ ┌───────┐ ┌────────┐ │   │
│   │  Create/Join │  │  Player Grid │  │  │ Stats  │ │ Bench │ │ Shop   │ │   │
│   │    Buttons   │  │  Start Btn   │  │  └────────┘ └───────┘ └────────┘ │   │
│   └──────────────┘  └──────────────┘  │  ┌────────────────────────────┐  │   │
│                                       │  │       GameCanvas           │  │   │
│                                       │  │  (Grid + Units + Tooltips) │  │   │
│                                       │  └────────────────────────────┘  │   │
│                                       │  ┌─────────────┐ ┌────────────┐  │   │
│                                       │  │TraitSidebar │ │ PlayerList │  │   │
│                                       │  └─────────────┘ └────────────┘  │   │
│                                       └──────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Responsibilities

| Component              | Responsibility                                                                 |
|------------------------|--------------------------------------------------------------------------------|
| `App.vue`              | WebSocket lifecycle, global state, view switching, event relay                 |
| `Lobby.vue`            | Create/join room forms, emits `create` and `join` events                       |
| `WaitingRoom.vue`      | Displays connected players, host can start game                                |
| `GameInterface.vue`    | Main game screen layout: stats panel, bench, shop, child components            |
| `GameCanvas.vue`       | Renders 8x7 grid, units, drag-and-drop, tooltips, animations, loot orbs        |
| `TraitSidebar.vue`     | Shows active traits/synergies with breakpoint progress and tooltips            |
| `PlayerList.vue`       | Displays all players' HP, level, sorted by health/elimination order            |
| `UnitTooltip.vue`      | Displays unit stats (HP, ATK, SPD, Range, Mana, Traits, Ability) with dynamic positioning |
| `PhaseAnnouncement.vue`| Animated banners for phase transitions (PLANNING PHASE / BATTLE START)        |
| `AttackAnimation.vue`  | Renders per-unit attack effects (punch, slash, projectile) and ability bursts  |
| `DamageReport.vue`     | Collapsible side panel showing damage dealt by each unit after combat          |
| `OutcomeOverlay.vue`   | Large "ROUND WON/LOST" splash after combat ends                                |
| `EndScreen.vue`        | Final leaderboard with "Play Again" reload button                              |

---

## Action Types (Emitted to Backend)

| Action Type   | Payload                                      | Description                     |
|---------------|----------------------------------------------|---------------------------------|
| `BUY`         | `{ shopIndex, playerId }`                    | Purchase unit from shop slot    |
| `SELL`        | `{ unitId, playerId }`                       | Sell unit for gold              |
| `REROLL`      | `{ playerId }`                               | Refresh shop (costs 2 gold)     |
| `EXP`         | `{ playerId }`                               | Buy XP (costs 4 gold)           |
| `MOVE`        | `{ unitId, targetX, targetY, playerId }`     | Move unit (board ↔ bench)       |
| `COLLECT_ORB` | `{ orbId, playerId }`                        | Collect loot orb (gold/unit)    |

---

## Conventions & Patterns

1. **No Router**: View state is a simple reactive string, toggled by WebSocket events.
2. **Prop Drilling**: Preferred over stores for this single-page, real-time app.
3. **Scoped Styles**: Every component encapsulates its own CSS.
4. **Responsive to Server State**: Frontend never computes game logic; it re-renders when `gameState` changes.
5. **Image Assets by ID**: Unit images follow the pattern `/assets/units/{definitionId}.png`.
6. **Transient Animations**: Phase announcements, ability casts, and outcome overlays auto-dismiss via `setTimeout`.
7. **Event Deduplication**: Combat events are deduplicated using timestamps to prevent animation spam.
8. **Dynamic Positioning**: Tooltips intelligently position above/below units based on grid location.
9. **Status Effect Stacking**: Visual effects (buffs, team borders) combine additively rather than overwriting.
10. **Lifecycle Cleanup**: All timers and animations are properly cleaned up on component unmount.

---

## Development Workflow

```bash
# Install dependencies
npm install

# Start dev server (with WebSocket proxy to backend)
npm run dev

# Build for production
npm run build
```

**Dev Server**: Runs on `http://localhost:5173` by default, proxies `/ws` to `http://localhost:8080`.

---

## Future Considerations

- **Pinia Store**: Currently unused. Could be leveraged for player settings, theme preferences, or cached data.
- **Vue Router**: If more views are added (e.g., profile, leaderboards), routing would be beneficial.
- **TypeScript Strictness**: Strong DTO interfaces have been added in `src/types/game.ts`. Some components still use `any` for nested properties—consider full strict typing.
- **Testing**: No tests present. Vitest + Vue Test Utils would be the natural choice.
- **Performance Optimization**: Consider virtualizing large lists (e.g., damage report with many units) for better performance.
