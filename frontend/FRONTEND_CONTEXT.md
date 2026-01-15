# Frontend Context (Vue.js 3 + Vite)

## High-Level Summary
This is the frontend application for a **Multiplayer Auto-Battler** game (TFT-clone). It serves as a visual renderer and input interface for the game state, which is fully managed by the Java backend. The application is designed to support multiple themes (e.g., One Piece, Pokemon) via configuration, but the display logic is primarily data-driven based on the `GameState` received from the server.

## Tech Stack & Configuration

| Category | Technology | Version | Notes |
| :--- | :--- | :--- | :--- |
| **Framework** | Vue.js 3 | ^3.4.0 | Using Composition API (`<script setup>`) |
| **Language** | TypeScript | ~5.2.0 | Strict typing enabled |
| **Build Tool** | Vite | ^5.0.0 | Fast HMR, Proxy configured for backend |
| **State Mgmt** | Reactive `ref` / Props | N/A | **Pinia is installed but currently UNUSED.** State is managed centrally in `App.vue`. |
| **Networking** | @stomp/stompjs | ^7.0.0 | WebSocket communication for real-time game state |
| **Styling** | Vanilla CSS | N/A | Scoped styles + `style.css`. No Tailwind CSS or Preprocessors. |

## Folder Structure

```
frontend/
├── public/                 # Static assets (favicons, images)
├── src/
│   ├── components/         # UI Components (Dumb & Smart)
│   │   ├── game/           # In-game specific overlays (e.g. OutcomeOverlay)
│   │   ├── GameInterface.vue # Main wrapper for the battle view
│   │   ├── Lobby.vue       # Initial entry screen
│   │   └── ...             # Smaller widgets (PlayerList, TraitSidebar, etc.)
│   ├── data/               # Static Typescript data files
│   │   └── traitData.ts    # Frontend definitions for traits/synergies
│   ├── App.vue             # Root Component & "Controller" (WebSocket Logic here)
│   ├── main.ts             # Application Entry Point
│   └── style.css           # Global basic styles
├── index.html              # HTML Entry point
├── package.json            # Dependencies
└── vite.config.ts          # Vite Config (Proxy: /ws -> localhost:8080)
```

## Key Architectural Decisions

### 1. State Management (The "Prop-Drill" Approach)
*   **Strategy**: The application does **not** use a global store (like Pinia) for the Game State.
*   **Implementation**: 
    *   `App.vue` holds the master `gameState` object as a `ref`.
    *   This state is passed down as a prop to `GameInterface.vue` and then to children.
    *   **Reasoning**: Single Source of Truth constraint. The Frontend strictly renders what the Backend sends.

### 2. Networking & Data Flow
*   **Protocol**: **STOMP over WebSockets**.
*   **Controller**: `App.vue` initializes the `Client`, manages the connection lifecycle, and handles subscriptions (`/topic/room/{id}`).
*   **Flow**:
    1.  **Read**: Backend pushes JSON `GameState` -> `App.vue` receives -> Updates `gameState` ref -> UI Reacts.
    2.  **Write**: User clicks -> Component emits `@action` -> `App.vue` catches -> Publishes message to `/app/...`.
*   **HTTP**: Used strictly for initial fetching of "Global Config" (e.g., detecting game mode).

### 3. Component Design
*   **Style**: Pure **Composition API** with `<script setup lang="ts">`.
*   **Smart vs. Dumb**:
    *   **Smart**: `App.vue` (Connects to WS), `GameInterface.vue` (Orchestrates layout).
    *   **Dumb**: Most other components (Receive data via props, emit events for interactions).
*   **Themes**: The app dynamically changes title/favicon based on the `gameMode` config fetched on mount.

## Important File Paths
*   **Main Logic**: `src/App.vue` - Contains the WebSocket client, state container, and event handling.
*   **Entry Point**: `src/main.ts` - Mounts the app (Note: Pinia is NOT loaded here).
*   **Proxy Config**: `vite.config.ts` - Maps `/ws` requests to the backend at `localhost:8080`.
*   **Styles**: `src/style.css` - Basic global resets and variables.
