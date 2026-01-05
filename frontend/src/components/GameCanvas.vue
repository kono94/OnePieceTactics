<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
    state: any
}>()

const units = computed(() => {
    if (!props.state || !props.state.players) return []
    let allUnits: any[] = []
    
    // Players is a Map/Object in JSON
    Object.values(props.state.players).forEach((player: any) => {
        if (player.board) {
            allUnits = allUnits.concat(player.board.map((u: any) => ({
                ...u,
                ownerId: player.playerId
            })))
        }
    })
    return allUnits
})

const GRID_SIZE = 8
const CELL_SIZE = 60

const getStyle = (unit: any) => {
    // Convert grid X/Y to pixel position
    // Assuming simple grid for now
    return {
        left: (unit.x * CELL_SIZE) + 'px',
        top: (unit.y * CELL_SIZE) + 'px',
        width: (CELL_SIZE - 4) + 'px',
        height: (CELL_SIZE - 4) + 'px',
        backgroundColor: getColor(unit.ownerId)
    }
}

const getColor = (id: string) => {
    // Simple hash for color
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
        hash = id.charCodeAt(i) + ((hash << 5) - hash);
    }
    const c = (hash & 0x00FFFFFF).toString(16).toUpperCase();
    return '#' + "00000".substring(0, 6 - c.length) + c;
}

</script>

<template>
  <div class="board-container">
    <div class="grid">
        <!-- Render Grid Lines -->
        <div v-for="i in GRID_SIZE * GRID_SIZE" :key="'cell-'+i" class="cell"></div>
        
        <!-- Render Units -->
        <div v-for="unit in units" :key="unit.id" class="unit" :style="getStyle(unit)">
            <div class="hp-bar" :style="{ width: (unit.currentHealth / unit.maxHealth * 100) + '%' }"></div>
            <span class="unit-name">{{ unit.name.substring(0, 2) }}</span>
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
    width: 480px; /* 8 * 60 */
    height: 480px;
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    background-color: #333;
    border: 2px solid #555;
}

.cell {
    border: 1px solid #444;
}

.unit {
    position: absolute;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: white;
    font-weight: bold;
    font-size: 12px;
    border: 2px solid white;
    transition: all 0.3s ease;
    box-shadow: 0 0 5px rgba(0,0,0,0.5);
    z-index: 10;
}

.unit-name {
    pointer-events: none;
    text-shadow: 1px 1px 2px black;
}

.hp-bar {
    position: absolute;
    top: -5px;
    left: 0;
    height: 4px;
    background-color: #ef4444;
    border-radius: 2px;
}
</style>
