# One Piece TFT Clone - Phase 2 Implementation Plan

## Goal Description
The backend skeleton runs, but lacks gameplay logic. Phase 2 focuses on implementing the **Core Game Loop** and **Combat System**, and initializing the **Frontend**.

## User Review Required
> [!IMPORTANT]
> **Combat Logic**: We will implement a simplified auto-battler logic. Units move to nearest enemy and attack. No complex pathfinding for now.
> **Frontend**: Initiating a standard Vue 3 + Vite project.

## Proposed Changes

### Backend (Logic Implementation)
#### [MODIFY] [GameRoom.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/engine/GameRoom.java)
- Implement State Machine: `WAITING_FOR_PLAYERS` -> `PLANNING` -> `COMBAT` -> `ROUND_END`.
- Add timer logic for phases.

#### [NEW] [CombatSystem.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/engine/CombatSystem.java)
- Implement `simulateTick()`:
    - Iterate all units.
    - Find target (nearest enemy).
    - Move or Attack.
    - Update HP/Mana.

#### [MODIFY] [StandardGameUnit.java](file:///home/kono/projects/tft-clone/backend/src/main/java/net/lwenstrom/tft/backend/core/impl/StandardGameUnit.java)
- Add combat stats (current HP, mana, targetId).
- Implement `attack(GameUnit target)`.

### Frontend (Initialization)
#### [NEW] Frontend Project Structure
- Initialize Vue 3, Vite, TailwindCSS (for rapid UI).
- Setup `GameCanvas` component.
- Setup WebSocket client (`stompjs`).

## Verification Plan

### Automated Tests
- `GameRoomTest`: Verify phase transitions.
- `CombatTest`: Verify units damage each other and die.

### Manual Verification
1. Start backend.
2. Start frontend (`npm run dev`).
3. Connect and verify "Planning" phase timer counts down.
4. Verify "Combat" phase triggers and units move (visualized or via console logs).