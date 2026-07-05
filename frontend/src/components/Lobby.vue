<script setup lang="ts">
import { ref } from 'vue'

defineOptions({
  name: 'GameLobby'
})

defineProps<{
  title: string
  error?: string
}>()

const createId = ref('')
const joinId = ref('')

defineEmits(['create', 'join'])
</script>

<template>
  <div class="lobby">
    <div class="title">
        <h1>{{ title }}</h1>
    </div>
    <div v-if="error" class="lobby-error">{{ error }}</div>
    
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
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 50px;
    color: var(--room-fg);
    background: var(--room-bg);
    min-height: 100vh;
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
</style>
