# One Piece TFT Clone

A browser-based auto-battler game inspired by Teamfight Tactics, featuring a replaceable One Piece theme. This project focuses on a clean, abstract core with a simplified combat system and real-time multiplayer interactions.

## Project Goal
To create a production-ready, clean, and extensible auto-battler engine in Java, with a modern reactive frontend. The core logic is abstract, allowing the "One Piece" theme to be easily swapped for others (e.g., Pokemon).

**Key Features:**
- Max 8 players per Game Room.
- Real-time state synchronization via WebSockets (STOMP).
- Simplified Grid Combat (Square grid affecting movement/range).
- Core TFT Mechanics: XP, Gold (Interest/Streak), Shop, Bench, Items, Combinations.
- No database persistence (In-memory game state).

## Tech Stack

### Backend
- **Language**: Java 25
- **Framework**: Spring Boot 4.0.1
- **Communication**: STOMP over WebSockets
- **Build Tool**: Maven

### Frontend
- **Framework**: Vue.js 3 + Vite
- **Styling**: TailwindCSS
- **State**: Reactive GameState rendering

### Infrastructure
- Docker & Docker Compose
- Nginx (Reverse Proxy)

## Setup & Running

**Prerequisites:**
- Java 25
- Node.js & npm
- Docker

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
*Note: Frontend is currently being initialized.*
```bash
cd frontend
npm install
npm run dev
```

### Docker
To run the full stack:
```bash
docker-compose up
```

## Current Status
**Phase 2**: Implementing Logic & Frontend Initialization
- [ ] Core Game Loop (Timer, Phases)
- [ ] Combat Simulation (Tick-based, Nearest Neighbor)
- [ ] Frontend Setup (Vue 3, Canvas/DOM rendering)
