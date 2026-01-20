<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import UnitTooltip from './UnitTooltip.vue'
import AttackAnimation from './game/AttackAnimation.vue'
import { getAttackConfig, getAbilityConfig } from '../data/animationConfig'

const props = defineProps<{
    state: any,
    myPlayerId?: string
}>()

const emit = defineEmits(['move'])

// Grid Constants
const GRID_ROWS = 8
const GRID_COLS = 7
const PLAYER_ROWS = 4 // Height of one player's board (half of arena)

const renderedUnits = computed(() => {
    if (!props.state || !props.state.players) return []
    let allUnits: any[] = []
    
    const isCombat = props.state.phase === 'COMBAT'
    const myId = props.myPlayerId
    
    // Check explicit combatSide from backend
    let shouldFlip = false
    if (isCombat && myId) {
        const myPlayer = props.state.players[myId]
        if (myPlayer && myPlayer.combatSide === 'TOP') {
            shouldFlip = true
        }
    }
    
    Object.values(props.state.players).forEach((player: any) => {
        if (player.playerId !== myId) {
             if (isCombat) {
                 const oppId = (props.state.matchups && myId) ? props.state.matchups[myId] : null
                 if (player.playerId !== oppId) {
                     return; 
                 }
             } else {
                 return; 
             }
        }

        const board = player.boardUnits || player.board
        if (board) {
            allUnits = allUnits.concat(board
                .filter((u:any) => u.x >= 0 && u.y >= 0 && u.currentHealth > 0)
                .map((u: any) => {
                    let visualX = u.x;
                    let visualY = u.y;
                    
                    if (isCombat) {
                        // Combat Logic using Constants
                        if (shouldFlip) {
                            visualX = u.x; // No X-flip, strict reflection
                            visualY = (GRID_ROWS - 1) - u.y;
                        }
                    } else {
                        // Planning Phase: I see only my units (0-3).
                        // I want them at Bottom (4-7).
                        if (player.playerId === myId) {
                             visualY = u.y + PLAYER_ROWS;
                        }
                    }
                    
                    return {
                        ...u,
                        visualX,
                        visualY,
                        ownerId: player.playerId,
                        isMine: player.playerId === myId,
                        image: `/assets/units/${u.definitionId}.png`
                    }
                }))
        }
    })
    return allUnits
})

const CELL_SIZE = 60

// Drag state
const isDragging = ref(false)
const dragOverCellIndex = ref(-1)
const hoveredUnitId = ref<string|null>(null)

const getUnitStyle = (unit: any) => {
    return {
        left: (unit.visualX * CELL_SIZE + 5) + 'px',
        top: (unit.visualY * CELL_SIZE + 5) + 'px',
        width: '50px',
        height: '50px',
        borderColor: unit.isMine ? '#10b981' : '#ef4444', 
        borderWidth: '2px',
        borderStyle: 'solid',
        boxShadow: unit.isMine ? '0 0 10px rgba(16, 185, 129, 0.6)' : 'none',
        zIndex: hoveredUnitId.value === unit.id ? 100 : 10
    }
}

const myPlayerName = computed(() => {
     if (!props.state || !props.state.players || !props.myPlayerId) return 'Me'
     const p = props.state.players[props.myPlayerId]
     return p ? p.name : 'Me'
})

const opponentName = computed(() => {
     if (!props.state || !props.state.matchups || !props.myPlayerId) return null
     const oppId = props.state.matchups[props.myPlayerId]
     if (!oppId) return null
     const p = props.state.players[oppId]
     return p ? p.name : 'Opponent'
})

const hoveredUnit = computed(() => {
    if (!hoveredUnitId.value) return null
    return renderedUnits.value.find((u: any) => u.id === hoveredUnitId.value)
})

const getTooltipAnchorStyle = (unit: any) => {
    return {
        left: (unit.visualX * CELL_SIZE + 5) + 'px',
        top: (unit.visualY * CELL_SIZE + 5) + 'px',
        width: '50px',
        height: '50px',
        position: 'absolute' as const,
        zIndex: 1000,
        pointerEvents: 'none' as const
    }
}

const getColor = (id: string) => {
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
        hash = id.charCodeAt(i) + ((hash << 5) - hash);
    }
    const c = (hash & 0x00FFFFFF).toString(16).toUpperCase();
    return '#' + "00000".substring(0, 6 - c.length) + c;
}

const onDragStart = (evt: DragEvent, unit: any) => {
    if (unit.ownerId !== props.myPlayerId) {
        evt.preventDefault()
        return
    }
    isDragging.value = true
    if (evt.dataTransfer) {
        evt.dataTransfer.setData('unitId', unit.id)
        evt.dataTransfer.effectAllowed = 'move'
        
        // Use the actual DOM element for the drag image if possible
        const target = evt.target as HTMLElement;
        const img = target.querySelector('img');
        if (img) {
             evt.dataTransfer.setDragImage(img, 25, 25);
        } else {
            // Fallback
             const fallbackImg = new Image();
             fallbackImg.src = unit.image;
             evt.dataTransfer.setDragImage(fallbackImg, 25, 25);
        }
    }
}

const onDragEnd = () => {
    isDragging.value = false
    dragOverCellIndex.value = -1
}

const onDrop = (evt: DragEvent, x: number, y: number) => {
    evt.preventDefault()
    isDragging.value = false
    dragOverCellIndex.value = -1
    
    if (evt.dataTransfer) {
        const unitId = evt.dataTransfer.getData('unitId')
        if (unitId) {
            // Translate Visual Drop Y to Backend Y (Planning Phase)
            // Visual (PLAYER_ROWS -> GRID_ROWS-1) -> Backend (0 -> PLAYER_ROWS-1)
            const backendY = y - PLAYER_ROWS
            if (backendY >= 0) {
                 emit('move', { unitId, x, y: backendY })
            }
        }
    }
}

const onDragOver = (evt: DragEvent, cellIndex: number) => {
    evt.preventDefault() 
    dragOverCellIndex.value = cellIndex
    if (evt.dataTransfer) {
        evt.dataTransfer.dropEffect = 'move'
    }
}

const onDragLeave = (evt: DragEvent) => {
    // Optional: could clear dragOverCellIndex if leaving the *grid* entirely,
    // but individual cell leave logic is tricky with child elements.
    // relying on dragOver updating consistently.
}

// Hover handlers for Tooltip
const onUnitMouseEnter = (unitId: string) => {
    hoveredUnitId.value = unitId
}
const onUnitMouseLeave = () => {
    hoveredUnitId.value = null
}

// ========== ANIMATION SYSTEM ==========

// Animation Types
interface AttackAnimData {
    id: number
    type: 'attack' | 'ability'
    attackType?: 'punch' | 'slash' | 'projectile'
    pattern?: string
    startX: number
    startY: number
    endX: number
    endY: number
    color: string
    definitionId: string
}

const activeAnimations = ref<AttackAnimData[]>([])
let nextAnimId = 0

// Track previous health to detect attacks
const prevHealthMap = ref<Record<string, number>>({})
const lastCastMap = ref<Record<string, string>>({})

// Floating Text for ability names (keep existing)
interface FloatingText {
    id: number
    x: number
    y: number
    text: string
}
const castingAnimations = ref<FloatingText[]>([])

// Find nearest enemy for a unit (to animate attacks toward)
function findNearestEnemy(unit: any, allUnits: any[]): any | null {
    const enemies = allUnits.filter(u => u.ownerId !== unit.ownerId && u.currentHealth > 0)
    if (enemies.length === 0) return null
    
    let nearest = enemies[0]
    let minDist = Math.abs(nearest.visualX - unit.visualX) + Math.abs(nearest.visualY - unit.visualY)
    
    for (const enemy of enemies) {
        const dist = Math.abs(enemy.visualX - unit.visualX) + Math.abs(enemy.visualY - unit.visualY)
        if (dist < minDist) {
            minDist = dist
            nearest = enemy
        }
    }
    return nearest
}

// Watch for health changes to spawn attack animations
watch(() => renderedUnits.value, (newUnits, oldUnits) => {
    const isCombat = props.state?.phase === 'COMBAT'
    if (!isCombat) {
        // Clear tracking when not in combat
        prevHealthMap.value = {}
        return
    }
    
    // Build map of current units
    const unitMap = new Map<string, any>()
    newUnits.forEach(u => unitMap.set(u.id, u))
    
    // Check for health decreases (indicates unit was attacked)
    newUnits.forEach((unit: any) => {
        const prevHealth = prevHealthMap.value[unit.id]
        
        // If health decreased, spawn impact animation on this unit
        if (prevHealth !== undefined && unit.currentHealth < prevHealth) {
            // Find potential attacker - nearest enemy
            const attacker = findNearestEnemy(unit, newUnits)
            if (attacker && activeAnimations.value.length < 6) {
                const config = getAttackConfig(attacker.definitionId)
                activeAnimations.value.push({
                    id: nextAnimId++,
                    type: 'attack',
                    attackType: config.type,
                    startX: attacker.visualX,
                    startY: attacker.visualY,
                    endX: unit.visualX,
                    endY: unit.visualY,
                    color: config.color,
                    definitionId: attacker.definitionId
                })
            }
        }
        
        // Update health tracking
        prevHealthMap.value[unit.id] = unit.currentHealth
        
        // Handle ability animations (enhanced version)
        if (unit.activeAbility) {
            if (lastCastMap.value[unit.id] !== unit.activeAbility) {
                lastCastMap.value[unit.id] = unit.activeAbility
                
                // Spawn ability visual effect
                const target = findNearestEnemy(unit, newUnits)
                const config = getAbilityConfig(unit.definitionId)
                
                if (activeAnimations.value.length < 8) {
                    activeAnimations.value.push({
                        id: nextAnimId++,
                        type: 'ability',
                        pattern: unit.ability?.pattern || 'SINGLE',
                        startX: unit.visualX,
                        startY: unit.visualY,
                        endX: target?.visualX ?? unit.visualX,
                        endY: target?.visualY ?? unit.visualY,
                        color: config.color,
                        definitionId: unit.definitionId
                    })
                }
                
                // Also spawn floating text
                const x = unit.visualX * CELL_SIZE + 30
                const y = unit.visualY * CELL_SIZE
                castingAnimations.value.push({
                    id: nextAnimId++,
                    x,
                    y,
                    text: unit.activeAbility
                })
                
                setTimeout(() => {
                    castingAnimations.value.shift()
                }, 1000)
            }
        } else {
            if (lastCastMap.value[unit.id]) {
                delete lastCastMap.value[unit.id]
            }
        }
    })
})

// Remove animation when complete
function removeAnimation(id: number) {
    activeAnimations.value = activeAnimations.value.filter(a => a.id !== id)
}
</script>

<template>
  <div class="board-container">
    <div class="grid" @mouseleave="dragOverCellIndex = -1">
        <!-- Render Grid Cells as Drop Zones -->
        <div v-for="i in (GRID_ROWS * GRID_COLS)" :key="'cell-'+i" 
             class="cell" 
             :class="{ 
                'player-half': Math.floor((i-1)/GRID_COLS) >= PLAYER_ROWS, 
                'enemy-half': Math.floor((i-1)/GRID_COLS) < PLAYER_ROWS,
                'highlight-drop': isDragging && Math.floor((i-1)/GRID_COLS) >= PLAYER_ROWS,
                'active-drop': dragOverCellIndex === (i-1) && Math.floor((i-1)/GRID_COLS) >= PLAYER_ROWS
             }"
             @dragover="(e) => onDragOver(e, i-1)"
             @drop="(e) => onDrop(e, (i-1)%GRID_COLS, Math.floor((i-1)/GRID_COLS))">
        </div>
        
        <!-- Render Units -->
        <div v-for="unit in renderedUnits" :key="unit.id" 
             class="unit" 
             :style="getUnitStyle(unit)"
             :class="{ 'mine': unit.ownerId === myPlayerId }"
             :draggable="unit.ownerId === myPlayerId"
             @dragstart="(e) => onDragStart(e, unit)"
             @dragend="onDragEnd"
             @mouseenter="onUnitMouseEnter(unit.id)"
             @mouseleave="onUnitMouseLeave">
             
            <div class="hp-bar" :style="{ width: (unit.currentHealth / unit.maxHealth * 100) + '%' }"></div>
            <div v-if="unit.maxMana > 0" class="mana-bar" :style="{ width: (unit.mana / unit.maxMana * 100) + '%' }"></div>
            <img :src="unit.image" class="unit-img" :alt="unit.name" />
        </div>

        <!-- Attack & Ability Animations -->
        <AttackAnimation 
            v-for="anim in activeAnimations" 
            :key="anim.id"
            :type="anim.type"
            :attack-type="anim.attackType"
            :pattern="anim.pattern"
            :start-x="anim.startX"
            :start-y="anim.startY"
            :end-x="anim.endX"
            :end-y="anim.endY"
            :color="anim.color"
            :definition-id="anim.definitionId"
            @complete="removeAnimation(anim.id)"
        />

        <!-- Shared Tooltip Anchor -->
         <div v-if="hoveredUnit" 
             class="tooltip-anchor"
             :style="getTooltipAnchorStyle(hoveredUnit)">
             <transition name="fade">
                 <UnitTooltip :unit="hoveredUnit" />
             </transition>
        </div>
        <!-- Player Names Overlay -->
        <div class="overlays">
             <div class="name-tag enemy" v-if="opponentName">{{ opponentName }}</div>
             <div class="name-tag me" v-if="myPlayerName">{{ myPlayerName }}</div>
             
             <!-- Ability Floating Text -->
             <div v-for="anim in castingAnimations" :key="anim.id"
                  class="floating-text"
                  :style="{ left: anim.x + 'px', top: anim.y + 'px' }">
                  {{ anim.text }}
             </div>
        </div>
    </div>
  </div>
</template>

<style scoped>
/* (Keep existing styles) */
.board-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

.grid {
    position: relative;
    width: 420px; /* GRID_COLS * CELL_SIZE */
    height: 480px; /* GRID_ROWS * CELL_SIZE */
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    grid-template-rows: repeat(8, 1fr);
    background-color: #1e293b;
    border: 2px solid #555;
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
    z-index: 50;
}

.cell {
    border: 1px solid #334155;
    transition: all 0.2s;
}
.cell.player-half {
    background-color: rgba(59, 130, 246, 0.05);
}
.cell.enemy-half {
    background-color: rgba(239, 68, 68, 0.05);
}

/* Hover effect for cells when NOT dragging */
.cell:not(.highlight-drop):hover {
    background-color: rgba(255,255,255,0.1);
}

/* Highlight available drop zones */
.cell.highlight-drop {
    border-color: #60a5fa;
    background-color: rgba(59, 130, 246, 0.15);
}

/* Active drop target (cursor is over this cell) */
.cell.active-drop {
    background-color: rgba(59, 130, 246, 0.4);
    box-shadow: inset 0 0 10px #3b82f6;
}

.unit {
    position: absolute;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 2px solid white;
    transition: left 0.2s, top 0.2s; 
    box-shadow: 0 4px 6px rgba(0,0,0,0.5);
    z-index: 10;
    pointer-events: auto; 
    background-color: #1e293b; 
}
.unit.mine {
    cursor: grab;
}
.unit.mine:active {
    cursor: grabbing;
}

.unit-img {
    width: 100%;
    height: 100%;
    object-fit: cover; 
    border-radius: 50%;
    pointer-events: none;
}

.hp-bar {
    position: absolute;
    top: -8px; 
    left: 0;
    width: 100%; 
    height: 4px;
    background-color: #ef4444;
    border-radius: 2px;
    box-shadow: 0 0 2px black;
}

.mana-bar {
    position: absolute;
    top: -4px;
    left: 0;
    width: 100%;
    height: 4px;
    background-color: #3b82f6;
    border-radius: 2px;
    box-shadow: 0 0 2px black;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.name-tag {
    position: absolute;
    left: -80px;
    padding: 5px 10px;
    background: rgba(0,0,0,0.7);
    color: white;
    border-radius: 4px;
    font-weight: bold;
}
.name-tag.enemy {
    top: 20px;
    border-left: 4px solid #ef4444;
}
.name-tag.me {
    bottom: 20px;
    border-left: 4px solid #10b981;
}

.floating-text {
    position: absolute;
    color: #fbbf24; /* Amber */
    font-weight: bold;
    font-size: 14px;
    text-shadow: 0 2px 2px black;
    pointer-events: none;
    animation: floatUp 1s ease-out forwards;
    z-index: 20;
    width: 100px; /* arbitrary, prevents wrapping too early */
    text-align: center;
    transform: translate(-25px, -20px); /* Centerish above unit */
}

@keyframes floatUp {
    0% { transform: translate(-25px, -20px); opacity: 0; scale: 0.5; }
    20% { transform: translate(-25px, -40px); opacity: 1; scale: 1.2; }
    100% { transform: translate(-25px, -60px); opacity: 0; scale: 1.0; }
}
</style>
