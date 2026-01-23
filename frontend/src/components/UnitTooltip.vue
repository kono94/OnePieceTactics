<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
    unit: any,
    placement?: 'top' | 'bottom'
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
const ability = computed(() => props.unit.ability)
</script>

<template>
  <div class="unit-tooltip" :class="placement || 'top'">
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
      
      <div class="ability-section" v-if="ability">
          <div class="ability-header">
              <span class="ability-name">{{ ability.name }}</span>
          </div>
          <div class="ability-description">
              {{ ability.description }}
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
    left: 50%;
    transform: translateX(-20%);
    background-color: rgba(15, 23, 42, 0.95);
    border: 1px solid #475569;
    padding: 10px;
    border-radius: 8px;
    color: white;
    width: 200px;
    z-index: 1000;
    pointer-events: none;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.unit-tooltip.top {
    bottom: 125%;
}

.unit-tooltip.bottom {
    top: 125%;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #334155;
    padding-bottom: 6px;
    margin-bottom: 6px;
}

.name {
    font-weight: bold;
    font-size: 0.95em;
    color: #ffd700;
}

.stars {
    font-size: 0.8em;
}

.stats-grid {
    display: flex;
    flex-direction: column;
    gap: 3px;
    font-size: 0.85em;
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

.ability-section {
    margin-top: 8px;
    padding-top: 8px;
    border-top: 1px solid #334155;
}

.ability-header {
    margin-bottom: 4px;
}

.ability-name {
    font-weight: bold;
    font-size: 0.85em;
    color: #60a5fa;
    text-transform: uppercase;
}

.ability-description {
    font-size: 0.8em;
    color: #cbd5e1;
    line-height: 1.4;
    font-style: italic;
}

.traits {
    margin-top: 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
}

.trait-tag {
    background-color: #334155;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.75em;
    color: #e2e8f0;
}
</style>
