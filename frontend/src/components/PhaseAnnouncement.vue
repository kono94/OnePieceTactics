<script setup lang="ts">
import { ref, watch, computed } from 'vue'

const props = defineProps<{
  phase: string
}>()

const showAnnouncement = ref(false)
const currentText = ref('')
const isCombat = ref(false)

// Watch for phase changes to trigger animations
watch(() => props.phase, (newPhase, oldPhase) => {
    if (newPhase === oldPhase) return

    if (newPhase === 'PLANNING') {
        currentText.value = 'PLANNING PHASE'
        isCombat.value = false
        triggerAnimation()
    } else if (newPhase === 'COMBAT') {
        currentText.value = 'BATTLE START'
        isCombat.value = true
        triggerAnimation()
    } else {
        // Other phases if any (e.g., DROPPING, END)
        showAnnouncement.value = false
    }
}, { immediate: true }) // Check immediately on load too

function triggerAnimation() {
    showAnnouncement.value = false
    // Small delay to allow reset if rapid changes
    requestAnimationFrame(() => {
        showAnnouncement.value = true
        // Auto hide after animation duration
        // Planning: 2s slide in/out
        // Combat: 2.5s big splash
        setTimeout(() => {
            showAnnouncement.value = false
        }, 3000)
    })
}
</script>

<template>
  <div class="phase-layer">
      <transition name="phase-anim">
          <div v-if="showAnnouncement" class="announcement-container" :class="{ 'combat-mode': isCombat }">
              
              <!-- Combat Visuals -->
              <div v-if="isCombat" class="combat-wrapper">
                  <div class="sword-left">⚔️</div>
                  <div class="text-content">
                      <h1 class="glitch" :data-text="currentText">{{ currentText }}</h1>
                  </div>
                  <div class="sword-right">⚔️</div>
              </div>

              <!-- Planning Visuals -->
              <div v-else class="planning-wrapper">
                  <div class="planning-bar">
                      <span class="planning-text">{{ currentText }}</span>
                  </div>
              </div>

          </div>
      </transition>
  </div>
</template>

<style scoped>
.phase-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none; /* Crucial: clicks pass through */
    z-index: 1000;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
}

/* --- animations --- */

.phase-anim-enter-active,
.phase-anim-leave-active {
    transition: opacity 0.3s;
}
.phase-anim-enter-from,
.phase-anim-leave-to {
    opacity: 0;
}

/* === COMBAT STYLES === */
.combat-wrapper {
    display: flex;
    align-items: center;
    gap: 20px;
    animation: combat-pop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

.combat-mode h1 {
    font-size: 5rem;
    font-weight: 900;
    color: #ef4444; /* Red */
    text-shadow: 0 0 10px rgba(0,0,0,0.8), 0 0 20px rgba(239, 68, 68, 0.6);
    margin: 0;
    text-transform: uppercase;
    letter-spacing: 5px;
    font-family: 'Impact', fantasy, sans-serif;
    transform: skew(-10deg);
}

.sword-left, .sword-right {
    font-size: 4rem;
    animation: clash 0.6s ease-in-out;
}
.sword-left {
    transform: scaleX(-1) rotate(-45deg); /* Mirror it */
}
.sword-right {
    transform: rotate(-45deg);
}

@keyframes combat-pop {
    0% { transform: scale(0.5); opacity: 0; }
    50% { transform: scale(1.2); opacity: 1; }
    100% { transform: scale(1); opacity: 1; }
}

@keyframes clash {
    0% { opacity: 0; transform: translateX(-50px) rotate(-90deg); }
    50% { opacity: 1; transform: translateX(0) rotate(-45deg); }
    70% { transform: translateX(10px) rotate(-45deg); } /* Bounce back slightly */
    100% { transform: translateX(0) rotate(-45deg); }
}

/* Glitch effect for combat text */
.glitch {
  position: relative;
}
.glitch::before,
.glitch::after {
  content: attr(data-text);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #0f172a00; /* transparent */
}
.glitch::before {
  left: 2px;
  text-shadow: -1px 0 #00ffff;
  clip: rect(24px, 550px, 90px, 0);
  animation: glitch-anim-2 3s infinite linear alternate-reverse;
}
.glitch::after {
  left: -2px;
  text-shadow: -1px 0 #ff00ff;
  clip: rect(85px, 550px, 140px, 0);
  animation: glitch-anim 2s infinite linear alternate-reverse;
}
@keyframes glitch-anim {
  0% { clip: rect(10px, 9999px, 30px, 0); }
  20% { clip: rect(80px, 9999px, 100px, 0); }
  40% { clip: rect(10px, 9999px, 110px, 0); }
  60% { clip: rect(60px, 9999px, 20px, 0); }
  80% { clip: rect(40px, 9999px, 60px, 0); }
  100% { clip: rect(120px, 9999px, 10px, 0); }
}
@keyframes glitch-anim-2 {
  0% { clip: rect(120px, 9999px, 140px, 0); }
  20% { clip: rect(10px, 9999px, 120px, 0); }
  40% { clip: rect(60px, 9999px, 10px, 0); }
  60% { clip: rect(10px, 9999px, 60px, 0); }
  80% { clip: rect(90px, 9999px, 20px, 0); }
  100% { clip: rect(30px, 9999px, 10px, 0); }
}

/* === PLANNING STYLES === */
.planning-wrapper {
    width: 100%;
    display: flex;
    justify-content: center;
    /* Slide in from top */
    animation: slide-down 0.5s ease-out forwards;
}

.planning-bar {
    background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.8), transparent);
    width: 100%;
    padding: 20px 0;
    text-align: center;
    backdrop-filter: blur(2px);
}

.planning-text {
    font-size: 2.5rem;
    font-weight: 700;
    color: #fff;
    text-shadow: 0 0 10px rgba(59, 130, 246, 0.8);
    letter-spacing: 8px;
    text-transform: uppercase;
    font-family: 'Segoe UI', sans-serif;
}

@keyframes slide-down {
    0% { transform: translateY(-50px); opacity: 0; }
    100% { transform: translateY(15vh); opacity: 1; } /* Position slightly down from top */
}

</style>
