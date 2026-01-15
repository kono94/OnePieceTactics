# Frontend Context & Architecture

> **Auto-Generated Source of Truth**
> Please update this file when making significant architectural changes.

## 1. High-Level Summary
The frontend is a **Real-Time Auto-Battler Client** built with **Vue 3** and **Vite**. It acts as a visual renderer for the authoritative Java backend.

The application follows a **Single Page Application (SPA)** model but primarily functions as a single "Game View" once connected. The core architecture relies on **Unidirectional Data Flow**: the Backend sends a complete snapshot of the `GameState` via WebSockets (STOMP), which is received by the root `App.vue` and propagated down to child components via props.

## 2. Technology Stack

| Category | Technology | Version | Notes |
| :--- | :--- | :--- | :--- |
| **Build Tool** | [Vite](https://vitejs.dev/) | ^5.0.0 | Fast HMR and build. |
| **Framework** | [Vue.js](https://vuejs.org/) | ^3.4.0 | Using **Composition API** (`<script setup>`). |
| **Language** | [TypeScript](https://www.typescriptlang.org/) | ^5.2.0 | Strict typing enabled. |
| **State Management** | Ref (Local) | - | **Pinia** is installed but currently **unused**. State is managed in `App.vue`. |
| **Networking** | [@stomp/stompjs](https://github.com/stomp-js/stompjs) | ^7.0.0 | WebSocket client for real-time game state sync. |
| **Styling** | Vanilla CSS | - | **TailwindCSS** is NOT currently implemented. Styles are written in Scoped CSS blocks. |

## 3. Directory Structure

```text
frontend/
├── src/
│   ├── components/            # UI Components
│   │   ├── game/              # Game-specific sub-components (overlays, etc.)
│   │   ├── EndScreen.vue      # Game over screen
│   │   ├── GameCanvas.vue     # The main grid/board renderer
│   │   ├── GameInterface.vue  # The HUD (Shop, Bench, Stats, Timer)
│   │   ├── Lobby.vue          # Entry screen (Create/Join room)
│   │   ├── PhaseAnnouncement.vue # Big text overlays for phase changes
│   │   ├── PlayerList.vue     # Right-side scoreboard/player list
│   │   ├── TraitSidebar.vue   # Left-side active traits display
│   │   └── UnitTooltip.vue    # Hover details for units
│   ├── data/                  # Static data definitions
│   │   └── traitData.ts       # Trait metadata (icons, tiers, descriptions)
│   ├── App.vue                # Root Component & "Smart" Container (WebSocket logic)
│   ├── main.ts                # Application Entry Point
│   └── style.css              # Global styles (resets, fonts)
├── index.html                 # HTML Entry point
├── package.json               # Dependencies
├── tsconfig.json              # TypeScript Config
└── vite.config.ts             # Vite Config
```

## 4. Key Architectural Decisions

### State Management Strategy
- **Centralized "Smart" Parent (`App.vue`)**:
  - The `App.vue` component acts as the Controller. It creates the WebSocket connection and subscribes to the game topic.
  - It holds the source-of-truth `gameState` object (received from Backend).
  - It handles all WebSocket publishing (actions like `BUY`, `MOVE`, `REROLL`).
- **Dumb Components**:
  - Components like `GameInterface`, `Lobby`, and `GameCanvas` receive `state` as props.
  - They emit events (e.g., `@action`) back up to `App.vue` to trigger server updates.
- **Pinia Usage**: While installed, Pinia stores are not currently the primary driver of state. The real-time nature of receiving full snapshots makes local stores less critical vs prop drilling the snapshot.

### API & Data Flow
1.  **Server -> Client**:
    -   Protocol: STOMP over WebSockets.
    -   Topic: `/topic/room/{roomId}` (State Updates), `/topic/room/{roomId}/event` (One-off events like Combat Results).
    -   Handling: `App.vue` parses the JSON payload and updates `gameState.value`.
2.  **Client -> Server**:
    -   Components emit events (e.g., `emit('action', { type: 'BUY', ... })`).
    -   `App.vue` listens to these events and publishes strict JSON commands to `/app/room/{roomId}/action`.

### Styling Approach
- **Scoped CSS**: Styles are encapsulated within each component's `<style scoped>` block.
- **Design System**: Currently ad-hoc. Flexbox is heavily used for layout. Colors are manually defined (e.g., `#0f172a` for background).

## 5. Important File Locations

-   **App Entry / State Controller**:
    `frontend/src/App.vue`
-   **Main Game HUD**:
    `frontend/src/components/GameInterface.vue`
-   **Game Board / Grid Implementation**:
    `frontend/src/components/GameCanvas.vue`
-   **WebSocket Client Setup**:
    Inside `App.vue` (`onMounted` hook).

## 6. Development Notes
-   **Running**: `npm run dev` starts the Vite server on port 5173 (usually).
-   **Connecting**: Requires the Backend Spring Boot server running on port 8080.
-   **Assets**: Images for units (Luffy, Zoro, etc.) are expected in `public/assets/units/`.
