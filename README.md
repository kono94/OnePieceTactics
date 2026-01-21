# One Piece Tactics

A browser-based **auto-battler game** inspired by Teamfight Tactics, featuring a theme-swappable engine with One Piece (default) and Pokemon themes. This project showcases a clean, production-grade architecture with real-time multiplayer via WebSockets.

![Java 25](https://img.shields.io/badge/Java-25-orange) ![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.0.1-green) ![Vue 3](https://img.shields.io/badge/Vue.js-3.4-blue) ![TypeScript](https://img.shields.io/badge/TypeScript-5.2-blue)

---

## 📖 Documentation

For detailed architectural information, refer to the context documents:

| Document | Description |
|----------|-------------|
| **[Backend Context](backend/BACKEND_CONTEXT.md)** | Game engine, combat system, WebSocket API, state management |
| **[Frontend Context](frontend/FRONTEND_CONTEXT.md)** | Vue.js architecture, component hierarchy, animation system |

---

## ✨ Features

- **Up to 8 players** per game room (human + AI bots)
- **Real-time state sync** via STOMP WebSockets (100ms tick rate)
- **Theme-agnostic core engine** — swap between One Piece and Pokemon themes
- **Auto-battler mechanics**: Shop, XP, Gold (with interest), Trait Synergies, Unit Combinations
- **Grid-based combat** with pathfinding (BFS), ability casting, and visual attack animations
- **Star-level progression** — combine 3 identical units to upgrade (1★ → 2★ → 3★)
- **In-memory game state** — no database required

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        BACKEND (Java 25 + Spring Boot 4)            │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────────┐ │
│  │ GameEngine │──│  GameRoom  │──│   Player   │──│   GameUnit     │ │
│  └────────────┘  └────────────┘  └────────────┘  └────────────────┘ │
│        │                │                                            │
│  ┌─────┴──────┐  ┌──────┴───────┐  ┌──────────────────────────────┐ │
│  │ DataLoader │  │ CombatSystem │  │ TraitManager                 │ │
│  └────────────┘  └──────────────┘  └──────────────────────────────┘ │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ WebSocket (STOMP)
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      FRONTEND (Vue 3 + TypeScript)                   │
│  ┌───────────┐  ┌───────────────┐  ┌────────────┐  ┌──────────────┐ │
│  │  App.vue  │──│ GameInterface │──│ GameCanvas │──│ Animations   │ │
│  └───────────┘  └───────────────┘  └────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

**Key Design Principles:**
- **Backend Authority** — All game logic runs on the server; frontend is a rendering layer
- **Theme-Agnostic Core** — `GameUnit`, `Trait`, `Origin` are generic; themes are loaded via `GameModeProvider`
- **Testability** — Time and randomness are abstracted (`Clock`, `RandomProvider`) for deterministic testing
- **Strategy Pattern** — Combat behaviors (`TargetSelector`, `UnitMover`, `AbilityCaster`) are injectable

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 (Preview) | Core language |
| Spring Boot | 4.0.1 | Application framework |
| WebSocket (STOMP) | — | Real-time communication |
| Maven | — | Build tool |
| Lombok | — | Boilerplate reduction |
| Jackson | — | JSON serialization |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Vue.js | 3.4 | UI framework |
| TypeScript | 5.2 | Type-safe JavaScript |
| Vite | 5.0 | Build tool & dev server |
| @stomp/stompjs | 7.0 | WebSocket client |
| Vanilla CSS | — | Scoped component styling |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker & Docker Compose | Containerization |
| Nginx | Reverse proxy |

---

## 🚀 Quick Start

### Prerequisites
- Java 25 (with preview features enabled)
- Node.js 18+ & npm
- Docker (optional, for containerized deployment)

### Run Backend
```bash
cd backend

# Default theme: One Piece
mvn spring-boot:run

# Or run with Pokemon theme
export GAME_MODE=pokemon && mvn spring-boot:run
```
Backend runs on `http://localhost:8080`

### Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173` with WebSocket proxy to backend

### Run with Docker
```bash
docker-compose up
```

---

## 🎮 Game Modes

| Mode | Property Value | Data Files |
|------|----------------|------------|
| One Piece | `game.mode=onepiece` | `units_onepiece.json`, `traits_onepiece.json` |
| Pokemon | `game.mode=pokemon` | `units_pokemon.json`, `traits_pokemon.json` |

To add a new theme, implement `GameModeProvider` and add corresponding JSON data files. See [Backend Context](backend/BACKEND_CONTEXT.md#8-game-mode-system) for details.

---

## 📡 API Reference

### WebSocket Endpoints
| Destination | Direction | Description |
|-------------|-----------|-------------|
| `/app/create` | Client → Server | Create a new game room |
| `/app/join` | Client → Server | Join an existing room |
| `/app/start` | Client → Server | Host starts the match |
| `/app/room/{id}/action` | Client → Server | Player action (BUY, MOVE, REROLL, EXP) |
| `/topic/room/{id}` | Server → Client | Game state broadcast (100ms) |
| `/topic/room/{id}/event` | Server → Client | Combat result events |

### REST Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/config` | GET | Current game mode configuration |
| `/api/traits` | GET | Trait definitions for UI |

---

## 🧪 Development

### Code Formatting
```bash
# Backend: Run Spotless formatter
cd backend && mvn spotless:apply
```

### Build for Production
```bash
# Backend
cd backend && mvn package

# Frontend
cd frontend && npm run build
```

---

## 📄 License

This project is for educational purposes.

---

*Last updated: 2026-01-21*
