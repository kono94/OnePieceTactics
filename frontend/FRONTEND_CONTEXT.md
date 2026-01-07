# FRONTEND_CONTEXT.md

## High-Level Summary
The One Piece TFT Clone frontend is a Vue.js 3 application designed as the visual interface and event gateway for a real-time multiplayer auto-battler. It operates under a **Backend Authority** model, where the frontend serves primarily as a state visualizer and user-action emitter via STOMP WebSockets.

## Tech Stack Table
| Core Library | Version | Role |
| :--- | :--- | :--- |
| **Vue.js** | `^3.4.0` | UI Framework (Composition API + `<script setup>`) |
| **Vite** | `^5.0.0` | Build Tool & Dev Server |
| **TypeScript** | `^5.2.0` | Language for Type Safety & DX |
| **@stomp/stompjs** | `^7.0.0` | WebSocket / Messaging Protocol |
| **Pinia** | `^2.1.7` | State Management (Installed, pending implementation) |

## folder-structure.md
```text
frontend/
├── dist/               # Production build output
├── public/             # Static assets (images, icons)
├── src/                # Application source code
│   ├── components/     # UI Components
│   │   ├── GameCanvas.vue      # unit/grid rendering logic
│   │   ├── GameInterface.vue   # HUD, Shop, Bench, and Action orchestration
│   │   ├── Lobby.vue           # Room creation/joining interface
│   │   ├── PhaseAnnouncement.vue # Animated phase transition overlays
│   │   ├── TraitSidebar.vue    # Active traits and synergies display
│   │   └── UnitTooltip.vue     # Hover detail card for units
│   ├── data/           # Static game data and helpers
│   │   └── traitData.ts        # Trait definitions, tiers, and icons
│   ├── App.vue         # Root component; manages WS connection & central state
│   ├── main.ts         # App entry point
│   ├── style.css       # Global base styles
│   └── env.d.ts        # TypeScript environmental definitions
├── index.html          # Main HTML entry
├── vite.config.ts      # Vite configuration
└── package.json        # Dependencies and scripts
```

## Key Architectural Decisions

1.  **Backend Authority**: The frontend does not manage authoritative game state (gold, HP, combat). It strictly renders the `GameState` received via WebSockets and sends user intentions (Actions) back to the server.
2.  **Centralized State (App.vue)**: Currently uses a "God Component" pattern. `App.vue` holds the single `gameState` ref, manages the WebSocket client, and handles all inbound/outbound messaging.
3.  **Data Flow**:
    - **Inbound**: Backend → WebSocket → `App.vue` (`gameState`) → Props → Components.
    - **Outbound**: UI Interaction → Event Emit (`@action`) → `App.vue` → WebSocket Publish → Backend.
4.  **Logic Sharing**: Uses simple utility functions and static data (e.g., `traitData.ts`) rather than complex Composables or Stores for now.

## Important File Paths
- **Entry Point**: [main.ts](file:///home/kono/projects/tft-clone/frontend/src/main.ts)
- **Main Controller**: [App.vue](file:///home/kono/projects/tft-clone/frontend/src/App.vue)
- **UI Orchestrator**: [GameInterface.vue](file:///home/kono/projects/tft-clone/frontend/src/components/GameInterface.vue)
- **Visual Engine**: [GameCanvas.vue](file:///home/kono/projects/tft-clone/frontend/src/components/GameCanvas.vue)
- **Trait Data**: [traitData.ts](file:///home/kono/projects/tft-clone/frontend/src/data/traitData.ts)
