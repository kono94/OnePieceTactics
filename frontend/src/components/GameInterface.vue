<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue'
import GameCanvas from './GameCanvas.vue'
import UnitTooltip from './UnitTooltip.vue'
import PhaseAnnouncement from './PhaseAnnouncement.vue'
import TraitSidebar from './TraitSidebar.vue'
import PlayerList from './PlayerList.vue'
import EndScreen from './EndScreen.vue'
import type { GameState, GameUnit, UnitDefinition, PlayerState } from '../types'
import { getUnitIconPath } from '../utils/iconUtils'
import { getRarityColor } from '../utils/colorUtils'
import { SHOP_ODDS } from '../data/shopOdds'

const props = defineProps<{
  state: GameState | null,
  currentPlayerName: string
}>()

const emit = defineEmits(['action'])

const myPlayer = computed((): PlayerState | null => {
    if (!props.state?.players) return null
    // Find player by name
    return Object.values(props.state.players).find((p: PlayerState) => p.name === props.currentPlayerName) ?? null
})

const allPlayers = computed((): PlayerState[] => {
    if (!props.state?.players) return []
    return Object.values(props.state.players)
})

const isDead = computed(() => {
    return myPlayer.value && myPlayer.value.health <= 0
})

const shopCards = computed((): UnitDefinition[] => {
    return myPlayer.value?.shop || []
})

const benchUnits = computed((): (GameUnit | null)[] => {
    return myPlayer.value?.bench || []
})

const myPlayerBoardUnits = computed((): GameUnit[] => {
    if (!myPlayer.value) return []
    return myPlayer.value.boardUnits || myPlayer.value.board || []
})

interface BenchSlot {
    index: number
    unit: GameUnit | null
}

const benchSlots = computed((): BenchSlot[] => {
    const bench = benchUnits.value
    return Array.from({ length: 9 }, (_, i) => ({
        index: i,
        unit: bench[i] ?? null
    }))
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

const onBenchDragStart = (evt: DragEvent, unit: GameUnit) => {
    isDraggingUnit.value = true
    draggedUnit.value = unit
    // Clear hover states when drag starts
    hoveredBenchUnitId.value = null
    hoveredShopIndex.value = null
    
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
    dragOverBenchIndex.value = -1
}

const onBenchDrop = (evt: DragEvent, index: number) => {
    evt.preventDefault()
    dragOverBenchIndex.value = -1
    if (evt.dataTransfer) {
        const unitId = evt.dataTransfer.getData('unitId')
        if (unitId) {
             // Move to bench slot index
             // Note: Backend needs to handle this logically. 
             // Ideally we distinguish bench move from board move. 
             // If targetY is -1, it's bench.
             emit('action', { type: 'MOVE', unitId, targetX: index, targetY: -1, playerId: myPlayer.value?.playerId })
        }
    }
}

const onBenchDragOver = (evt: DragEvent, index: number) => {
    evt.preventDefault()
    dragOverBenchIndex.value = index
    if (evt.dataTransfer) {
        evt.dataTransfer.dropEffect = 'move'
    }
}

const onBenchDragLeave = () => {
    dragOverBenchIndex.value = -1
}

interface MovePayload {
    unitId: string
    x: number
    y: number
}

const handleBoardMove = (movePayload: MovePayload) => {
    console.log("Emitting MOVE action", movePayload)
    emit('action', { type: 'MOVE', unitId: movePayload.unitId, targetX: movePayload.x, targetY: movePayload.y, playerId: myPlayer.value?.playerId })
}

const handleCollectOrb = (orbId: string) => {
    console.log("Emitting COLLECT_ORB action", orbId)
    emit('action', { type: 'COLLECT_ORB', orbId, playerId: myPlayer.value?.playerId })
}

// Tooltip state for bench and shop
const hoveredBenchUnitId = ref<string|null>(null)
const hoveredShopIndex = ref<number|null>(null)

// ========== DRAG AND SELL STATE ==========
const isDraggingUnit = ref(false)
const draggedUnit = ref<GameUnit | null>(null)
const isSellZoneHovered = ref(false)
const isDraggingFromGrid = ref(false)
const dragOverBenchIndex = ref(-1)

// Calculate sell value: cost × 3^(starLevel - 1)
function calculateSellRefund(unit: GameUnit | null): number {
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
const onGridDragStart = (unit: GameUnit) => {
    isDraggingUnit.value = true
    isDraggingFromGrid.value = true
    draggedUnit.value = unit
    // Clear hover states
    hoveredBenchUnitId.value = null
    hoveredShopIndex.value = null
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
    newBench.forEach((unit: GameUnit | null) => {
        if (!unit) return
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

// ========== SHOP ODDS TOOLTIP ==========
const hoveredLevelBadge = ref(false)

const currentShopOdds = computed(() => {
    if (!myPlayer.value) return [0, 0, 0, 0, 0]
    const levelIndex = Math.max(0, Math.min(myPlayer.value.level - 1, SHOP_ODDS.length - 1))
    return SHOP_ODDS[levelIndex]
})

const rarityColors = [
    '#94a3b8', // 1-cost
    '#22c55e', // 2-cost
    '#3b82f6', // 3-cost
    '#a855f7', // 4-cost
    '#eab308'  // 5-cost
]

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
                <div class="game-meta">
                    <span class="game-mode">{{ state.gameMode }}</span>
                    <span class="room-id">Room: {{ state.roomId }}</span>
                </div>
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
                @drag-end="onGridDragEnd"
                @collect-orb="handleCollectOrb" />
            <PlayerList v-if="state" :players="allPlayers" :my-player-id="myPlayer?.playerId" />
        </div>

        <!-- Bottom UI -->
        <div class="bottom-ui" v-if="myPlayer" :class="{ 'dead-state': isDead }">
            <!-- Player Stats -->
            <div class="stats-panel">
                <div class="level-info">
                    <div class="level-badge" 
                         @mouseenter="hoveredLevelBadge = true" 
                         @mouseleave="hoveredLevelBadge = false">
                        Lvl {{ myPlayer.level }}
                        
                        <!-- Shop Odds Tooltip -->
                        <transition name="fade">
                            <div v-if="hoveredLevelBadge" class="odds-tooltip">
                                <div class="odds-header">Shop Odds</div>
                                <div class="odds-grid">
                                    <div v-for="(prob, tier) in currentShopOdds" :key="tier" class="odds-row">
                                        <span class="tier-indicator" :style="{ backgroundColor: rarityColors[tier] }"></span>
                                        <span class="tier-label">{{ tier + 1 }}-Cost:</span>
                                        <span class="prob-value">{{ prob }}%</span>
                                    </div>
                                </div>
                            </div>
                        </transition>
                    </div>
                    <div class="xp-bar" :title="`XP: ${myPlayer.xp} / ${myPlayer.nextLevelXp || 10}`">
                        <div class="xp-fill" :style="{ width: (myPlayer.xp / (myPlayer.nextLevelXp || 10) * 100) + '%' }"></div>
                        <span class="xp-text">{{ myPlayer.xp }} / {{ myPlayer.nextLevelXp || 10 }} XP</span>
                    </div>
                </div>
                <div class="stats-row">
                    <div class="gold-info">
                        <span class="gold-amount">{{ myPlayer.gold }}</span>
                        <span class="gold-label">Gold</span>
                    </div>
                    <div class="unit-count" :class="{ 'max-units': myPlayerBoardUnits.length >= myPlayer.level }">
                        {{ myPlayerBoardUnits.length }}/{{ myPlayer.level }}
                    </div>
                </div>
                <button class="xp-btn" @click="buyXp" :disabled="myPlayer.gold < 4">
                    XP (4g)
                </button>
            </div>

            <!-- Bench Area -->
            <div class="bench-area-wrapper">
                <div class="bench-area">
                    <div class="bench-slots">
                        <!-- 9 slots or however many -->
                        <div v-for="slot in benchSlots" :key="'slot-'+slot.index" 
                             class="bench-slot"
                             :class="{ 
                                'highlight-drop': isDraggingUnit,
                                'active-drop': dragOverBenchIndex === slot.index
                             }"
                             @dragover="(e) => onBenchDragOver(e, slot.index)"
                             @dragleave="onBenchDragLeave"
                             @drop="(e) => onBenchDrop(e, slot.index)">
                            
                           <div v-if="slot.unit" 
                                class="bench-unit" 
                                :class="{ 'star-up': isStarringUp(slot.unit.id) }"
                                :style="{ '--rarity-color': getRarityColor(slot.unit.cost) }"
                                draggable="true"
                                @dragstart="(e) => onBenchDragStart(e, slot.unit!)"
                                @dragend="onBenchDragEnd"
                                @mouseenter="!isDraggingUnit ? hoveredBenchUnitId = slot.unit!.id : null"
                                @mouseleave="hoveredBenchUnitId = null">
                               <!-- Cost Top Glow (Outside inner to avoid clipping) -->
                               <div class="cost-top-glow"></div>

                               <!-- 2-Star Energy Halo Effect (Before inner to be behind) -->
                               <div v-if="slot.unit.starLevel === 2" class="star-2-halo">
                                   <div class="halo-ring"></div>
                               </div>

                               <!-- 3-Star Conqueror Flow Effect (Before inner to be behind) -->
                               <div v-if="slot.unit.starLevel === 3" class="star-3-flow"></div>

                               <div class="bench-unit-inner" :style="{ borderColor: getRarityColor(slot.unit.cost) }">
                                  <img :src="getUnitIconPath(slot.unit.definitionId, props.state?.gameMode)" 
                                       class="bench-unit-img" />
                               </div>
                               
                               <!-- Star-up celebration effect -->
                               <div v-if="isStarringUp(slot.unit.id)" class="star-up-burst">
                                   <span v-for="j in 8" :key="j" class="star-particle" :style="{ '--particle-index': j }"></span>
                               </div>
                              
                              <!-- Tooltip for Bench -->
                              <transition name="fade">
                                 <UnitTooltip v-if="hoveredBenchUnitId === slot.unit.id" 
                                              :unit="slot.unit" 
                                              class="bench-tooltip" />
                              </transition>
                           </div>
                        </div>
                    </div>
                </div>

                <!-- Permanent Sell Zone (Below Bench) -->
                <div class="sell-zone bench-sell-zone" 
                     :class="{ 'active': draggedUnit }"
                     @dragover.prevent 
                     @drop="onSellDrop">
                    <div class="sell-content">
                        <span class="sell-icon">💰</span>
                        <span class="sell-text">{{ draggedUnit ? 'SELL UNIT FOR' : 'DRAG HERE TO SELL' }}</span>
                        <div v-if="draggedUnit" class="sell-refund">+{{ calculateSellRefund(draggedUnit) }} gold</div>
                    </div>
                </div>
            </div>

            <!-- Shop -->
            <div class="shop-area">
                <!-- Shop Cards (Top) -->
                <div class="shop-cards">
                    <div v-for="(card, idx) in shopCards" :key="idx" class="shop-card" 
                         :class="{ 'empty': !card, 'can-buy': card && myPlayer.gold >= card.cost, [`rarity-${card?.cost || 1}`]: card }"
                         @click="card && buyUnit(Number(idx))"
                         @mouseenter="card && !isDraggingUnit ? hoveredShopIndex = Number(idx) : null"
                         @mouseleave="hoveredShopIndex = null">
                         <template v-if="card">
                             <div class="shop-card-portrait">
                                 <img :src="getUnitIconPath(card.id, props.state?.gameMode)" class="shop-card-img" />
                             </div>
                             <div class="shop-card-content">
                                 <div class="name">{{ card.name }}</div>
                                 <div class="cost">{{ card.cost }}g</div>
                             </div>
                             <transition name="fade">
                               <UnitTooltip v-if="hoveredShopIndex === idx" 
                                           :unit="card" 
                                           placement="top"
                                           :shift="idx === 4 ? 'more-left' : idx === 3 ? 'left' : undefined"
                                           class="shop-tooltip" />
                             </transition>
                         </template>
                    </div>
                </div>
                
                <!-- Refresh Button (Middle) -->
                <div class="shop-actions">
                    <button class="reroll-btn horizontal" @click="refreshShop" :disabled="myPlayer.gold < 2">
                        <span class="refresh-icon">⚓</span>
                        <span class="btn-text">Refresh Shop</span>
                        <span class="cost">2g</span>
                    </button>
                </div>

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
    align-items: center;
}

.game-meta {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
}

.game-mode {
    font-size: 10px;
    opacity: 0.6;
    text-transform: uppercase;
}

.room-id {
    font-size: 12px;
    color: #94a3b8;
    background: rgba(255, 255, 255, 0.05);
    padding: 2px 8px;
    border-radius: 4px;
    letter-spacing: 0.5px;
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
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: bold;
    color: black;
    font-size: 11px;
    background-color: #1e293b;
    border: 2px solid #334155; 
    box-shadow: 0 4px 6px rgba(0,0,0,0.5);
    overflow: hidden;
    z-index: 5; /* Ensure icon is above halo/flow */
}
.bench-unit-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
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
    height: 160px; /* Slightly more buffer than 150px, still slim */
    background: #0f172a;
    border-top: 2px solid #334155;
    display: grid;
    grid-template-columns: 160px 1.5fr 0.8fr;
    padding: 8px 12px;
    gap: 12px;
    position: relative;
    z-index: 60;
    overflow: visible;
}

/* Stats Panel */
.stats-panel {
    width: 160px; /* Reduced from 180px */
    min-width: 160px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    background: rgba(30, 41, 59, 0.5);
    padding: 10px; /* Tightened */
    border-radius: 12px;
    border: 1px solid #334155;
}

.level-info {
    display: flex;
    align-items: center;
    gap: 8px;
}

.level-badge {
    background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
    padding: 4px 12px;
    border-radius: 14px;
    font-weight: 900;
    font-size: 14px;
    border: 1px solid rgba(255,255,255,0.2);
    box-shadow: 0 2px 4px rgba(0,0,0,0.3);
    white-space: nowrap; /* Fix: prevent wrapping */
    flex-shrink: 0;
    cursor: default;
    user-select: none;
    position: relative;
}

.odds-tooltip {
    position: absolute;
    bottom: 130%;
    left: 0;
    background: rgba(15, 23, 42, 0.95);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 10px;
    border-radius: 8px;
    width: 140px;
    z-index: 1000;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5);
    pointer-events: none;
}

.odds-header {
    font-size: 11px;
    font-weight: 800;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin-bottom: 6px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding-bottom: 4px;
}

.odds-grid {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.odds-row {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
}

.tier-indicator {
    width: 8px;
    height: 8px;
    border-radius: 2px;
}

.tier-label {
    flex: 1;
    color: #cbd5e1;
    font-weight: 500;
}

.prob-value {
    color: white;
    font-weight: 800;
}

.xp-bar {
    height: 24px;
    width: 100%;
    background: rgba(15, 23, 42, 0.6);
    border: 1px solid #334155;
    border-radius: 12px;
    position: relative;
    overflow: hidden;
    box-shadow: inset 0 2px 4px rgba(0,0,0,0.5);
    display: block; /* Ensure visibility */
}

.xp-fill {
    height: 100%;
    background: linear-gradient(90deg, #3b82f6, #60a5fa);
    border-radius: 10px;
    transition: width 0.3s ease;
}
.xp-text {
    position: absolute;
    inset: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 10px;
    font-weight: 800;
    color: white;
    text-shadow: 0 1px 2px rgba(0,0,0,0.8);
    pointer-events: none;
}

.stats-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 4px;
    width: 100%;
}

.gold-info {
    display: flex;
    align-items: baseline;
    gap: 8px;
}
.gold-amount {
    color: #fbbf24;
    font-size: 28px; /* Reduced from 32px */
    font-weight: 900;
    line-height: 1;
    text-shadow: 0 2px 4px rgba(0,0,0,0.5);
    cursor: default;
    user-select: none;
}
.gold-label {
    font-size: 11px;
    opacity: 0.8;
    cursor: default;
    user-select: none;
}



.unit-count {
    background: rgba(15, 23, 42, 0.6);
    padding: 4px 10px;
    border-radius: 6px;
    border: 1px solid #334155;
    text-align: center;
    font-size: 14px;
    font-weight: 900;
    color: #94a3b8;
    cursor: default;
    user-select: none;
}
.unit-count.max-units {
    color: #ef4444;
    border-color: #ef4444;
    background: rgba(239, 68, 68, 0.1);
}

/* Bench */
.bench-area {
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(15, 23, 42, 0.4);
    border: 1px solid #334155;
    border-radius: 12px;
    padding: 12px;
    box-shadow: inset 0 2px 10px rgba(0,0,0,0.5);
    flex: 1; /* Stretch to fill available vertical space */
}

.bench-slots {
    display: flex;
    gap: 6px;
}

.bench-slot {
    width: 60px; /* Increased from 52px */
    height: 60px; /* Increased from 52px */
    background: #1e293b;
    border: 1px solid #334155;
    border-radius: 8px;
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

.bench-slot.active-drop {
    background: rgba(59, 130, 246, 0.4);
    box-shadow: inset 0 0 10px #3b82f6;
    border-color: #3b82f6;
}

/* Bench Area Wrapper */
.bench-area-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: stretch; /* Enforce uniform width */
    gap: 6px; /* Reduced from 8px to accommodate larger sell zone */
    min-width: 0;
    height: 100%;
}

/* Sell Zone */
.sell-zone {
    height: 52px; /* Increased from 40px */
    width: 100%;
    background: rgba(15, 23, 42, 0.6);
    border: 1px solid #334155;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    opacity: 0.8;
    padding: 12px; /* Sync with bench area padding */
}

.sell-zone.active {
    opacity: 1;
    border-color: #fbbf24;
    background: linear-gradient(90deg, rgba(234, 179, 8, 0.1) 0%, rgba(234, 179, 8, 0.3) 50%, rgba(234, 179, 8, 0.1) 100%);
    box-shadow: 0 0 20px rgba(251, 191, 36, 0.3);
    border-style: solid;
    transform: scale(1.01);
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
    font-weight: 800;
    font-size: 14px;
    letter-spacing: 2px;
    color: #94a3b8;
    text-transform: uppercase;
    cursor: default;
    user-select: none;
}

.sell-zone.active .sell-text {
    color: #f87171;
    text-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
}

.sell-refund {
    font-size: 14px;
    color: #fbbf24;
    font-weight: 800;
    margin-left: 8px;
}

.slide-up-enter-active, .slide-up-leave-active {
    transition: all 0.3s ease;
}
.slide-up-enter-from, .slide-up-leave-to {
    opacity: 0;
    transform: translateY(10px);
}

/* Shop */
.shop-area {
    flex: 1;
    min-width: 380px;
    display: flex;
    flex-direction: column;
    gap: 8px; /* Reduced gap */
    height: 100%;
}

.shop-cards {
    display: flex;
    gap: 6px;
    flex: 1;
}

.shop-card {
    position: relative;
    flex: 1 1 0;
    min-width: 70px; /* Reduced from 80px */
    height: 100%;
    background: #1e293b;
    border: 2px solid #334155;
    border-radius: 8px; /* Slightly tighter radius */
    cursor: pointer;
    display: flex;
    flex-direction: column;
    padding: 4px; /* Reduced padding */
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    /* overflow: hidden; -- Allows tooltips to be visible */
}

.shop-card.rarity-1 { border-color: #64748b; background: linear-gradient(135deg, #1e293b 0%, #334155 100%); }
.shop-card.rarity-2 { border-color: #22c55e; background: linear-gradient(135deg, #064e3b 0%, #1e293b 100%); }
.shop-card.rarity-3 { border-color: #1a0dab; background: linear-gradient(135deg, #1e3a8a 0%, #1e293b 100%); }
.shop-card.rarity-4 { border-color: #a855f7; background: linear-gradient(135deg, #581c87 0%, #1e293b 100%); }
.shop-card.rarity-5 { border-color: #eab308; background: linear-gradient(135deg, #78350f 0%, #1e293b 100%); }

.shop-card.can-buy:hover {
    transform: translateY(-4px); /* Enhanced jump */
    z-index: 100; /* Ensure tooltip is above everything */
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.6), 0 0 15px rgba(96, 165, 250, 0.3);
    border-color: #60a5fa;
}

.shop-card.rarity-1.can-buy:hover { border-color: #94a3b8; box-shadow: 0 0 10px rgba(148, 163, 184, 0.3); }
.shop-card.rarity-2.can-buy:hover { border-color: #4ade80; box-shadow: 0 0 10px rgba(74, 222, 128, 0.3); }
.shop-card.rarity-3.can-buy:hover { border-color: #60a5fa; box-shadow: 0 0 10px rgba(96, 165, 250, 0.3); }
.shop-card.rarity-4.can-buy:hover { border-color: #c084fc; box-shadow: 0 0 10px rgba(192, 132, 252, 0.3); }
.shop-card.rarity-5.can-buy:hover { border-color: #fbbf24; box-shadow: 0 0 10px rgba(251, 191, 36, 0.3); }


.shop-card.empty {
    opacity: 0.2;
    cursor: default;
    background: transparent;
    border: 1px solid #1e293b;
}

.shop-card-portrait {
    width: 100%;
    height: 52px; /* Reduced from 60px */
    margin-bottom: 3px; /* Reduced margin */
    flex-shrink: 0;
    overflow: hidden;
    border-radius: 6px;
    background: #000;
}

.shop-card-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.shop-card-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    min-width: 0;
    width: 100%;
}

.shop-card .name {
    width: 100%;
    text-align: center;
    font-weight: 800;
    font-size: 13px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: #ffffff;
    text-shadow: 0 1px 2px rgba(0,0,0,0.8);
    margin-bottom: 2px;
}

.shop-card .cost {
    font-size: 11px;
    color: #fbbf24;
    font-weight: 600;
}

.shop-tooltip, .bench-tooltip {
    position: absolute;
    z-index: 10000;
    pointer-events: none;
}

.shop-actions {
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding-top: 4px; /* Ensure space for button shadow/hover */
}

.reroll-btn.horizontal {
    flex-direction: row;
    height: 34px; /* Reduced from 42px */
    width: 100%;
    gap: 8px;
    padding: 0 15px;
    background: linear-gradient(180deg, #1e3a8a 0%, #1e40af 100%);
    border: 2px solid #3b82f6;
    border-radius: 4px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.1);
    font-family: 'Inter', sans-serif;
    transition: all 0.2s ease;
}

.reroll-btn.horizontal:hover:not(:disabled) {
    background: linear-gradient(180deg, #2563eb 0%, #1d4ed8 100%);
    border-color: #60a5fa;
    transform: translateY(-1px);
}

.reroll-btn.horizontal .refresh-icon { 
    font-size: 18px; 
    filter: drop-shadow(0 0 5px rgba(255, 255, 255, 0.5));
}
.reroll-btn.horizontal .btn-text { 
    font-size: 13px; 
    font-weight: 800; 
    text-transform: uppercase;
    letter-spacing: 1px;
}
.reroll-btn.horizontal .cost { 
    font-size: 14px; 
    color: #fbbf24; 
    font-weight: 900;
    text-shadow: 0 0 10px rgba(251, 191, 36, 0.4);
}

.xp-btn {
    height: 34px; /* Reduced from 42px */
    width: 100%;
    font-size: 13px;
    background: linear-gradient(180deg, #3b82f6 0%, #2563eb 100%);
    border: 2px solid #60a5fa;
    border-radius: 6px;
    color: white;
    font-weight: 800;
    cursor: pointer;
    white-space: nowrap;
    text-transform: uppercase;
    letter-spacing: 1px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3);
    transition: all 0.2s ease;
}

.xp-btn:hover:not(:disabled) {
    background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
    transform: translateY(-1px);
}

.reroll-btn:disabled, .xp-btn:disabled {
    opacity: 0.5;
    background: #1e293b;
    border-color: #334155;
    box-shadow: none;
    transform: none;
    cursor: not-allowed;
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
/* Cost Top Glow */
.cost-top-glow {
    position: absolute;
    top: 5px;
    left: 50%;
    transform: translateX(-50%);
    width: 20px;
    height: 8px;
    background: radial-gradient(ellipse at center, var(--rarity-color), transparent 70%);
    opacity: 0.6;
    pointer-events: none;
    z-index: 5;
    filter: blur(2px);
}

/* 2-Star Energy Halo Effect */
.star-2-halo {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 66px; /* Increased to avoid any overlap */
    height: 66px;
    pointer-events: none;
    z-index: 1; 
}

.halo-ring {
    position: absolute;
    inset: 0;
    border: 2px dashed var(--rarity-color);
    border-radius: 50%;
    opacity: 0.5;
    animation: rotate-halo 15s linear infinite;
    box-shadow: 0 0 8px var(--rarity-color);
}

@keyframes rotate-halo { 
    from { transform: rotate(0deg); } 
    to { transform: rotate(360deg); } 
}

/* 3-Star Conqueror Flow Effect */
.star-3-flow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 66px;
    height: 66px;
    border-radius: 50%;
    padding: 5px; /* Gradient starts outside 56px */
    background-clip: content-box;
    pointer-events: none;
    z-index: 1;
}

.star-3-flow::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    background: conic-gradient(from 0deg, var(--rarity-color), #fff, var(--rarity-color), #000, var(--rarity-color));
    animation: rotate-halo 1.5s linear infinite;
    z-index: -1;
}

.star-3-flow::after {
    content: '';
    position: absolute;
    inset: -6px;
    border-radius: 50%;
    box-shadow: 0 0 20px var(--rarity-color);
    opacity: 0.6;
    z-index: -2;
}

</style>
