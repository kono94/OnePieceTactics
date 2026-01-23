<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from 'vue'
import UnitTooltip from './UnitTooltip.vue'
import AttackAnimation from './game/AttackAnimation.vue'
import { getAttackConfig, getAbilityConfig } from '../data/animationConfig'
import type { GameState, GameUnit, GamePhase } from '../types'

const props = defineProps<{
    state: GameState | null,
    myPlayerId?: string,
    isDraggingProp?: boolean
}>()

const emit = defineEmits(['move', 'drag-start', 'drag-end', 'collect-orb'])

// Grid Constants
const GRID_ROWS = 8
const GRID_COLS = 7
const PLAYER_ROWS = 4 // Height of one player's board (half of arena)

const renderedUnits = computed(() => {
    const state = props.state
    if (!state || !state.players) return []
    let allUnits: any[] = []
    
    const isCombat = state.phase === 'COMBAT'
    const myId = props.myPlayerId
    
    // Check explicit combatSide from backend
    let shouldFlip = false
    if (isCombat && myId) {
        const myPlayer = state.players[myId]
        if (myPlayer && myPlayer.combatSide === 'TOP') {
            shouldFlip = true
        }
    }
    
    Object.values(state.players).forEach((player: any) => {
        if (player.playerId !== myId) {
             if (isCombat) {
                 const oppId = (state.matchups && myId) ? state.matchups[myId] : null
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

const renderedOrbs = computed(() => {
    const state = props.state
    if (!state || !state.players || !props.myPlayerId) return []
    const myPlayer = state.players[props.myPlayerId]
    if (!myPlayer || !myPlayer.lootOrbs) return []

    return myPlayer.lootOrbs.map(orb => {
        // Place orbs in the top half (visual rows 0-3)
        return {
            ...orb,
            visualX: orb.x,
            visualY: orb.y
        }
    })
})

const CELL_SIZE = 55

// Drag state
const isDragging = ref(false)
const draggingUnitId = ref<string|null>(null)
const dragOverCellIndex = ref(-1)
const hoveredUnitId = ref<string|null>(null)

const getUnitStyle = (unit: any) => {
    // Disable pointer events on units when dragging, EXCEPT the unit being dragged.
    // This allows drops to fall through to the grid cell for swapping.
    const shouldDisablePointer = (isDragging.value || props.isDraggingProp) 
                                 && unit.id !== draggingUnitId.value;

    const styles: any = {
        left: (unit.visualX * CELL_SIZE + 5) + 'px',
        top: (unit.visualY * CELL_SIZE + 5) + 'px',
        width: '45px',
        height: '45px',
        borderColor: unit.isMine ? '#10b981' : '#ef4444', 
        borderWidth: '2px',
        borderStyle: 'solid',
        boxShadow: unit.isMine ? '0 0 10px rgba(16, 185, 129, 0.6)' : 'none',
        zIndex: hoveredUnitId.value === unit.id ? 100 : 10,
        pointerEvents: shouldDisablePointer ? 'none' : 'auto',
        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)'
    }

    // Apply status effect visuals
    if (unit.stunTicksRemaining > 0) {
        styles.filter = 'grayscale(1) brightness(0.8)';
    } else {
        const extraGlows: string[] = [];
        if (unit.atkBuff > 1.01) {
            extraGlows.push('0 0 15px rgba(249, 115, 22, 0.8)');
        }
        if (unit.spdBuff > 1.01) {
            extraGlows.push('0 0 15px rgba(59, 130, 246, 0.8)');
        }
        
        if (extraGlows.length > 0) {
            const teamGlow = unit.isMine ? '0 0 10px rgba(16, 185, 129, 0.6)' : '';
            styles.boxShadow = [...extraGlows, teamGlow].filter(g => g).join(', ');
        }
    }

    return styles;
}

const myPlayerName = computed(() => {
     const state = props.state
     if (!state || !state.players || !props.myPlayerId) return 'Me'
     const p = state.players[props.myPlayerId]
     return p ? p.name : 'Me'
})

const opponentName = computed(() => {
     const state = props.state
     if (!state || !state.matchups || !props.myPlayerId) return null
     const oppId = state.matchups[props.myPlayerId]
     if (!oppId) return null
     const p = state.players[oppId]
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
        width: '45px',
        height: '45px',
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
    draggingUnitId.value = unit.id
    emit('drag-start', unit)
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
    draggingUnitId.value = null
    dragOverCellIndex.value = -1
    emit('drag-end')
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

// ========== DEATH ANIMATION SYSTEM ==========
// Track units that are dying (animating death)
const dyingUnits = ref<Set<string>>(new Set())
// Store the last known position/data for dead units during animation
const dyingUnitData = ref<Map<string, any>>(new Map())
const DEATH_ANIMATION_DURATION = 600 // ms

// Cleanup timers on unmount
const deathTimers = ref<number[]>([])
onUnmounted(() => {
    deathTimers.value.forEach(timer => clearTimeout(timer))
})

// ========== STAR-UP CELEBRATION SYSTEM ==========
// Track units that just leveled up for celebration animation
const starUpUnits = ref<Set<string>>(new Set())
const prevStarLevelMap = ref<Record<string, number>>({})
const STAR_UP_ANIMATION_DURATION = 1200 // ms

// Floating Text for ability names (keep existing)
interface FloatingText {
    id: number
    x: number
    y: number
    text: string
}
const castingAnimations = ref<FloatingText[]>([])
const floatingHeals = ref<FloatingText[]>([])

// Find nearest enemy for a unit (to animate attacks toward)
function findNearestEnemy(unit: any, allUnits: any[]): any | null {
    const enemies = allUnits.filter(u => u.ownerId !== unit.ownerId && u.currentHealth > 0)
    if (enemies.length === 0) return null
    
    let nearest = enemies[0]
    let minDist = Math.max(Math.abs(nearest.visualX - unit.visualX), Math.abs(nearest.visualY - unit.visualY))
    
    for (const enemy of enemies) {
        const dist = Math.max(Math.abs(enemy.visualX - unit.visualX), Math.abs(enemy.visualY - unit.visualY))
        if (dist < minDist) {
            minDist = dist
            nearest = enemy
        }
    }
    return nearest
}

// Watch for health changes to spawn attack animations
// Store previous units for death detection
const prevUnitsMap = ref<Map<string, any>>(new Map())
const prevPhase = ref<GamePhase | undefined | null>(null)
const lastProcessedEventTime = ref(0)

const unitsById = computed(() => {
    const map = new Map()
    if (renderedUnits.value) {
        renderedUnits.value.forEach((u: any) => map.set(u.id, u))
    }
    return map
})

watch(() => props.state?.recentEvents, (newEvents) => {
    if (!newEvents || newEvents.length === 0) return
    
    // Deduplication based on timestamp
    let maxTime = lastProcessedEventTime.value
    
    newEvents.forEach((event: any) => {
        if (event.timestamp <= lastProcessedEventTime.value) return
        if (event.timestamp > maxTime) maxTime = event.timestamp
        
        const source = unitsById.value.get(event.sourceId)
        if (!source) return

        if (event.type === 'DAMAGE') {
            const target = unitsById.value.get(event.targetId)
            // Value can be positive (damage) or negative (heal)
            if (event.value > 0 && target && activeAnimations.value.length < 15) {
                const config = getAttackConfig(source.definitionId)
                activeAnimations.value.push({
                    id: nextAnimId++,
                    type: 'attack',
                    attackType: config.type,
                    startX: source.visualX,
                    startY: source.visualY,
                    endX: target.visualX,
                    endY: target.visualY,
                    color: config.color,
                    definitionId: source.definitionId
                })
            } else if (event.value < 0) {
                const target = unitsById.value.get(event.targetId) || source
                const healAmount = Math.abs(event.value)
                const healId = nextAnimId++
                floatingHeals.value.push({
                    id: healId,
                    x: target.visualX * CELL_SIZE + 25,
                    y: target.visualY * CELL_SIZE + 10,
                    text: `+${healAmount}`
                })
                setTimeout(() => {
                    floatingHeals.value = floatingHeals.value.filter(h => h.id !== healId)
                }, 1000)
            }
        } else if (event.type === 'SKILL') {
            const config = getAbilityConfig(source.definitionId)
            let targetX = source.visualX
            let targetY = source.visualY
            
            if (event.targetId) {
                const target = unitsById.value.get(event.targetId)
                if (target) {
                    targetX = target.visualX
                    targetY = target.visualY
                }
            } else {
                const nearest = findNearestEnemy(source, renderedUnits.value)
                if (nearest) {
                    targetX = nearest.visualX
                    targetY = nearest.visualY
                }
            }

            if (activeAnimations.value.length < 15) {
                activeAnimations.value.push({
                    id: nextAnimId++,
                    type: 'ability',
                    pattern: source.ability?.pattern || 'SINGLE',
                    startX: source.visualX,
                    startY: source.visualY,
                    endX: targetX,
                    endY: targetY,
                    color: config.color,
                    definitionId: source.definitionId
                })
            }
            
            // Floating text for skill
            castingAnimations.value.push({
                id: nextAnimId++,
                x: source.visualX * CELL_SIZE + 30,
                y: source.visualY * CELL_SIZE,
                text: source.activeAbility || 'Ability!'
            })
            setTimeout(() => {
                castingAnimations.value.shift()
            }, 1000)
        }
    })
    
    lastProcessedEventTime.value = maxTime
}, { deep: true })

watch(() => renderedUnits.value, (newUnits, oldUnits) => {
    const currentPhase = props.state?.phase
    const isCombat = currentPhase === 'COMBAT'
    const wasInCombat = prevPhase.value === 'COMBAT'
    
    // Update phase tracking
    prevPhase.value = currentPhase
    
    if (!isCombat) {
        // Clear tracking when not in combat, including dying units
        prevHealthMap.value = {}
        prevUnitsMap.value.clear()
        // Clear dying units to stop any looping death animations
        dyingUnits.value.clear()
        dyingUnitData.value.clear()
        return
    }
    
    // Build map of current alive units
    const newUnitIds = new Set(newUnits.map((u: any) => u.id))
    
    // DEATH DETECTION: Only trigger if we were already in combat (not transitioning INTO combat)
    // This prevents false deaths when combat starts or ends
    if (wasInCombat) {
        prevUnitsMap.value.forEach((prevUnit, unitId) => {
            if (!newUnitIds.has(unitId) && !dyingUnits.value.has(unitId)) {
                // Unit disappeared during combat - trigger death animation
                triggerDeathAnimation(prevUnit)
                // IMPORTANT: Remove from prevUnitsMap to prevent re-triggering
                prevUnitsMap.value.delete(unitId)
            }
        })
    }
    
    // Check for health decreases (indicates unit was attacked)
    newUnits.forEach((unit: any) => {
        const prevHealth = prevHealthMap.value[unit.id]
        
        // Update health tracking
        prevHealthMap.value[unit.id] = unit.currentHealth
        
        // Store current unit data for next frame's death detection
        prevUnitsMap.value.set(unit.id, { ...unit })
    })
})

// Remove animation when complete
function removeAnimation(id: number) {
    activeAnimations.value = activeAnimations.value.filter(a => a.id !== id)
}

// Trigger death animation for a unit
function triggerDeathAnimation(unit: any) {
    if (dyingUnits.value.has(unit.id)) return // Already dying
    
    console.log('🔴 Death animation triggered for:', unit.name, unit.id)
    
    dyingUnits.value.add(unit.id)
    dyingUnitData.value.set(unit.id, { ...unit, isDying: true })
    
    // Remove after animation completes
    const timer = window.setTimeout(() => {
        dyingUnits.value.delete(unit.id)
        dyingUnitData.value.delete(unit.id)
        delete prevHealthMap.value[unit.id]
    }, DEATH_ANIMATION_DURATION)
    
    deathTimers.value.push(timer)
}

// Combined units: alive units + dying units (for animation)
const displayedUnits = computed(() => {
    const alive = renderedUnits.value
    const dying = Array.from(dyingUnitData.value.values())
    return [...alive, ...dying]
})

// Trigger star-up celebration for a unit
function triggerStarUpCelebration(unitId: string) {
    if (starUpUnits.value.has(unitId)) return // Already celebrating
    
    console.log('⭐ Star-up celebration triggered for unit:', unitId)
    
    starUpUnits.value.add(unitId)
    
    // Remove after animation completes
    const timer = window.setTimeout(() => {
        starUpUnits.value.delete(unitId)
    }, STAR_UP_ANIMATION_DURATION)
    
    deathTimers.value.push(timer)
}

// Watch for star level changes (happens during planning phase when combining units)
watch(() => props.state, (newState) => {
    if (!newState || !newState.players || !props.myPlayerId) return
    
    const myPlayer = newState.players[props.myPlayerId]
    if (!myPlayer) return
    
    // Check all units (board and bench) for star level changes
    const allMyUnits = [...(myPlayer.board || []), ...(myPlayer.bench || [])]
    
    allMyUnits.forEach((unit: any) => {
        const prevStarLevel = prevStarLevelMap.value[unit.id]
        const currentStarLevel = unit.starLevel || 1
        
        // Debug log to see all star level checks
        if (currentStarLevel >= 2 || prevStarLevel !== undefined) {
            console.log(`⭐ Star check: ${unit.name} (${unit.id.slice(0,8)}) - prev: ${prevStarLevel}, current: ${currentStarLevel}`)
        }
        
        // Trigger celebration if:
        // 1. Existing unit's star level increased, OR
        // 2. New unit appeared with star level >= 2 (just combined)
        if (prevStarLevel !== undefined && currentStarLevel > prevStarLevel) {
            // Existing unit leveled up
            console.log(`✨ TRIGGER (existing unit upgraded): ${unit.name}`)
            triggerStarUpCelebration(unit.id)
        } else if (prevStarLevel === undefined && currentStarLevel >= 2) {
            // New high-star unit appeared (result of combining)
            console.log(`✨ TRIGGER (new combined unit): ${unit.name} star=${currentStarLevel}`)
            triggerStarUpCelebration(unit.id)
        }
        
        // Update tracking
        prevStarLevelMap.value[unit.id] = currentStarLevel
    })
}, { deep: true })

// Check if a unit is celebrating star-up
function isStarringUp(unitId: string): boolean {
    return starUpUnits.value.has(unitId)
}

const onOrbClick = (orbId: string) => {
    emit('collect-orb', orbId)
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
        
        <!-- Render Units (including dying units for animation) -->
        <div v-for="unit in displayedUnits" :key="unit.id" 
             class="unit" 
             :style="getUnitStyle(unit)"
             :class="{ 'mine': unit.ownerId === myPlayerId, 'dying': unit.isDying, 'star-up': isStarringUp(unit.id) }"
             :draggable="unit.ownerId === myPlayerId && !unit.isDying"
             @dragstart="(e) => onDragStart(e, unit)"
             @dragend="onDragEnd"
             @mouseenter="onUnitMouseEnter(unit.id)"
             @mouseleave="onUnitMouseLeave">
             
            <div class="hp-bar" :style="{ width: (unit.currentHealth / unit.maxHealth * 100) + '%' }"></div>
            <div v-if="unit.maxMana > 0" class="mana-bar" :style="{ width: (unit.mana / unit.maxMana * 100) + '%' }"></div>
            <img :src="unit.image" class="unit-img" :alt="unit.name" />
            <div class="star-indicator" :class="'stars-' + (unit.starLevel || 1)">
                <span v-for="n in (unit.starLevel || 1)" :key="n" class="star-dot"></span>
            </div>

            <!-- Stun Badge -->
            <div v-if="unit.stunTicksRemaining > 0" class="stun-badge">
                STUNNED
            </div>
            
            <!-- Star-up celebration effect -->
            <div v-if="isStarringUp(unit.id)" class="star-up-burst">
                <span v-for="i in 8" :key="i" class="star-particle" :style="{ '--particle-index': i }"></span>
            </div>
        </div>

        <!-- Render Loot Orbs -->
        <div v-for="orb in renderedOrbs" :key="orb.id"
             class="loot-orb"
             :class="orb.type.toLowerCase()"
             :style="{ 
                left: (orb.visualX * CELL_SIZE + 10) + 'px', 
                top: (orb.visualY * CELL_SIZE + 10) + 'px' 
             }"
             @click="onOrbClick(orb.id)">
            <div class="orb-inner">
                <div class="orb-glow"></div>
                <div class="orb-content">
                    <span v-if="orb.type === 'GOLD'">🪙</span>
                    <span v-else>🎁</span>
                </div>
            </div>
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
                 <UnitTooltip :unit="hoveredUnit" :placement="hoveredUnit.visualY < 4 ? 'bottom' : 'top'" />
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

             <!-- Heal Floating Text -->
             <div v-for="heal in floatingHeals" :key="heal.id"
                  class="floating-text heal"
                  :style="{ left: heal.x + 'px', top: heal.y + 'px' }">
                  {{ heal.text }}
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
    margin-top: 5px;
}

.grid {
    position: relative;
    width: 385px; /* GRID_COLS * CELL_SIZE (7*55) */
    height: 440px; /* GRID_ROWS * CELL_SIZE (8*55) */
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

/* ========== DEATH ANIMATION ========== */
.unit.dying {
    animation: unitDeath 0.6s ease-out forwards;
    pointer-events: none;
}

@keyframes unitDeath {
    0% {
        opacity: 1;
        transform: scale(1);
        filter: brightness(1);
    }
    30% {
        opacity: 1;
        transform: scale(1.1);
        filter: brightness(1.5) saturate(0.5);
        box-shadow: 0 0 20px rgba(239, 68, 68, 0.8);
    }
    100% {
        opacity: 0;
        transform: scale(0.3);
        filter: brightness(0.5) saturate(0);
    }
}

/* ========== STAR-UP CELEBRATION ========== */
.unit.star-up {
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
    /* Spread particles in a circle using CSS variable */
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

.stun-badge {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: rgba(0, 0, 0, 0.8);
    color: #94a3b8;
    font-size: 8px;
    font-weight: bold;
    padding: 2px 4px;
    border-radius: 4px;
    border: 1px solid #475569;
    z-index: 100;
    pointer-events: none;
    letter-spacing: 0.5px;
}

.floating-text.heal {
    color: #22c55e; /* Green */
    font-size: 16px;
}

.unit.atk-buffed {
    animation: atkBuffPulse 2s infinite alternate;
}

.unit.spd-buffed {
    animation: spdBuffPulse 2s infinite alternate;
}

@keyframes atkBuffPulse {
    from { box-shadow: 0 0 10px rgba(249, 115, 22, 0.4); }
    to { box-shadow: 0 0 20px rgba(249, 115, 22, 0.8); }
}

@keyframes spdBuffPulse {
    from { box-shadow: 0 0 10px rgba(59, 130, 246, 0.4); }
    to { box-shadow: 0 0 20px rgba(59, 130, 246, 0.8); }
}
@keyframes float-up-particle {
    0% { transform: translate(0, 0) scale(0); opacity: 0; }
    20% { opacity: 1; scale: 1.2; }
    100% { transform: translate(var(--tx), var(--ty)) scale(0.5); opacity: 0; }
}

/* Loot Orbs */
.loot-orb {
    position: absolute;
    width: 35px;
    height: 35px;
    cursor: pointer;
    z-index: 60;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.loot-orb:hover {
    transform: scale(1.2);
}

.orb-inner {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 50%;
    background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.4), rgba(255,255,255,0));
    box-shadow: 0 0 10px rgba(0,0,0,0.5);
}

.loot-orb.gold .orb-inner {
    background-color: #fbbf24; /* Gold color */
    border: 2px solid #d97706;
}

.loot-orb.unit .orb-inner {
    background-color: #a855f7; /* Purple color */
    border: 2px solid #7e22ce;
}

.orb-glow {
    position: absolute;
    top: -5px;
    left: -5px;
    right: -5px;
    bottom: -5px;
    border-radius: 50%;
    background: inherit;
    filter: blur(8px);
    opacity: 0.6;
    animation: orb-pulse 2s infinite ease-in-out;
}

.orb-content {
    font-size: 18px;
    z-index: 1;
}

@keyframes orb-pulse {
    0%, 100% { transform: scale(1); opacity: 0.4; }
    50% { transform: scale(1.2); opacity: 0.8; }
}

/* Collection animation */
.loot-orb:active {
    transform: scale(0.8);
    opacity: 0.5;
}
</style>


