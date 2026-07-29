<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue'
import type { EmergencyDropPayload } from '../types'

const props = defineProps<{
  phase: string,
  emergencyDrop?: EmergencyDropPayload | null,
  suppressPlanningAnnouncement?: boolean
}>()

const showAnnouncement = ref(false)
const currentText = ref('')
const currentSubtitle = ref('')
const announcementMode = ref<'planning' | 'combat' | 'emergency'>('planning')
let planningTimer: number | null = null
let hideTimer: number | null = null
let animationFrame: number | null = null

const clearPlanningTimer = () => {
    if (planningTimer !== null) {
        window.clearTimeout(planningTimer)
        planningTimer = null
    }
}

const clearPresentationTimers = () => {
    if (hideTimer !== null) {
        window.clearTimeout(hideTimer)
        hideTimer = null
    }
    if (animationFrame !== null) {
        cancelAnimationFrame(animationFrame)
        animationFrame = null
    }
}

watch(() => props.phase, (newPhase, oldPhase) => {
    if (newPhase === oldPhase) return

    if (newPhase === 'PLANNING') {
        schedulePlanningAnnouncement()
    } else if (newPhase === 'COMBAT') {
        clearPlanningTimer()
        currentText.value = 'BATTLE START'
        currentSubtitle.value = ''
        announcementMode.value = 'combat'
        triggerAnimation()
    } else {
        clearPlanningTimer()
        showAnnouncement.value = false
    }
}, { immediate: true })

watch(() => props.suppressPlanningAnnouncement, (shouldSuppress) => {
    if (!shouldSuppress) return
    clearPlanningTimer()
    if (announcementMode.value === 'planning') {
        showAnnouncement.value = false
    }
})

watch(() => props.emergencyDrop, (drop, previousDrop) => {
    if (!drop || drop.dropId === previousDrop?.dropId) return

    clearPlanningTimer()
    currentText.value = 'EMERGENCY DROP'
    currentSubtitle.value = `${drop.orbIds.length} bonus ${drop.orbIds.length === 1 ? 'orb' : 'orbs'} deployed`
    announcementMode.value = 'emergency'
    triggerAnimation(3800)
}, { immediate: true })

function schedulePlanningAnnouncement() {
    clearPlanningTimer()
    if (props.suppressPlanningAnnouncement || props.emergencyDrop) return

    planningTimer = window.setTimeout(() => {
        planningTimer = null
        if (props.phase !== 'PLANNING' || props.suppressPlanningAnnouncement || props.emergencyDrop) return
        currentText.value = 'PLANNING PHASE'
        currentSubtitle.value = ''
        announcementMode.value = 'planning'
        triggerAnimation()
    }, 150)
}

function triggerAnimation(duration = 3000) {
    clearPresentationTimers()
    showAnnouncement.value = false
    animationFrame = requestAnimationFrame(() => {
        animationFrame = null
        showAnnouncement.value = true
        hideTimer = window.setTimeout(() => {
            showAnnouncement.value = false
            hideTimer = null
        }, duration)
    })
}

onUnmounted(() => {
    clearPlanningTimer()
    clearPresentationTimers()
})
</script>

<template>
  <div class="phase-layer">
      <transition name="phase-anim">
          <div v-if="showAnnouncement"
               class="announcement-container"
               :class="`${announcementMode}-mode`"
               role="status"
               aria-live="assertive"
               aria-atomic="true">
              
              <!-- Combat Visuals -->
              <div v-if="announcementMode === 'combat'" class="combat-wrapper">
                  <div class="sword-left">⚔️</div>
                  <div class="text-content">
                      <h1 class="glitch" :data-text="currentText">{{ currentText }}</h1>
                  </div>
                  <div class="sword-right">⚔️</div>
              </div>

              <!-- Emergency Drop Visuals -->
              <div v-else-if="announcementMode === 'emergency'" class="emergency-wrapper">
                  <div class="emergency-beacon" aria-hidden="true">◆</div>
                  <div class="emergency-copy">
                      <p class="emergency-kicker">Round {{ emergencyDrop?.round }}</p>
                      <h1>{{ currentText }}</h1>
                      <p>{{ currentSubtitle }}</p>
                  </div>
                  <div class="emergency-beacon" aria-hidden="true">◆</div>
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
    font-family: var(--app-font-family);
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
    font-family: var(--app-font-family);
}

@keyframes slide-down {
    0% { transform: translateY(-50px); opacity: 0; }
    100% { transform: translateY(15vh); opacity: 1; } /* Position slightly down from top */
}

/* === EMERGENCY DROP STYLES === */
.emergency-wrapper {
    position: relative;
    display: flex;
    align-items: center;
    gap: 24px;
    max-width: min(680px, calc(100vw - 40px));
    padding: 20px 34px;
    border: 2px solid rgba(245, 158, 11, 0.92);
    border-radius: 18px;
    background:
        linear-gradient(135deg, rgba(69, 26, 3, 0.94), rgba(15, 23, 42, 0.96) 58%, rgba(112, 26, 117, 0.9));
    box-shadow:
        0 0 0 5px rgba(245, 158, 11, 0.12),
        0 20px 70px rgba(0, 0, 0, 0.62),
        0 0 42px rgba(217, 70, 239, 0.3);
    animation: emergency-arrival 0.7s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.emergency-copy {
    min-width: 0;
    text-align: center;
}

.emergency-copy h1 {
    margin: 1px 0 5px;
    color: #fbbf24;
    font-family: var(--app-font-family);
    font-size: clamp(2rem, 5vw, 4.25rem);
    font-weight: 950;
    letter-spacing: 0.08em;
    line-height: 0.95;
    text-shadow: 0 0 22px rgba(245, 158, 11, 0.66);
}

.emergency-copy p {
    margin: 0;
    color: #fdf4ff;
    font-size: clamp(0.85rem, 1.8vw, 1.1rem);
    font-weight: 800;
    letter-spacing: 0.08em;
    text-transform: uppercase;
}

.emergency-copy .emergency-kicker {
    color: #f0abfc;
    font-size: 0.75rem;
}

.emergency-beacon {
    flex: 0 0 auto;
    color: #f59e0b;
    font-size: clamp(1.4rem, 4vw, 2.5rem);
    filter: drop-shadow(0 0 10px rgba(245, 158, 11, 0.9));
    animation: emergency-beacon 0.8s ease-in-out infinite alternate;
}

@keyframes emergency-arrival {
    0% { opacity: 0; transform: translateY(-70px) scale(0.84); }
    65% { opacity: 1; transform: translateY(8px) scale(1.025); }
    100% { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes emergency-beacon {
    from { opacity: 0.45; transform: scale(0.82) rotate(0deg); }
    to { opacity: 1; transform: scale(1.12) rotate(45deg); }
}

@media (prefers-reduced-motion: reduce) {
    .phase-anim-enter-active,
    .phase-anim-leave-active {
        transition-duration: 0.01ms;
    }

    .combat-wrapper,
    .sword-left,
    .sword-right,
    .glitch::before,
    .glitch::after,
    .planning-wrapper,
    .emergency-wrapper,
    .emergency-beacon {
        animation: none;
    }
}

</style>
