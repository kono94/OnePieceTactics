<template>
  <div class="player-list">
    <h3 class="lobby-title">
      Lobby
    </h3>
    
    <div class="list-container custom-scrollbar">
      <div 
        v-for="(player, index) in sortedPlayers" 
        :key="player.playerId"
        class="player-item"
        :class="[
          player.playerId === myPlayerId ? 'my-player' : 'other-player',
          player.health <= 0 ? 'dead' : ''
        ]"
      >
        <!-- Avatar/Icon Placeholder -->
        <div class="avatar-box">
           <span class="level-text">{{ player.level }}</span>
        </div>
        
        <!-- Info -->
        <div class="info-col">
           <div class="name-row">
              <span class="player-name" :title="player.name">
                {{ player.name }}
              </span>
              <span class="health-text" :class="getHealthColor(player.health)">
                {{ player.health }}
              </span>
           </div>
           
           <!-- HP Bar -->
           <div class="hp-bar-bg">
              <div 
                class="hp-bar-fill"
                :class="getHealthBarClass(player.health)"
                :style="{ width: Math.max(0, player.health) + '%' }"
              ></div>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  players: {
    type: Array, // Array of PlayerState
    required: true
  },
  myPlayerId: {
    type: String,
    required: true
  }
});

const sortedPlayers = computed(() => {
  return [...props.players].sort((a, b) => {
    const aDead = a.health <= 0;
    const bDead = b.health <= 0;
    
    // 1. Living players above dead players
    if (aDead !== bDead) {
      return aDead ? 1 : -1;
    }
    
    // 2. If both dead, sort by Place (ASC)
    if (aDead && bDead) {
       if (a.place && b.place) return a.place - b.place;
       if (a.place) return -1;
       if (b.place) return 1;
    }
    
    // 3. Keep Active players sorted by HP (DESC)
    if (b.health !== a.health) {
        return b.health - a.health;
    }
    
    return 0;
  });
});

function getPlaceClass(place) {
  if (place === 1) return 'text-amber-400 scale-110';
  if (place === 2) return 'text-slate-300';
  if (place === 3) return 'text-amber-700';
  return 'text-slate-500';
}

function getHealthColor(health) {
    if (health > 50) return 'text-green-400';
    if (health > 20) return 'text-yellow-400';
    return 'text-red-500';
}

function getHealthBarClass(health) {
    if (health > 50) return 'bg-green-500';
    if (health > 20) return 'bg-yellow-500';
    return 'bg-red-600';
}
</script>

<style scoped>
.player-list {
    position: absolute;
    right: 20px;
    top: 10px;
    width: 250px;
    max-height: calc(100% - 20px); /* Constrain to parent height minus padding */
    background: rgba(15, 23, 42, 0.9);
    border: 1px solid #334155;
    border-radius: 8px;
    padding: 12px;
    z-index: 40;
    display: flex;
    flex-direction: column;
    gap: 10px;
    pointer-events: auto;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
}

.lobby-title {
    font-size: 14px;
    font-weight: bold;
    color: #f59e0b; /* amber-500 */
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid #334155;
    padding-bottom: 6px;
    margin: 0;
}

.list-container {
    display: flex;
    flex-direction: column;
    gap: 4px;
    flex: 1; /* Fill remaining space */
    min-height: 0; /* Allow shrinking */
    overflow-y: auto;
}

.player-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px;
    border-radius: 6px;
    background: rgba(30, 41, 59, 0.4);
    transition: all 0.2s;
}

.player-item.my-player {
    background: rgba(51, 65, 85, 0.6);
    border: 1px solid rgba(245, 158, 11, 0.5);
}

.player-item:hover {
    background: rgba(51, 65, 85, 0.8);
}

.player-item.dead {
    opacity: 0.6;
    filter: grayscale(1);
}

.avatar-box {
    width: 32px;
    height: 32px;
    border-radius: 4px;
    background: #334155;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid #475569;
    flex-shrink: 0;
}

.level-text {
    font-size: 12px;
    font-weight: bold;
    color: white;
}

.info-col {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
}

.name-row {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
}

.player-name {
    font-size: 12px;
    font-weight: 500;
    color: #e2e8f0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100px;
}

.health-text {
    font-size: 12px;
    font-weight: bold;
}

.hp-bar-bg {
    height: 6px;
    width: 100%;
    background: #020617;
    border-radius: 3px;
    overflow: hidden;
}

.hp-bar-fill {
    height: 100%;
    transition: width 0.5s ease-out;
}

/* Colors helpers that were Tailwind classes */
.text-green-400 { color: #4ade80; }
.text-yellow-400 { color: #facc15; }
.text-red-500 { color: #ef4444; }

.bg-green-500 { background-color: #22c55e; }
.bg-yellow-500 { background-color: #eab308; }
.bg-red-600 { background-color: #dc2626; }

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(30, 41, 59, 0.5);
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 2px;
}
</style>
