<script setup lang="ts">
defineProps<{
  type: 'WON' | 'LOST' | 'DRAW' | null
}>()
</script>

<template>
  <div v-if="type" class="overlay">
    <div class="result-text" :class="type.toLowerCase()">
      <span v-if="type === 'WON'">ROUND WON</span>
      <span v-else-if="type === 'LOST'">ROUND LOST</span>
      <span v-else>DRAW</span>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  top: 30%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 9999;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.result-text {
  font-size: 80px;
  font-weight: 900;
  text-transform: uppercase;
  text-shadow: 0 4px 10px rgba(0,0,0,0.8);
  padding: 20px 60px;
  border-radius: 20px;
  background: rgba(0,0,0,0.7);
  border: 4px solid white;
  box-shadow: 0 0 50px rgba(0,0,0,0.5);
}

.result-text.won {
  color: #10b981; /* Green */
  border-color: #10b981;
}

.result-text.lost {
  color: #ef4444; /* Red */
  border-color: #ef4444;
}

.result-text.draw {
  color: #fbbf24; /* Amber */
  border-color: #fbbf24;
}

@keyframes popIn {
  from {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 0;
  }
  to {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
}
</style>
