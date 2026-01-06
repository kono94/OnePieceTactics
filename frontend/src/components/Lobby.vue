<script setup lang="ts">
import { ref } from 'vue'

const createId = ref('')
const joinId = ref('')

defineEmits(['create', 'join'])
</script>

<template>
  <div class="lobby">
    <div class="title">
        <h1>One Piece TFT</h1>
        <p>Tactical Auto Battler</p>
    </div>
    
    <div class="actions">
       <div class="card">
         <h3>Create New Room</h3>
         <p>Start a game with 7 AI bots</p>
         <input v-model="createId" placeholder="Enter Room ID" />
         <button @click="$emit('create', createId)" :disabled="!createId">Create Game</button>
       </div>
       
       <div class="separator">OR</div>

       <div class="card">
         <h3>Join Existing Room</h3>
         <p>Play with others</p>
         <input v-model="joinId" placeholder="Enter Room ID" />
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
    color: white;
}

.title h1 {
    font-size: 3em;
    margin-bottom: 10px;
    background: linear-gradient(to right, #ffd700, #ff8c00);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.actions {
    display: flex;
    gap: 40px;
    align-items: center;
    margin-top: 50px;
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
    background: #ffd700;
    color: black;
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
}

.separator {
    font-weight: bold;
    color: #888;
}
</style>
