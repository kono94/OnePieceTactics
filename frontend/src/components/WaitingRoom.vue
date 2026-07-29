<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { GameMode, GameState, PlayerState } from '../types'
import { getGameModeMetadata, sortGameModes } from '../data/gameModeMetadata'

const props = defineProps<{
  gameState: GameState
  currentPlayerName: string
  availableModes: GameMode[]
  defaultMode: GameMode
  themeClass?: string
}>()

const emit = defineEmits(['start', 'leave', 'mode-change'])

const isHost = computed(() => {
    if (!props.gameState || !props.gameState.players) return false
    const myPlayer = Object.values(props.gameState.players).find((p: PlayerState) => p.name === props.currentPlayerName)
    return myPlayer && myPlayer.playerId === props.gameState.hostId
})

const players = computed((): PlayerState[] => {
    if (!props.gameState || !props.gameState.players) return []
    return Object.values(props.gameState.players)
})

const selectedMode = ref<GameMode>(props.gameState?.gameMode ?? props.defaultMode ?? 'onepiece')

watch(
    () => props.gameState?.gameMode,
    (mode) => {
        if (mode && mode !== selectedMode.value) {
            selectedMode.value = mode
        }
    }
)

const modeOptions = computed((): GameMode[] => {
    return props.availableModes && props.availableModes.length > 0
        ? sortGameModes(props.availableModes)
        : ['onepiece', 'pokemon', 'palworld']
})

const modeMetadata = (mode: GameMode) => getGameModeMetadata(mode)

const themeClass = computed(() => {
    return props.themeClass ?? getGameModeMetadata(props.gameState?.gameMode ?? props.defaultMode).themeClass
})

function selectMode(mode: GameMode) {
    if (mode === selectedMode.value) return

    selectedMode.value = mode
    emit('mode-change', mode)
}

</script>

<template>
  <div :class="['waiting-room', themeClass]">
    <div class="header">
        <h2>Lobby: {{ gameState.roomId }}</h2>
        <div class="host-info">
            Host: {{ gameState.hostId }}
        </div>
    </div>

    <div class="player-list">
        <h3>Connected Players ({{ players.length }}/8)</h3>
        <div class="players-grid">
            <div v-for="player in players" :key="player.playerId" class="player-card">
                <div class="avatar">
                    <!-- Placeholder avatar -->
                    {{ player.name.charAt(0).toUpperCase() }}
                </div>
                <div class="name">
                    {{ player.name }}
                    <span v-if="player.playerId === gameState.hostId" class="host-badge">HOST</span>
                    <span v-if="player.name === currentPlayerName" class="me-badge">YOU</span>
                </div>
            </div>
             <!-- Empty slots -->
             <div v-for="i in (8 - players.length)" :key="'empty-' + i" class="player-card empty">
                <div class="avatar empty-avatar">?</div>
                <div class="name">Waiting...</div>
            </div>
        </div>
    </div>

    <div class="mode-panel">
        <div class="mode-label">Theme</div>
        <div class="mode-control" role="group" aria-label="Select game theme">
            <button
                v-for="mode in modeOptions"
                :key="mode"
                type="button"
                class="mode-option"
                :class="{ active: selectedMode === mode }"
                :aria-pressed="selectedMode === mode"
                :aria-label="`Select ${modeMetadata(mode).label} mode`"
                :aria-disabled="!isHost"
                :disabled="!isHost"
                @click="selectMode(mode)"
            >
                <span class="mode-motif" :class="`motif-${mode}`" aria-hidden="true">
                    <span v-if="mode === 'palworld'" class="mode-motif-button" />
                    <span v-else>{{ modeMetadata(mode).shortLabel.charAt(0) }}</span>
                </span>
                <span>{{ modeMetadata(mode).label }}</span>
            </button>
        </div>
    </div>

    <div class="actions">
        <button class="leave-btn" @click="$emit('leave')">Leave Lobby</button>
        <button v-if="isHost" class="start-btn" @click="$emit('start')">START GAME</button>
        <div v-else class="waiting-msg">Waiting for host to start...</div>
    </div>
  </div>
</template>

<style scoped>
.waiting-room {
    position: relative;
    isolation: isolate;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 50px;
    height: 100vh;
    color: var(--room-fg);
    background: var(--room-bg);
    transition: background 0.4s ease, color 0.4s ease;
    overflow: auto;
}

.header {
    text-align: center;
    margin-bottom: 40px;
}

.header h2 {
    font-size: 2.5em;
    margin: 0;
    color: var(--room-accent);
}

.host-info {
    color: #94a3b8;
    margin-top: 5px;
}

.player-list {
    width: 100%;
    max-width: 800px;
    margin-bottom: 50px;
}

.player-list h3 {
    text-align: center;
    margin-bottom: 20px;
    color: var(--room-muted);
}

.players-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
}

.player-card {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: transform 0.2s;
}

.player-card:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.15);
}

.player-card.empty {
    opacity: 0.5;
    border-style: dashed;
}

.avatar {
    width: 60px;
    height: 60px;
    background: var(--room-avatar);
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 1.5em;
    font-weight: bold;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}

.empty-avatar {
    background: transparent;
    border: 2px solid rgba(255, 255, 255, 0.3);
}

.name {
    font-weight: bold;
    display: flex;
    gap: 5px;
    align-items: center;
}

.host-badge, .me-badge {
    font-size: 0.7em;
    padding: 2px 6px;
    border-radius: 4px;
    background: var(--room-accent);
    color: var(--room-accent-contrast);
}

.me-badge {
    background: #4ade80;
    color: #0f172a;
}

.mode-panel {
    width: 100%;
    max-width: 420px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-radius: 12px;
    margin-bottom: 30px;
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(8px);
}

.mode-label {
    font-weight: 600;
    letter-spacing: 0.04em;
    text-transform: uppercase;
    color: var(--room-muted);
    font-size: 0.85em;
}

.mode-control {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 4px;
    padding: 4px;
    border-radius: 8px;
    background: rgba(0, 0, 0, 0.45);
    border: 1px solid rgba(255, 255, 255, 0.14);
    user-select: none;
}

.mode-option {
    display: inline-flex;
    min-width: 108px;
    min-height: 44px;
    padding: 10px 14px;
    border-radius: 6px;
    border: none;
    background: transparent;
    color: white;
    font-size: 0.95em;
    font-weight: 700;
    cursor: pointer;
    transition: background 0.16s ease, color 0.16s ease, box-shadow 0.16s ease;
    -webkit-user-select: none;
    user-select: none;
}

.mode-option:focus-visible {
    outline: 3px solid #fbbf24;
    outline-offset: 2px;
}

.mode-option:disabled {
    cursor: default;
    opacity: 0.9;
}

.mode-motif {
    position: relative;
    display: inline-flex;
    width: 22px;
    height: 22px;
    align-items: center;
    justify-content: center;
    margin-right: 7px;
    border: 2px solid currentColor;
    border-radius: 50%;
    font-size: 0.7em;
    line-height: 1;
}

.motif-palworld {
    overflow: hidden;
    border-color: #173443;
    background: linear-gradient(180deg, #ff7f6e 0 44%, #173443 44% 56%, #f5feff 56%);
    color: #173443;
}

.mode-motif-button {
    width: 7px;
    height: 7px;
    border: 2px solid #173443;
    border-radius: 50%;
    background: #f5feff;
}

.motif-onepiece {
    color: #fbbf24;
}

.motif-pokemon {
    color: #f97316;
}

.mode-option:hover {
    background: rgba(255, 255, 255, 0.12);
}

.mode-option.active {
    color: #0f172a;
    background: var(--room-accent);
    box-shadow: 0 0 18px rgba(255, 255, 255, 0.18);
}

.actions {
    display: flex;
    gap: 20px;
    align-items: center;
}

button {
    padding: 15px 40px;
    border-radius: 8px;
    border: none;
    font-size: 1.2em;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.2s;
}

.start-btn {
    background: linear-gradient(to right, #ffd700, #f59e0b);
    color: black;
    box-shadow: 0 0 20px rgba(255, 215, 0, 0.3);
}

.start-btn:hover {
    transform: scale(1.05);
    box-shadow: 0 0 30px rgba(255, 215, 0, 0.5);
}

.leave-btn {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.leave-btn:hover {
    background: rgba(255, 0, 0, 0.2);
    border-color: rgba(255, 0, 0, 0.4);
}

.waiting-msg {
    font-size: 1.2em;
    color: #94a3b8;
    font-style: italic;
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% { opacity: 0.6; }
    50% { opacity: 1; }
    100% { opacity: 0.6; }
}

.waiting-room.theme-palworld {
    --pw-sky: #5bc9e8;
    --pw-sky-deep: #1888a6;
    --pw-teal: #187c6b;
    --pw-leaf: #48a868;
    --pw-sand: #f0d59a;
    --pw-coral: #ff7f6e;
    --pw-gold: #d9a441;
    --pw-ink: #173443;
    --pw-cloud: #f5feff;
    --room-bg: linear-gradient(180deg, var(--pw-sky) 0%, #bfefff 48%, var(--pw-sand) 100%);
    --room-fg: var(--pw-ink);
    --room-muted: #285767;
    --room-accent: var(--pw-teal);
    --room-accent-contrast: var(--pw-cloud);
    --room-avatar: var(--pw-leaf);
    color: var(--pw-ink);
}

.waiting-room.theme-palworld::before {
    position: absolute;
    inset: 0 0 42%;
    z-index: -1;
    background:
        radial-gradient(ellipse at 16% 25%, rgba(245, 254, 255, 0.9) 0 9%, transparent 10%),
        radial-gradient(ellipse at 80% 17%, rgba(245, 254, 255, 0.78) 0 12%, transparent 13%),
        linear-gradient(180deg, rgba(91, 201, 232, 0.9), rgba(191, 239, 255, 0.5));
    content: '';
    pointer-events: none;
}

.waiting-room.theme-palworld .header h2 {
    color: var(--pw-teal);
}

.waiting-room.theme-palworld .player-card,
.waiting-room.theme-palworld .mode-panel {
    border-color: rgba(245, 254, 255, 0.78);
    background: rgba(245, 254, 255, 0.78);
    box-shadow: 0 10px 24px rgba(24, 136, 166, 0.14);
}

.waiting-room.theme-palworld .player-card.empty {
    border-color: rgba(24, 124, 107, 0.4);
}

.waiting-room.theme-palworld .mode-control {
    border-color: rgba(24, 124, 107, 0.35);
    background: rgba(23, 52, 67, 0.82);
}

.waiting-room.theme-palworld .mode-option.active {
    color: var(--pw-ink);
    background: var(--pw-sand);
    border: 1px solid var(--pw-teal);
    box-shadow: 0 0 0 2px var(--pw-coral), 0 0 16px rgba(217, 164, 65, 0.55);
}

.waiting-room.theme-palworld .mode-option.active .motif-palworld {
    border-color: var(--pw-coral);
}

.waiting-room.theme-palworld .host-badge {
    background: var(--pw-gold);
    color: var(--pw-ink);
}

.waiting-room.theme-palworld .me-badge {
    background: var(--pw-leaf);
    color: var(--pw-ink);
}

.waiting-room.theme-palworld .start-btn {
    background: var(--pw-teal);
    color: var(--pw-cloud);
    box-shadow: 0 0 20px rgba(24, 124, 107, 0.3);
}

.waiting-room.theme-palworld .leave-btn {
    border-color: rgba(23, 52, 67, 0.26);
    background: rgba(245, 254, 255, 0.65);
    color: var(--pw-ink);
}

@media (max-width: 720px) {
    .waiting-room {
        padding: 28px 14px;
    }

    .players-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 10px;
    }

    .mode-panel {
        flex-direction: column;
        gap: 10px;
    }

    .mode-control {
        width: 100%;
    }

    .mode-option {
        flex: 1 1 120px;
    }
}

@media (prefers-reduced-motion: reduce) {
    .waiting-room *,
    .waiting-room *::before,
    .waiting-room *::after {
        animation: none !important;
        transition-duration: 0.01ms !important;
    }
}
</style>
