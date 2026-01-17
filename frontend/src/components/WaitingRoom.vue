<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  gameState: any
  currentPlayerName: string
}>()

const emit = defineEmits(['start', 'leave'])

const isHost = computed(() => {
    if (!props.gameState || !props.gameState.players) return false
    const myPlayer = Object.values(props.gameState.players).find((p: any) => p.name === props.currentPlayerName) as any
    return myPlayer && myPlayer.playerId === props.gameState.hostId
})

const players = computed(() => {
    if (!props.gameState || !props.gameState.players) return []
    return Object.values(props.gameState.players)
})

</script>

<template>
  <div class="waiting-room">
    <div class="header">
        <h2>Lobby: {{ gameState.roomId }}</h2>
        <div class="host-info">
            Host: {{ gameState.hostId }}
        </div>
    </div>

    <div class="player-list">
        <h3>Connected Players ({{ players.length }}/8)</h3>
        <div class="players-grid">
            <div v-for="player in players" :key="(player as any).playerId" class="player-card">
                <div class="avatar">
                    <!-- Placeholder avatar -->
                    {{ (player as any).name.charAt(0).toUpperCase() }}
                </div>
                <div class="name">
                    {{ (player as any).name }}
                    <span v-if="(player as any).playerId === gameState.hostId" class="host-badge">HOST</span>
                    <span v-if="(player as any).name === currentPlayerName" class="me-badge">YOU</span>
                </div>
            </div>
             <!-- Empty slots -->
             <div v-for="i in (8 - players.length)" :key="'empty-' + i" class="player-card empty">
                <div class="avatar empty-avatar">?</div>
                <div class="name">Waiting...</div>
            </div>
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
    color: white;
    background: radial-gradient(circle at center, #1e293b 0%, #0f172a 100%);
}

.header {
    text-align: center;
    margin-bottom: 40px;
}

.header h2 {
    font-size: 2.5em;
    margin: 0;
    color: #ffd700;
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
    color: #e2e8f0;
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
    background: #3b82f6;
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
    background: #ffd700;
    color: black;
}

.me-badge {
    background: #4ade80;
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
