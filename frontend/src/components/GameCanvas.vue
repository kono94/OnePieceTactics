<script setup lang="ts">
import { computed, ref } from 'vue'
import UnitTooltip from './UnitTooltip.vue'

const props = defineProps<{
    state: any,
    myPlayerId?: string
}>()

const emit = defineEmits(['move'])

// 8 Rows x 7 Cols grid
const GRID_ROWS = 8
const GRID_COLS = 7

const renderedUnits = computed(() => {
    if (!props.state || !props.state.players) return []
    let allUnits: any[] = []
    
    const isCombat = props.state.phase === 'COMBAT'
    const myId = props.myPlayerId
    
    // Heuristic to detect if we are the "Top" player (Rows 0-3) in combat
    // If we are, we need to flip the view.
    // However, the backend mirrors P2 to rows 4-7. 
    // If I am P2, my units are at 4-7. I want to see them at 4-7 (Bottom). OK.
    // If I am P1, my units are at 0-3. I want to see them at 4-7 (Bottom). Need Flip.
    // How do we know if we are P1 or P2? 
    // We check our units' backend positions. If any of our units is at y < 4, we assume we are P1.
    
    let amITopPlayer = false
    if (isCombat && myId) {
        // Find my units
        const myPlayer = props.state.players[myId]
        if (myPlayer && myPlayer.board) {
            const hasTopUnits = myPlayer.board.some((u: any) => u.y < 4)
            if (hasTopUnits) {
                amITopPlayer = true
            }
        }
    }
    
    Object.values(props.state.players).forEach((player: any) => {
        const board = player.boardUnits || player.board
        if (board) {
            allUnits = allUnits.concat(board
                .filter((u:any) => u.x >= 0 && u.y >= 0)
                .map((u: any) => {
                    let visualX = u.x;
                    let visualY = u.y;
                    
                    if (isCombat) {
                        // If I am the Top player (backend 0-3), I want to see myself at Bottom (4-7).
                        // So I flip everyone.
                        // P1 (y=0) becomes y=7. P2 (y=7) becomes y=0.
                        if (amITopPlayer) {
                            visualX = 6 - u.x;
                            visualY = 7 - u.y;
                        }
                    } else {
                        // Planning Phase: I see only my units (0-3).
                        // I want them at 4-7.
                        if (player.playerId === myId) {
                             visualY = u.y + 4;
                        }
                    }
                    
                    return {
                        ...u,
                        visualX,
                        visualY,
                        ownerId: player.playerId,
                        isMine: player.playerId === myId,
                        image: `/assets/units/${u.name === 'Monkey D. Luffy' ? 'luffy_v1' : u.name === 'Roronoa Zoro' ? 'zoro_v1' : 'nami_v1'}.png`
                    }
                }))
        }
    })
    return allUnits
})

const GRID_SIZE = 8
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
        boxShadow: unit.isMine ? '0 0 10px rgba(16, 185, 129, 0.6)' : 'none'
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
            // Visual 4-7 -> Backend 0-3
            const backendY = y - 4
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

</script>

<template>
  <div class="board-container">
    <div class="grid" @mouseleave="dragOverCellIndex = -1">
        <!-- Render Grid Cells as Drop Zones (8 rows x 7 cols = 56 cells) -->
        <div v-for="i in 56" :key="'cell-'+i" 
             class="cell" 
             :class="{ 
                'player-half': Math.floor((i-1)/7) >= 4, 
                'enemy-half': Math.floor((i-1)/7) < 4,
                'highlight-drop': isDragging && Math.floor((i-1)/7) >= 4,
                'active-drop': dragOverCellIndex === (i-1) && Math.floor((i-1)/7) >= 4
             }"
             @dragover="(e) => onDragOver(e, i-1)"
             @drop="(e) => onDrop(e, (i-1)%7, Math.floor((i-1)/7))">
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
            
            <!-- Tooltip -->
            <transition name="fade">
                <UnitTooltip v-if="hoveredUnitId === unit.id" :unit="unit" />
            </transition>
        </div>
        <!-- Player Names Overlay -->
        <div class="overlays">
             <div class="name-tag enemy" v-if="opponentName">{{ opponentName }}</div>
             <div class="name-tag me" v-if="myPlayerName">{{ myPlayerName }}</div>
        </div>
    </div>
  </div>
</template>

<style scoped>
.board-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

.grid {
    position: relative;
    width: 420px; 
    height: 480px;
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    grid-template-rows: repeat(8, 1fr);
    background-color: #1e293b;
    border: 2px solid #555;
    box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
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
    /* removed border:none to keep the colored border from getStyle */
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
    object-fit: cover; /* Changed to cover for better look in circle */
    border-radius: 50%; /* Ensure image is clipped */
    pointer-events: none;
}

.hp-bar {
    position: absolute;
    top: -8px; 
    left: 0;
    width: 100%; /* controlled by style width, but container is full width */
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
</style>

