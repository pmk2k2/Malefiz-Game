<template>
  <main class="main-view">
    <Dice3D ref="diceRef" />
    <RollButton buttonId="diceButton" @trigger="onRoll" />
    
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
