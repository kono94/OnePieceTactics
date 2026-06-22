<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import CombatEffectsCanvas from './CombatEffectsCanvas.vue'
import { ULTIMATE_GALLERY_ROSTER, type UltimateGalleryUnit } from '../../data/ultimateGalleryRoster'
import { getAbilityConfig, getAttackConfig } from '../../data/animationConfig'
import type { AbilityDefinition, RenderedUnit } from '../../types'
import type { NormalizedCombatVisualEvent } from '../../types/combatEffects'
import { getUnitIconPath } from '../../utils/iconUtils'

const GRID_COLS = 9
const GRID_ROWS = 6
const CELL_SIZE = 78

const selectedId = ref('whitebeard_v1')
const starLevel = ref(3)
const autoReplay = ref(true)
const events = ref<NormalizedCombatVisualEvent[]>([])

let nextEventId = 1
let replayTimer: number | null = null

const selectedUnit = computed(() => ULTIMATE_GALLERY_ROSTER.find(unit => unit.id === selectedId.value) ?? ULTIMATE_GALLERY_ROSTER[0])
const selectedAbilityConfig = computed(() => getAbilityConfig(selectedUnit.value.id))
const selectedTargetIsAlly = computed(() => isSupportAbility(selectedUnit.value.abilityType))

const sourceUnit = computed(() => createRenderedUnit(selectedUnit.value, 'gallery-source', 2, 4, 'PLAYER', true))
const enemyUnit = computed(() => createRenderedUnit(createDummyUnit('Training Target', 'target_dummy_v1', 3), 'gallery-target', 6, 1, 'ENEMY', false))
const allyUnit = computed(() => createRenderedUnit(createDummyUnit('Ally Preview', 'luffy_v1', 3), 'gallery-ally', 5, 4, 'PLAYER', true))
const targetUnit = computed(() => selectedTargetIsAlly.value ? allyUnit.value : enemyUnit.value)
const renderedUnits = computed(() => selectedTargetIsAlly.value
    ? [sourceUnit.value, allyUnit.value]
    : [sourceUnit.value, enemyUnit.value]
)

const filteredRoster = computed(() => ULTIMATE_GALLERY_ROSTER)

function createDummyUnit(name: string, id: string, cost: number): UltimateGalleryUnit {
    return {
        id,
        name,
        cost,
        abilityType: 'DAMAGE',
        pattern: 'SINGLE',
        abilityName: 'Preview'
    }
}

function createRenderedUnit(unit: UltimateGalleryUnit, instanceId: string, x: number, y: number, ownerId: string, isMine: boolean): RenderedUnit {
    const ability: AbilityDefinition = {
        name: unit.abilityName,
        description: '',
        type: unit.abilityType,
        pattern: unit.pattern,
        range: [1, 2, 3],
        values: unit.abilityType === 'HEAL' ? [-80, -140, -220] : [120, 190, 300]
    }

    return {
        id: instanceId,
        definitionId: unit.id,
        name: unit.name,
        cost: unit.cost,
        maxHealth: 1000,
        currentHealth: 1000,
        mana: 100,
        maxMana: 100,
        attackDamage: 70,
        abilityPower: 100,
        armor: 30,
        magicResist: 30,
        attackSpeed: 0.8,
        range: 1,
        traits: [],
        items: [],
        x,
        y,
        visualX: x,
        visualY: y,
        starLevel: starLevel.value,
        ownerId,
        ability,
        activeAbility: null,
        stunSecondsRemaining: 0,
        atkBuff: unit.abilityType === 'BUFF_ATK' ? 1.2 : 1,
        spdBuff: unit.abilityType === 'BUFF_SPD' ? 1.2 : 1,
        isMine,
        image: getUnitIconPath(unit.id, 'onepiece')
    }
}

function pointFor(unit: RenderedUnit) {
    return {
        x: unit.visualX * CELL_SIZE + CELL_SIZE / 2,
        y: unit.visualY * CELL_SIZE + CELL_SIZE / 2
    }
}

function isSupportAbility(type: string) {
    return type === 'HEAL' || type.startsWith('BUFF')
}

function replayUltimate() {
    const source = sourceUnit.value
    const target = targetUnit.value
    const abilityConfig = getAbilityConfig(source.definitionId)
    const value = source.ability?.type === 'HEAL' ? -180 : (source.ability?.type.startsWith('BUFF') ? 0 : 240)

    events.value = [
        ...events.value.slice(-4),
        {
            id: nextEventId++,
            timestamp: performance.now(),
            type: 'SKILL',
            sourceId: source.id,
            targetId: target.id,
            value,
            skillName: abilityConfig.signature ?? source.ability?.name,
            source,
            target,
            start: pointFor(source),
            end: pointFor(target),
            definitionId: source.definitionId,
            attack: getAttackConfig(source.definitionId),
            ability: abilityConfig,
            pattern: source.ability?.pattern ?? 'SINGLE',
            starLevel: starLevel.value,
            intensity: 'ultimate',
            batchSize: 1,
            crowded: false
        }
    ]
}

function selectUnit(id: string) {
    selectedId.value = id
    window.setTimeout(replayUltimate, 80)
}

function scheduleReplay() {
    if (replayTimer !== null) {
        window.clearInterval(replayTimer)
        replayTimer = null
    }
    if (autoReplay.value) {
        replayTimer = window.setInterval(replayUltimate, 2200)
    }
}

watch([selectedId, starLevel], () => {
    window.setTimeout(replayUltimate, 80)
})

watch(autoReplay, scheduleReplay)

onMounted(() => {
    document.title = 'Ultimate Gallery'
    replayUltimate()
    scheduleReplay()
})

onUnmounted(() => {
    if (replayTimer !== null) window.clearInterval(replayTimer)
})
</script>

<template>
  <main class="ultimate-gallery">
    <section class="gallery-preview">
      <div class="preview-header">
        <div>
          <a href="#" class="back-link">Back to game</a>
          <h1>Ultimate Gallery</h1>
          <p>{{ selectedUnit.name }} - {{ selectedAbilityConfig.signature || selectedUnit.abilityName }}</p>
        </div>
        <div class="preview-controls">
          <button
            v-for="level in [1, 2, 3]"
            :key="level"
            class="star-button"
            :class="{ active: starLevel === level }"
            type="button"
            @click="starLevel = level"
          >
            {{ level }} Star
          </button>
          <button type="button" class="replay-button" @click="replayUltimate">Replay</button>
          <label class="auto-toggle">
            <input v-model="autoReplay" type="checkbox">
            Auto
          </label>
        </div>
      </div>

      <div class="board-shell">
        <div
          class="gallery-board"
          :style="{ width: `${GRID_COLS * CELL_SIZE}px`, height: `${GRID_ROWS * CELL_SIZE}px` }"
        >
          <div
            v-for="index in GRID_COLS * GRID_ROWS"
            :key="index"
            class="gallery-cell"
            :style="{
              width: `${CELL_SIZE}px`,
              height: `${CELL_SIZE}px`,
              left: `${((index - 1) % GRID_COLS) * CELL_SIZE}px`,
              top: `${Math.floor((index - 1) / GRID_COLS) * CELL_SIZE}px`
            }"
          />
          <CombatEffectsCanvas
            :events="events"
            :units="renderedUnits"
            :cell-size="CELL_SIZE"
            :grid-rows="GRID_ROWS"
            :grid-cols="GRID_COLS"
            phase="COMBAT"
          />
          <div
            v-for="unit in renderedUnits"
            :key="unit.id"
            class="gallery-unit"
            :class="{ ally: unit.isMine, enemy: !unit.isMine, selected: unit.id === 'gallery-source' }"
            :style="{
              width: `${CELL_SIZE - 12}px`,
              height: `${CELL_SIZE - 12}px`,
              left: `${unit.visualX * CELL_SIZE + 6}px`,
              top: `${unit.visualY * CELL_SIZE + 6}px`
            }"
          >
            <div class="unit-health" />
            <img :src="unit.image" :alt="unit.name" draggable="false">
            <span class="unit-label">{{ unit.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="roster-panel" aria-label="Ultimate roster">
      <button
        v-for="unit in filteredRoster"
        :key="unit.id"
        type="button"
        class="roster-card"
        :class="{ active: selectedId === unit.id }"
        @click="selectUnit(unit.id)"
      >
        <img :src="getUnitIconPath(unit.id, 'onepiece')" :alt="unit.name" draggable="false">
        <span class="roster-name">{{ unit.name }}</span>
        <span class="roster-meta">{{ getAbilityConfig(unit.id).signature || unit.abilityName }} · {{ unit.cost }}g</span>
      </button>
    </section>
  </main>
</template>

<style scoped>
.ultimate-gallery {
    height: 100vh;
    display: grid;
    grid-template-columns: minmax(760px, 1fr) 390px;
    grid-template-rows: minmax(0, 1fr);
    gap: 18px;
    padding: 18px;
    background: radial-gradient(circle at 30% 0%, #1f2937 0%, #0b1120 62%);
    color: #f8fafc;
    overflow: hidden;
}

.gallery-preview,
.roster-panel {
    min-height: 0;
    height: 100%;
}

.preview-header {
    min-height: 94px;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 18px;
    padding: 4px 4px 16px;
}

.back-link {
    color: #93c5fd;
    font-size: 13px;
    font-weight: 800;
    letter-spacing: 0.08em;
    text-decoration: none;
    text-transform: uppercase;
}

h1 {
    margin: 5px 0 3px;
    font-size: 34px;
    line-height: 1;
}

p {
    margin: 0;
    color: #cbd5e1;
    font-weight: 700;
}

.preview-controls {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    justify-content: flex-end;
    gap: 8px;
}

.star-button,
.replay-button {
    min-height: 38px;
    border: 1px solid #334155;
    border-radius: 8px;
    background: #111827;
    color: #e5e7eb;
    font-weight: 900;
    cursor: pointer;
}

.star-button {
    padding: 0 12px;
}

.star-button.active,
.replay-button {
    border-color: #fbbf24;
    background: #1d4ed8;
    color: #ffffff;
}

.auto-toggle {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    min-height: 38px;
    padding: 0 10px;
    border: 1px solid #334155;
    border-radius: 8px;
    background: #111827;
    color: #cbd5e1;
    font-weight: 800;
}

.board-shell {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 132px);
    border: 1px solid #1f3b63;
    border-radius: 8px;
    background: linear-gradient(180deg, rgba(15, 23, 42, 0.78), rgba(2, 6, 23, 0.94));
    box-shadow: inset 0 0 42px rgba(30, 64, 175, 0.18);
    overflow: auto;
}

.gallery-board {
    position: relative;
    flex: 0 0 auto;
    border: 3px solid #243b5a;
    border-radius: 8px;
    background: linear-gradient(180deg, rgba(53, 39, 55, 0.56) 0 50%, rgba(15, 23, 42, 0.88) 50% 100%);
    overflow: hidden;
}

.gallery-cell {
    position: absolute;
    border-right: 1px solid rgba(148, 163, 184, 0.12);
    border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}

.gallery-unit {
    position: absolute;
    z-index: 40;
    border: 2px solid #22c55e;
    border-radius: 50%;
    background: #0f172a;
    box-shadow: 0 0 16px rgba(34, 197, 94, 0.5);
}

.gallery-unit.enemy {
    border-color: #ef4444;
    box-shadow: 0 0 16px rgba(239, 68, 68, 0.45);
}

.gallery-unit.selected {
    border-color: #fbbf24;
    box-shadow: 0 0 22px rgba(251, 191, 36, 0.64);
}

.gallery-unit img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
    user-select: none;
}

.unit-health {
    position: absolute;
    top: -8px;
    left: 10%;
    width: 80%;
    height: 6px;
    border-radius: 999px;
    background: #22c55e;
}

.unit-label {
    position: absolute;
    left: 50%;
    top: 100%;
    width: 120px;
    transform: translateX(-50%);
    color: #e5e7eb;
    font-size: 11px;
    font-weight: 900;
    line-height: 1.1;
    text-align: center;
    text-shadow: 0 2px 8px #020617;
}

.roster-panel {
    display: grid;
    grid-template-columns: 1fr;
    align-content: start;
    gap: 8px;
    padding: 10px;
    border: 1px solid #334155;
    border-radius: 8px;
    background: rgba(15, 23, 42, 0.88);
    overflow: auto;
    overscroll-behavior: contain;
}

.roster-card {
    display: grid;
    grid-template-columns: 48px 1fr;
    grid-template-rows: auto auto;
    column-gap: 10px;
    align-items: center;
    min-height: 62px;
    padding: 7px;
    border: 1px solid transparent;
    border-radius: 8px;
    background: #111827;
    color: #f8fafc;
    text-align: left;
    cursor: pointer;
}

.roster-card.active {
    border-color: #fbbf24;
    background: #172554;
}

.roster-card img {
    grid-row: 1 / span 2;
    width: 48px;
    height: 48px;
    border-radius: 8px;
    object-fit: cover;
}

.roster-name {
    font-weight: 900;
}

.roster-meta {
    color: #94a3b8;
    font-size: 12px;
    font-weight: 700;
}

@media (max-width: 1120px) {
    .ultimate-gallery {
        grid-template-columns: 1fr;
        height: 100vh;
        overflow: auto;
    }

    .roster-panel {
        grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
        height: auto;
        max-height: none;
        overflow: visible;
    }
}
</style>
