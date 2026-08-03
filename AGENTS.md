# Theme Fusion Tactics (TFT) Project Guidelines

## Project Context
- Start with [README.md](README.md) for project overview, quick start, architecture, and API reference.
- Use [backend/BACKEND_CONTEXT.md](backend/BACKEND_CONTEXT.md) for backend architecture, game engine details, WebSocket contracts, and state management.
- Use [frontend/FRONTEND_CONTEXT.md](frontend/FRONTEND_CONTEXT.md) for Vue architecture, component structure, rendering responsibilities, and frontend data flow.
- Keep `AGENTS.md` for stable repository rules. Keep the context files focused on the current architecture, contracts, operational boundaries, and change checklists; do not use them as changelogs or duplicate source code line by line.
- Treat files under `archive/` as historical plans, not as current implementation guidance.

## 1. Tech Stack & Environment
- **Java**: Version 25 (Preview features enabled).
- **Spring Boot**: Version 4.x as pinned in `backend/pom.xml`.
- **Frontend**: Vue.js 3 + strict TypeScript + Vite + scoped vanilla CSS.
- **Build**: Maven (Backend), NPM (Frontend).
- **Containerization**: Docker & Docker Compose.

## 2. Java Coding Standards

### Modern Syntax
- Use `var` keyword whenever it is suitable (when using var a = new A(), or var m = List.of("d")) but not when getting something from methods that is not totally clear.
- Prefer **Java Stream API** for collection transformations when it improves clarity; keep indexed, mutation-heavy, or performance-sensitive algorithms imperative.
- Use **Records** (`record`) for DTOs and immutable data structures.
- Run `mvn spotless:apply` after completing a task in /backend folder to format the code

### Dependency Injection & IoC
- **Constructor Injection ONLY**.
- All injected fields must be `final`.
- Use Lombok `@RequiredArgsConstructor` when no custom constructor logic is needed; explicit constructors are appropriate for validation or configuration conversion.
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
- One Piece, Pokemon, and Palworld are skin/theme configurations, not hardcoded into the engine core.
- Use generic terms like `GameUnit`, `Trait`, and `GameModeProvider` in the core, and load specific data (Luffy, Pirate) from config/providers.

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
- Keep STOMP connection, subscriptions, and publishing in `App.vue`; child components receive state through props and emit typed UI events.
- Use server-issued player IDs for identity. Display names are presentation only.
- Keep `frontend/src/types/game.ts` aligned with backend wire records, including nullable fields and coordinate semantics.

### Frontend Coding Standards
- Use Vue Composition API with `<script setup lang="ts">`, strict TypeScript, and typed props/emits.
- Reuse shared DTO and domain types through `frontend/src/types`; do not create divergent component-local copies of backend contracts.
- Put reusable pure mappings and calculations in `frontend/src/utils` or `frontend/src/data` and add colocated Vitest coverage.
- Use scoped component CSS and existing CSS variables. Put only genuinely shared base styles in `frontend/src/style.css` or the intentional global style block in `App.vue`.
- Follow the repository Prettier configuration (no semicolons, single quotes, 100-column target) and run `npm test`, `npm run lint`, and `npm run build` for frontend changes.

## 4. Game Mechanics Constraints
- **Grid**: Each player plans on a 9×3 board; combat uses a 9×6 arena. MOVE uses x `0-8`, board y `0-2`, and y `-1` with x as the bench slot. Distance rules use Manhattan/Chebyshev distance as appropriate.
- **Combat**: Simplified Auto-Battler mechanics (Move to nearest -> Attack).
- **Themes**: One Piece is the initial room mode; the host can select One Piece, Pokemon, or Palworld during `LOBBY`. Do not add deployment-time mode selection or hardcode franchise behavior into the core.

## 5. Misc
- In unit JSON files, keep arrays of exactly three numeric values on one line.

## 6. Changelog & Release Notes
- When adding a commit-worthy change or any balance change, also update the in-app changelog page in `frontend/src/components/Changelog.vue`.
- The changelog should list all commits after the latest git tag under a temporary next-version heading like `Version X.X.X`.
- Before creating a release tag, replace the temporary `Version X.X.X` heading with the actual git tag/version.
- Balance notes must include both new and previous values (passed through) and "=>" before the new values, and should visually distinguish buffs and nerfs when shown in the changelog UI.
- Follow the established balance-entry style: give each character or unit its own `.balance-block` with its name in the heading, and keep each old value, `=>` arrow, and new value in separate styled spans/elements. Do not combine multiple character changes into a wall of text.
- When reformatting an existing release, change only the presentation and preserve the existing changelog entries and values; do not add new changelog items or alter balance values unless explicitly requested.

## 7. Manual Dependency Maintenance
- Dependency maintenance is explicitly initiated during development. Do not add scheduled Renovate workflows, dependency-dashboard automation, or dependency automerge to GitHub.
- Keep `renovate.json` as a local discovery configuration. Renovate's local platform is lookup-only: it inventories updates but does not edit files, create branches, or open pull requests.
- Start dependency work from a clean, current feature branch. Never perform a dependency upgrade directly on `main`.
- Inventory Maven, npm, Docker/Compose, and GitHub Actions updates from the repository root with `docker run --rm -e LOG_LEVEL=debug -v "$PWD:/usr/src/app:ro" -w /usr/src/app ghcr.io/renovatebot/renovate:44.8.0 --platform=local --dry-run=lookup`. The pinned container keeps Renovate's Node and tool requirements separate from the frontend runtime.
- Start Docker Desktop before using the container command.

### Non-Major Updates
- Patch, minor, pin, digest, and lockfile updates may be handled together in one maintenance branch.
- Review the Renovate lookup and upstream release notes, then apply updates with the native package manager or an explicit manifest edit. Renovate local lookup does not apply them.
- For npm, update `package.json` through `npm install` with the appropriate `--save-exact` and dependency-type flag so npm regenerates `package-lock.json`. Never hand-edit the lockfile, and never use `--force` or `--legacy-peer-deps` to hide peer conflicts.
- For Maven, update `backend/pom.xml`, run `mvn spotless:apply`, and then run `mvn -B verify` from `backend`.
- For Docker/Compose and GitHub Actions, verify tags and digests against the official registry or upstream repository. Keep GitHub Actions pinned to complete commit SHAs with the readable version in a comment.
- Run `npm test`, `npm run lint`, and `npm run build` from `frontend`, plus relevant container validation, before presenting the branch for review.

### Major Updates
- Handle exactly one major dependency or one tightly coupled toolchain family per branch. Never batch unrelated major upgrades.
- Read the official release notes and migration guide before editing. Identify peer-dependency, runtime, configuration, and source migrations, then apply them as part of the same branch.
- Regenerate all affected lockfiles with the native package manager, adapt application and build code, and run the complete backend and frontend validation suites.
- If no compatible dependency graph exists, leave the major version unchanged and document the upstream compatibility blocker. Do not force an unsupported installation.
- Record the completed upgrade and noteworthy migration effects in `frontend/src/components/Changelog.vue`, then submit the branch for human review and merge.
