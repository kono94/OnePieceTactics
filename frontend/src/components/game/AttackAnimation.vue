<script setup lang="ts">
import { computed, onMounted } from 'vue'
import type { AttackType } from '../../data/animationConfig'

const props = defineProps<{
  type: 'attack' | 'ability'
  attackType?: AttackType
  pattern?: string
  startX: number
  startY: number
  endX: number
  endY: number
  color: string
  definitionId: string
}>()

const emit = defineEmits(['complete'])

const animationClass = computed(() => {
  if (props.type === 'attack') {
    return `attack-${props.attackType || 'punch'}`
  }
  // Abilities use pattern-based animations
  switch (props.pattern) {
    case 'LINE': return 'ability-line'
    case 'SURROUND': return 'ability-surround'
    case 'SINGLE': 
    default: return 'ability-single'
  }
})

// Calculate travel direction for projectiles/lines
const travelStyle = computed(() => {
  const dx = props.endX - props.startX
  const dy = props.endY - props.startY
  const distance = Math.sqrt(dx * dx + dy * dy)
  const angle = Math.atan2(dy, dx) * (180 / Math.PI)
  
  return {
    '--travel-x': `${dx * 60}px`,
    '--travel-y': `${dy * 60}px`,
    '--travel-distance': `${distance * 60}px`,
    '--angle': `${angle}deg`,
    '--color': props.color
  }
})

onMounted(() => {
  // Auto-remove after animation completes
  const duration = props.type === 'ability' ? 600 : 300
  setTimeout(() => emit('complete'), duration)
})
</script>

<template>
  <div 
    class="attack-animation" 
    :class="animationClass"
    :style="[
      { left: startX * 60 + 30 + 'px', top: startY * 60 + 30 + 'px' },
      travelStyle
    ]"
  >
    <!-- Punch/Slash impact -->
    <div v-if="attackType === 'punch' || attackType === 'slash'" class="impact-ring"></div>
    
    <!-- Projectile -->
    <div v-if="attackType === 'projectile'" class="projectile-orb"></div>
    
    <!-- Ability effects -->
    <div v-if="type === 'ability'" class="ability-effect"></div>
  </div>
</template>

<style scoped>
.attack-animation {
  position: absolute;
  pointer-events: none;
  z-index: 100;
  transform: translate(-50%, -50%);
}

/* === PUNCH ANIMATION === */
.attack-punch .impact-ring {
  width: 30px;
  height: 30px;
  border: 3px solid var(--color, #f59e0b);
  border-radius: 50%;
  animation: punch-impact 0.3s ease-out forwards;
}

@keyframes punch-impact {
  0% { transform: scale(0.5); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
  100% { transform: scale(1.5) translate(var(--travel-x), var(--travel-y)); opacity: 0; }
}

/* === SLASH ANIMATION === */
.attack-slash .impact-ring {
  width: 40px;
  height: 40px;
  border: 2px solid var(--color, #22c55e);
  border-radius: 0;
  animation: slash-arc 0.3s ease-out forwards;
  transform-origin: center;
}

@keyframes slash-arc {
  0% { transform: rotate(0deg) scaleX(0.2); opacity: 1; }
  50% { transform: rotate(45deg) scaleX(1); opacity: 0.8; }
  100% { transform: rotate(90deg) scaleX(0.5); opacity: 0; }
}

/* === PROJECTILE ANIMATION === */
.attack-projectile .projectile-orb {
  width: 12px;
  height: 12px;
  background: var(--color, #38bdf8);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--color, #38bdf8);
  animation: projectile-fly 0.3s ease-out forwards;
}

@keyframes projectile-fly {
  0% { transform: translate(0, 0); opacity: 1; }
  100% { transform: translate(var(--travel-x), var(--travel-y)); opacity: 0.5; }
}

/* === ABILITY: SINGLE TARGET === */
.ability-single .ability-effect {
  width: 50px;
  height: 50px;
  border: 4px solid var(--color, #fbbf24);
  border-radius: 50%;
  animation: ability-single-burst 0.6s ease-out forwards;
}

@keyframes ability-single-burst {
  0% { transform: scale(0.3); opacity: 1; box-shadow: 0 0 0 var(--color); }
  50% { transform: scale(1.2) translate(var(--travel-x), var(--travel-y)); opacity: 0.8; box-shadow: 0 0 20px var(--color); }
  100% { transform: scale(1.5) translate(var(--travel-x), var(--travel-y)); opacity: 0; }
}

/* === ABILITY: LINE === */
.ability-line .ability-effect {
  width: var(--travel-distance, 120px);
  height: 8px;
  background: linear-gradient(90deg, var(--color, #38bdf8), transparent);
  transform-origin: left center;
  transform: rotate(var(--angle, 0deg));
  animation: ability-line-beam 0.6s ease-out forwards;
}

@keyframes ability-line-beam {
  0% { width: 0; opacity: 1; }
  30% { width: var(--travel-distance, 120px); opacity: 1; }
  100% { width: var(--travel-distance, 120px); opacity: 0; }
}

/* === ABILITY: SURROUND === */
.ability-surround .ability-effect {
  width: 80px;
  height: 80px;
  border: 3px solid var(--color, #fbbf24);
  border-radius: 50%;
  animation: ability-surround-ring 0.6s ease-out forwards;
}

@keyframes ability-surround-ring {
  0% { transform: scale(0.3); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.7; box-shadow: 0 0 30px var(--color); }
  100% { transform: scale(2); opacity: 0; }
}
</style>
