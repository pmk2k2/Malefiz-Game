<script setup lang="ts">
import { ref } from 'vue'

const roll = ref<number | null>(null)

function rollDice() {
  roll.value = Math.floor(Math.random() * 6) + 1
}
</script>

<template>
  <div class="main-div">
    <h1>Milefiz - Game</h1>

    <div class="roll-div" v-if="roll !== null">
      {{ roll }}
    </div>

    <button @click="rollDice">🎲 Roll the Dice</button>
  </div>
</template>
