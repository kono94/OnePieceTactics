<script setup lang="ts">
import { computed, ref, watch, onUnmounted, onMounted } from 'vue'
import CombatEffectsCanvas from './game/CombatEffectsCanvas.vue'
import { getAttackConfig, getAbilityConfig } from '../data/animationConfig'
import type { GameState, GameUnit, GamePhase, RenderedUnit, RenderedOrb, PlayerState, DisplayedUnit, CombatEvent } from '../types'
import type { NormalizedCombatVisualEvent } from '../types/combatEffects'
import { getUnitIconPath } from '../utils/iconUtils'
import { getRarityColor, TEAM_COLORS } from '../utils/colorUtils'

const props = defineProps<{
    state: GameState | null,
    myPlayerId?: string,
    isDraggingProp?: boolean
}>()

const emit = defineEmits(['move', 'drag-start', 'drag-end', 'collect-orb', 'update:cell-size', 'update:is-over-grid', 'show-tooltip', 'hide-tooltip'])

// Grid Constants
const GRID_ROWS = 6
const GRID_COLS = 9
const PLAYER_ROWS = 3 // Height of one player's board (half of arena)
const GRID_GUTTER = 6 // Space between cells and board border to prevent clipping

const renderedUnits = computed((): RenderedUnit[] => {
    const state = props.state
    if (!state || !state.players) return []
    let allUnits: RenderedUnit[] = []
    
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
    
    Object.values(state.players).forEach((player: PlayerState) => {
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
                .filter((u: GameUnit) => u.x >= 0 && u.y >= 0 && u.currentHealth > 0)
                .map((u: GameUnit): RenderedUnit => {
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
                        image: getUnitIconPath(u.definitionId, props.state?.gameMode)
                    }
                }))
        }
    })
    return allUnits
})

const renderedOrbs = computed((): RenderedOrb[] => {
    const state = props.state
    if (!state || !state.players || !props.myPlayerId) return []
    const myPlayer = state.players[props.myPlayerId]
    if (!myPlayer || !myPlayer.lootOrbs) return []

    return myPlayer.lootOrbs.map((orb): RenderedOrb => {
        // Place orbs in the top half (visual rows 0-3)
        return {
            ...orb,
            visualX: orb.x,
            visualY: orb.y
        }
    })
})

const containerRef = ref<HTMLElement | null>(null)
const CELL_SIZE = ref(60)

const updateCellSize = () => {
    if (!containerRef.value) return
    
    const container = containerRef.value
    const padding = 60 // Increased padding for more breathing room
    const availableWidth = container.clientWidth - padding
    const availableHeight = container.clientHeight - padding
    
    // Calculate max cell size for width and height
    const maxCellWidth = availableWidth / GRID_COLS
    const maxCellHeight = availableHeight / GRID_ROWS
    
    // Use the smaller of the two, capped at a reasonable max/min
    // REFINED: Clamping to 85px max to prevent "oversized" look
    CELL_SIZE.value = Math.max(40, Math.min(85, Math.min(maxCellWidth, maxCellHeight)))
    emit('update:cell-size', CELL_SIZE.value)
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
    updateCellSize()
    resizeObserver = new ResizeObserver(() => {
        updateCellSize()
    })
    if (containerRef.value) {
        resizeObserver.observe(containerRef.value)
    }
})

onUnmounted(() => {
    if (resizeObserver) {
        resizeObserver.disconnect()
    }
})

// Drag state
const isDragging = ref(false)
const draggingUnitId = ref<string|null>(null)
const dragOverCellIndex = ref(-1)
const hoveredUnitId = ref<string|null>(null)


const getUnitStyle = (unit: RenderedUnit) => {
    // Disable pointer events on units when dragging, EXCEPT the unit being dragged.
    // This allows drops to fall through to the grid cell for swapping.
    const shouldDisablePointer = (isDragging.value || props.isDraggingProp) 
                                 && unit.id !== draggingUnitId.value;

    const styles: Record<string, string | number> = {
        left: (unit.visualX * CELL_SIZE.value + 5) + 'px',
        top: (unit.visualY * CELL_SIZE.value + 5) + 'px',
        width: (CELL_SIZE.value - 10) + 'px',
        height: (CELL_SIZE.value - 10) + 'px',
        borderColor: getRarityColor(unit.cost), 
        borderWidth: '2px',
        borderStyle: 'solid',
        boxShadow: unit.isMine ? `0 0 10px ${TEAM_COLORS.FRIENDLY}99` : 'none',
        zIndex: hoveredUnitId.value === unit.id ? 100 : 10,
        pointerEvents: shouldDisablePointer ? 'none' : 'auto',
        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        '--rarity-color': getRarityColor(unit.cost)
    }

    // Apply status effect visuals
    if (unit.stunSecondsRemaining > 0) {
        styles.filter = 'grayscale(1) brightness(0.8)';
    } else {
        // Clean look: team-only glow
        if (unit.isMine) {
            styles.boxShadow = `0 0 10px ${TEAM_COLORS.FRIENDLY}99`;
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
     if (!p) return 'Opponent'
     return p.isGhost ? `${p.name} (Ghost)` : p.name
})

const onDragStart = (evt: DragEvent, unit: RenderedUnit) => {
    if (unit.ownerId !== props.myPlayerId || props.state?.phase === 'COMBAT') {
        evt.preventDefault()
        return
    }
    isDragging.value = true
    draggingUnitId.value = unit.id
    // Clear hover state on drag start
    hoveredUnitId.value = null
    emit('drag-start', { unit, x: evt.clientX, y: evt.clientY })
    if (evt.dataTransfer) {
        evt.dataTransfer.setData('unitId', unit.id)
        evt.dataTransfer.effectAllowed = 'move'
        
        // Use only the icon for the drag image to avoid capturing bars/halos
        const img = (evt.currentTarget as HTMLElement)?.querySelector('.unit-img');
        if (img) {
            evt.dataTransfer.setDragImage(img, 32, 32);
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
    
    // Block grid drops during combat
    if (props.state?.phase === 'COMBAT') {
        onDragEnd() // Ensure drag state is cleared immediately (fixes lag)
        return 
    }
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
    
    // Block grid interaction during combat
    if (props.state?.phase === 'COMBAT') {
        dragOverCellIndex.value = -1
        if (evt.dataTransfer) {
            evt.dataTransfer.dropEffect = 'none'
        }
        return
    }

    dragOverCellIndex.value = cellIndex
    emit('update:is-over-grid', true)
    if (evt.dataTransfer) {
        evt.dataTransfer.dropEffect = 'move'
    }
}

const onDragLeave = () => {
    dragOverCellIndex.value = -1
    emit('update:is-over-grid', false)
}

// Hover handlers for Tooltip
const onUnitMouseEnter = (evt: MouseEvent, unit: RenderedUnit) => {
    if (!isDragging.value && !props.isDraggingProp) {
        hoveredUnitId.value = unit.id
        emit('show-tooltip', {
            rect: (evt.currentTarget as HTMLElement).getBoundingClientRect(),
            unit: unit,
            placement: unit.visualY < 4 ? 'bottom' : 'top'
        })
    }
}
const onUnitMouseLeave = () => {
    hoveredUnitId.value = null
    emit('hide-tooltip')
}

// ========== ANIMATION SYSTEM ==========

const combatVisualEvents = ref<NormalizedCombatVisualEvent[]>([])
let nextVisualEventId = 0

const hitFlashUnits = ref<Set<string>>(new Set())
const attackingUnits = ref<Set<string>>(new Set())
const castingUnits = ref<Set<string>>(new Set())

// Track previous health to detect attacks
const prevHealthMap = ref<Record<string, number>>({})

// ========== DEATH ANIMATION SYSTEM ==========
// Track units that are dying (animating death)
const dyingUnits = ref<Set<string>>(new Set())
// Store the last known position/data for dead units during animation
const dyingUnitData = ref<Map<string, DisplayedUnit>>(new Map())
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
function findNearestEnemy(unit: RenderedUnit, allUnits: RenderedUnit[]): RenderedUnit | null {
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
const prevUnitsMap = ref<Map<string, RenderedUnit>>(new Map())
const prevPhase = ref<GamePhase | undefined | null>(null)
const lastProcessedEventTime = ref(0)

const unitsById = computed(() => {
    const map = new Map<string, RenderedUnit>()
    if (renderedUnits.value) {
        renderedUnits.value.forEach((u: RenderedUnit) => map.set(u.id, u))
    }
    return map
})

function lookupUnit(unitId: string): RenderedUnit | DisplayedUnit | undefined {
    return unitsById.value.get(unitId) || dyingUnitData.value.get(unitId) || prevUnitsMap.value.get(unitId)
}

function pointForUnit(unit: RenderedUnit | DisplayedUnit) {
    return {
        x: unit.visualX * CELL_SIZE.value + CELL_SIZE.value / 2,
        y: unit.visualY * CELL_SIZE.value + CELL_SIZE.value / 2
    }
}

function addTimedFeedback(targetSet: typeof hitFlashUnits, unitId: string, duration: number) {
    const next = new Set(targetSet.value)
    next.add(unitId)
    targetSet.value = next

    const timer = window.setTimeout(() => {
        const updated = new Set(targetSet.value)
        updated.delete(unitId)
        targetSet.value = updated
    }, duration)
    deathTimers.value.push(timer)
}

function normalizeCombatEvent(
    event: CombatEvent,
    source: RenderedUnit | DisplayedUnit,
    target: RenderedUnit | DisplayedUnit,
    batchSize: number
): NormalizedCombatVisualEvent {
    const isSkill = event.type === 'SKILL'
    const attack = getAttackConfig(source.definitionId)
    const ability = getAbilityConfig(source.definitionId)
    const aliveUnits = renderedUnits.value.filter(unit => unit.currentHealth > 0).length

    return {
        id: nextVisualEventId++,
        timestamp: event.timestamp,
        type: event.type,
        sourceId: event.sourceId,
        targetId: event.targetId,
        value: event.value,
        skillName: event.skillName,
        source: source as RenderedUnit,
        target: target as RenderedUnit,
        start: pointForUnit(source),
        end: pointForUnit(target),
        definitionId: source.definitionId,
        attack,
        ability,
        pattern: source.ability?.pattern || 'SINGLE',
        starLevel: source.starLevel || 1,
        intensity: isSkill ? 'ultimate' : (event.type === 'DAMAGE' ? 'normal' : 'low'),
        batchSize,
        crowded: aliveUnits >= 12 || batchSize > 6
    }
}

function isHealingCombatEvent(event: CombatEvent, source: RenderedUnit | DisplayedUnit): boolean {
    return event.type === 'HEAL' || event.value < 0 || source.ability?.type === 'HEAL'
}

watch(() => props.state?.recentEvents, (newEvents) => {
    if (!newEvents || newEvents.length === 0) return
    
    // Deduplication based on timestamp
    let maxTime = lastProcessedEventTime.value
    
    newEvents.forEach((event: CombatEvent) => {
        if (event.timestamp <= lastProcessedEventTime.value) return
        if (event.timestamp > maxTime) maxTime = event.timestamp
        
        const targetFromEvent = lookupUnit(event.targetId)
        const source = lookupUnit(event.sourceId) || targetFromEvent
        if (!source) return

        if (event.type === 'DAMAGE') {
            const target = targetFromEvent
            // Value can be positive (damage) or negative (heal)
            if (event.value > 0 && target) {
                combatVisualEvents.value.push(normalizeCombatEvent(event, source, target, newEvents.length))
                addTimedFeedback(attackingUnits, source.id, 360)
                addTimedFeedback(hitFlashUnits, target.id, 420)
            } else if (event.value < 0) {
                const target = targetFromEvent || source
                combatVisualEvents.value.push(normalizeCombatEvent(event, source, target, newEvents.length))
                const healAmount = Math.abs(event.value)
                const healId = nextVisualEventId++
                floatingHeals.value.push({
                    id: healId,
                    x: target.visualX * CELL_SIZE.value + CELL_SIZE.value / 2,
                    y: target.visualY * CELL_SIZE.value + 10,
                    text: `+${healAmount}`
                })
                setTimeout(() => {
                    floatingHeals.value = floatingHeals.value.filter(h => h.id !== healId)
                }, 1000)
            }
        } else if (event.type === 'SKILL') {
            let target = targetFromEvent || source
            const isHealingSkill = isHealingCombatEvent(event, source)
            
            if (!targetFromEvent && !isHealingSkill) {
                const nearest = findNearestEnemy(source, renderedUnits.value)
                if (nearest) {
                    target = nearest
                }
            }

            combatVisualEvents.value.push(normalizeCombatEvent(event, source, target, newEvents.length))
            addTimedFeedback(castingUnits, source.id, 900)
            if (!isHealingSkill && target.id !== source.id) addTimedFeedback(hitFlashUnits, target.id, 520)
            if (isHealingSkill) addTimedFeedback(hitFlashUnits, target.id, 420)
            
            // Floating text for skill
            castingAnimations.value.push({
                id: nextVisualEventId++,
                x: source.visualX * CELL_SIZE.value + CELL_SIZE.value / 2,
                y: source.visualY * CELL_SIZE.value,
                text: event.skillName || getAbilityConfig(source.definitionId).signature || source.activeAbility || (source.ability ? source.ability.name : 'Ability!')
            })
            setTimeout(() => {
                castingAnimations.value.shift()
            }, 1000)
            if (isHealingSkill) {
                const healAmount = Math.abs(event.value)
                const healId = nextVisualEventId++
                floatingHeals.value.push({
                    id: healId,
                    x: target.visualX * CELL_SIZE.value + CELL_SIZE.value / 2,
                    y: target.visualY * CELL_SIZE.value + 10,
                    text: `+${healAmount}`
                })
                setTimeout(() => {
                    floatingHeals.value = floatingHeals.value.filter(h => h.id !== healId)
                }, 1000)
            }
        } else if (event.type === 'HEAL') {
            const target = targetFromEvent || source
            combatVisualEvents.value.push(normalizeCombatEvent(event, source, target, newEvents.length))
            const healAmount = Math.abs(event.value)
            const healId = nextVisualEventId++
            floatingHeals.value.push({
                id: healId,
                x: target.visualX * CELL_SIZE.value + CELL_SIZE.value / 2,
                y: target.visualY * CELL_SIZE.value + 10,
                text: `+${healAmount}`
            })
            setTimeout(() => {
                floatingHeals.value = floatingHeals.value.filter(h => h.id !== healId)
            }, 1000)
        } else if (event.type === 'SHIELD') {
            const target = targetFromEvent || source
            combatVisualEvents.value.push(normalizeCombatEvent(event, source, target, newEvents.length))
        } else if (event.type === 'DEATH') {
            const target = targetFromEvent || source
            combatVisualEvents.value.push(normalizeCombatEvent(event, source, target, newEvents.length))
            addTimedFeedback(hitFlashUnits, target.id, 500)
        }
    })
    
    if (combatVisualEvents.value.length > 120) {
        combatVisualEvents.value = combatVisualEvents.value.slice(-80)
    }
    lastProcessedEventTime.value = maxTime
}, { deep: true })

watch(() => renderedUnits.value, (newUnits) => {
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
        combatVisualEvents.value = []
        hitFlashUnits.value = new Set()
        attackingUnits.value = new Set()
        castingUnits.value = new Set()
        return
    }
    
    // Build map of current alive units
    const newUnitIds = new Set(newUnits.map((u: RenderedUnit) => u.id))
    
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
    newUnits.forEach((unit: RenderedUnit) => {
        // Update health tracking
        prevHealthMap.value[unit.id] = unit.currentHealth
        
        // Store current unit data for next frame's death detection
        prevUnitsMap.value.set(unit.id, { ...unit })
    })
})

// Trigger death animation for a unit
function triggerDeathAnimation(unit: RenderedUnit) {
    if (dyingUnits.value.has(unit.id)) return // Already dying
    
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
const displayedUnits = computed((): DisplayedUnit[] => {
    const alive = renderedUnits.value
    const dying = Array.from(dyingUnitData.value.values())
    return [...alive, ...dying]
})

// Trigger star-up celebration for a unit
function triggerStarUpCelebration(unitId: string) {
    if (starUpUnits.value.has(unitId)) return // Already celebrating
    
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
    
    allMyUnits.forEach((unit: GameUnit) => {
        if (!unit) return
        const prevStarLevel = prevStarLevelMap.value[unit.id]
        const currentStarLevel = unit.starLevel || 1
        
        // Trigger celebration if:
        // 1. Existing unit's star level increased, OR
        // 2. New unit appeared with star level >= 2 (just combined)
        if (prevStarLevel !== undefined && currentStarLevel > prevStarLevel) {
            // Existing unit leveled up
            triggerStarUpCelebration(unit.id)
        } else if (prevStarLevel === undefined && currentStarLevel >= 2) {
            // New high-star unit appeared (result of combining)
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
  <div class="main-canvas-container" ref="containerRef">
    <div class="board-container" :style="{ 
        width: (GRID_COLS * CELL_SIZE + (GRID_GUTTER * 2)) + 'px', 
        height: (GRID_ROWS * CELL_SIZE + (GRID_GUTTER * 2)) + 'px'
    }">
        <div class="board-clipper">
            <div class="grid" :style="{
                gridTemplateColumns: `repeat(${GRID_COLS}, ${CELL_SIZE}px)`,
                gridTemplateRows: `repeat(${GRID_ROWS}, ${CELL_SIZE}px)`
            }" @mouseleave="dragOverCellIndex = -1">
                <!-- Render Grid Cells as Drop Zones -->
                <div v-for="i in (GRID_ROWS * GRID_COLS)" :key="'cell-'+i" 
                     class="cell" 
                     :class="{ 
                        'player-half': Math.floor((i-1)/GRID_COLS) >= PLAYER_ROWS, 
                        'enemy-half': Math.floor((i-1)/GRID_COLS) < PLAYER_ROWS,
                        'highlight-drop': (isDragging || isDraggingProp) && Math.floor((i-1)/GRID_COLS) >= PLAYER_ROWS && props.state?.phase !== 'COMBAT',
                        'active-drop': dragOverCellIndex === (i-1) && Math.floor((i-1)/GRID_COLS) >= PLAYER_ROWS && props.state?.phase !== 'COMBAT'
                     }"
                     @dragover="(e) => onDragOver(e, i-1)"
                     @dragleave="onDragLeave"
                     @drop="(e) => onDrop(e, (i-1)%GRID_COLS, Math.floor((i-1)/GRID_COLS))">
                </div>

                <!-- Absolute content overlay (aligned to grid cells) -->
                <div class="grid-overlay" :style="{ inset: GRID_GUTTER + 'px' }">
                    <CombatEffectsCanvas
                        :events="combatVisualEvents"
                        :units="displayedUnits"
                        :cell-size="CELL_SIZE"
                        :grid-rows="GRID_ROWS"
                        :grid-cols="GRID_COLS"
                        :phase="props.state?.phase"
                    />

                    <!-- Render Units -->
                    <div v-for="unit in displayedUnits" :key="unit.id" 
                         class="unit" 
                         :style="getUnitStyle(unit)"
                         :class="{ 
                            'mine': unit.ownerId === myPlayerId, 
                            'dying': unit.isDying, 
                            'star-up': isStarringUp(unit.id),
                            'hit-flash': hitFlashUnits.has(unit.id),
                            'attacking-lunge': attackingUnits.has(unit.id),
                            'casting-glow': castingUnits.has(unit.id),
                            'ultimate-caster': castingUnits.has(unit.id) && (unit.starLevel || 1) >= 3
                         }"
                         :draggable="unit.ownerId === myPlayerId && !unit.isDying"
                         @dragstart="(e) => onDragStart(e, unit)"
                         @dragend="onDragEnd"
                         @mouseenter="(e) => onUnitMouseEnter(e, unit)"
                         @mouseleave="onUnitMouseLeave">
                         
                        <div class="hp-bar-container">
                            <div class="hp-bar-fill" :style="{ 
                                width: (unit.currentHealth / unit.maxHealth * 100) + '%',
                                backgroundColor: unit.ownerId === myPlayerId ? TEAM_COLORS.FRIENDLY : TEAM_COLORS.OPPONENT
                            }"></div>
                        </div>
                        <div v-if="unit.maxMana > 0" class="mana-pill">
                            <div class="mana-fill-btm" :style="{ height: (unit.mana / unit.maxMana * 100) + '%' }"></div>
                        </div>
                        <div class="buff-container">
                            <div v-if="unit.atkBuff > 1.01" class="buff-icon atk-buff">⚔️</div>
                            <div v-if="unit.spdBuff > 1.01" class="buff-icon spd-buff">⚡</div>
                            <div v-if="unit.atkBuff < 0.99" class="buff-icon atk-debuff">🔻</div>
                            <div v-if="unit.spdBuff < 0.99" class="buff-icon spd-debuff">❄️</div>
                        </div>
                        <img :src="unit.image" class="unit-img" :alt="unit.name" draggable="false" />
                        <div class="cost-top-glow"></div>
                        <div v-if="unit.starLevel === 2" class="star-2-halo"></div>
                        <div v-if="unit.starLevel === 3" class="star-3-flow"></div>
                        <div v-if="unit.stunSecondsRemaining > 0" class="stun-badge">STUNNED ({{ unit.stunSecondsRemaining.toFixed(1) }}s)</div>
                        <div v-if="isStarringUp(unit.id)" class="star-up-burst">
                            <span v-for="i in 8" :key="i" class="star-particle" :style="{ '--particle-index': i }"></span>
                        </div>
                    </div>

                    <!-- Render Loot Orbs -->
                    <div v-for="orb in renderedOrbs" :key="orb.id"
                         class="loot-orb"
                         :class="orb.type.toLowerCase()"
                         :style="{ 
                            left: (orb.visualX * CELL_SIZE + (CELL_SIZE - (CELL_SIZE - 20))/2) + 'px', 
                            top: (orb.visualY * CELL_SIZE + (CELL_SIZE - (CELL_SIZE - 20))/2) + 'px',
                            width: (CELL_SIZE - 20) + 'px',
                            height: (CELL_SIZE - 20) + 'px'
                         }"
                         @click="onOrbClick(orb.id)">
                        <div class="orb-inner">
                            <div class="orb-glow"></div>
                            <div class="orb-content" :style="{ fontSize: (CELL_SIZE * 0.45) + 'px' }">
                                <span v-if="orb.type === 'GOLD'">🪙</span>
                                <span v-else>🎁</span>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
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
.main-canvas-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    overflow: visible; /* CRITICAL: Allow tooltips to float over panels */
    padding: 30px;
}

.board-container {
    position: relative;
    padding: 0;
    background: rgba(15, 23, 42, 0.4);
    border-radius: 12px;
    border: 3px solid rgba(51, 65, 85, 0.8);
    box-shadow: 
        0 20px 50px rgba(0, 0, 0, 0.6),
        inset 0 0 40px rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(4px);
    overflow: visible; /* CRITICAL: Allow names/tooltips outside grid */
}

.board-clipper {
    position: absolute;
    inset: 0;
    border-radius: 9px; /* Slightly inner for the outer border */
    overflow: hidden; /* CRITICAL: Clips cell backgrounds/units at board edge */
    z-index: 1;
}

.board-container::before {
    content: '';
    position: absolute;
    inset: 4px;
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    pointer-events: none;
}

.grid {
    position: relative;
    display: grid;
    width: 100%;
    height: 100%;
    z-index: 1;
    background: transparent;
    transition: width 0.2s, height 0.2s;
    padding: 6px; /* GRID_GUTTER constant value */
    box-sizing: border-box;
}

.grid-overlay {
    position: absolute;
    pointer-events: none;
    z-index: 10;
}

.cell {
    border: 1px solid rgba(255, 255, 255, 0.05); 
    box-sizing: border-box;
    transition: all 0.2s;
    background: radial-gradient(circle at center, rgba(30, 41, 59, 0.1) 0%, transparent 70%);
}

.cell.player-half {
    background-color: rgba(59, 130, 246, 0.03);
}

.cell.enemy-half {
    background-color: rgba(239, 68, 68, 0.03);
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
    transition: left 0.2s, top 0.2s, border-color 0.2s, box-shadow 0.2s; 
    box-shadow: 0 4px 6px rgba(0,0,0,0.5);
    z-index: 10;
    pointer-events: auto; 
    background-color: #1e293b; 
}
.unit.mine {
    cursor: grab;
}
.unit:hover {
    z-index: 2000; /* Unified top layer */
}
.unit.mine:active {
    cursor: grabbing;
}

.unit.hit-flash {
    animation: unitHitFlash 0.42s ease-out;
}

.unit.attacking-lunge {
    animation: unitAttackLunge 0.36s ease-out;
}

.unit.casting-glow {
    animation: unitCastingGlow 0.9s ease-out;
}

.unit.ultimate-caster {
    animation: unitUltimateCaster 0.9s ease-out;
}

@keyframes unitHitFlash {
    0% {
        filter: brightness(1);
        transform: translate(0, 0) scale(1);
    }
    18% {
        filter: brightness(2.8) saturate(0.7);
        transform: translate(-2px, 1px) scale(1.08);
        box-shadow: 0 0 22px rgba(255, 255, 255, 0.95);
    }
    44% {
        transform: translate(2px, -1px) scale(0.98);
    }
    100% {
        filter: brightness(1);
        transform: translate(0, 0) scale(1);
    }
}

@keyframes unitAttackLunge {
    0% {
        transform: translate(0, 0) scale(1);
    }
    45% {
        transform: translate(5px, -5px) scale(1.08);
        filter: brightness(1.35);
    }
    100% {
        transform: translate(0, 0) scale(1);
        filter: brightness(1);
    }
}

@keyframes unitCastingGlow {
    0% {
        filter: brightness(1);
        box-shadow: 0 0 10px var(--rarity-color);
    }
    24% {
        filter: brightness(1.8) saturate(1.4);
        box-shadow: 0 0 26px var(--rarity-color), 0 0 42px rgba(255, 255, 255, 0.36);
    }
    100% {
        filter: brightness(1);
        box-shadow: 0 0 10px var(--rarity-color);
    }
}

@keyframes unitUltimateCaster {
    0% {
        transform: scale(1);
    }
    25% {
        transform: scale(1.18);
        filter: brightness(2);
    }
    100% {
        transform: scale(1);
    }
}

/* Cost Top Glow */
.cost-top-glow {
    position: absolute;
    top: 5px;
    left: 50%;
    transform: translateX(-50%);
    width: 30px;
    height: 10px;
    background: radial-gradient(ellipse at center, var(--rarity-color), transparent 70%);
    opacity: 0.6;
    pointer-events: none;
    z-index: 5;
    filter: blur(2px);
}

.unit-img {
    width: 100%;
    height: 100%;
    object-fit: cover; 
    border-radius: 50%;
    pointer-events: none;
    z-index: 2; /* Relative to parent .unit, ensures it stays above halos but under bars */
}

.hp-bar-container {
    position: absolute;
    top: -6px; /* Moved lower, closer to unit */
    left: 2px;
    right: 2px;
    height: 6px;
    background-color: rgba(0, 0, 0, 0.6);
    border-radius: 3px;
    /* Removed heavy border for a cleaner "glass" look */
    box-shadow: 0 2px 4px rgba(0,0,0,0.5);
    overflow: hidden;
    z-index: 20;
}

.hp-bar-fill {
    height: 100%;
    transition: width 0.3s cubic-bezier(0.1, 0.7, 0.1, 1);
}

.mana-pill {
    position: absolute;
    bottom: -4px;
    right: -4px;
    width: 20px; /* Reduced size from 24px */
    height: 20px;
    background-color: rgba(15, 23, 42, 0.9);
    border-radius: 50%;
    /* Use a themed, transparent border to make it less dominant */
    border: 1.5px solid rgba(125, 211, 252, 0.4); 
    box-shadow: 0 2px 6px rgba(0,0,0,0.6);
    z-index: 30;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
}

.mana-fill-btm {
    width: 100%;
    background-color: #2563eb; /* Slightly deeper, more vibrant blue */
    transition: height 0.3s ease-out;
}

.buff-container {
    position: absolute;
    left: -15px;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    gap: 4px;
    z-index: 50;
    pointer-events: none;
}

.buff-icon {
    width: 20px;
    height: 20px;
    border-radius: 4px;
    border: 1px solid white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.5);
    background-color: #0f172a;
    color: white;
}

.buff-icon.atk-buff { background-color: #22c55e; }
.buff-icon.spd-buff { background-color: #3b82f6; }
.buff-icon.atk-debuff { background-color: #ef4444; }
.buff-icon.spd-debuff { background-color: #6366f1; }

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
    left: -110px;
    width: 120px;
    z-index: 60;
    padding: 5px 10px;
    background: rgba(0,0,0,0.7);
    color: white;
    border-radius: 4px;
    font-weight: bold;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    text-align: center;
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

/* 2-Star Energy Halo Effect */
.star-2-halo {
    position: absolute;
    top: -6px;
    left: -6px;
    width: calc(100% + 12px);
    height: calc(100% + 12px);
    transform: none;
    border-radius: 50%;
    border: 1px dashed var(--rarity-color);
    opacity: 0.6;
    animation: rotate-halo 10s linear infinite;
    z-index: 2;
    pointer-events: none;
}

/* 3-Star Conqueror Flow Effect */
.star-3-flow {
    position: absolute;
    top: -4px;
    left: -4px;
    width: calc(100% + 8px);
    height: calc(100% + 8px);
    transform: none;
    border-radius: 50%;
    pointer-events: none;
    z-index: 5;
}

.star-3-flow::before {
    content: '';
    position: absolute;
    top: -2px;
    left: -2px;
    width: calc(100% + 4px);
    height: calc(100% + 4px);
    border-radius: 50%;
    background: conic-gradient(from 0deg, var(--rarity-color), #fff, var(--rarity-color), #000, var(--rarity-color));
    animation: rotate-halo 1.5s linear infinite;
    z-index: -1;
    -webkit-mask: radial-gradient(circle at center, transparent 65%, black 66%);
    mask: radial-gradient(circle at center, transparent 65%, black 66%);
}

.star-3-flow::after {
    content: '';
    position: absolute;
    top: -6px;
    left: -6px;
    width: calc(100% + 12px);
    height: calc(100% + 12px);
    border-radius: 50%;
    box-shadow: 0 0 20px var(--rarity-color);
    opacity: 0.6;
    z-index: -2;
}

@keyframes rotate-halo {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

@keyframes floatUp {
    0% { transform: translate(-25px, -20px); opacity: 0; scale: 0.5; }
    20% { transform: translate(-25px, -40px); opacity: 1; scale: 1.2; }
    100% { transform: translate(-25px, -60px); opacity: 0; scale: 1.0; }
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
    pointer-events: auto; /* Ensure they work inside overlay */
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
/* Overlays & Tooltips */
.overlays {
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 100; /* Above board and units */
}


</style>
