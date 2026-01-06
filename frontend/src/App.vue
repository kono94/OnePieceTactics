<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import Lobby from './components/Lobby.vue'
import GameInterface from './components/GameInterface.vue'

const isConnected = ref(false)
const gameState = ref<any>(null)
const client = ref<Client | null>(null)
const currentView = ref<'lobby' | 'game'>('lobby')
const currentRoomId = ref('')

// Random player name for now
const PLAYER_NAME = "Player_" + Math.floor(Math.random() * 10000)

onMounted(() => {
    client.value = new Client({
        brokerURL: 'ws://localhost:8080/tft-websocket',
        onConnect: () => {
            isConnected.value = true
            console.log("Connected to WebSocket")
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

const subscribeToRoom = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    
    // Unsubscribe previous if any? (Not needed for single game session)
    
    client.value.subscribe(`/topic/room/${roomId}`, (message) => {
        try {
            gameState.value = JSON.parse(message.body)
        } catch (e) {
            console.error("Failed to parse game state", e)
        }
    })
}

const handleCreate = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    currentRoomId.value = roomId
    
    subscribeToRoom(roomId)
    
    client.value.publish({ 
        destination: '/app/create', 
        body: JSON.stringify({ roomId: roomId, playerName: PLAYER_NAME }) 
    })
    
    currentView.value = 'game'
}

const handleJoin = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    currentRoomId.value = roomId
    
    subscribeToRoom(roomId)
    
    client.value.publish({ 
        destination: '/app/join', 
        body: JSON.stringify({ roomId: roomId, playerName: PLAYER_NAME }) 
    })
    
    currentView.value = 'game'
}

const handleGameAction = (action: any) => {
    if (!client.value || !isConnected.value) return
    
    console.log("Publishing Action:", action)
    client.value.publish({
        destination: `/app/room/${currentRoomId.value}/action`,
        body: JSON.stringify(action)
    })
}

</script>

<template>
  <div class="app-container">
    <div v-if="!isConnected" class="loading-screen">
        Connecting to Server...
    </div>
    
    <template v-else>
        <Lobby v-if="currentView === 'lobby'" 
               @create="handleCreate" 
               @join="handleJoin" />
               
        <GameInterface v-else 
                       :state="gameState" 
                       :current-player-name="PLAYER_NAME"
                       :is-connected="isConnected"
                       @action="handleGameAction" />
    </template>
  </div>
</template>

<style>
body {
    margin: 0;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: #0f172a;
    color: white;
}

*, *::before, *::after {
    box-sizing: border-box;
}
</style>

<style scoped>
.app-container {
    width: 100%;
    height: 100vh;
    overflow: hidden;
}
.loading-screen {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 2em;
}
</style>
