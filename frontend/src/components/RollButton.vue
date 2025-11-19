<template>
  <main class="main">
    <h1>Milefiz - Game</h1>
    <div class="btn-wrapper">
      <button class="roll-button" :disabled="rolling" @click="rollDice">
        <span v-if="!rolling">🎲 Würfel werfen!</span>
        <span v-else class="dots">würfelt</span>
      </button>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const rolling = ref(false)
const emit = defineEmits(['trigger'])

function rollDice() {
  if (rolling.value) return
  rolling.value = true
  emit('trigger', 'diceButton') 
  setTimeout(() => (rolling.value = false), 1500) 
}
</script>

