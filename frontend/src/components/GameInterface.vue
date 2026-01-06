<script setup lang="ts">
import { computed, ref } from 'vue'
import GameCanvas from './GameCanvas.vue'
import UnitTooltip from './UnitTooltip.vue'
import PhaseAnnouncement from './PhaseAnnouncement.vue'
import TraitSidebar from './TraitSidebar.vue'

const props = defineProps<{
  state: any,
  currentPlayerName: string
}>()

const emit = defineEmits(['action'])

const myPlayer = computed(() => {
    if (!props.state?.players) return null
    // Find player by name
    return Object.values(props.state.players).find((p: any) => p.name === props.currentPlayerName) as any
})

const shopCards = computed(() => {
    return myPlayer.value?.shop || []
})

const benchUnits = computed(() => {
    return myPlayer.value?.bench || []
})

const myPlayerBoardUnits = computed(() => {
    if (!myPlayer.value) return []
    return myPlayer.value.boardUnits || myPlayer.value.board || []
})

function buyUnit(index: number) {
    if (!myPlayer.value) return
    emit('action', { type: 'BUY', shopIndex: index, playerId: myPlayer.value.playerId })
}

function refreshShop() {
    if (!myPlayer.value) return
    emit('action', { type: 'REROLL', playerId: myPlayer.value.playerId })
}

function buyXp() {
    if (!myPlayer.value) return
    emit('action', { type: 'EXP', playerId: myPlayer.value.playerId })
}

const onBenchDragStart = (evt: DragEvent, unit: any) => {
    if (evt.dataTransfer) {
        evt.dataTransfer.setData('unitId', unit.id)
        evt.dataTransfer.effectAllowed = 'move'
        
        // Set drag image to the image element inside the bench unit
        const target = evt.target as HTMLElement;
        const img = target.querySelector('img');
        if (img) {
             evt.dataTransfer.setDragImage(img, 25, 25);
        }
    }
}

const onBenchDrop = (evt: DragEvent, index: number) => {
    evt.preventDefault()
    if (evt.dataTransfer) {
        const unitId = evt.dataTransfer.getData('unitId')
        if (unitId) {
             // Move to bench slot index
             // Note: Backend needs to handle this logically. 
             // Ideally we distinguish bench move from board move. 
             // If targetY is -1, it's bench.
             emit('action', { type: 'MOVE', unitId, targetX: index, targetY: -1, playerId: myPlayer.value.playerId })
        }
    }
}

const handleBoardMove = (movePayload: any) => {
    console.log("Emitting MOVE action", movePayload)
    emit('action', { type: 'MOVE', unitId: movePayload.unitId, targetX: movePayload.x, targetY: movePayload.y, playerId: myPlayer.value.playerId })
}

// Tooltip state for bench and shop
const hoveredBenchUnitId = ref<string|null>(null)
const hoveredShopIndex = ref<number|null>(null)

</script>

<template>
  <div class="game-interface">
    <PhaseAnnouncement v-if="state" :phase="state.phase" />
    <template v-if="state">
        <!-- Top Bar -->
        <div class="top-bar" :class="{ 'combat': state.phase === 'COMBAT' }">
            <div class="phase-info">
                <span class="phase-name">{{ state.phase }}</span>
                <span class="round-name">Round {{ state.round }}</span>
            </div>
            <div class="timer-bar-container">
                <div class="timer-bar-fill" 
                     :style="{ 
                        width: (state.timeRemainingMs / (state.phase === 'PLANNING' ? 30000 : 60000) * 100) + '%',
                        backgroundColor: state.phase === 'COMBAT' ? '#ef4444' : '#3b82f6'
                     }">
                </div>
            </div>
        </div>

        <!-- Main Game Area -->
        <div class="main-area">
            <TraitSidebar v-if="myPlayer" :units="myPlayerBoardUnits" />
            <GameCanvas :state="state" :my-player-id="myPlayer?.playerId" @move="handleBoardMove" />
        </div>

        <!-- Bottom UI -->
        <div class="bottom-ui" v-if="myPlayer">
            <!-- Player Stats -->
            <div class="stats-panel">
                <div class="level-info">
                    <div class="level-badge">Lvl {{ myPlayer.level }}</div>
                    <div class="xp-bar">
                        <div class="xp-fill" :style="{ width: (myPlayer.xp / 10 * 100) + '%' }"></div>
                        <span class="xp-text">{{ myPlayer.xp }} / 10 XP</span>
                    </div>
                </div>
                <div class="gold-info">
                    <span class="gold-amount">{{ myPlayer.gold }}</span>
                    <span class="gold-label">Gold</span>
                </div>
                <button class="xp-btn" @click="buyXp" :disabled="myPlayer.gold < 4">
                    Buy XP (4g)
                </button>
            </div>

            <!-- Bench -->
            <div class="bench-area">
                <div class="bench-slots">
                    <!-- 9 slots or however many -->
                    <div v-for="i in 9" :key="'slot-'+(i-1)" 
                         class="bench-slot"
                         @dragover.prevent
                         @drop="(e) => onBenchDrop(e, i-1)">
                        
                       <div v-if="benchUnits[i-1]" 
                            class="bench-unit" 
                            draggable="true"
                            @dragstart="(e) => onBenchDragStart(e, benchUnits[i-1])"
                            @mouseenter="hoveredBenchUnitId = benchUnits[i-1].id"
                            @mouseleave="hoveredBenchUnitId = null">
                          
                          <div class="bench-unit-inner">
                             <img :src="`/assets/units/${benchUnits[i-1].name === 'Monkey D. Luffy' ? 'luffy_v1' : benchUnits[i-1].name === 'Roronoa Zoro' ? 'zoro_v1' : 'nami_v1'}.png`" 
                                  class="bench-unit-img" />
                          </div>
                          
                          <!-- Tooltip for Bench -->
                          <transition name="fade">
                             <UnitTooltip v-if="hoveredBenchUnitId === benchUnits[i-1].id" :unit="benchUnits[i-1]" />
                          </transition>
                       </div>
                    </div>
                </div>
            </div>

            <!-- Shop -->
            <div class="shop-area">
                 <div class="shop-cards">
                     <div v-for="(card, idx) in shopCards" :key="idx" class="shop-card" 
                          :class="{ 'empty': !card, 'can-buy': card && myPlayer.gold >= card.cost }"
                          style="position: relative;"
                          @click="card && buyUnit(Number(idx))"
                          @mouseenter="card ? hoveredShopIndex = Number(idx) : null"
                          @mouseleave="hoveredShopIndex = null">
                          <template v-if="card">
                              <div class="shop-card-inner">
                                  <img :src="`/assets/units/${card.name === 'Monkey D. Luffy' ? 'luffy_v1' : card.name === 'Roronoa Zoro' ? 'zoro_v1' : 'nami_v1'}.png`" 
                                       class="shop-card-img" />
                                  <div class="shop-card-info">
                                      <div class="cost">{{ card.cost }}</div>
                                      <div class="name">{{ card.name }}</div>
                                      <div class="traits">{{ card.traits?.join(', ') }}</div>
                                  </div>
                              </div>
                              <transition name="fade">
                                <UnitTooltip v-if="hoveredShopIndex === idx" :unit="card" />
                              </transition>
                          </template>
                     </div>
                 </div>
                 <button class="reroll-btn" @click="refreshShop" :disabled="myPlayer.gold < 2">
                     Refresh (2g)
                 </button>
            </div>
        </div>
        <div v-else class="waiting-message">
            Waiting for player data...
        </div>
    </template>
    
    <div v-else class="waiting-message">
        Waiting for game state...
    </div>
  </div>
</template>

<style scoped>
.game-interface {
    width: 100%;
    height: 100vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    position: relative;
    color: white;
    background: #0f172a;
}

.top-bar {
    display: flex;
    flex-direction: column;
    padding: 0;
    background: rgba(15, 23, 42, 0.9);
    border-bottom: 1px solid #334155;
    transition: background-color 0.5s;
}

.top-bar.combat {
    background: rgba(69, 10, 10, 0.9);
    border-bottom-color: #ef4444;
}

.phase-info {
    display: flex;
    justify-content: space-between;
    padding: 10px 20px;
    font-size: 18px;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.timer-bar-container {
    width: 100%;
    height: 4px;
    background: #1e293b;
}

.timer-bar-fill {
    height: 100%;
    transition: width 0.1s linear, background-color 0.5s;
}

.bench-unit {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative; /* For tooltip positioning */
    cursor: grab;
}
.bench-unit:active {
    cursor: grabbing;
}

.bench-unit-inner {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: bold;
    color: black;
    font-size: 12px;
}
.bench-unit-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    pointer-events: none;
}

.main-area {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #1a1a1a;
}

.bottom-ui {
    height: 250px;
    background: #0f172a;
    border-top: 2px solid #334155;
    display: flex;
    padding: 10px 20px;
    gap: 20px;
}

/* Stats Panel */
.stats-panel {
    width: 200px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    background: rgba(30, 41, 59, 0.5);
    padding: 10px;
    border-radius: 8px;
}

.level-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.level-badge {
    background: #3b82f6;
    padding: 5px 10px;
    border-radius: 50%;
    font-weight: bold;
}

.xp-bar {
    flex: 1;
    height: 10px;
    background: #334155;
    border-radius: 5px;
    position: relative;
}

.xp-fill {
    height: 100%;
    background: #3b82f6;
    border-radius: 5px;
}
.xp-text {
    font-size: 10px;
    position: absolute;
    top: 12px;
    right: 0;
}

.gold-info {
    display: flex;
    align-items: center;
    gap: 5px;
    margin-top: auto;
    background: rgba(0,0,0,0.3);
    padding: 5px;
    border-radius: 4px;
}
.gold-amount {
    color: #eab308;
    font-size: 24px;
    font-weight: bold;
}

/* Bench */
.bench-area {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(0,0,0,0.2);
    border-radius: 10px;
}

.bench-slots {
    display: flex;
    gap: 10px;
}

.bench-slot {
    width: 60px;
    height: 60px;
    background: #1e293b;
    border: 2px dashed #475569;
    border-radius: 8px;
    display: flex;
    justify-content: center;
    align-items: center;
}


/* Shop */
.shop-area {
    width: 500px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.shop-cards {
    display: flex;
    gap: 10px;
    flex: 1;
}

.shop-card {
    flex: 1;
    background: #1e293b;
    border: 1px solid #475569;
    border-radius: 6px;
    cursor: pointer;
    padding: 5px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    transition: all 0.2s;
}

.shop-card.can-buy:hover {
    transform: translateY(-5px);
    border-color: #eab308;
    background: #334155;
    box-shadow: 0 0 10px rgba(234, 179, 8, 0.3);
}

.shop-card.empty {
    opacity: 0.3;
    cursor: default;
    background: #0f172a;
    border: none;
}

.shop-card .cost {
    color: #eab308;
    font-weight: bold;
}

.shop-card .name {
    font-weight: bold;
    text-align: center;
    font-size: 14px;
}

.shop-card .traits {
    font-size: 10px;
    color: #94a3b8;
    text-align: center;
}

.shop-card-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 100%;
}
.shop-card-img {
    width: 60px;
    height: 60px;
    object-fit: contain;
    margin-bottom: 5px;
}
.shop-card-info {
    text-align: center;
}

.reroll-btn, .xp-btn {
    padding: 10px;
    background: #3b82f6;
    border: none;
    border-radius: 6px;
    color: white;
    font-weight: bold;
    cursor: pointer;
}
.reroll-btn:disabled, .xp-btn:disabled {
    opacity: 0.5;
    background: #475569;
}
.reroll-btn {
    background: #ef4444; 
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
