<template>
  <div class="damage-report-wrapper" :class="{ 'is-collapsed': isCollapsed }">
    <!-- Toggle Button (Tab) -->
    <button 
      @click="isCollapsed = !isCollapsed"
      class="toggle-btn"
    >
      <div class="tab-label">
        <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
        </svg>
        <span>DAMAGE DEALT</span>
      </div>
    </button>

    <!-- Main Panel -->
    <div class="report-panel">
      <div class="header">
        <h2 class="header-title">Combat Stats</h2>
      </div>

      <div class="content custom-scrollbar">
        <div v-if="sortedEntries.length > 0" class="entries-list">
          <div v-for="entry in sortedEntries" :key="entry.unitId" class="entry-row">
            <div class="unit-icon">
               <img :src="entry.image" class="unit-img" />
            </div>
            <div class="unit-details">
                <div class="name-dmg-row">
                  <span class="unit-name">{{ entry.unitName }}</span>
                  <span class="dmg-val">{{ entry.damage.toLocaleString() }}</span>
                </div>
                <div class="dmg-bar-container">
                  <div 
                    class="dmg-bar"
                    :style="{ width: `${(entry.damage / maxDamage) * 100}%` }"
                  ></div>
                </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
            <p>Waiting for combat data...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { DamageEntry } from '../../types';

const props = defineProps<{
  damageLog: Record<string, DamageEntry> | null,
  myPlayerId?: string
}>();

const isCollapsed = ref(true);

const sortedEntries = computed(() => {
  if (!props.damageLog || !props.myPlayerId) return [];
  
  return Object.entries(props.damageLog)
    .filter(([_, data]) => data.ownerId === props.myPlayerId)
    .map(([unitId, data]) => ({
      unitId,
      unitName: data.unitName,
      damage: data.damage,
      image: `/assets/units/${data.definitionId}.png`
    }))
    .sort((a, b) => b.damage - a.damage);
});

const maxDamage = computed(() => {
  if (sortedEntries.value.length === 0) return 1;
  return Math.max(...sortedEntries.value.map(e => e.damage));
});
</script>

<style scoped>
.damage-report-wrapper {
  position: fixed;
  top: 100px;
  right: 0;
  bottom: 200px; 
  width: 260px;
  z-index: 1000;
  display: flex;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.damage-report-wrapper.is-collapsed {
  transform: translateX(260px);
}

.toggle-btn {
  position: absolute;
  left: -32px;
  top: 0;
  width: 32px;
  height: 140px;
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-right: none;
  border-radius: 8px 0 0 8px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: -4px 0 15px rgba(0,0,0,0.4);
}

.tab-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.tab-label span {
  writing-mode: vertical-rl;
  text-transform: uppercase;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
  white-space: nowrap;
}

.report-panel {
  width: 100%;
  height: 100%;
  background: #0f172a;
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
}

.header {
  padding: 10px 12px;
  background: rgba(0,0,0,0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-title {
  font-size: 12px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
  letter-spacing: 2px;
  margin: 0;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.entries-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.entry-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.unit-icon {
  width: 32px;
  height: 32px;
  background: #000;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.unit-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.unit-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.name-dmg-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
}

.unit-name {
  font-size: 11px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dmg-val {
  font-size: 12px;
  font-weight: 900;
  font-family: monospace;
  color: #fbbf24;
  flex-shrink: 0;
}

.dmg-bar-container {
  height: 6px;
  background: rgba(0,0,0,0.5);
  border-radius: 3px;
  overflow: hidden;
}

.dmg-bar {
  height: 100%;
  background: linear-gradient(90deg, #f97316, #ef4444);
  transition: width 0.5s ease-out;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: rgba(255,255,255,0.2);
  font-size: 10px;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 1px;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}
</style>
