<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import GameCanvas from './components/GameCanvas.vue'

const isConnected = ref(false)
const gameState = ref<any>(null)
const client = ref<Client | null>(null)

// Hardcoded for clone
const PLAYER_NAME = "Player_" + Math.floor(Math.random() * 1000)

onMounted(() => {
    client.value = new Client({
        brokerURL: 'ws://localhost:8080/tft-websocket', // Assuming standard Spring Boot STOMP endpoint
        onConnect: () => {
            isConnected.value = true
            console.log("Connected to WebSocket")
            
            // Subscribe to game state updates
            client.value?.subscribe('/topic/gamestate', (message) => {
                try {
                    gameState.value = JSON.parse(message.body)
                } catch (e) {
                    console.error("Failed to parse game state", e)
                }
            })
            
            // Join room (dummy implementation)
            client.value?.publish({ destination: '/app/join', body: JSON.stringify({ name: PLAYER_NAME }) })
        },
        onDisconnect: () => {
            isConnected.value = false
            console.log("Disconnected")
        }
    })
    
    client.value.activate()
})

onUnmounted(() => {
    client.value?.deactivate()
})
</script>

<template>
  <div class="container">
    <h1>One Piece TFT Clone</h1>
    <div class="status">
        Status: <span :class="{ connected: isConnected, disconnected: !isConnected }">{{ isConnected ? 'Connected' : 'Disconnected' }}</span>
        <span v-if="isConnected && gameState"> | Phase: {{ gameState.phase }} | Round: {{ gameState.round }} | Time: {{ (gameState.timeRemainingMs / 1000).toFixed(1) }}s</span>
    </div>

    <div v-if="!isConnected" class="loading">
        Connecting to server...
    </div>
    
    <GameCanvas v-if="gameState" :state="gameState" />
  </div>
</template>

<style scoped>
.container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}
.status {
    font-size: 1.2em;
    font-weight: bold;
}
.connected { color: #4ade80; }
.disconnected { color: #ef4444; }
.loading { margin-top: 50px; font-style: italic; }
</style>
