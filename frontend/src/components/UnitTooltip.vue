<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
    unit: any
}>()

const stats = computed(() => {
    if (!props.unit) return {}
    return {
        hp: `${props.unit.currentHealth || 0}/${props.unit.maxHealth || 100}`,
        atk: props.unit.attackDamage || 0,
        spd: (props.unit.attackSpeed || 0).toFixed(2),
        range: props.unit.range || 0,
        mana: `${props.unit.mana || 0}/${props.unit.maxMana || 100}`
    }
})

const starLevel = computed(() => props.unit.starLevel || 1)
</script>

<template>
  <div class="unit-tooltip">
      <div class="header">
          <span class="name">{{ unit.name }}</span>
          <span class="stars">
              <span v-for="n in starLevel" :key="n">⭐</span>
          </span>
      </div>
      <div class="stats-grid">
          <div class="stat-row">
              <span class="label">HP:</span>
              <span class="value">{{ stats.hp }}</span>
          </div>
          <div class="stat-row">
              <span class="label">Mana:</span>
              <span class="value">{{ stats.mana }}</span>
          </div>
          <div class="stat-row">
              <span class="label">ATK:</span>
              <span class="value">{{ stats.atk }}</span>
          </div>
          <div class="stat-row">
              <span class="label">SPD:</span>
              <span class="value">{{ stats.spd }}</span>
          </div>
          <div class="stat-row">
              <span class="label">Range:</span>
              <span class="value">{{ stats.range }}</span>
          </div>
      </div>
      <div class="traits" v-if="unit.traits && unit.traits.length">
          <span v-for="trait in unit.traits" :key="trait" class="trait-tag">{{ trait }}</span>
      </div>
  </div>
</template>

<style scoped>
.unit-tooltip {
    position: absolute;
    bottom: 120%; /* Position above the unit */
    left: 50%;
    transform: translateX(-50%);
    background-color: rgba(15, 23, 42, 0.95);
    border: 1px solid #475569;
    padding: 8px;
    border-radius: 6px;
    color: white;
    width: 160px;
    z-index: 1000;
    pointer-events: none;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #334155;
    padding-bottom: 4px;
    margin-bottom: 4px;
}

.name {
    font-weight: bold;
    font-size: 0.9em;
    color: #ffd700;
}

.stars {
    font-size: 0.8em;
}

.stats-grid {
    display: flex;
    flex-direction: column;
    gap: 2px;
    font-size: 0.8em;
}

.stat-row {
    display: flex;
    justify-content: space-between;
}

.label {
    color: #94a3b8;
}

.value {
    font-weight: 500;
}

.traits {
    margin-top: 6px;
    display: flex;
    flex-wrap: wrap;
    gap: 2px;
}

.trait-tag {
    background-color: #334155;
    padding: 2px 4px;
    border-radius: 4px;
    font-size: 0.7em;
}
</style>
