<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { GameMode, GameState, PlayerState } from '../types'

const props = defineProps<{
  gameState: GameState
  currentPlayerName: string
  availableModes: GameMode[]
  defaultMode: GameMode
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
        ? props.availableModes
        : ['onepiece', 'pokemon']
})

const modeLabel = (mode: GameMode) => {
    if (mode === 'pokemon') return 'Pokemon'
    if (mode === 'onepiece') return 'One Piece'
    return mode
}

const themeClass = computed(() => {
    if (!props.gameState?.gameMode) return 'theme-generic'
    return `theme-${props.gameState.gameMode}`
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
        <div v-if="isHost" class="mode-control" role="group" aria-label="Select game theme">
            <button
                v-for="mode in modeOptions"
                :key="mode"
                type="button"
                class="mode-option"
                :class="{ active: selectedMode === mode }"
                :aria-pressed="selectedMode === mode"
                @click="selectMode(mode)"
            >
                    {{ modeLabel(mode) }}
            </button>
        </div>
        <div v-else class="mode-display">
            {{ modeLabel(selectedMode) }}
        </div>
    </div>

    <div class="actions">
        <button class="leave-btn" @click="$emit('leave')">Leave Lobby</button>
        <button v-if="isHost" class="start-btn" @click="() => { console.log('Start clicked in WaitingRoom'); $emit('start'); }">START GAME</button>
        <div v-else class="waiting-msg">Waiting for host to start...</div>
    </div>
  </div>
</template>

<style scoped>
.waiting-room {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 50px;
    height: 100vh;
    color: var(--room-fg);
    background: var(--room-bg);
    transition: background 0.4s ease, color 0.4s ease;
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
    gap: 4px;
    padding: 4px;
    border-radius: 8px;
    background: rgba(0, 0, 0, 0.45);
    border: 1px solid rgba(255, 255, 255, 0.14);
    user-select: none;
}

.mode-option {
    min-width: 96px;
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

.mode-option:hover {
    background: rgba(255, 255, 255, 0.12);
}

.mode-option.active {
    color: #0f172a;
    background: var(--room-accent);
    box-shadow: 0 0 18px rgba(255, 255, 255, 0.18);
}

.mode-display {
    font-size: 1.1em;
    font-weight: 600;
    color: var(--room-fg);
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
</style>
