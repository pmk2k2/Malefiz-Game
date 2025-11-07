<template>
  <main class="main-view">
    <h1>Milefiz - Würfeltest</h1>

    <RollButton buttonId="diceButton" @trigger="onRoll" />
    <Dice3D ref="diceRef" />
  </main>
</template>

<script setup lang="ts">
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'

function onRoll(id: string) {
  console.log('Button pressed:', id)
  rollDice()
}
</script>
