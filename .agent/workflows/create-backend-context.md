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
    - Trace the "Tick" or "Simulation" logic (How does the game advance?).
    - formatting: Explain how `GameUnit` and `GameRoom` interact.
3.  **Communication & API Strategy**:
    - **WebSockets**: Locate the WebSocket config, message handlers, and the specific *JSON structure* of messages sent/received. This is CRITICAL for frontend alignment.
    - **REST API**: List key endpoints if they exist.
4.  **Java 25 & Spring Features**:
    - Note usage of modern features (Records, Pattern Matching, Virtual Threads/Project Loom, new Stream API features).
    - Identify Dependency Injection patterns (Constructor injection, Lombok usage).

## Output Requirements (The `BACKEND_CONTEXT.md` File in the /backend folder)
The generated file must contain:
1.  **System Overview**: A high-level summary of the backend's role (e.g., "Stateful Game Server").
2.  **Tech Stack & Standards**: Java version, Spring Boot version, Build tool (Gradle/Maven), and key libraries.
3.  **Architecture Map**: A tree view of the significant packages with 1-line descriptions of their duties.
4.  **The "Game Loop" Explained**: A specific section explaining how the backend handles the game simulation, threading, and state updates.
5.  **API & Event Intermediary**:
    - A table or list of known WebSocket Events (e.g., `Events.PLAYER_JOIN`, `Events.COMBAT_START`).
    - The expected Payload definition for these events.
6.  **Key File Locations**: Paths to the Main class, Game Loop Service, and WebSocket Configuration.

## Constraint
Focus relentlessly on the **"How"** and **"Why"**. Do not describe generic Spring concepts. Describe precisely how *this* specific Game Engine implementation works.