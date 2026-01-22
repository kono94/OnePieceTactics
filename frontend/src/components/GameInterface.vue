<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue'
import GameCanvas from './GameCanvas.vue'
import UnitTooltip from './UnitTooltip.vue'
import PhaseAnnouncement from './PhaseAnnouncement.vue'
import TraitSidebar from './TraitSidebar.vue'
import PlayerList from './PlayerList.vue'
import EndScreen from './EndScreen.vue'

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

const allPlayers = computed(() => {
    if (!props.state?.players) return []
    return Object.values(props.state.players)
})

const isDead = computed(() => {
    return myPlayer.value && myPlayer.value.health <= 0
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
    isDraggingUnit.value = true
    draggedUnit.value = unit
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

const onBenchDragEnd = () => {
    isDraggingUnit.value = false
    draggedUnit.value = null
    isSellZoneHovered.value = false
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

// ========== DRAG AND SELL STATE ==========
const isDraggingUnit = ref(false)
const draggedUnit = ref<any>(null)
const isSellZoneHovered = ref(false)
const isDraggingFromGrid = ref(false)

// Calculate sell value: cost × 3^(starLevel - 1)
function calculateSellRefund(unit: any): number {
    if (!unit) return 0
    const cost = unit.cost || 1
    const starLevel = unit.starLevel || 1
    return cost * Math.pow(3, starLevel - 1)
}

function sellUnit(unitId: string) {
    if (!myPlayer.value) return
    emit('action', { type: 'SELL', unitId, playerId: myPlayer.value.playerId })
}

const onSellDragOver = (evt: DragEvent) => {
    evt.preventDefault()
    isSellZoneHovered.value = true
    if (evt.dataTransfer) {
        evt.dataTransfer.dropEffect = 'move'
    }
}

const onSellDragLeave = () => {
    isSellZoneHovered.value = false
}

const onSellDrop = (evt: DragEvent) => {
    evt.preventDefault()
    isSellZoneHovered.value = false
    if (evt.dataTransfer) {
        const unitId = evt.dataTransfer.getData('unitId')
        if (unitId) {
            sellUnit(unitId)
        }
    }
    draggedUnit.value = null
    isDraggingUnit.value = false
}

// Grid drag handlers
const onGridDragStart = (unit: any) => {
    isDraggingUnit.value = true
    isDraggingFromGrid.value = true
    draggedUnit.value = unit
}

const onGridDragEnd = () => {
    isDraggingUnit.value = false
    isDraggingFromGrid.value = false
    draggedUnit.value = null
    isSellZoneHovered.value = false
}

// ========== STAR-UP CELEBRATION FOR BENCH ==========
const starUpUnits = ref<Set<string>>(new Set())
const prevStarLevelMap = ref<Record<string, number>>({})
const STAR_UP_ANIMATION_DURATION = 1200

// Cleanup timers on unmount
const starUpTimers = ref<number[]>([])
onUnmounted(() => {
    starUpTimers.value.forEach(timer => clearTimeout(timer))
})

function triggerStarUpCelebration(unitId: string) {
    if (starUpUnits.value.has(unitId)) return
    console.log('⭐ Bench star-up celebration for:', unitId)
    starUpUnits.value.add(unitId)
    
    const timer = window.setTimeout(() => {
        starUpUnits.value.delete(unitId)
    }, STAR_UP_ANIMATION_DURATION)
    starUpTimers.value.push(timer)
}

function isStarringUp(unitId: string): boolean {
    return starUpUnits.value.has(unitId)
}

// Watch for star level changes in bench units
watch(() => benchUnits.value, (newBench) => {
    newBench.forEach((unit: any) => {
        const prevStarLevel = prevStarLevelMap.value[unit.id]
        const currentStarLevel = unit.starLevel || 1
        
        if (prevStarLevel !== undefined && currentStarLevel > prevStarLevel) {
            triggerStarUpCelebration(unit.id)
        } else if (prevStarLevel === undefined && currentStarLevel >= 2) {
            triggerStarUpCelebration(unit.id)
        }
        
        prevStarLevelMap.value[unit.id] = currentStarLevel
    })
}, { deep: true })

</script>

<template>
  <div class="game-interface">
    <PhaseAnnouncement v-if="state" :phase="state.phase" />
    <EndScreen v-if="state?.phase === 'END'" :players="allPlayers" :my-player-id="myPlayer?.playerId" />


    <template v-if="state">
        <!-- Top Bar -->
        <div class="top-bar" :class="{ 'combat': state.phase === 'COMBAT' }">
            <div class="phase-info">
                <span class="phase-name">{{ state.phase }}</span>
                <span class="round-name" style="font-size: 12px; opacity: 0.7;">{{ state.gameMode }}</span>
                <span class="round-name">Round {{ state.round }}</span>
            </div>
            <div class="timer-bar-container">
                <div class="timer-bar-fill" 
                     :style="{ 
                        width: (state.timeRemainingMs / Math.max(1, state.totalPhaseDuration || (state.phase === 'PLANNING' ? 8000 : 20000)) * 100) + '%',
                        backgroundColor: state.phase === 'COMBAT' ? '#ef4444' : '#3b82f6'
                     }">
                </div>
            </div>
        </div>

        <!-- Main Game Area -->
        <div class="main-area" :class="{ 'dead-state': isDead }">
            <TraitSidebar v-if="myPlayer" :units="myPlayerBoardUnits" />
            <GameCanvas :state="state" :my-player-id="myPlayer?.playerId" 
                :is-dragging-prop="isDraggingUnit"
                @move="handleBoardMove" 
                @drag-start="onGridDragStart"
                @drag-end="onGridDragEnd" />
            <PlayerList v-if="state" :players="allPlayers" :my-player-id="myPlayer?.playerId" />
        </div>

        <!-- Bottom UI -->
        <div class="bottom-ui" v-if="myPlayer" :class="{ 'dead-state': isDead }">
            <!-- Player Stats -->
            <div class="stats-panel">
                <div class="level-info">
                    <div class="level-badge">Lvl {{ myPlayer.level }}</div>
                    <div class="xp-bar" :title="`XP: ${myPlayer.xp} / ${myPlayer.nextLevelXp || 10}`">
                        <div class="xp-fill" :style="{ width: (myPlayer.xp / (myPlayer.nextLevelXp || 10) * 100) + '%' }"></div>
                        <span class="xp-text">{{ myPlayer.xp }} / {{ myPlayer.nextLevelXp || 10 }} XP</span>
                    </div>
                </div>
                <div class="gold-info">
                    <span class="gold-amount">{{ myPlayer.gold }}</span>
                    <span class="gold-label">Gold</span>
                </div>
                <div class="unit-count" :class="{ 'max-units': myPlayerBoardUnits.length >= myPlayer.level }">
                     Units: {{ myPlayerBoardUnits.length }} / {{ myPlayer.level }}
                </div>
                <button class="xp-btn" @click="buyXp" :disabled="myPlayer.gold < 4">
                    Buy XP (4g)
                </button>
            </div>

            <!-- Bench + Sell Zone -->
            <div class="bench-sell-wrapper">
                <div class="bench-area">
                    <div class="bench-slots">
                        <!-- 9 slots or however many -->
                        <div v-for="i in 9" :key="'slot-'+(i-1)" 
                             class="bench-slot"
                             :class="{ 'highlight-drop': isDraggingFromGrid }"
                             @dragover.prevent
                             @drop="(e) => onBenchDrop(e, i-1)">
                            
                           <div v-if="benchUnits[i-1]" 
                                class="bench-unit" 
                                :class="{ 'star-up': isStarringUp(benchUnits[i-1].id) }"
                                draggable="true"
                                @dragstart="(e) => onBenchDragStart(e, benchUnits[i-1])"
                                @dragend="onBenchDragEnd"
                                @mouseenter="hoveredBenchUnitId = benchUnits[i-1].id"
                                @mouseleave="hoveredBenchUnitId = null">
                              
                              <div class="bench-unit-inner">
                                 <img :src="`/assets/units/${benchUnits[i-1].definitionId}.png`" 
                                      class="bench-unit-img" />
                                 <div class="star-indicator" :class="'stars-' + (benchUnits[i-1].starLevel || 1)">
                                     <span v-for="n in (benchUnits[i-1].starLevel || 1)" :key="n" class="star-dot"></span>
                                 </div>
                                 
                                 <!-- Star-up celebration effect -->
                                 <div v-if="isStarringUp(benchUnits[i-1].id)" class="star-up-burst">
                                     <span v-for="j in 8" :key="j" class="star-particle" :style="{ '--particle-index': j }"></span>
                                 </div>
                              </div>
                              
                              <!-- Tooltip for Bench -->
                              <transition name="fade">
                                 <UnitTooltip v-if="hoveredBenchUnitId === benchUnits[i-1].id" :unit="benchUnits[i-1]" />
                              </transition>
                           </div>
                        </div>
                    </div>
                </div>

                <!-- Sell Zone -->
                <div class="sell-zone" 
                     :class="{ 
                        'visible': isDraggingUnit, 
                        'active': isSellZoneHovered 
                     }"
                     @dragover="onSellDragOver"
                     @dragleave="onSellDragLeave"
                     @drop="onSellDrop">
                    <div class="sell-content">
                        <div class="sell-icon">💰</div>
                        <div class="sell-text">SELL</div>
                        <div v-if="draggedUnit" class="sell-refund">
                            +{{ calculateSellRefund(draggedUnit) }} gold
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
                                  <img :src="`/assets/units/${card.id}.png`" 
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
    padding: 6px 20px;
    font-size: 16px;
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
    position: relative;
    width: 42px;
    height: 42px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: bold;
    color: black;
    font-size: 11px;
}
.bench-unit-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    pointer-events: none;
}

.main-area {
    position: relative;
    flex: 1;
    min-height: 0; /* Critical for flex shrinking */
    display: flex;
    justify-content: center;
    align-items: center;
    background: #1a1a1a;
    overflow: visible;
}

.bottom-ui {
    flex-shrink: 0;
    background: #0f172a;
    border-top: 2px solid #334155;
    display: flex;
    padding: 8px 12px;
    gap: 10px;
    position: relative;
    z-index: 60;
    overflow: visible;
}

/* Stats Panel */
.stats-panel {
    width: 200px;
    min-width: 200px;
    flex-shrink: 0;
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
    min-width: 80px;
    background: #334155;
    border-radius: 5px;
    position: relative;
    overflow: visible;
}

.xp-fill {
    height: 100%;
    background: #3b82f6;
    border-radius: 5px;
}
.xp-text {
    font-size: 10px;
    position: absolute;
    top: 14px;
    left: 0;
    white-space: nowrap;
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



.unit-count {
    background: rgba(0,0,0,0.3);
    padding: 5px;
    border-radius: 4px;
    text-align: center;
    font-size: 14px;
    font-weight: bold;
}
.unit-count.max-units {
    color: #ef4444; /* Red warning */
    border: 1px solid #ef4444;
}

/* Bench */
.bench-area {
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(0,0,0,0.2);
    border-radius: 8px;
    padding: 6px;
}

.bench-slots {
    display: flex;
    gap: 6px;
}

.bench-slot {
    width: 48px;
    height: 48px;
    background: #1e293b;
    border: 2px dashed #475569;
    border-radius: 6px;
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 0.2s ease;
}

.bench-slot.highlight-drop {
    border-color: #60a5fa;
    background: rgba(59, 130, 246, 0.2);
    box-shadow: 0 0 10px rgba(59, 130, 246, 0.3);
}

/* Bench + Sell Zone Wrapper */
.bench-sell-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

/* Sell Zone */
.sell-zone {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    min-width: 450px;
    height: 40px;
    background: linear-gradient(135deg, #1e293b, #0f172a);
    border: 2px dashed #475569;
    border-radius: 8px;
    opacity: 0.4;
    transform: scale(0.98);
    transition: all 0.3s ease;
    pointer-events: none;
}

.sell-zone.visible {
    opacity: 1;
    transform: scale(1);
    pointer-events: auto;
    border-color: #ef4444;
    background: linear-gradient(135deg, rgba(127, 29, 29, 0.5), rgba(15, 23, 42, 0.9));
    box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

.sell-zone.active {
    border-color: #fbbf24;
    background: linear-gradient(135deg, rgba(234, 179, 8, 0.4), rgba(15, 23, 42, 0.9));
    box-shadow: 0 0 30px rgba(251, 191, 36, 0.5);
    transform: scale(1.02);
}

.sell-content {
    display: flex;
    align-items: center;
    gap: 12px;
}

.sell-icon {
    font-size: 24px;
}

.sell-text {
    font-weight: bold;
    font-size: 16px;
    letter-spacing: 3px;
    color: #f87171;
}

.sell-zone.active .sell-text {
    color: #fbbf24;
}

.sell-refund {
    font-size: 14px;
    color: #fbbf24;
    font-weight: bold;
    padding: 4px 8px;
    background: rgba(0, 0, 0, 0.3);
    border-radius: 4px;
}

/* Shop */
.shop-area {
    flex: 1;
    min-width: 350px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.shop-cards {
    display: flex;
    gap: 8px;
    flex: 1;
}

.shop-card {
    flex: 1 1 0;
    min-width: 80px;
    max-width: 120px;
    background: #1e293b;
    border: 1px solid #475569;
    border-radius: 6px;
    cursor: pointer;
    padding: 5px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    transition: all 0.2s;
    overflow: hidden;
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
    width: 50px;
    height: 50px;
    object-fit: contain;
    margin-bottom: 4px;
}
.shop-card-info {
    text-align: center;
}

.reroll-btn, .xp-btn {
    padding: 8px;
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

.dead-state {
    filter: grayscale(100%) brightness(0.6);
    pointer-events: none;
    transition: all 1s ease;
}

/* Star Level Indicator */
.star-indicator {
    position: absolute;
    bottom: -2px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 2px;
    z-index: 5;
}

.star-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: linear-gradient(135deg, #fbbf24, #f59e0b);
    box-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

/* Enhanced glow for 2-star units */
.stars-2 .star-dot {
    background: linear-gradient(135deg, #fcd34d, #fbbf24);
    box-shadow: 0 0 3px #fbbf24, 0 0 1px rgba(0, 0, 0, 0.5);
}

/* Bright gold glow for 3-star (max) units */
.stars-3 .star-dot {
    width: 7px;
    height: 7px;
    background: linear-gradient(135deg, #fef3c7, #fbbf24);
    box-shadow: 0 0 4px #fbbf24, 0 0 8px rgba(251, 191, 36, 0.6);
}

/* ========== STAR-UP CELEBRATION ========== */
.bench-unit.star-up .bench-unit-inner {
    animation: starUpGlow 1.2s ease-out;
}

@keyframes starUpGlow {
    0% {
        filter: brightness(1);
        box-shadow: 0 0 0 rgba(251, 191, 36, 0);
    }
    15% {
        filter: brightness(2);
        box-shadow: 0 0 30px rgba(251, 191, 36, 1);
    }
    50% {
        filter: brightness(1.5);
        box-shadow: 0 0 20px rgba(251, 191, 36, 0.8);
    }
    100% {
        filter: brightness(1);
        box-shadow: 0 0 0 rgba(251, 191, 36, 0);
    }
}

.star-up-burst {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    pointer-events: none;
    z-index: 20;
}

.star-particle {
    position: absolute;
    width: 6px;
    height: 6px;
    background: linear-gradient(135deg, #fef3c7, #fbbf24);
    border-radius: 50%;
    animation: particleBurst 1s ease-out forwards;
    --angle: calc(var(--particle-index) * 45deg);
    transform-origin: center;
}

@keyframes particleBurst {
    0% {
        opacity: 1;
        transform: rotate(var(--angle)) translateY(0) scale(1);
    }
    50% {
        opacity: 1;
        transform: rotate(var(--angle)) translateY(-35px) scale(1.2);
    }
    100% {
        opacity: 0;
        transform: rotate(var(--angle)) translateY(-50px) scale(0.5);
    }
}
</style>
