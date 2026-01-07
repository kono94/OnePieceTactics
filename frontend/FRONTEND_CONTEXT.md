# FRONTEND_CONTEXT.md

## High-Level Summary
The One Piece TFT Clone frontend is a **Vue.js 3** application acting as a real-time visualizer and input interface for the Java/Spring backend. It follows a strict **Backend Authority** model where the frontend holds no game logic, only rendering the snapshot of the `GameState` received via WebSockets (STOMP). It uses a "God Component" architecture centered around `App.vue` for state management and communication.

## Tech Stack Table
| Core Library | Version | Role | Status |
| :--- | :--- | :--- | :--- |
| **Vue.js** | `^3.4.0` | UI Framework (Composition API + `<script setup>`) | Active |
| **Vite** | `^5.0.0` | Build Tool & Dev Server | Active |
| **TypeScript** | `^5.2.0` | Language | Active |
| **@stomp/stompjs** | `^7.0.0` | WebSocket / Messaging Protocol | Active |
| **Pinia** | `^2.1.7` | State Management | Installed, **Unused** |
| **TailwindCSS** | N/A | Utility-first CSS | **Not Installed** (Vanilla CSS used) |

> **Note**: While project guidelines mention TailwindCSS, the current implementation relies entirely on Vanilla CSS (Global variables in `style.css` + Scoped Component Styles).

## folder-structure.md
```text
frontend/
├── dist/                   # Production build output
├── public/                 # Static assets (images, icons)
├── src/                    # Application source code
│   ├── components/         # UI Components
│   │   ├── game/           # In-game specific overlays
│   │   │   └── OutcomeOverlay.vue # "Victory/Defeat" splash text
│   │   ├── EndScreen.vue   # Match conclusion screen
│   │   ├── GameCanvas.vue  # CORE: Grid, Units, Drag & Drop, Animations
│   │   ├── GameInterface.vue # HUD Wrapper, Shop, XP Bar, Bench
│   │   ├── Lobby.vue       # Room creation/joining form
│   │   ├── PhaseAnnouncement.vue # "Planning" / "Combat" transition animations
│   │   ├── PlayerList.vue  # Right-side leaderboard/player status
│   │   ├── TraitSidebar.vue # Left-side trait synergies & counters
│   │   └── UnitTooltip.vue # Hover detail card for units
│   ├── data/               # Static game data
│   │   └── traitData.ts    # Trait definitions, icons, and descriptions
│   ├── App.vue             # ROOT: WebSocket Client, Global State, View Switching
│   ├── main.ts             # Entry point
│   ├── style.css           # Global CSS variables & reset
│   └── env.d.ts            # TS definitions
├── index.html              # HTML entry
├── vite.config.ts          # Vite configuration
└── package.json            # Dependencies
```

## Key Architectural Decisions

### 1. Backend Authority & State
- **Single Source of Truth**: The Backend is the sole authority. The frontend does not calculate damage, gold interest, or movement.
- **State Object**: `App.vue` receives a massive JSON `GameState` via `/topic/room/{id}` and replaces the local `gameState` ref entirely or incrementally updates it. This state is prop-drilled down to `GameInterface` -> `GameCanvas`.

### 2. Communication Strategy (STOMP)
- **Centralized Handler**: `App.vue` owns the `Client` instance.
- **Inbound**:
    - **State Update**: `/topic/room/{id}` -> Parsed -> Replaces `gameState`.
    - **Events**: `/topic/room/{id}/event` -> Parsed -> Triggers instant UI effects (e.g., `COMBAT_RESULT` for the Overlay).
- **Outbound**: Components emit generic `@action` events up to `App.vue`, which publishes to `/app/room/{id}/action`.

### 3. Component Hierarchy
- **App.vue**: The "God Component". Manages Connection, Routing (Lobby vs Game).
- **GameInterface.vue**: Layout orchestrator. Arranges Sidebar, Canvas, and Player List.
- **GameCanvas.vue**: The heaviest component.
    - Handles Grid logic (8x7).
    - **Coordinate Flipping**: Mirrors the grid if the player is "Player 1" (backend y: 0-3) to ensure self is always drawn at the bottom.
    - **Drag & Drop**: Native HTML5 Drag API. Translates visual drop coordinates back to backend coordinates before emitting.
    - **Visual Interpolation**: Basic CSS transitions for smoothing, but relies on tick frequency.

### 4. Styling & Theming
- **Approach**: Scoped CSS blocks (`<style scoped>`) in single-file components.
- **Design System**: None formal. Uses literal hex codes (e.g., `#1e293b`) and ad-hoc transparency.
- **Assets**: Unit images loaded from `/assets/units/..`.

## Important File Paths
- **Entry Point**: [main.ts](file:///home/kono/projects/tft-clone/frontend/src/main.ts)
- **App Controller**: [App.vue](file:///home/kono/projects/tft-clone/frontend/src/App.vue)
- **Grid & Rendering**: [GameCanvas.vue](file:///home/kono/projects/tft-clone/frontend/src/components/GameCanvas.vue)
- **Layout Manager**: [GameInterface.vue](file:///home/kono/projects/tft-clone/frontend/src/components/GameInterface.vue)
- **Combat Overlay**: [OutcomeOverlay.vue](file:///home/kono/projects/tft-clone/frontend/src/components/game/OutcomeOverlay.vue)
