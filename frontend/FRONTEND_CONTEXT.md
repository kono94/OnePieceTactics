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
    │       └── OutcomeOverlay.vue # "ROUND WON/LOST" splash after combat
    │
    └── data/
        └── traitData.ts      # Trait definitions & helpers (loaded from backend)
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

### 6. Trait Data: Dynamic Loading from Backend

Trait definitions (synergy breakpoints, descriptions) are **not hardcoded**. On mount, `App.vue` fetches `/api/traits` and populates a global object:

```typescript
// data/traitData.ts
export const TRAIT_DATA: Record<string, TraitDefinition> = {};
export const setTraitData = (traits: TraitDefinition[]) => { ... };
```

This enables **theme-swapping** (One Piece → Pokemon) without frontend code changes.

### 7. Drag-and-Drop: Native HTML5 API

Unit placement uses the HTML5 Drag and Drop API:
- `draggable="true"` on units and bench slots
- `@dragstart`, `@dragover.prevent`, `@drop` handlers
- Visual feedback via `isDragging` and `dragOverCellIndex` reactive refs

**Coordinate Mapping**:
- During PLANNING, player units (backend Y: 0–3) are visually rendered at Y: 4–7 (bottom half).
- On drop, visual Y is translated back: `backendY = dropY - 4`

### 8. Game Grid Rendering

The grid is a CSS Grid with constants defined in `GameCanvas.vue`:

```typescript
const GRID_ROWS = 8
const GRID_COLS = 7
const PLAYER_ROWS = 4  // One player's half
const CELL_SIZE = 60   // px
```

Units are positioned absolutely within the grid container using inline styles.

---

## Important File Paths

| Purpose                      | Path                                          |
|------------------------------|-----------------------------------------------|
| Vue App Entry                | `src/main.ts`                                 |
| Root Component               | `src/App.vue`                                 |
| Global Styles                | `src/style.css`                               |
| Main Game UI                 | `src/components/GameInterface.vue`            |
| Grid/Board Renderer          | `src/components/GameCanvas.vue`               |
| Trait Definitions            | `src/data/traitData.ts`                       |
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
| `GameCanvas.vue`       | Renders 8x7 grid, units, drag-and-drop, unit tooltips, ability animations      |
| `TraitSidebar.vue`     | Shows active traits/synergies with breakpoint progress and tooltips            |
| `PlayerList.vue`       | Displays all players' HP, level, sorted by health/elimination order            |
| `UnitTooltip.vue`      | Displays unit stats (HP, ATK, SPD, Range, Mana, Traits) on hover               |
| `PhaseAnnouncement.vue`| Animated banners for phase transitions (PLANNING PHASE / BATTLE START)        |
| `OutcomeOverlay.vue`   | Large "ROUND WON/LOST" splash after combat ends                                |
| `EndScreen.vue`        | Final leaderboard with "Play Again" reload button                              |

---

## Action Types (Emitted to Backend)

| Action Type | Payload                                      | Description                     |
|-------------|----------------------------------------------|---------------------------------|
| `BUY`       | `{ shopIndex, playerId }`                    | Purchase unit from shop slot    |
| `REROLL`    | `{ playerId }`                               | Refresh shop (costs 2 gold)     |
| `EXP`       | `{ playerId }`                               | Buy XP (costs 4 gold)           |
| `MOVE`      | `{ unitId, targetX, targetY, playerId }`     | Move unit (board ↔ bench)       |

---

## Conventions & Patterns

1. **No Router**: View state is a simple reactive string, toggled by WebSocket events.
2. **Prop Drilling**: Preferred over stores for this single-page, real-time app.
3. **Scoped Styles**: Every component encapsulates its own CSS.
4. **Responsive to Server State**: Frontend never computes game logic; it re-renders when `gameState` changes.
5. **Image Assets by ID**: Unit images follow the pattern `/assets/units/{definitionId}.png`.
6. **Transient Animations**: Phase announcements, ability casts, and outcome overlays auto-dismiss via `setTimeout`.

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
- **TypeScript Strictness**: Some components use `any` for game state—consider defining strong DTO interfaces.
- **Testing**: No tests present. Vitest + Vue Test Utils would be the natural choice.
