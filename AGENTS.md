# Project Guidelines

## Project Context
- Start with [README.md](README.md) for project overview, quick start, architecture, and API reference.
- Use [backend/BACKEND_CONTEXT.md](backend/BACKEND_CONTEXT.md) for backend architecture, game engine details, WebSocket contracts, and state management.
- Use [frontend/FRONTEND_CONTEXT.md](frontend/FRONTEND_CONTEXT.md) for Vue architecture, component structure, rendering responsibilities, and frontend data flow.

## 1. Tech Stack & Environment
- **Java**: Version 25 (Preview features enabled).
- **Spring Boot**: Version 4+ (Latest).
- **Frontend**: Vue.js 3 + Vite + TailwindCSS.
- **Build**: Maven (Backend), NPM (Frontend).
- **Containerization**: Docker & Docker Compose.

## 2. Java Coding Standards

### Modern Syntax
- Use `var` keyword whenever it is suitable (when using var a = new A(), or var m = List.of("d")) but not when getting something from methods that is not totally clear.
- Prefer **Java Stream API** over imperative loops for collections processing.
- Use **Records** (`record`) for DTOs and immutable data structures.
- Run `mvn spotless:apply` after completing a task in /backend folder to format the code

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

## 5. Misc
- when defining units in the .json files. When you define multiple values in an array and its just 3 numbers, keep them in one line and not multiple line

## 6. Changelog & Release Notes
- When adding a commit-worthy change or any balance change, also update the in-app changelog page in `frontend/src/components/Changelog.vue`.
- The changelog should list all commits after the latest git tag under a temporary next-version heading like `Version X.X.X`.
- Before creating a release tag, replace the temporary `Version X.X.X` heading with the actual git tag/version.
- Balance notes must include both new and previous values (passed through) and "=>" before the new values, and should visually distinguish buffs and nerfs when shown in the changelog UI.
- Follow the established balance-entry style: give each character or unit its own `.balance-block` with its name in the heading, and keep each old value, `=>` arrow, and new value in separate styled spans/elements. Do not combine multiple character changes into a wall of text.
- When reformatting an existing release, change only the presentation and preserve the existing changelog entries and values; do not add new changelog items or alter balance values unless explicitly requested.
