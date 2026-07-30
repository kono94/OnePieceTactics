<script setup lang="ts">
import { computed } from 'vue'

defineProps<{
  visible?: boolean
}>()

const gitTag = import.meta.env.VITE_GIT_TAG || 'dev'
const gitCommit = import.meta.env.VITE_GIT_COMMIT || 'local'
const buildTime = import.meta.env.VITE_BUILD_TIME || ''

const versionText = computed(() => {
  // Show tag if available, otherwise commit hash
  return gitTag !== 'dev' ? gitTag : gitCommit
})

const displayText = computed(() => {
  // Don't add 'v' if it already starts with 'v'
  return versionText.value.startsWith('v') ? versionText.value : `v${versionText.value}`
})

const tooltip = computed(() => {
  const parts = [`Version: ${gitTag}`]
  if (gitCommit !== 'unknown') {
    parts.push(`Commit: ${gitCommit}`)
  }
  if (buildTime) {
    parts.push(`Built: ${buildTime}`)
  }
  return parts.join('\n')
})
</script>

<template>
  <div v-if="visible !== false" class="version-display" :title="tooltip">
    <div class="version">{{ displayText }}</div>
  </div>
</template>

<style scoped>
.version-display {
  position: fixed;
  bottom: 8px;
  left: 8px;
  font-family: 'Courier New', monospace;
  z-index: 9999;
  pointer-events: none;
  user-select: none;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  transition: opacity 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: flex-start;
}

.version {
  font-size: 0.65rem;
  color: rgba(255, 255, 255, 0.5);
  font-weight: bold;
  line-height: 1;
  white-space: nowrap;
}

.version-display:hover {
  opacity: 1;
  pointer-events: auto;
}

.version-display:hover .version {
  color: rgba(255, 255, 255, 0.8);
}

</style>
