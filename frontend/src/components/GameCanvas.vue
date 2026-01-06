<script setup lang="ts">
import { computed, ref } from 'vue'
import UnitTooltip from './UnitTooltip.vue'

const props = defineProps<{
    state: any,
    myPlayerId?: string
}>()

const emit = defineEmits(['move'])

const units = computed(() => {
    if (!props.state || !props.state.players) return []
    let allUnits: any[] = []
    
    // Iterate over ALL players to show all units
    Object.values(props.state.players).forEach((player: any) => {
        const board = player.boardUnits || player.board
        if (board) {
            console.log(`Player ${player.name} board size:`, board.length, board)
            allUnits = allUnits.concat(board
                .filter((u:any) => {
                     const valid = u.x >= 0 && u.y >= 0
                     if (!valid) console.log('Filtered invalid unit:', u)
                     return valid
                })
                .map((u: any) => ({
                    ...u,
                    ownerId: player.playerId,
                    image: `/assets/units/${u.name === 'Monkey D. Luffy' ? 'luffy_v1' : u.name === 'Roronoa Zoro' ? 'zoro_v1' : 'nami_v1'}.png`
                })))
        } else {
             console.log(`Player ${player.name} has no board property`)
        }
    })
    console.log('Rendered Units:', allUnits)
    return allUnits
})

const GRID_SIZE = 8
const CELL_SIZE = 54

// Drag state
const isDragging = ref(false)
const dragOverCellIndex = ref(-1)
const hoveredUnitId = ref<string|null>(null)

const getStyle = (unit: any) => {
    return {
        left: (unit.x * CELL_SIZE) + 'px',
        top: (unit.y * CELL_SIZE) + 'px',
        width: (CELL_SIZE - 4) + 'px',
        height: (CELL_SIZE - 4) + 'px',
        borderColor: getColor(unit.ownerId)
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
        // Create invisible drag image to prevent default browser ghost behavior if desired,
        // or just let it be. User complained "character disappears", which usually means
        // the original element is hidden or the drag image is transparency.
        // We'll trust the default behavior but ensure we don't hide the original *too* much.
        
        // Setting a custom drag image can help visibility
        const img = new Image();
        img.src = unit.image;
        evt.dataTransfer.setDragImage(img, 25, 25);
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
            emit('move', { unitId, x, y })
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
        <!-- Render Grid Cells as Drop Zones -->
        <div v-for="i in GRID_SIZE * GRID_SIZE" :key="'cell-'+i" 
             class="cell" 
             :class="{ 
                'player-half': Math.floor((i-1)/GRID_SIZE) >= 4, 
                'enemy-half': Math.floor((i-1)/GRID_SIZE) < 4,
                'highlight-drop': isDragging && Math.floor((i-1)/GRID_SIZE) >= 4, /* Only highlight player half for drop */
                'active-drop': dragOverCellIndex === (i-1) && Math.floor((i-1)/GRID_SIZE) >= 4
             }"
             @dragover="(e) => onDragOver(e, i-1)"
             @drop="(e) => onDrop(e, (i-1)%GRID_SIZE, Math.floor((i-1)/GRID_SIZE))">
        </div>
        
        <!-- Render Units -->
        <div v-for="unit in units" :key="unit.id" 
             class="unit" 
             :style="getStyle(unit)"
             :class="{ 'mine': unit.ownerId === myPlayerId }"
             :draggable="unit.ownerId === myPlayerId"
             @dragstart="(e) => onDragStart(e, unit)"
             @dragend="onDragEnd"
             @mouseenter="onUnitMouseEnter(unit.id)"
             @mouseleave="onUnitMouseLeave">
             
            <div class="hp-bar" :style="{ width: (unit.currentHealth / unit.maxHealth * 100) + '%' }"></div>
            <img :src="unit.image" class="unit-img" :alt="unit.name" />
            
            <!-- Tooltip -->
            <transition name="fade">
                <UnitTooltip v-if="hoveredUnitId === unit.id" :unit="unit" />
            </transition>
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
    width: 480px; 
    height: 480px;
    display: grid;
    grid-template-columns: repeat(8, 1fr);
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

