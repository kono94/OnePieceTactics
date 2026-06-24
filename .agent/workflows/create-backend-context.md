---
description: Create context and important information from the whole backend project for the AI to have an entrypoint and understanding where everything is and how it works
---

@backend

## Role
Act as a **Senior Java Backend Engineer & Systems Architect** specializing in Modern Java (Java 25) and Spring Boot.

## Objective
Thoroughly analyze the internal backend codebase and generate a technical `BACKEND_CONTEXT.md` file. This file will serve as the "Architectural Blueprint" and "Source of Truth" for an AI developer to understand the system's brain, game loop, and communication patterns without manual file searching.

## Analysis Instructions
1.  **Package & Layer Analysis**:
    -Map the `src/main/java` directory.
    - Strictly define the responsibility of each package (e.g., `core/engine`, `model`, `controller`, `websocket`).
    - Identify the separation of concerns: Is it a standard Layered Architecture? Hexagonal? Or a custom Game Loop architecture?
2.  **Game Mechanics & State Management**:
    - Identify where the `GameState` is held (In-memory, singleton, or per-room instance?).
    - Note whether game mode is global only or can be changed per room, and how room mode changes reset data/state.
    - Trace the "Tick" or "Simulation" logic (How does the game advance?).
    - Explain how `GameUnit`, `Player`, and `GameRoom` interact.
    - Document unit definitions, star-level forms/evolutions, and how upgrades can change unit id/name/traits/range/ability.
    - Call out theme-specific combat rules such as Pokemon type effectiveness, if present.
3.  **Communication & API Strategy**:
    - **WebSockets**: Locate the WebSocket config, message handlers, and the specific *JSON structure* of messages sent/received. This is CRITICAL for frontend alignment.
    - Include room-scoped control messages such as mode changes in the WebSocket event list.
    - **REST API**: List key endpoints if they exist, including config values such as default and available game modes.
4.  **Java 25 & Spring Features**:
    - Note usage of modern features (Records, Pattern Matching, Virtual Threads/Project Loom, new Stream API features).
    - Identify Dependency Injection patterns (Constructor injection, Lombok usage).
5.  **Data-Driven Content & Tests**:
    - Describe how units and traits are loaded for each mode, including eager vs lazy loading/caching.
    - Identify generic trait effect types, target scopes, custom handlers, and ability modifiers.
    - Mention important validation/regression tests that protect mode data, evolutions/forms, combat modifiers, and type damage.

## Output Requirements (The `BACKEND_CONTEXT.md` File in the /backend folder)
The generated file must contain:
1.  **System Overview**: A high-level summary of the backend's role (e.g., "Stateful Game Server").
2.  **Tech Stack & Standards**: Java version, Spring Boot version, Build tool (Gradle/Maven), and key libraries.
3.  **Architecture Map**: A tree view of the significant packages with 1-line descriptions of their duties.
4.  **The "Game Loop" Explained**: A specific section explaining how the backend handles the game simulation, threading, and state updates.
5.  **API & Event Intermediary**:
    - A table or list of known WebSocket Events (e.g., `Events.PLAYER_JOIN`, `Events.COMBAT_START`).
    - The expected Payload definition for these events.
    - REST responses that the frontend depends on, especially mode/config payloads.
6.  **Key File Locations**: Paths to the Main class, Game Loop Service, and WebSocket Configuration.

## Constraint
Focus relentlessly on the **"How"** and **"Why"**. Do not describe generic Spring concepts. Describe precisely how *this* specific Game Engine implementation works.
