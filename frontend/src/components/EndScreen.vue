<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/90 backdrop-blur-sm animate-fade-in">
    <div class="bg-slate-900 border border-slate-700 p-8 rounded-2xl shadow-2xl max-w-2xl w-full text-center flex flex-col gap-6 relative overflow-hidden">
        
        <!-- Background accents -->
        <div class="absolute top-0 left-0 w-full h-2 bg-gradient-to-r from-transparent via-amber-500 to-transparent opacity-50"></div>
        
        <h1 class="text-6xl font-black uppercase tracking-tighter" :class="isWinner ? 'text-transparent bg-clip-text bg-gradient-to-b from-amber-300 to-amber-600 drop-shadow-lg' : 'text-slate-400'">
            {{ isWinner ? 'Victory' : 'Game Over' }}
        </h1>
        
         <div class="text-xl text-slate-300 font-medium">
            You finished <span :class="getPlaceClass(myPlace)">#{{ myPlace || '-' }}</span>
         </div>

        <div class="flex flex-col gap-2 bg-slate-800/50 rounded-lg p-4 max-h-[50vh] overflow-y-auto custom-scrollbar text-left">
             <div class="flex justify-between text-xs font-bold text-slate-500 uppercase px-4 pb-2 border-b border-slate-700/50">
                <span>Player</span>
                <span>Rank</span>
             </div>
             
             <div 
                v-for="player in sortedPlayers" 
                :key="player.playerId"
                class="flex items-center justify-between p-3 rounded bg-slate-800 hover:bg-slate-700 transition"
                :class="{'ring-1 ring-amber-500/30 bg-slate-700/50': player.playerId === myPlayerId}"
             >
                <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded bg-slate-600 flex items-center justify-center font-bold text-slate-200">
                        {{ player.level }}
                    </div>
                    <span class="font-bold text-slate-200">{{ player.name }}</span>
                    <span v-if="player.playerId === myPlayerId" class="text-xs bg-amber-500/20 text-amber-300 px-1.5 py-0.5 rounded">YOU</span>
                </div>
                
                <div class="font-black text-xl" :class="getPlaceClass(player.place)">
                    #{{ player.place || '-' }}
                </div>
             </div>
        </div>
        
        <button 
            @click="reloadGame"
            class="mt-4 px-8 py-3 bg-amber-600 hover:bg-amber-500 text-white font-bold rounded shadow-lg hover:shadow-amber-500/20 transition-all transform hover:-translate-y-0.5"
        >
            Play Again
        </button>

    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  players: {
    type: Array,
    required: true
  },
  myPlayerId: {
    type: String,
    required: true
  }
});

const sortedPlayers = computed(() => {
    // Sort by Place (ASC)
    // If place is null (shouldn't happen in END phase unless active), treat as last
    return [...props.players].sort((a, b) => {
        const pA = a.place || 99;
        const pB = b.place || 99;
        return pA - pB;
    });
});

const myPlayer = computed(() => props.players.find(p => p.playerId === props.myPlayerId));
const myPlace = computed(() => myPlayer.value ? myPlayer.value.place : '?');
const isWinner = computed(() => myPlace.value === 1);

function getPlaceClass(place) {
  if (place === 1) return 'text-amber-400';
  if (place === 2) return 'text-slate-300';
  if (place === 3) return 'text-amber-700';
  return 'text-slate-500';
}

function reloadGame() {
    window.location.reload();
}
</script>

<style scoped>
.animate-fade-in {
    animation: fadeIn 0.5s ease-out;
}
@keyframes fadeIn {
    from { opacity: 0; transform: scale(0.95); }
    to { opacity: 1; transform: scale(1); }
}
</style>
