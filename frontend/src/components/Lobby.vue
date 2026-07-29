<script setup lang="ts">
import { ref } from 'vue'

defineOptions({
  name: 'GameLobby'
})

const props = withDefaults(defineProps<{
  title: string
  themeClass?: string
  error?: string
}>(), {
  themeClass: 'theme-generic',
})

const createId = ref('')
const joinId = ref('')

defineEmits(['create', 'join'])
</script>

<template>
  <div :class="['lobby', props.themeClass]">
    <div class="title">
        <h1>{{ props.title }}</h1>
        <p class="subtitle">Create or join a tactics room</p>
        <div v-if="props.themeClass === 'theme-palworld'" class="pal-sphere" aria-hidden="true">
            <span class="pal-sphere-button" />
        </div>
    </div>
    <div v-if="props.error" class="lobby-error">{{ props.error }}</div>
    
    <div class="actions">
       <div class="card">
         <h3>Create New Room</h3>
         <p>Start a game with 7 AI bots</p>
         <input v-model="createId" placeholder="Enter Room ID" @keyup.enter="createId && $emit('create', createId)" />
         <button @click="$emit('create', createId)" :disabled="!createId">Create Game</button>
       </div>
       
       <div class="separator">OR</div>

       <div class="card">
         <h3>Join Existing Room</h3>
         <p>Play with others</p>
         <input v-model="joinId" placeholder="Enter Room ID" @keyup.enter="joinId && $emit('join', joinId)" />
         <button @click="$emit('join', joinId)" :disabled="!joinId" class="secondary">Join Game</button>
       </div>
    </div>
  </div>
</template>

<style scoped>
.lobby {
    position: relative;
    isolation: isolate;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 50px;
    color: var(--room-fg);
    background: var(--room-bg);
    min-height: 100vh;
    overflow: hidden;
}

.lobby > * {
    position: relative;
    z-index: 1;
}

.title h1 {
    font-size: 3em;
    margin-bottom: 10px;
    background: linear-gradient(to right, var(--room-accent), #ff8c00);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.title {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.subtitle {
    margin: 0;
    color: var(--room-muted);
    font-size: 1.05rem;
    font-weight: 600;
}

.actions {
    display: flex;
    gap: 40px;
    align-items: center;
    margin-top: 50px;
}

.lobby-error {
    margin-top: 20px;
    padding: 10px 14px;
    border-radius: 6px;
    background: rgba(239, 68, 68, 0.16);
    border: 1px solid rgba(248, 113, 113, 0.5);
    color: #fecaca;
    font-weight: 600;
}

.card {
    background: rgba(255, 255, 255, 0.1);
    padding: 30px;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    gap: 15px;
    width: 300px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

input {
    padding: 10px;
    border-radius: 6px;
    border: none;
    background: rgba(0, 0, 0, 0.5);
    color: white;
    font-size: 16px;
}

button {
    padding: 12px;
    border-radius: 6px;
    border: none;
    background: var(--room-accent);
    color: var(--room-accent-contrast);
    font-weight: bold;
    font-size: 16px;
    cursor: pointer;
    transition: transform 0.2s;
}

button:hover:not(:disabled) {
    transform: scale(1.05);
}

button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

button.secondary {
    background: #4ade80;
    color: #0f172a;
}

.separator {
    font-weight: bold;
    color: #888;
}

.lobby.theme-palworld {
    --pw-sky: #5bc9e8;
    --pw-sky-deep: #1888a6;
    --pw-teal: #187c6b;
    --pw-leaf: #48a868;
    --pw-sand: #f0d59a;
    --pw-coral: #ff7f6e;
    --pw-gold: #d9a441;
    --pw-ink: #173443;
    --pw-cloud: #f5feff;
    --room-bg: linear-gradient(180deg, var(--pw-sky) 0%, #bfefff 48%, var(--pw-sand) 100%);
    --room-fg: var(--pw-ink);
    --room-muted: #285767;
    --room-accent: var(--pw-teal);
    --room-accent-contrast: var(--pw-cloud);
    --room-avatar: var(--pw-leaf);
}

.lobby.theme-palworld::before,
.lobby.theme-palworld::after {
    position: absolute;
    z-index: 0;
    content: '';
    pointer-events: none;
}

.lobby.theme-palworld::before {
    inset: 0 0 35%;
    background:
        radial-gradient(ellipse at 20% 28%, rgba(245, 254, 255, 0.9) 0 9%, transparent 10%),
        radial-gradient(ellipse at 76% 18%, rgba(245, 254, 255, 0.78) 0 12%, transparent 13%),
        linear-gradient(180deg, rgba(91, 201, 232, 0.9), rgba(191, 239, 255, 0.55));
}

.lobby.theme-palworld::after {
    inset: auto 0 0;
    height: 38%;
    background:
        radial-gradient(ellipse at 50% 100%, var(--pw-teal) 0 36%, transparent 37%),
        linear-gradient(180deg, transparent 0 30%, var(--pw-sand) 31%);
}

.lobby.theme-palworld .title h1 {
    color: var(--pw-ink);
    background: linear-gradient(90deg, var(--pw-teal), var(--pw-sky-deep));
    background-clip: text;
    -webkit-background-clip: text;
}

.lobby.theme-palworld .card {
    border-color: rgba(245, 254, 255, 0.75);
    border-radius: 20px;
    background: rgba(245, 254, 255, 0.86);
    box-shadow: 0 12px 30px rgba(24, 136, 166, 0.2);
    color: var(--pw-ink);
}

.lobby.theme-palworld .card p {
    color: #285767;
}

.lobby.theme-palworld input {
    border: 1px solid rgba(24, 124, 107, 0.35);
    background: rgba(255, 255, 255, 0.92);
    color: var(--pw-ink);
}

.lobby.theme-palworld input:focus {
    outline: 3px solid rgba(255, 127, 110, 0.42);
    border-color: var(--pw-coral);
}

.lobby.theme-palworld button {
    background: var(--pw-teal);
    color: var(--pw-cloud);
}

.lobby.theme-palworld button:hover:not(:disabled) {
    background: var(--pw-leaf);
}

.lobby.theme-palworld button.secondary {
    background: var(--pw-coral);
    color: var(--pw-ink);
}

.pal-sphere {
    position: absolute;
    top: 0;
    right: -58px;
    width: 72px;
    height: 72px;
    border: 4px solid var(--pw-ink);
    border-radius: 50%;
    background: linear-gradient(180deg, var(--pw-coral) 0 46%, var(--pw-ink) 46% 54%, var(--pw-cloud) 54%);
    box-shadow: 0 6px 0 rgba(217, 164, 65, 0.8);
}

.pal-sphere-button {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 18px;
    height: 18px;
    border: 4px solid var(--pw-ink);
    border-radius: 50%;
    background: var(--pw-cloud);
    transform: translate(-50%, -50%);
}

@media (max-width: 720px) {
    .actions {
        flex-direction: column;
        gap: 18px;
        width: min(92vw, 340px);
        margin-top: 32px;
    }

    .card {
        width: 100%;
    }

    .pal-sphere {
        right: -26px;
        width: 56px;
        height: 56px;
    }
}

@media (prefers-reduced-motion: reduce) {
    .lobby *,
    .lobby *::before,
    .lobby *::after {
        transition-duration: 0.01ms !important;
    }
}
</style>
