<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, shallowRef } from 'vue'
import type { Component } from 'vue'
import { Client } from '@stomp/stompjs'
import type { StompSubscription } from '@stomp/stompjs'
import Lobby from './components/Lobby.vue'
import Changelog from './components/Changelog.vue'
import WaitingRoom from './components/WaitingRoom.vue'
import GameInterface from './components/GameInterface.vue'
import OutcomeOverlay from './components/game/OutcomeOverlay.vue'
import DamageReport from './components/game/DamageReport.vue'
import VersionDisplay from './components/VersionDisplay.vue'
import AdminAnalytics from './components/admin/AdminAnalytics.vue'

import { setTraitData } from './data/traitData'
import type {
    CombatResultPayload,
    EmergencyDropPayload,
    GameAction,
    GameMode,
    GameState,
    RoomGameEvent,
} from './types'
import {
    clearActiveRoomSession,
    createActiveRoomSession,
    getAnalyticsClientId,
    loadActiveRoomSession,
} from './utils/clientIdentity'

const isConnected = ref(false)
const gameState = ref<GameState | null>(null)
const client = ref<Client | null>(null)
const currentView = ref<'lobby' | 'game' | 'changelog'>('lobby')
const currentRoomId = ref('')
const gameTitle = ref('Tactics Arena')
const availableModes = ref<GameMode[]>(['onepiece', 'pokemon'])
const defaultMode = ref<GameMode>('onepiece')
const activeTraitMode = ref<GameMode | null>(null)
const roomSubscription = ref<StompSubscription | null>(null)
const eventSubscription = ref<StompSubscription | null>(null)
const isUltimateGallery = ref(false)
const isAdminAnalytics = ref(false)
const ultimateGalleryMode = ref<GameMode>('onepiece')
const UltimateGallery = shallowRef<Component | null>(null)
const viewedPlayerId = ref<string | null>(null)
const pendingJoinRoomId = ref<string | null>(null)
const lobbyError = ref('')
let restoredRoomTimeout: number | null = null

const restoredRoom = loadActiveRoomSession()
const PLAYER_NAME = restoredRoom?.playerName ?? "Player_" + Math.floor(Math.random() * 10000)
const analyticsClientId = getAnalyticsClientId()

onMounted(async () => {
    updateStandaloneRoute()
    window.addEventListener('hashchange', updateStandaloneRoute)
    if (isUltimateGallery.value || isAdminAnalytics.value) return

    document.title = 'Tactics Arena'
    try {
        const configRes = await fetch('/api/config');

        if (configRes.ok) {
            const data = await configRes.json();
            const fallbackModes = Array.isArray(data.availableModes) ? data.availableModes : [];
            if (fallbackModes.length > 0) {
                availableModes.value = fallbackModes as GameMode[];
            }
            if (data.defaultGameMode) {
                defaultMode.value = data.defaultGameMode as GameMode;
            }
        }
    } catch (e) {
        console.error("Failed to fetch initial data", e);
    }

    const envWsUrl = import.meta.env.VITE_WS_URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsUrl = envWsUrl || `${protocol}//${window.location.host}/ws`

    client.value = new Client({
        brokerURL: wsUrl,
        onConnect: () => {
            isConnected.value = true
            console.log("Connected to WebSocket")
            const activeRoom = loadActiveRoomSession()
            if (activeRoom) {
                gameState.value = null
                currentRoomId.value = activeRoom.roomId
                pendingJoinRoomId.value = activeRoom.roomId
                subscribeToRoom(activeRoom.roomId)
                client.value?.publish({
                    destination: '/app/join',
                    body: JSON.stringify({
                        roomId: activeRoom.roomId,
                        playerName: activeRoom.playerName,
                        analyticsClientId,
                        reconnectToken: activeRoom.reconnectToken,
                    }),
                })
                currentView.value = 'game'
                startRestoredRoomTimeout(activeRoom.roomId)
            }
        },
        onDisconnect: () => {
            isConnected.value = false
            console.log("Disconnected")
        },
        reconnectDelay: 5000,
    })
    
    client.value.activate()
})

onUnmounted(() => {
    window.removeEventListener('hashchange', updateStandaloneRoute)
    clearRestoredRoomTimeout()
    clearEmergencyDropPresentation()
    if (outcomeTimer !== null) window.clearTimeout(outcomeTimer)
    client.value?.deactivate()
})

const encounterResult = ref<'WON' | 'LOST' | 'DRAW' | null>(null)
const emergencyDrop = ref<EmergencyDropPayload | null>(null)
const pendingEmergencyDrop = ref<EmergencyDropPayload | null>(null)
const hasQueuedEmergencyDrop = computed(() => emergencyDrop.value !== null || pendingEmergencyDrop.value !== null)
let outcomeTimer: number | null = null
let emergencyDropDelayTimer: number | null = null
let emergencyDropClearTimer: number | null = null

const myPlayerId = computed(() => {
    if (!gameState.value) return undefined;
    return Object.values(gameState.value.players).find(p => p.name === PLAYER_NAME)?.playerId;
});

const opponentId = computed(() => {
    if (!gameState.value || !damageReportPlayerId.value) return undefined;
    return gameState.value.matchups[damageReportPlayerId.value];
});

const opponentName = computed(() => {
    if (!gameState.value || !opponentId.value) return undefined;
    return gameState.value.players[opponentId.value]?.name || 'Opponent';
});

const damageReportPlayerId = computed(() => {
    return viewedPlayerId.value || myPlayerId.value;
});

const damageReportPlayerName = computed(() => {
    if (!gameState.value || !damageReportPlayerId.value || damageReportPlayerId.value === myPlayerId.value) {
        return 'YOU';
    }
    return gameState.value.players[damageReportPlayerId.value]?.name || 'Viewed';
});

const showVersion = computed(() => {
    if (isUltimateGallery.value) return false
    // Show version only on lobby view or during LOBBY phase in game view
    return currentView.value === 'lobby' || gameState.value?.phase === 'LOBBY';
});


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
            if (!gameState.value) return;
            clearRestoredRoomTimeout()
            if (gameState.value.phase === 'END_CELEBRATION' || gameState.value.phase === 'END') {
                clearActiveRoomSession()
            }
            
            // Check Game Mode and Update Title
            const mode = gameState.value.gameMode;

            // console.log("Received Game Mode:", mode);
            
            applyThemeMeta(mode);

            if (activeTraitMode.value !== mode) {
                activeTraitMode.value = mode;
                fetchTraitsForMode(mode);
            }

            if (pendingJoinRoomId.value === roomId && !hasCurrentPlayer(gameState.value) && gameState.value.phase !== 'LOBBY') {
                rejectPendingJoin('That game has already started.')
            } else if (pendingJoinRoomId.value === roomId && hasCurrentPlayer(gameState.value)) {
                pendingJoinRoomId.value = null
            }

        } catch (e) {
            console.error("Failed to parse game state", e)
        }
    })

    // Subscribe to events
    eventSubscription.value = client.value.subscribe(`/topic/room/${roomId}/event`, (message) => {
        try {
            const event = JSON.parse(message.body) as RoomGameEvent
            console.log("Received Game Event:", event)
            if (event.type === 'COMBAT_RESULT') {
                handleCombatResult(event.payload)
            } else if (event.type === 'EMERGENCY_DROP') {
                handleEmergencyDrop(event.payload)
            }
        } catch (e) {
            console.error("Failed to parse event", e)
        }
    })
}

const hasCurrentPlayer = (state: GameState) => {
    return Object.values(state.players).some((player) => player.name === PLAYER_NAME)
}

const clearRoomSubscriptions = () => {
    if (roomSubscription.value) {
        roomSubscription.value.unsubscribe()
        roomSubscription.value = null
    }
    if (eventSubscription.value) {
        eventSubscription.value.unsubscribe()
        eventSubscription.value = null
    }
}

const rejectPendingJoin = (message: string) => {
    clearRestoredRoomTimeout()
    clearRoomSubscriptions()
    clearActiveRoomSession()
    pendingJoinRoomId.value = null
    currentView.value = 'lobby'
    gameState.value = null
    currentRoomId.value = ''
    activeTraitMode.value = null
    viewedPlayerId.value = null
    lobbyError.value = message
    applyThemeMeta(null)
}

const clearRestoredRoomTimeout = () => {
    if (restoredRoomTimeout !== null) {
        window.clearTimeout(restoredRoomTimeout)
        restoredRoomTimeout = null
    }
}

const startRestoredRoomTimeout = (roomId: string) => {
    clearRestoredRoomTimeout()
    restoredRoomTimeout = window.setTimeout(() => {
        if (pendingJoinRoomId.value === roomId && !gameState.value) {
            rejectPendingJoin('Your previous game is no longer available.')
        }
    }, 5000)
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

    if (emergencyDrop.value) {
        pendingEmergencyDrop.value = emergencyDrop.value
        emergencyDrop.value = null
    }
    if (emergencyDropDelayTimer !== null) {
        window.clearTimeout(emergencyDropDelayTimer)
        emergencyDropDelayTimer = null
    }
    if (emergencyDropClearTimer !== null) {
        window.clearTimeout(emergencyDropClearTimer)
        emergencyDropClearTimer = null
    }

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

    // Clear after 3 seconds (Outcome overlay only)
    if (outcomeTimer) clearTimeout(outcomeTimer)
    outcomeTimer = window.setTimeout(() => {
        encounterResult.value = null
        outcomeTimer = null
        scheduleEmergencyDropPresentation()
    }, 3000)
}

const handleEmergencyDrop = (payload: EmergencyDropPayload) => {
    if (!payload.dropId || payload.playerId !== myPlayerId.value) return
    if (emergencyDrop.value?.dropId === payload.dropId || pendingEmergencyDrop.value?.dropId === payload.dropId) return

    pendingEmergencyDrop.value = payload
    scheduleEmergencyDropPresentation()
}

const scheduleEmergencyDropPresentation = () => {
    if (!pendingEmergencyDrop.value || encounterResult.value) return
    if (emergencyDropDelayTimer !== null) window.clearTimeout(emergencyDropDelayTimer)

    // Combat result and emergency-drop events are delivered independently. This
    // brief queueing window lets the outcome claim the foreground first.
    emergencyDropDelayTimer = window.setTimeout(() => {
        emergencyDropDelayTimer = null
        if (!pendingEmergencyDrop.value || encounterResult.value) return

        emergencyDrop.value = pendingEmergencyDrop.value
        pendingEmergencyDrop.value = null
        if (emergencyDropClearTimer !== null) window.clearTimeout(emergencyDropClearTimer)
        emergencyDropClearTimer = window.setTimeout(() => {
            emergencyDrop.value = null
            emergencyDropClearTimer = null
        }, 4500)
    }, 150)
}

const clearEmergencyDropPresentation = () => {
    if (emergencyDropDelayTimer !== null) window.clearTimeout(emergencyDropDelayTimer)
    if (emergencyDropClearTimer !== null) window.clearTimeout(emergencyDropClearTimer)
    emergencyDropDelayTimer = null
    emergencyDropClearTimer = null
    pendingEmergencyDrop.value = null
    emergencyDrop.value = null
}

const handleCreate = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    lobbyError.value = ''
    pendingJoinRoomId.value = null
    currentRoomId.value = roomId
    const roomSession = createActiveRoomSession(roomId, PLAYER_NAME)
    
    subscribeToRoom(roomId)
    
    client.value.publish({ 
        destination: '/app/create', 
        body: JSON.stringify({
            roomId,
            playerName: PLAYER_NAME,
            analyticsClientId,
            reconnectToken: roomSession.reconnectToken,
        })
    })
    
    currentView.value = 'game'
}

const handleJoin = (roomId: string) => {
    if (!client.value || !isConnected.value) return
    lobbyError.value = ''
    pendingJoinRoomId.value = roomId
    currentRoomId.value = roomId
    const roomSession = createActiveRoomSession(roomId, PLAYER_NAME)
    
    subscribeToRoom(roomId)
    
    client.value.publish({ 
        destination: '/app/join', 
        body: JSON.stringify({
            roomId,
            playerName: PLAYER_NAME,
            analyticsClientId,
            reconnectToken: roomSession.reconnectToken,
        })
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

const fetchTraitsForMode = async (mode: GameMode) => {
    try {
        const traitsRes = await fetch(`/api/traits?mode=${mode}`);
        if (traitsRes.ok) {
            const traits = await traitsRes.json();
            setTraitData(traits);
        }
    } catch (e) {
        console.error("Failed to fetch traits for mode", mode, e);
    }
}

const handleModeChange = (mode: GameMode) => {
    if (!client.value || !isConnected.value) return
    client.value.publish({
        destination: `/app/room/${currentRoomId.value}/mode`,
        body: JSON.stringify({ playerName: PLAYER_NAME, gameMode: mode })
    })
}

const resetToLobby = () => {
    clearRoomSubscriptions()
    clearRestoredRoomTimeout()
    clearActiveRoomSession()
    clearEmergencyDropPresentation()

    currentView.value = 'lobby'
    gameState.value = null
    currentRoomId.value = ''
    activeTraitMode.value = null
    viewedPlayerId.value = null
    pendingJoinRoomId.value = null

    applyThemeMeta(null)
}

const leaveCurrentGame = () => {
    if (client.value && isConnected.value && currentRoomId.value) {
        client.value.publish({
            destination: '/app/leave',
            body: JSON.stringify({ roomId: currentRoomId.value, playerName: PLAYER_NAME })
        })
    }
    resetToLobby()
}

const abandonCurrentGame = () => {
    if (client.value && isConnected.value && currentRoomId.value) {
        client.value.publish({
            destination: '/app/abandon',
            body: JSON.stringify({ roomId: currentRoomId.value, playerName: PLAYER_NAME })
        })
    }
    resetToLobby()
}

const handleLeaveLobby = () => {
    leaveCurrentGame()
}

const themeClass = computed(() => {
    if (isAdminAnalytics.value) return 'theme-generic'
    if (isUltimateGallery.value) return `theme-${ultimateGalleryMode.value}`
    if (currentView.value === 'lobby' || !gameState.value) return 'theme-generic'
    return `theme-${gameState.value.gameMode}`
})

const loadUltimateGallery = async () => {
    if (!import.meta.env.DEV || UltimateGallery.value) return

    const galleryPath = './components/game/UltimateGallery.vue'
    const galleryModule = await import(/* @vite-ignore */ galleryPath)
    UltimateGallery.value = galleryModule.default
}

const updateStandaloneRoute = () => {
    const wasAdmin = isAdminAnalytics.value
    const isAdmin = window.location.hash.startsWith('#/admin/analytics')
    isAdminAnalytics.value = isAdmin
    if (isAdmin) {
        const validAdminRoute = /^#\/admin\/analytics(?:\/runs\/[^/?#]+)?$/.test(window.location.hash)
        if (!validAdminRoute) window.location.hash = '#/admin/analytics'
        document.title = 'Gameplay Analytics'
        client.value?.deactivate()
        return
    }
    if (wasAdmin) {
        window.location.reload()
        return
    }
    const isGallery = import.meta.env.DEV && window.location.hash.startsWith('#/ultimate-gallery')
    isUltimateGallery.value = isGallery
    if (isGallery) {
        ultimateGalleryMode.value = window.location.hash.includes('/pokemon') ? 'pokemon' : 'onepiece'
        applyThemeMeta(ultimateGalleryMode.value)
        void loadUltimateGallery()
    }
}

const applyThemeMeta = (mode: GameMode | null) => {
    const link = document.querySelector("link[rel*='icon']") as HTMLLinkElement;
    if (!mode) {
        document.title = 'Tactics Arena';
        if (link && !link.href.includes('favicon.svg')) link.href = '/favicon.svg';
        return;
    }

    if (mode === 'pokemon') {
        if (document.title !== 'Pokemon TFT') document.title = 'Pokemon TFT';
        if (link && !link.href.includes('pokeball.png')) link.href = '/pokeball.png';
    } else {
        if (document.title !== 'OnePieceTactics') document.title = 'OnePieceTactics';
        if (link && !link.href.includes('favicon.svg')) link.href = '/favicon.svg';
    }
}

</script>

<template>
  <AdminAnalytics v-if="isAdminAnalytics" />
  <div v-else :class="['app-container', themeClass]">
    <button v-if="showVersion && currentView === 'lobby'"
            class="changelog-dock"
            type="button"
            @click="currentView = 'changelog'">
        Changelog
    </button>
    <VersionDisplay :visible="showVersion" />

    <component :is="UltimateGallery" v-if="isUltimateGallery && UltimateGallery" :mode="ultimateGalleryMode" />

    <div v-else-if="!isConnected" class="loading-screen">
        Connecting to Server...
    </div>
    
    <template v-else>
        <Lobby v-if="currentView === 'lobby'" 
               :title="gameTitle"
               :error="lobbyError"
               @create="handleCreate" 
               @join="handleJoin" />

        <Changelog v-else-if="currentView === 'changelog'"
                   @back="currentView = 'lobby'" />
               
        <div v-else class="game-container">
             <!-- If in LOBBY phase, show WaitingRoom -->
             <template v-if="gameState">
                 <WaitingRoom v-if="gameState.phase === 'LOBBY'"
                              :game-state="gameState"
                              :current-player-name="PLAYER_NAME"
                              :available-modes="availableModes"
                              :default-mode="defaultMode"
                              @start="handleStartGame"
                              @leave="handleLeaveLobby"
                              @mode-change="handleModeChange" />

	                 <!-- Otherwise show GameInterface -->
	                 <template v-else>
	                     <GameInterface :state="gameState"
	                                    :current-player-name="PLAYER_NAME"
	                                    :is-connected="isConnected"
	                                    :emergency-drop="emergencyDrop"
	                                    :queued-emergency-drop="pendingEmergencyDrop"
	                                    :suppress-planning-announcement="hasQueuedEmergencyDrop"
	                                    @action="handleGameAction"
	                                    @view-player="(playerId) => viewedPlayerId = playerId"
	                                    @exit-game="leaveCurrentGame"
	                                    @abandon-game="abandonCurrentGame" />
	                     <Transition name="outcome">
	                        <OutcomeOverlay v-if="encounterResult" :type="encounterResult" />
	                     </Transition>
	                     <DamageReport v-if="gameState.damageLog"
	                                   :damage-log="gameState.damageLog"
	                                   :my-player-id="damageReportPlayerId"
	                                   :my-player-name="damageReportPlayerName"
	                                   :opponent-id="opponentId"
	                                   :opponent-name="opponentName"
	                                   :game-mode="gameState.gameMode" />
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
    font-family: var(--app-font-family);
    background-color: #0f172a;
    color: #f8fafc;
}

*, *::before, *::after {
    box-sizing: border-box;
}

.app-container {
    width: 100%;
    height: 100vh;
    overflow: hidden;
    background: var(--app-bg);
    color: var(--app-fg);
    transition: background 0.4s ease, color 0.4s ease;
}

.app-container.theme-generic {
    --app-bg: radial-gradient(circle at top, #1f2937 0%, #0b1120 70%);
    --app-fg: #f8fafc;
    --room-bg: radial-gradient(circle at top, #1f2937 0%, #0b1120 70%);
    --room-fg: #f8fafc;
    --room-muted: #94a3b8;
    --room-accent: #f59e0b;
    --room-accent-contrast: #0f172a;
    --room-avatar: #3b82f6;
}

.app-container.theme-onepiece {
    --app-bg: radial-gradient(circle at 20% 10%, #233044 0%, #0b1120 65%);
    --app-fg: #f8fafc;
    --room-bg: radial-gradient(circle at 20% 10%, #233044 0%, #0b1120 65%);
    --room-fg: #f8fafc;
    --room-muted: #cbd5f5;
    --room-accent: #fbbf24;
    --room-accent-contrast: #0b1120;
    --room-avatar: #22d3ee;
}

.app-container.theme-pokemon {
    --app-bg: radial-gradient(circle at top, #1e3a8a 0%, #0f172a 60%);
    --app-fg: #f8fafc;
    --room-bg: radial-gradient(circle at top, #1e3a8a 0%, #0f172a 60%);
    --room-fg: #f8fafc;
    --room-muted: #c7d2fe;
    --room-accent: #fcd34d;
    --room-accent-contrast: #0b1120;
    --room-avatar: #f97316;
}
</style>

<style scoped>
.loading-screen {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 2em;
}

.outcome-enter-active {
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.changelog-dock {
  position: fixed;
  bottom: 28px;
  left: 8px;
  z-index: 10000;
  padding: 5px 8px;
  border: 1px solid rgba(251, 191, 36, 0.34);
  border-radius: 5px;
  background: rgba(15, 23, 42, 0.76);
  color: #fde68a;
  font-family: 'Courier New', monospace;
  font-size: 0.66rem;
  font-weight: 900;
  text-transform: uppercase;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.changelog-dock:hover {
  border-color: rgba(251, 191, 36, 0.68);
  background: rgba(30, 41, 59, 0.92);
  color: #fef3c7;
}

.outcome-leave-active {
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.outcome-leave-to {
  opacity: 0;
  transform: translate(-50%, -60%) scale(0.8);
}

@keyframes popIn {
  from {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 0;
  }
  to {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
}
</style>
