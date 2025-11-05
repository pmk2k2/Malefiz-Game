<script setup lang="ts">
import { ref } from 'vue'

const roll = ref<number | null>(null)

function rollDice() {
  roll.value = Math.floor(Math.random() * 6) + 1
}
</script>

<template>
  <main
    style="
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      gap: 1rem;
      color: #f5f5f5;
    "
  >
    <h1 style="font-size: 3rem; margin: 0">Milefiz - Game</h1>

    <div
      v-if="roll !== null"
      style="
        font-size: 5rem;
        background: white;
        color: black;
        width: 100px;
        height: 100px;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 12px;
      "
    >
      {{ roll }}
    </div>

    <button @click="rollDice" style="">🎲 Roll the Dice</button>
  </main>
</template>
