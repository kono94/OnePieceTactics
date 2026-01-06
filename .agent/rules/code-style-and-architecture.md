---
trigger: always_on
---

# Project Guidelines

## 1. Tech Stack & Environment
- **Java**: Version 25 (Preview features enabled).
- **Spring Boot**: Version 4+ (Latest).
- **Frontend**: Vue.js 3 + Vite + TailwindCSS.
- **Build**: Maven (Backend), NPM (Frontend).
- **Containerization**: Docker & Docker Compose.

## 2. Java Coding Standards

### Modern Syntax
- Use `var` keyword explicitly for local variables.
- Prefer **Java Stream API** over imperative loops for collections processing.
- Use **Records** (`record`) for DTOs and immutable data structures.

### Dependency Injection & IoC
- **Constructor Injection ONLY**.
- All injected fields must be `final`.
- Use Lombok `@RequiredArgsConstructor` to generate constructors.
- **Forbidden**: Field injection (`@Autowired` on fields).

### Naming & Style
- `camelCase` for all variables, methods, and fields, including acronyms.
  - **Correct**: `userId`, `xmlRequest`, `aiPlayer`.
  - **Incorrect**: `userID`, `XMLRequest`, `AIPlayer`.
- **No Wildcard Imports**: Never use `import java.util.*;`. Explicitly import classes.

### Comments & Documentation
- **No Javadoc** for classes or methods.
- **No bloated comments** explaining "what" the code does (code should be self-documenting).
- **Exception**: Comment only highly complex algorithms or non-obvious business logic.

## 3. Architecture & Design Principles

### Clean Abstract Core
- The core game engine **MUST** be theme-agnostic.
- "One Piece" is a skin/theme configuration, not hardcoded into the engine core.
- Use generic terms like `GameUnit`, `Trait`, `Origin` in the core, and load specific data (Luffy, Pirate) from config/factories.

### State Management
- **Backend Authority**: The Backend is the single source of truth for `GameState`.
- **In-Memory**: Game state is held in memory (no database persistence for match state).

### Communication
- Use **STOMP WebSockets** for real-time state sync.
- Prefer event-driven updates over polling.

### Frontend Responsibilities
- Render the `GameState` received from Backend.
- Send user actions (Move, Buy, Sell) as events to Backend.
- Do not implement authoritative logic (e.g., verifying gold) on Frontend.

## 4. Game Mechanics Constraints
- **Grid**: Square grid logic (Backend checks neighbors using Manhattan/Chebyshev distance as appropriate).
- **Combat**: Simplified Auto-Battler mechanics (Move to nearest -> Attack).
- **Theme**: One Piece (Initial implementation), but capable of hot-swapping to Pokemon/etc via config.