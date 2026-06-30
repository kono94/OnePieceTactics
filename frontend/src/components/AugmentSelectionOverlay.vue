<script setup lang="ts">
import { computed, ref } from 'vue'
import type { AugmentOffer, AugmentTier } from '../types'

const props = defineProps<{
  choices: AugmentOffer[]
}>()

const emit = defineEmits<{
  select: [augmentId: string]
}>()

const selectedId = ref<string | null>(null)

const tier = computed<AugmentTier>(() => props.choices[0]?.tier ?? 'SILVER')

const tierTitle = computed(() => {
    return tier.value.charAt(0) + tier.value.slice(1).toLowerCase()
})

function selectAugment(augmentId: string) {
    if (selectedId.value) return
    selectedId.value = augmentId
    emit('select', augmentId)
}

function imagePath(choice: AugmentOffer): string {
    return choice.image || '/assets/augments/placeholder.svg'
}
</script>

<template>
  <div class="augment-layer" :class="`tier-${tier.toLowerCase()}`">
    <div class="augment-shell">
      <div class="augment-heading">
        <span class="tier-label">{{ tierTitle }} Augment</span>
        <h2>Choose Your Power</h2>
      </div>

      <div class="augment-grid">
        <button
          v-for="choice in choices"
          :key="choice.id"
          class="augment-card"
          :class="{ selected: selectedId === choice.id }"
          type="button"
          :disabled="selectedId !== null"
          :aria-label="`Select ${choice.name}`"
          @click="selectAugment(choice.id)">
          <span class="card-shine"></span>
          <span class="image-frame">
            <img :src="imagePath(choice)" :alt="choice.name" draggable="false" />
          </span>
          <span class="card-content">
            <span class="card-tier">{{ choice.tier }}</span>
            <span class="card-name">{{ choice.name }}</span>
            <span class="card-description">{{ choice.description }}</span>
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.augment-layer {
    position: fixed;
    inset: 0;
    z-index: 90;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 32px;
    background:
        radial-gradient(circle at 50% 12%, rgba(255, 255, 255, 0.18), transparent 24%),
        rgba(2, 6, 23, 0.86);
    backdrop-filter: blur(14px);
}

.augment-shell {
    width: min(1180px, 100%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 26px;
}

.augment-heading {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    text-align: center;
}

.tier-label {
    padding: 6px 16px;
    border: 1px solid var(--augment-border);
    border-radius: 999px;
    color: var(--augment-soft);
    background: rgba(15, 23, 42, 0.7);
    font-size: 13px;
    font-weight: 900;
    letter-spacing: 1.5px;
    text-transform: uppercase;
}

h2 {
    margin: 0;
    color: #f8fafc;
    font-size: 48px;
    line-height: 1;
    letter-spacing: 0;
}

.augment-grid {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 24px;
}

.augment-card {
    position: relative;
    min-height: 470px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    border: 3px solid var(--augment-border);
    border-radius: 8px;
    color: #f8fafc;
    background:
        linear-gradient(180deg, var(--augment-card-top), rgba(15, 23, 42, 0.98)),
        #0f172a;
    box-shadow:
        0 0 0 1px rgba(255, 255, 255, 0.12) inset,
        0 20px 60px rgba(0, 0, 0, 0.45),
        0 0 34px var(--augment-glow);
    cursor: pointer;
    transform: translateZ(0);
    transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.augment-card:hover,
.augment-card:focus-visible {
    z-index: 2;
    transform: scale(1.07);
    box-shadow:
        0 0 0 1px rgba(255, 255, 255, 0.18) inset,
        0 26px 80px rgba(0, 0, 0, 0.55),
        0 0 54px var(--augment-glow-strong);
    outline: none;
}

.augment-card:disabled {
    cursor: default;
}

.augment-card.selected {
    transform: scale(1.04);
    filter: brightness(1.15);
}

.card-shine {
    position: absolute;
    inset: -40% -80%;
    z-index: 1;
    background: linear-gradient(110deg, transparent 35%, var(--augment-shine), transparent 62%);
    transform: translateX(-34%);
    animation: shine-sweep 3.2s ease-in-out infinite;
    pointer-events: none;
}

.image-frame {
    position: relative;
    z-index: 2;
    height: 235px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 28px 28px 14px;
}

.image-frame::before {
    content: "";
    position: absolute;
    width: 164px;
    height: 164px;
    border-radius: 50%;
    background: radial-gradient(circle, var(--augment-glow-strong), transparent 70%);
    filter: blur(3px);
}

.image-frame img {
    position: relative;
    width: 170px;
    height: 170px;
    object-fit: contain;
    filter: drop-shadow(0 16px 22px rgba(0, 0, 0, 0.45));
}

.card-content {
    position: relative;
    z-index: 2;
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 8px 28px 34px;
    text-align: center;
}

.card-tier {
    color: var(--augment-soft);
    font-size: 12px;
    font-weight: 900;
    letter-spacing: 1.2px;
}

.card-name {
    color: #fff;
    font-size: 27px;
    font-weight: 950;
    line-height: 1.08;
}

.card-description {
    max-width: 280px;
    color: #cbd5e1;
    font-size: 17px;
    font-weight: 650;
    line-height: 1.35;
}

.tier-silver {
    --augment-border: #d1d5db;
    --augment-soft: #e5e7eb;
    --augment-glow: rgba(203, 213, 225, 0.32);
    --augment-glow-strong: rgba(226, 232, 240, 0.58);
    --augment-shine: rgba(255, 255, 255, 0.34);
    --augment-card-top: rgba(100, 116, 139, 0.58);
}

.tier-gold {
    --augment-border: #fbbf24;
    --augment-soft: #fde68a;
    --augment-glow: rgba(251, 191, 36, 0.34);
    --augment-glow-strong: rgba(253, 224, 71, 0.68);
    --augment-shine: rgba(254, 243, 199, 0.42);
    --augment-card-top: rgba(146, 64, 14, 0.64);
}

.tier-diamond {
    --augment-border: #f0abfc;
    --augment-soft: #f5d0fe;
    --augment-glow: rgba(217, 70, 239, 0.54);
    --augment-glow-strong: rgba(34, 211, 238, 0.86);
    --augment-shine: rgba(255, 255, 255, 0.7);
    --augment-card-top: rgba(88, 28, 135, 0.72);
}

.tier-diamond .augment-card {
    background:
        radial-gradient(circle at 50% 18%, rgba(255, 255, 255, 0.22), transparent 28%),
        radial-gradient(circle at 18% 18%, rgba(34, 211, 238, 0.26), transparent 32%),
        radial-gradient(circle at 86% 76%, rgba(217, 70, 239, 0.34), transparent 36%),
        linear-gradient(180deg, rgba(88, 28, 135, 0.78), rgba(15, 23, 42, 0.99));
    box-shadow:
        0 0 0 1px rgba(255, 255, 255, 0.34) inset,
        0 24px 76px rgba(0, 0, 0, 0.52),
        0 0 42px rgba(217, 70, 239, 0.72),
        0 0 84px rgba(34, 211, 238, 0.52),
        0 0 118px rgba(255, 255, 255, 0.32);
    animation: diamond-card-pulse 2.6s ease-in-out infinite;
}

.tier-diamond .augment-card::after {
    content: "";
    position: absolute;
    inset: -30%;
    z-index: 1;
    background: conic-gradient(
        from 0deg,
        transparent,
        rgba(34, 211, 238, 0.28),
        rgba(255, 255, 255, 0.24),
        rgba(217, 70, 239, 0.3),
        transparent
    );
    opacity: 0.34;
    mix-blend-mode: screen;
    animation: diamond-aura 5s linear infinite;
    pointer-events: none;
}

.tier-diamond .card-tier {
    color: #f5d0fe;
}

.tier-diamond .image-frame img {
    filter:
        drop-shadow(0 16px 22px rgba(0, 0, 0, 0.48))
        drop-shadow(0 0 22px rgba(34, 211, 238, 0.42));
}

@keyframes shine-sweep {
    0%, 28% {
        transform: translateX(-34%);
    }
    64%, 100% {
        transform: translateX(34%);
    }
}

@keyframes diamond-card-pulse {
    0%, 100% {
        filter: brightness(1);
    }
    50% {
        filter: brightness(1.18);
    }
}

@keyframes diamond-aura {
    to {
        transform: rotate(360deg);
    }
}

@media (max-width: 920px) {
    .augment-layer {
        align-items: flex-start;
        padding: 18px;
        overflow-y: auto;
    }

    .augment-shell {
        gap: 18px;
    }

    h2 {
        font-size: 34px;
    }

    .augment-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .augment-card {
        min-height: 260px;
        display: grid;
        grid-template-columns: 132px 1fr;
        text-align: left;
    }

    .augment-card:hover,
    .augment-card:focus-visible {
        transform: scale(1.02);
    }

    .image-frame {
        height: auto;
        padding: 18px;
    }

    .image-frame::before {
        width: 108px;
        height: 108px;
    }

    .image-frame img {
        width: 112px;
        height: 112px;
    }

    .card-content {
        align-items: flex-start;
        justify-content: center;
        padding: 22px 20px 22px 4px;
        text-align: left;
    }

    .card-name {
        font-size: 21px;
    }

    .card-description {
        max-width: none;
        font-size: 15px;
    }
}
</style>
