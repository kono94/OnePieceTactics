# Theme Fusion Tactics (TFT)

**Theme Fusion Tactics (TFT)** is a browser-based **auto-battler game** inspired by Teamfight Tactics, featuring a theme-swappable engine with lobby-selectable One Piece (default), Pokemon, and Palworld modes. This project showcases a clean, production-grade architecture with real-time multiplayer via WebSockets.

![Java 25](https://img.shields.io/badge/Java-25-orange) ![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4.1.0-green) ![Vue 3](https://img.shields.io/badge/Vue.js-3.5-blue) ![TypeScript](https://img.shields.io/badge/TypeScript-5.9-blue)

![Theme Fusion Tactics Board](docs/board_preview.jpg)

---

## 📖 Documentation

For detailed architectural information, refer to the context documents:

| Document | Description |
|----------|-------------|
| **[Backend Context](backend/BACKEND_CONTEXT.md)** | Game engine, combat system, WebSocket API, state management |
| **[Frontend Context](frontend/FRONTEND_CONTEXT.md)** | Vue.js architecture, component hierarchy, animation system |
| **[Deployment Guide](deployment/DEPLOYMENT_GUIDE.md)** | Docker Compose setup, GitOps deployment to production |

---

## ✨ Features

### Core Gameplay
- **Up to 8 players** per game room (human + AI bots with adaptive shop odds)
- **Real-time state sync** via STOMP WebSockets (100ms tick rate)
- **Theme-agnostic core engine** — hosts choose One Piece, Pokemon, or Palworld per room in the lobby; config controls the default
- **Auto-battler mechanics**: Shop, XP, Gold (with interest), Trait Synergies, Unit Combinations
- **Grid-based combat** with BFS pathfinding, ability casting, and directional attack animations
- **Star-level progression** — combine 3 matching 1★ units into 1 2★, then 2 matching 2★ units into 1 3★, including Pokemon evolution forms
- **Round-based augment choices** — players choose team-wide economy or combat bonuses on rounds 3, 6, and 11
- **Advanced ability system** — Damage, Stun, Shield, Heal, Buff with modifiers (Lifesteal, Execute, Scaling, Conditional, Knockback)
- **Data-driven trait system** — All trait effects loaded from JSON configuration (no hardcoded logic)
- **TFT-style shop odds** — Level-based probability distribution for unit costs (1★-5★)
- **In-memory game state** — live matches remain memory-only; SQLite analytics persist production outcomes

### Combat & Progression
- **Ghost/clone matchmaking** — Odd player count creates AI clones for balanced combat
- **Player elimination** — Ranked placement system with game-ending logic
- **Loot orbs** — Gold and unit rewards spawn after combat rounds, with a one-time emergency drop when a surviving human first falls to 20 health or lower
- **Data-loaded elemental combat** — Pokemon and Palworld auto attacks and damage abilities derive their offensive element from traits using the best-attacker-trait rule and mode-owned affinity data; One Piece remains neutral
- **Role-based unit identity** — Every form is labeled Damage, Tank, or Support; Pokemon evolutions can change roles
- **Unified DEF combat stat** — DEF mitigates attacks, abilities, and damage-over-time and can be buffed or shredded
- **Tabbed damage report** — Post-combat tracking for your units vs opponent with visual damage bars
- **Per-unit attack animations** — Punch, slash, projectile with directional orientation
- **Ability patterns** — Single-target, line, and AoE effects with range-based targeting

### UI/UX Enhancements
- **Cost-based visual styling** — Dynamic borders and glows based on unit rarity (1★ gray → 5★ gold)
- **Star-level visual effects** — Enhanced borders and top glows for 2★ and 3★ units
- **Shop probability tooltip** — Hover over player level to see current unit cost distribution
- **Git-based version display** — Build metadata (tag, commit, timestamp) in bottom-left corner
- **Smart unit tooltips** — Role, melee/ranged, trait-color, and DEF badges with star-level ability highlighting
- **Player board spectating** — Click alive players in the right panel to view their board and combat from their perspective
- **Keyboard shortcuts** — Enter key support for room creation/joining
- **Bench reordering** — Swap and rearrange units even during combat phase
- **Team-colored health bars** — Emerald for allies, red for opponents

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        BACKEND (Java 25 + Spring Boot 4)            │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────────┐ │
│  │ GameEngine │──│  GameRoom  │──│   Player   │──│   GameUnit     │ │
│  └────────────┘  └────────────┘  └────────────┘  └────────────────┘ │
│        │                │                                           │
│  ┌─────┴──────┐  ┌──────┴───────┐  ┌──────────────────────────────┐ │
│  │ DataLoader │  │ CombatSystem │  │ TraitManager                 │ │
│  └────────────┘  └──────────────┘  └──────────────────────────────┘ │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ WebSocket (STOMP)
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      FRONTEND (Vue 3 + TypeScript)                  │
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
| Spring Boot | 4.1.0 | Application framework |
| WebSocket (STOMP) | — | Real-time communication |
| Maven | — | Build tool |
| Lombok | — | Boilerplate reduction |
| Jackson | — | JSON serialization |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Vue.js | 3.5 | UI framework |
| TypeScript | 5.9 | Type-safe JavaScript |
| Vite | 8.1 | Build tool & dev server |
| @stomp/stompjs | 7.3 | WebSocket client |
| Vanilla CSS | — | Scoped component styling |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker & Docker Compose | Containerization |
| Nginx | Reverse proxy |
| SQLite | Production gameplay analytics |

---

## 🚀 Quick Start

### Prerequisites
- Java 25
- Node.js 20.19+ or 22.12+ & npm
- Docker (optional, for containerized deployment)

### Run Backend
```bash
cd backend

# Default lobby mode: One Piece
mvn spring-boot:run

# Or make Pokemon or Palworld the default lobby mode
GAME_MODE=pokemon mvn spring-boot:run
# GAME_MODE=palworld mvn spring-boot:run
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

Rooms start with the configured default mode, then the host can switch modes in the lobby before starting the match. Changing the room mode swaps the unit set, traits, visuals, and mode-specific combat rules for that room.

| Mode | Property Value | Data Files |
|------|----------------|------------|
| One Piece | `game.mode=onepiece` | `units_onepiece.json`, `traits_onepiece.json`, `augments_onepiece.json` |
| Pokemon | `game.mode=pokemon` | `units_pokemon.json`, `traits_pokemon.json`, `augments_pokemon.json`, `affinities_pokemon.json` |
| Palworld | `game.mode=palworld` | `units_palworld.json`, `traits_palworld.json`, `augments_palworld.json`, `affinities_palworld.json` |

Use `GAME_MODE` or `game.mode` to choose the default lobby selection. To add a new theme, implement `GameModeProvider` and add corresponding JSON data files. See [Backend Context](backend/BACKEND_CONTEXT.md#5-game-modes-room-modes-and-data) for details.

### Palworld mode architecture

Palworld is the third lobby-selectable mode, not a separate combat engine. Its provider loads 55 purchasable Pal lines, nine team-wide elemental traits, 15 augments, and one root ability per Pal with star-scaled values. The shared engine supplies the Pokemon-style best-attacker-trait affinity resolver, typed composite ability effects, status lifecycle, and per-combat-pair scheduling; Palworld-specific ids stay in data and provider registration.

Basic attacks, abilities, and damage-over-time effects derive their offensive element from the caster's trait list against the target's defensive trait list; dual-trait Pals use the best attacking trait for each target, matching Pokemon. No separate basic-element or ability-element fields are required. Pokemon and Palworld resolve their own data-loaded affinity graphs; One Piece has no affinity configuration and keeps neutral damage behavior. Composite abilities can select targets by generic selectors and shapes, then apply ordered damage, healing, shields, buffs, statuses, movement, multi-hit, and zone effects. Active statuses and delayed effects are authoritative backend state and are cleared with their combat context.

The frontend uses a mode metadata registry for labels, title, favicon, asset folder, theme class, and gallery route. Palworld portraits resolve from `/assets/units/palworld/{definitionId}_v1.png`, with `/pal-sphere.png` as the favicon. The Palworld palette is intentionally limited to the public lobby and waiting room; the board, shop, trait sidebar, overlays, and end screen retain shared in-match chrome. Its animation gallery is available at `#/ultimate-gallery/palworld` and previews 55 definition-based attack configs plus 55 root-ability configs, reusing each ability at 1/2/3 stars with scaled values. Combat events carry stable event, cast, animation-identity, hit, status, zone, and coordinate metadata, so the renderer never guesses from display names.

### Palworld validation and release gates

The implementation gate checks 55 lines with the required 12/13/11/12/7 cost distribution, 23/16/16 role distribution, nine elements, 15 augments, 55 attack previews, and 55 root abilities. Asset QA requires exactly 55 decodable 512×512 PNG portraits with no unexpected filenames. The focused backend/frontend gates are:

- `cd backend && mvn -Dtest=PalworldDataValidationTest,DamageResolverTest,CompositeAbilityCasterTest test`
- `cd backend && mvn test`
- `cd frontend && npm test && npm run lint && npm run build`
- `python3 scripts/validate_palworld_assets.py frontend/public/assets/units/palworld`
- `python3 scripts/compress_images.py frontend/public/assets/units/palworld`

Final release acceptance also requires deterministic 55-ability smoke coverage, Pokemon affinity parity, One Piece regression coverage, 10,000-run tuning smoke, million-run Palworld simulations, the 100,000-run role gate, manual lobby/multiplayer/reduced-motion checks, and production container smoke. The release remains `Version X.X.X` until every blocking gate passes and the release procedure explicitly promotes it to `2.0.0`.

---

## 📡 API Reference

### WebSocket Endpoints
| Destination | Direction | Description |
|-------------|-----------|-------------|
| `/app/create` | Client → Server | Create a new game room |
| `/app/join` | Client → Server | Join an existing room |
| `/app/start` | Client → Server | Host starts the match |
| `/app/room/{id}/mode` | Client → Server | Host changes the room game mode during lobby |
| `/app/room/{id}/action` | Client → Server | Player action (BUY, MOVE, REROLL, EXP, SELL, LOCK, COLLECT_ORB, READY_FOR_COMBAT, SELECT_AUGMENT) |
| `/topic/room/{id}` | Server → Client | Game state broadcast (100ms) |
| `/topic/room/{id}/event` | Server → Client | Typed combat-result and emergency-drop events |

Client actions are bound to the STOMP session that joined the room. The backend rejects actions, start requests, and mode changes that attempt to act as a different player id.
Augment choices are included in each player's `GameState` snapshot as `augmentChoices`; selected augments are exposed as `selectedAugments`.

### REST Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/config` | GET | Default game mode and available lobby modes |
| `/api/mode` | GET | Default game mode |
| `/api/traits?mode={mode}` | GET | Trait definitions for the selected mode |
| `/api/admin/auth/login` | POST | Exchange the configured admin password for an eight-hour bearer token |
| `/api/admin/analytics/summary` | GET | Protected aggregate gameplay analytics |
| `/api/admin/analytics/runs` | GET | Protected, paginated player runs |

The production analytics dashboard is available at `/#/admin/analytics`. Match state remains backend-authoritative and
in memory; only anonymous analytics snapshots are written to SQLite. See the deployment guide for password and storage
configuration.

---

## 🧪 Development

### Code Formatting
```bash
# Backend: Run Spotless formatter
cd backend && mvn spotless:apply
```

### Balance Simulation Reports
```bash
cd backend

# Existing same-star board simulation
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 test

# One piece
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=onepiece -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 test

# Keep the same board-building rules, but randomize every unit's star level
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 -Dsimulation.style=random-stars test

# Randomize board sizes, units, star levels, and positions
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 -Dsimulation.style=random-boards test

# Compare equal-value balanced role boards against Damage-only boards
mvn -Dtest=RoleBalanceSimulationTest -Dsimulation.role-report=true -Dsimulation.runs=100000 -Dsimulation.seed=42 test
```

Reports are written to `backend/target/simulation-reports`. Every style also writes unit and trait rankings for board
sizes 2–7. The role report keeps Damage-only boards viable at size 3, requires a balanced advantage from size 4, and
raises the minimum balanced win-rate target to 65% for sizes 6–7.

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

*Last updated: 2026-07-17*
