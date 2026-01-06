# Frontend Context & Architecture

This document provides a high-level overview of the frontend codebase for the TFT Clone project. It is intended to give backend developers and AI agents instant context on how the frontend is structured and how it communicates with the backend.

## Technology Stack
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **Language**: TypeScript
- **Styling**: Standard CSS (scoped in components)
- **WebSocket Client**: `@stomp/stompjs`

## Directory Structure (`frontend/src`)

- **`main.ts`**: Entry point. Mounts the Vue app.
- **`App.vue`**: The "God Component" / Controller. 
    - Manages the WebSocket connection.
    - Holds the central `gameState`.
    - Handles routing between `Lobby` and `GameInterface`.
    - Handles all WebSocket publishing (actions).
- **`components/`**:
    - **`Lobby.vue`**: UI for creating or joining a game room.
    - **`GameInterface.vue`**: The main game HUD.
        - Renders the Top Bar (Phase, Timer).
        - Renders the Bottom UI (Shop, Bench, Player Stats like Gold/XP).
        - Contains logic for drag-and-drop (Bench) and buying units/XP.
        - Emits `action` events up to `App.vue`.
    - **`GameCanvas.vue`**: Renders the game board (likely the visual representation of units on the field).
    - **`UnitTooltip.vue`**: Displays unit details on hover.

## Architecture & Data Flow

### 1. State Management
Currently, the application does **not** effectively use Pinia stores for game state.
- **Source of Truth**: The `gameState` ref in `App.vue`.
- **Updates**: The backend sends the full (or partial) game state via WebSocket. `App.vue` receives this and updates `gameState`.
- **Propagation**: `gameState` is passed down as a prop to `GameInterface`, which passes relevant parts to sub-components.

### 2. Event Handling
- User interactions (clicks, drags) in components (like `GameInterface`) do **not** directly modify state.
- Instead, they emit an `action` event with a payload (e.g., `{ type: 'BUY', shopIndex: 0 }`).
- `App.vue` listens for these events and forwards them to the backend via WebSocket.

### 3. WebSocket Communication
**Broker URL**: `ws://localhost:8080/tft-websocket`

#### Subscriptions (Inbound)
- **`/topic/room/${roomId}`**: Receives `GameState` updates.
  - Payload: JSON object representing the entire game state (players, board, phase, timer, etc.).

#### Publications (Outbound / Actions)
All actions are published by `App.vue`.

| Action | Destination | Payload Structure |
|--------|-------------|-------------------|
| **Create Game** | `/app/create` | `{ roomId: string, playerName: string }` |
| **Join Game** | `/app/join` | `{ roomId: string, playerName: string }` |
| **Game Action** | `/app/room/${roomId}/action` | `{ type: ActionType, ...args }` |

**Common Action Types**:
- `BUY`: `{ type: 'BUY', shopIndex: number, playerId: string }`
- `REROLL`: `{ type: 'REROLL', playerId: string }`
- `EXP`: `{ type: 'EXP', playerId: string }`
- `MOVE`: `{ type: 'MOVE', unitId: string, targetX: number, targetY: number, playerId: string }`
  - Note: `targetY: -1` typically indicates a move to/from the bench.

## Key Files for Context
- **`src/App.vue`**: Check this for the WS connection logic and the list of handled actions.
- **`src/components/GameInterface.vue`**: Check this for how the UI maps to game data (Shop, Bench) and how user inputs are captured.
