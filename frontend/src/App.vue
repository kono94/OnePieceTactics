<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Client, type IMessage } from '@stomp/stompjs'
import type { StompSubscription } from '@stomp/stompjs'
import Lobby from './components/Lobby.vue'
import WaitingRoom from './components/WaitingRoom.vue'
import GameInterface from './components/GameInterface.vue'
import OutcomeOverlay from './components/game/OutcomeOverlay.vue'
import DamageReport from './components/game/DamageReport.vue'

import { setTraitData } from './data/traitData'
import type { GameState, GameAction, CombatResultPayload, GameEvent, DamageEntry } from './types'

const isConnected = ref(false)
const gameState = ref<GameState | null>(null)
const client = ref<Client | null>(null)
const currentView = ref<'lobby' | 'game'>('lobby')
const currentRoomId = ref('')
const gameTitle = ref('OnePieceTactics')
const roomSubscription = ref<StompSubscription | null>(null)
const eventSubscription = ref<StompSubscription | null>(null)

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
const damageReport = ref<Record<string, DamageEntry> | null>(null)

const subscribeToRoom = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    
    // Unsubscribe from previous if exists
    if (roomSubscription.value) {
        roomSubscription.value.unsubscribe()
        roomSubscription.value = null
    }
    if (eventSubscription.value) {
        eventSubscription.value.unsubscribe()
        eventSubscription.value = null
    }
    
    // Subscribe to state updates
    roomSubscription.value = client.value.subscribe(`/topic/room/${roomId}`, (message) => {
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
                if (link && !link.href.includes('favicon.svg')) link.href = '/favicon.svg';
            }

        } catch (e) {
            console.error("Failed to parse game state", e)
        }
    })

    // Subscribe to events
    eventSubscription.value = client.value.subscribe(`/topic/room/${roomId}/event`, (message) => {
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

const handleCombatResult = (payload: CombatResultPayload) => {
    console.log("Handling Combat Result:", payload)
    if (!gameState.value) return
    
    // Find my ID
    const myPlayerEntry = Object.values(gameState.value.players).find((p) => p.name === PLAYER_NAME)
    if (!myPlayerEntry) return
    
    const myId = myPlayerEntry.playerId
    
    // Was I in this combat?
    const wasParticipant = payload.participantIds.includes(myId)
    if (!wasParticipant) return

    // Determine result type
    if (payload.winnerId === myId) {
        encounterResult.value = 'WON'
    } else if (payload.loserId === myId) {
        encounterResult.value = 'LOST'
    } else {
        encounterResult.value = 'DRAW'
    }

    // Store damage report (deprecated, using live state now)
    // damageReport.value = payload.damageLog

    // Clear after 8 seconds (Outcome overlay only)
    setTimeout(() => {
        encounterResult.value = null
    }, 8000)
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

const handleGameAction = (action: GameAction) => {
    if (!client.value || !isConnected.value) return
    
    console.log("Publishing Action:", action)
    client.value.publish({
        destination: `/app/room/${currentRoomId.value}/action`,
        body: JSON.stringify(action)
    })
}

const handleStartGame = () => {
    console.log("handleStartGame called");
    if (!client.value || !isConnected.value) {
        console.error("Cannot start game: Disconnected");
        return;
    }
    console.log("Publishing /app/start for room:", currentRoomId.value);
    client.value.publish({
        destination: '/app/start',
        body: JSON.stringify({ roomId: currentRoomId.value, playerName: PLAYER_NAME })
    })
}

const handleLeaveLobby = () => {
    if (client.value && isConnected.value) {
        client.value.publish({
            destination: '/app/leave',
            body: JSON.stringify({ roomId: currentRoomId.value, playerName: PLAYER_NAME })
        })
    }
    
    if (roomSubscription.value) {
        roomSubscription.value.unsubscribe()
        roomSubscription.value = null
    }
    if (eventSubscription.value) {
        eventSubscription.value.unsubscribe()
        eventSubscription.value = null
    }
    
    currentView.value = 'lobby'
    gameState.value = null
    currentRoomId.value = ''
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
             <!-- If in LOBBY phase, show WaitingRoom -->
             <template v-if="gameState">
                 <WaitingRoom v-if="gameState.phase === 'LOBBY'"
                              :game-state="gameState"
                              :current-player-name="PLAYER_NAME"
                              @start="handleStartGame"
                              @leave="handleLeaveLobby" />
                              
                 <!-- Otherwise show GameInterface -->
                 <template v-else>
                     <GameInterface :state="gameState" 
                                    :current-player-name="PLAYER_NAME"
                                    :is-connected="isConnected"
                                    @action="handleGameAction" />
                     <OutcomeOverlay v-if="encounterResult" :type="encounterResult" />
                     <DamageReport v-if="gameState.damageLog" 
                                   :damage-log="gameState.damageLog" 
                                   :my-player-id="Object.values(gameState.players).find(p => p.name === PLAYER_NAME)?.playerId" />
                 </template>
             </template>
             <div v-else class="loading-screen">
                 Initializing Game Room...
             </div>
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
