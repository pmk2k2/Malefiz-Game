<script setup lang="ts">
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import TheGrid from '@/components/playingfield/TheGrid.vue'
import PopupSpielende from '@/components/playingfield/PopupSpielende.vue'

function onRoll(id: string) {
  console.log('Button pressed:', id)
  rollDice()
}
</script>

<template>
  <div class="relative h-screen w-screen overflow-hidden bg-[#111827]">
    <!-- 3D-Spielfeld -->
    <TheGrid />

    <!-- Popup für Spielende -->
    <PopupSpielende /> 

    <!-- Overlay-UI oben links -->
    <div class="pointer-events-none absolute inset-0 flex items-start m-2">
      <div class="pointer-events-auto flex w-80 flex-col gap-6 rounded-2xl bg-black/40 p-2">
        <div class="flex flex-col items-center">
          <div class="h-60 w-60">
            <Dice3D />
          </div>
          <RollButton buttonId="diceButton" @trigger="onRoll" />
        </div>
      </div>
    </div>
  </div>
</template>
