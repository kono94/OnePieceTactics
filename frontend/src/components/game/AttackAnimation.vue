<script setup lang="ts">
import { computed, onMounted } from 'vue'
import type { AttackType, AbilityEffectStyle } from '../../data/animationConfig'

const props = defineProps<{
  type: 'attack' | 'ability'
  attackType?: AttackType
  effectStyle?: AbilityEffectStyle
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
  // Abilities use pattern-based animations or custom styles
  const style = props.effectStyle || 'DEFAULT'
  if (style !== 'DEFAULT') return `ability-${style.toLowerCase().replace('_', '-')}`

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
    '--travel-x': `${dx * 55}px`,
    '--travel-y': `${dy * 55}px`,
    '--travel-distance': `${distance * 55}px`,
    '--angle': `${angle}deg`,
    '--color': props.color,
    '--color-glow': props.color + '88'
  }
})

onMounted(() => {
  // Auto-remove after animation completes
  const duration = props.type === 'ability' ? 800 : 300
  setTimeout(() => emit('complete'), duration)
})
</script>

<template>
  <div 
    class="attack-animation" 
    :class="animationClass"
    :style="[
      { left: startX * 55 + 27 + 'px', top: startY * 55 + 27 + 'px' },
      travelStyle
    ]"
  >
    <!-- Basic Attack Effects -->
    <div v-if="type === 'attack'" class="impact-container">
        <div v-if="attackType === 'punch' || attackType === 'kick'" class="impact-ring"></div>
        <div v-if="attackType === 'slash'" class="slash-line"></div>
        <div v-if="attackType === 'blunt'" class="blunt-wave"></div>
        <div v-if="attackType === 'projectile'" class="projectile-orb"></div>
    </div>
    
    <!-- Ability Effects -->
    <div v-if="type === 'ability'" class="ability-container">
        <!-- Generic Patterns -->
        <div v-if="!effectStyle || effectStyle === 'DEFAULT'" class="ability-effect"></div>
        
        <!-- Premium Styles -->
        <div v-if="effectStyle === 'BEAM_HEAVY'" class="heavy-beam"></div>
        <div v-if="effectStyle === 'MAGMA_RAIN'" class="magma-rain">
            <div v-for="i in 5" :key="i" class="magma-drop"></div>
        </div>
        <div v-if="effectStyle === 'QUAKE'" class="quake-effect">
            <div class="crack"></div>
            <div class="crack-2"></div>
        </div>
        <div v-if="effectStyle === 'SOUL_SPIRAL'" class="soul-spiral">
            <div class="soul-orb"></div>
            <div class="soul-orb-2"></div>
        </div>
        <div v-if="effectStyle === 'DRAGON_ROAR'" class="dragon-roar"></div>
    </div>
  </div>
</template>

<style scoped>
.attack-animation {
  position: absolute;
  pointer-events: none;
  z-index: 100;
  transform: translate(-50%, -50%);
}

/* === ATTACK: PUNCH/KICK === */
.attack-punch .impact-ring, .attack-kick .impact-ring {
  width: 30px;
  height: 30px;
  border: 3px solid var(--color);
  border-radius: 50%;
  animation: impact-pop 0.3s ease-out forwards;
}

.attack-kick .impact-ring {
  border-width: 5px;
  box-shadow: 0 0 15px var(--color);
}

@keyframes impact-pop {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.8) translate(var(--travel-x), var(--travel-y)); opacity: 0; }
}

/* === ATTACK: SLASH === */
.attack-slash .slash-line {
  width: 50px;
  height: 4px;
  background: var(--color);
  box-shadow: 0 0 10px var(--color);
  transform: rotate(var(--angle, 0deg));
  animation: slash-swipe 0.3s ease-out forwards;
}

@keyframes slash-swipe {
  0% { transform: scaleX(0) rotate(var(--angle)); opacity: 1; }
  50% { transform: scaleX(1.5) rotate(var(--angle)); opacity: 1; }
  100% { transform: scaleX(0.5) rotate(var(--angle)) translate(var(--travel-x), var(--travel-y)); opacity: 0; }
}

/* === ATTACK: BLUNT === */
.attack-blunt .blunt-wave {
  width: 40px;
  height: 40px;
  border: 4px double var(--color);
  border-radius: 10px;
  animation: blunt-hit 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28) forwards;
}

@keyframes blunt-hit {
  0% { transform: scale(0.2); opacity: 1; }
  100% { transform: scale(2.2); opacity: 0; }
}

/* === ATTACK: PROJECTILE === */
.attack-projectile .projectile-orb {
  width: 14px;
  height: 14px;
  background: var(--color);
  border-radius: 50%;
  box-shadow: 0 0 15px var(--color);
  animation: projectile-fly 0.4s ease-in forwards;
}

@keyframes projectile-fly {
  0% { transform: translate(0, 0); opacity: 1; }
  100% { transform: translate(var(--travel-x), var(--travel-y)); opacity: 0.8; }
}

/* === ABILITY: GENERIC === */
.ability-single .ability-effect, .ability-surround .ability-effect {
  width: 60px;
  height: 60px;
  border: 4px solid var(--color);
  border-radius: 50%;
  animation: ability-pulse 0.6s ease-out forwards;
}

.ability-surround .ability-effect { transform: scale(2); }

@keyframes ability-pulse {
  0% { transform: scale(0.5); opacity: 1; box-shadow: 0 0 0 var(--color); }
  100% { transform: scale(2); opacity: 0; box-shadow: 0 0 40px var(--color); }
}

.ability-line .ability-effect {
  width: var(--travel-distance, 120px);
  height: 12px;
  background: linear-gradient(90deg, var(--color), transparent);
  transform-origin: left center;
  transform: rotate(var(--angle, 0deg));
  animation: beam-fire 0.6s ease-out forwards;
}

@keyframes beam-fire {
  0% { width: 0; opacity: 1; }
  50% { width: var(--travel-distance); opacity: 1; }
  100% { width: var(--travel-distance); opacity: 0; }
}

/* === PREMIUM: HEAVY BEAM === */
.ability-beam-heavy .heavy-beam {
  width: var(--travel-distance);
  height: 24px;
  background: white;
  box-shadow: 0 0 30px var(--color), 0 0 60px var(--color);
  transform-origin: left center;
  transform: rotate(var(--angle, 0deg));
  animation: beam-heavy-fire 0.8s ease-out forwards;
}

@keyframes beam-heavy-fire {
  0% { transform: scaleY(0.1) rotate(var(--angle)); opacity: 1; }
  20% { transform: scaleY(1.5) rotate(var(--angle)); opacity: 1; }
  100% { transform: scaleY(1) rotate(var(--angle)); opacity: 0; }
}

/* === PREMIUM: MAGMA RAIN === */
.magma-rain { position: relative; }
.magma-drop {
  position: absolute;
  width: 15px;
  height: 15px;
  background: var(--color);
  border-radius: 50%;
  filter: blur(2px);
  box-shadow: 0 0 10px var(--color);
  animation: magma-fall 0.8s ease-in forwards;
}
.magma-drop:nth-child(1) { left: -20px; animation-delay: 0s; }
.magma-drop:nth-child(2) { left: 0px; animation-delay: 0.1s; }
.magma-drop:nth-child(3) { left: 20px; animation-delay: 0.2s; }
.magma-drop:nth-child(4) { left: -10px; top: -20px; animation-delay: 0.15s; }
.magma-drop:nth-child(5) { left: 10px; top: -20px; animation-delay: 0.25s; }

@keyframes magma-fall {
  0% { transform: translateY(-100px) scale(1); opacity: 0; }
  30% { opacity: 1; }
  100% { transform: translateY(var(--travel-y)) scale(1.5); opacity: 0; }
}

/* === PREMIUM: QUAKE === */
.quake-effect .crack {
  position: absolute;
  width: 100px;
  height: 2px;
  background: white;
  box-shadow: 0 0 15px var(--color);
  transform: rotate(45deg);
  animation: crack-spread 0.6s ease-out forwards;
}
.quake-effect .crack-2 {
  position: absolute;
  width: 80px;
  height: 2px;
  background: white;
  box-shadow: 0 0 15px var(--color);
  transform: rotate(-30deg);
  animation: crack-spread 0.6s ease-out 0.1s forwards;
}

@keyframes crack-spread {
  0% { transform: scaleX(0) rotate(inherit); opacity: 1; }
  100% { transform: scaleX(1.5) rotate(inherit); opacity: 0; }
}

/* === PREMIUM: SOUL SPIRAL === */
.soul-spiral { position: relative; }
.soul-orb, .soul-orb-2 {
  position: absolute;
  width: 15px;
  height: 15px;
  background: var(--color);
  border-radius: 50%;
  box-shadow: 0 0 15px var(--color);
  filter: blur(1px);
}
.soul-orb { animation: soul-orbit 0.8s linear forwards; }
.soul-orb-2 { animation: soul-orbit 0.8s linear 0.2s reverse forwards; }

@keyframes soul-orbit {
  0% { transform: rotate(0deg) translateX(30px); opacity: 1; }
  100% { transform: rotate(720deg) translateX(0px); opacity: 0; }
}

/* === PREMIUM: DRAGON ROAR === */
.dragon-roar {
  width: 100px;
  height: 80px;
  border: 8px solid var(--color);
  border-bottom: none;
  border-radius: 100px 100px 0 0;
  animation: roar-wave 0.8s ease-out forwards;
}

@keyframes roar-wave {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(3); opacity: 0; filter: blur(5px); }
}
</style>
