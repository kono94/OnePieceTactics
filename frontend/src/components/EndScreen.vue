<template>
  <div class="end-screen">
    <div class="end-screen__card">
        
        <!-- Background accent -->
        <div class="end-screen__accent"></div>
        
        <h1 class="end-screen__title" :class="isWinner ? 'end-screen__title--winner' : 'end-screen__title--loser'">
            {{ isWinner ? 'Victory' : 'Game Over' }}
        </h1>
        
         <div class="end-screen__result">
            You finished <span :class="getPlaceClass(myPlace)">#{{ myPlace || '-' }}</span>
         </div>

        <div class="end-screen__rankings">
             <div class="end-screen__rankings-header">
                <span>Player</span>
                <span>Rank</span>
             </div>
             
             <div 
                v-for="player in sortedPlayers" 
                :key="player.playerId"
                class="end-screen__player"
                :class="{'end-screen__player--me': player.playerId === myPlayerId}"
             >
                <div class="end-screen__player-info">
                    <div class="end-screen__player-level">
                        {{ player.level }}
                    </div>
                    <span class="end-screen__player-name">{{ player.name }}</span>
                    <span v-if="player.playerId === myPlayerId" class="end-screen__you-badge">YOU</span>
                </div>
                
                <div class="end-screen__player-place" :class="getPlaceClass(player.place)">
                    #{{ player.place || '-' }}
                </div>
             </div>
        </div>
        
        <button 
            @click="reloadGame"
            class="end-screen__play-again"
        >
            Play Again
        </button>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { PlayerState } from '../types';

const props = defineProps<{
  players: PlayerState[];
  myPlayerId: string | undefined;
}>();

const sortedPlayers = computed(() => {
    return [...props.players].sort((a, b) => {
        const pA = a.place || 99;
        const pB = b.place || 99;
        return pA - pB;
    });
});

const myPlayer = computed(() => props.players.find(p => p.playerId === props.myPlayerId));
const myPlace = computed(() => myPlayer.value ? myPlayer.value.place : '?');
const isWinner = computed(() => myPlace.value === 1);

function getPlaceClass(place: number | string | null | undefined) {
  if (place === 1) return 'place--gold';
  if (place === 2) return 'place--silver';
  if (place === 3) return 'place--bronze';
  return 'place--default';
}

function reloadGame() {
    window.location.reload();
}
</script>

<style scoped>
.end-screen {
    position: fixed;
    inset: 0;
    z-index: 50;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(15, 23, 42, 0.9);
    backdrop-filter: blur(4px);
    animation: fadeIn 0.5s ease-out;
}

.end-screen__card {
    background: #0f172a;
    border: 1px solid #334155;
    padding: 32px;
    border-radius: 16px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    max-width: 640px;
    width: 100%;
    text-align: center;
    display: flex;
    flex-direction: column;
    gap: 24px;
    position: relative;
    overflow: hidden;
}

.end-screen__accent {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 2px;
    background: linear-gradient(to right, transparent, #f59e0b, transparent);
    opacity: 0.5;
}

.end-screen__title {
    font-size: 48px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: -0.025em;
}

.end-screen__title--winner {
    background: linear-gradient(to bottom, #fcd34d, #d97706);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.3));
}

.end-screen__title--loser {
    color: #94a3b8;
}

.end-screen__result {
    font-size: 20px;
    color: #cbd5e1;
    font-weight: 500;
}

.end-screen__rankings {
    display: flex;
    flex-direction: column;
    gap: 8px;
    background: rgba(30, 41, 59, 0.5);
    border-radius: 8px;
    padding: 16px;
    max-height: 50vh;
    overflow-y: auto;
    text-align: left;
}

.end-screen__rankings-header {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    font-weight: 700;
    color: #64748b;
    text-transform: uppercase;
    padding: 0 16px 8px;
    border-bottom: 1px solid rgba(51, 65, 85, 0.5);
}

.end-screen__player {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px;
    border-radius: 6px;
    background: #1e293b;
    transition: background 0.2s;
}

.end-screen__player:hover {
    background: #334155;
}

.end-screen__player--me {
    box-shadow: inset 0 0 0 1px rgba(245, 158, 11, 0.3);
    background: rgba(51, 65, 85, 0.5);
}

.end-screen__player-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.end-screen__player-level {
    width: 32px;
    height: 32px;
    border-radius: 4px;
    background: #475569;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    color: #e2e8f0;
}

.end-screen__player-name {
    font-weight: 700;
    color: #e2e8f0;
}

.end-screen__you-badge {
    font-size: 11px;
    background: rgba(245, 158, 11, 0.2);
    color: #fcd34d;
    padding: 2px 6px;
    border-radius: 4px;
}

.end-screen__player-place {
    font-weight: 900;
    font-size: 20px;
}

.end-screen__play-again {
    margin-top: 16px;
    padding: 12px 32px;
    background: #d97706;
    color: white;
    font-weight: 700;
    border-radius: 6px;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.end-screen__play-again:hover {
    background: #f59e0b;
    box-shadow: 0 10px 15px -3px rgba(245, 158, 11, 0.3);
    transform: translateY(-2px);
}

/* Place ranking colors */
.place--gold {
    color: #fbbf24;
}

.place--silver {
    color: #cbd5e1;
}

.place--bronze {
    color: #b45309;
}

.place--default {
    color: #64748b;
}

@keyframes fadeIn {
    from { opacity: 0; transform: scale(0.95); }
    to { opacity: 1; transform: scale(1); }
}
</style>
