<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import Lobby from './components/Lobby.vue'
import GameInterface from './components/GameInterface.vue'
import OutcomeOverlay from './components/game/OutcomeOverlay.vue'

import { setTraitData } from './data/traitData'

const isConnected = ref(false)
const gameState = ref<any>(null)
const client = ref<Client | null>(null)
const currentView = ref<'lobby' | 'game'>('lobby')
const currentRoomId = ref('')
const gameTitle = ref('OnePieceTactics')

// Random player name for now
const PLAYER_NAME = "Player_" + Math.floor(Math.random() * 10000)

onMounted(async () => {
    // Fetch Global Config and Traits
    try {
        const [configRes, traitsRes] = await Promise.all([
            fetch('http://localhost:8080/api/config'),
            fetch('http://localhost:8080/api/traits')
        ]);

        if (configRes.ok) {
            const data = await configRes.json();
            const mode = data.gameMode;
            console.log("Global Config Loaded:", mode);
            
            const link = document.querySelector("link[rel*='icon']") as HTMLLinkElement;
            if (mode === 'pokemon') {
                gameTitle.value = 'Pokemon TFT';
                document.title = 'Pokemon TFT';
                if (link) link.href = '/pokeball.png';
            } else {
                gameTitle.value = 'OnePieceTactics';
                document.title = 'OnePieceTactics';
                if (link && !link.href.includes('favicon.svg')) link.href = '/favicon.svg';
            }
        }

        if (traitsRes.ok) {
            const traits = await traitsRes.json();
            console.log("Traits Loaded:", traits);
            setTraitData(traits);
        }
    } catch (e) {
        console.error("Failed to fetch initial data", e);
    }

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

const encounterResult = ref<'WON' | 'LOST' | 'DRAW' | null>(null)

const subscribeToRoom = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    
    // Subscribe to state updates
    client.value.subscribe(`/topic/room/${roomId}`, (message) => {
        try {
            gameState.value = JSON.parse(message.body)
            
            // Check Game Mode and Update Title
            const mode = gameState.value.gameMode;
            // console.log("Received Game Mode:", mode);
            
            const link = document.querySelector("link[rel*='icon']") as HTMLLinkElement;
            if (mode === 'pokemon') {
                if (document.title !== 'Pokemon TFT') document.title = 'Pokemon TFT';
                if (link && !link.href.includes('pokeball.png')) link.href = '/pokeball.png';
            } else {
                if (document.title !== 'OnePieceTactics') document.title = 'OnePieceTactics';
                // Assuming original favicon is favicon.svg or similar. 
                // index.html said /favicon.svg originally?
                // Step 187 showed <link rel="icon" type="image/svg+xml" href="/favicon.svg">
                // Step 217 changed it to pokeball.png.
                // If we want to support switching back, we should know the OnePiece icon path.
                // Use /favicon.svg if it exists, or just leave it. 
                // But user complained it's not working, maybe because index.html is hardcoded to pokeball now?
                // I will set it to /favicon.svg if not Pokemon.
                if (link && !link.href.includes('favicon.svg')) link.href = '/favicon.svg';
            }

        } catch (e) {
            console.error("Failed to parse game state", e)
        }
    })

    // Subscribe to events
    client.value.subscribe(`/topic/room/${roomId}/event`, (message) => {
        try {
            const event = JSON.parse(message.body)
            console.log("Received Game Event:", event)
            if (event.type === 'COMBAT_RESULT') {
                handleCombatResult(event.payload)
            }
        } catch (e) {
            console.error("Failed to parse event", e)
        }
    })
}

const handleCombatResult = (payload: any) => {
    console.log("Handling Combat Result:", payload)
    // Determine if I won or lost
    if (!gameState.value) return
    
    // Find my ID
    const myPlayerEntry = Object.values(gameState.value.players).find((p: any) => p.name === PLAYER_NAME) as any
    if (!myPlayerEntry) return
    
    const myId = myPlayerEntry.playerId

    if (payload.winnerId === myId) {
        encounterResult.value = 'WON'
    } else if (payload.loserId === myId) {
        encounterResult.value = 'LOST'
    } else {
        // Maybe a draw or unrelated combat (if >2 players)
        // If unrelated, ignore.
        return 
    }

    // Clear after 2 seconds
    setTimeout(() => {
        encounterResult.value = null
    }, 2000)
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
               :title="gameTitle"
               @create="handleCreate" 
               @join="handleJoin" />
               
        <div v-else class="game-container">
             <GameInterface :state="gameState" 
                            :current-player-name="PLAYER_NAME"
                            :is-connected="isConnected"
                            @action="handleGameAction" />
             <OutcomeOverlay :type="encounterResult" />
        </div>
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
