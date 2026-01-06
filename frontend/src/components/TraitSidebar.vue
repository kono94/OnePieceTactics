<script setup lang="ts">
import { computed, ref } from 'vue'
import { TRAIT_DATA, normalizeTraitId, type TraitDefinition, type TraitEffect } from '../data/traitData'

const props = defineProps<{
  units: any[] // Array of board units
}>()

const hoveredTraitId = ref<string | null>(null)

// Computed property to calculate trait states
const processedTraits = computed(() => {
    if (!props.units) return [];
    
    const uniqueUnits = new Set<string>();
    const traitsCount: Record<string, number> = {};

    props.units.forEach((u: any) => {
        // Guard against incomplete unit data
        if (!u.name) return;
        
        // Only count unique units (by name) for trait purposes
        if (!uniqueUnits.has(u.name)) {
            uniqueUnits.add(u.name);
            
            if (u.traits && Array.isArray(u.traits)) {
                u.traits.forEach((tName: string) => {
                    const tId = normalizeTraitId(tName);
                    traitsCount[tId] = (traitsCount[tId] || 0) + 1;
                });
            }
        }
    });

    // Convert to rich objects
    const list = Object.entries(traitsCount).map(([id, count]) => {
        const def = TRAIT_DATA[id];
        if (!def) return null;

        const activeEffect = getActiveEffect(def, count);
        const nextBreakpoint = getNextBreakpoint(def, count);
        
        let style = 'inactive';
        if (activeEffect) {
            style = activeEffect.style;
        }
        
        return {
            id,
            def,
            count,
            activeEffect,
            nextBreakpoint, // Can be null if maxed
            style
        };
    }).filter((item): item is NonNullable<typeof item> => item !== null);
    
    // Sort: High tier > Low tier, then Count desc
    list.sort((a, b) => {
        const score = (s: string) => {
             switch(s) {
                case 'prismatic': return 5;
                case 'gold': return 4;
                case 'silver': return 3;
                case 'bronze': return 2;
                default: return 1;
            }
        }
        const sA = score(a.style);
        const sB = score(b.style);
        if (sA !== sB) return sB - sA;
        return b.count - a.count;
    });
    
    return list;
})

function getActiveEffect(trait: TraitDefinition, count: number): TraitEffect | null {
    let active = null;
    // trait.effects is assumed sorted/ordered by minUnits ASC
    for (const effect of trait.effects) {
        if (count >= effect.minUnits) {
            active = effect;
        } else {
            // Once we fail a check (and if sorted), we could stop, 
            // but let's just checking all to find the max valid one.
        }
    }
    return active;
}

function getNextBreakpoint(trait: TraitDefinition, count: number): number | null {
    for (const effect of trait.effects) {
        if (effect.minUnits > count) {
            return effect.minUnits;
        }
    }
    return null;
}
</script>

<template>
  <div class="trait-sidebar">
      <div v-for="item in processedTraits" :key="item.id" 
           class="trait-item"
           :class="item.style"
           @mouseenter="hoveredTraitId = item.id"
           @mouseleave="hoveredTraitId = null">
           
           <div class="trait-icon" :style="{ backgroundColor: item.def.iconColor }">
               {{ item.def.name[0] }}
               <!-- Using first letter as icon placeholder -->
           </div>
           
           <div class="trait-info">
               <div class="trait-name">{{ item.def.name }}</div>
               <div class="trait-count">
                   {{ item.count }} / {{ item.nextBreakpoint || item.activeEffect?.minUnits || 'Max' }}
               </div>
           </div>

           <!-- Helper Tooltip -->
           <transition name="fade">
               <div v-if="hoveredTraitId === item.id" class="trait-tooltip">
                   <div class="tt-header">{{ item.def.name }}</div>
                   <div class="tt-desc">{{ item.def.description }}</div>
                   <div class="tt-effects">
                       <div v-for="(effect, i) in item.def.effects" :key="i"
                            class="tt-effect-row"
                            :class="{ 'active': item.count >= effect.minUnits }">
                           <span class="tt-meta">{{ effect.minUnits }}</span>
                           <span>{{ effect.description }}</span>
                       </div>
                   </div>
               </div>
           </transition>
      </div>
  </div>
</template>

<style scoped>
.trait-sidebar {
    position: absolute;
    left: 20px;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    gap: 8px;
    z-index: 100;
    pointer-events: none; /* Let clicks pass active areas */
}

.trait-item {
    pointer-events: auto; /* Enable hover/click on items */
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    background: rgba(15, 23, 42, 0.8);
    border: 1px solid #334155;
    border-radius: 6px;
    width: 60px; /* Collapsed view primarily */
    overflow: visible;
    transition: width 0.2s, background-color 0.2s;
    position: relative;
    cursor: default;
}
.trait-item:hover {
    width: 160px; /* Expand on hover if desired, or just show tooltip */
    background: rgba(15, 23, 42, 0.95);
}

/* Styles */
.trait-item.inactive { opacity: 0.6; border-color: #334155; }
.trait-item.bronze { border-color: #cd7f32; box-shadow: 0 0 5px rgba(205, 127, 50, 0.2); }
.trait-item.silver { border-color: #c0c0c0; box-shadow: 0 0 5px rgba(192, 192, 192, 0.2); }
.trait-item.gold { border-color: #ffd700; box-shadow: 0 0 10px rgba(255, 215, 0, 0.4); }
.trait-item.prismatic { border-color: #a855f7; box-shadow: 0 0 15px rgba(168, 85, 247, 0.5); }


.trait-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: bold;
    color: #1e293b;
    font-size: 14px;
    flex-shrink: 0;
}

.trait-info {
    display: flex;
    flex-direction: column;
    white-space: nowrap;
    opacity: 0; /* Hidden by default if collapsed */
    transition: opacity 0.2s;
}
.trait-item:hover .trait-info {
    opacity: 1;
}

.trait-name {
    font-size: 12px;
    font-weight: bold;
    color: white;
}
.trait-count {
    font-size: 10px;
    color: #94a3b8;
}

/* Tooltip */
.trait-tooltip {
    position: absolute;
    left: 100%;
    top: 0;
    margin-left: 10px;
    background: #1e293b;
    border: 1px solid #475569;
    padding: 12px;
    border-radius: 8px;
    width: 200px;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5);
    z-index: 200;
}
.tt-header {
    font-weight: bold;
    color: #eab308;
    margin-bottom: 4px;
}
.tt-desc {
    font-size: 12px;
    color: #cbd5e1;
    margin-bottom: 8px;
    font-style: italic;
}
.tt-effects {
    display: flex;
    flex-direction: column;
    gap: 4px;
}
.tt-effect-row {
    display: flex;
    gap: 8px;
    font-size: 11px;
    color: #64748b;
}
.tt-effect-row.active {
    color: white;
    font-weight: bold;
}
.tt-meta {
    background: #334155;
    padding: 1px 6px;
    border-radius: 4px;
    min-width: 20px;
    text-align: center;
}
.tt-effect-row.active .tt-meta {
    background: #eab308;
    color: black;
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
