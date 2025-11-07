<template>
  <main class="main">
    <h1>Milefiz - Game</h1>
    <div class="btn-wrapper">
      <button class="roll-button" :disabled="rolling" @click="rollDice">
        <span v-if="!rolling">🎲 Würfel werfen!</span>
        <span v-else class="dots">würfelt</span>
      </button>
    </div>
    <div v-if="roll !== null" class="dice">{{ roll }}</div>


  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const roll = ref<number | null>(null)
const rolling = ref(false)

const rollDice = () => {
  if (rolling.value) return
  rolling.value = true
  roll.value = null
  setTimeout(() => {
    roll.value = Math.floor(Math.random() * 6) + 1
    rolling.value = false
  }, 1000)
}
</script>

<style scoped>

/* Disable wenn geklickt und hover bewegt button hoch für mehr indikation */
.roll-button:hover:not(:disabled) { transform: translateY(-4px); }
.roll-button:disabled { opacity: 0.6; cursor: not-allowed; background: #374151; }

/* Animation für die Punkte während es "läd" */
.dots::after {
  content: '';
  display: inline-block;
  width: 1em;
  animation: dots 1.2s steps(4, end) infinite;
}
/* Genauer ablauf der Animation */
@keyframes dots {
  0%, 20% { content: ''; }
  40% { content: '.'; }
  60% { content: '..'; }
  80%, 100% { content: '...'; }
}
</style>

