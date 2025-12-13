<script setup lang="ts">
  import { ref } from 'vue'
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import TheGrid from '@/components/playingfield/TheGrid.vue'
import { useGameStore } from '@/stores/gamestore'


const gameStore = useGameStore()
const isRolling = ref(false)

async function onRoll(id: string) {
  const { gameCode, playerId } = gameStore.gameData

  // Sicherheitscheck
  if (!gameCode || !playerId) {
    console.error("GameCode oder PlayerId fehlen!")
    return
  }

  // Button sperren während des Wurfs
  if (isRolling.value) return
  isRolling.value = true

  try {
    console.log('Würfeln angefordert von:', playerId)
    await rollDice(gameCode, playerId)
  } catch (e) {
    console.error(e)
  } finally {
    isRolling.value = false
  }
}
</script>

<template>
  <div class="relative h-screen w-screen overflow-hidden bg-[#111827]">
    
    <TheGrid />

    <div class="pointer-events-none absolute inset-0 flex items-start m-2 z-50">
      
      <div class="pointer-events-auto flex w-80 flex-col gap-6 rounded-2xl bg-black/40 p-4 backdrop-blur-sm border border-white/10">
        
        <div class="flex flex-col items-center gap-4">
          <div class="h-40 w-40 relative">
            <Dice3D />
          </div>
          
          <RollButton 
            :disabled="isRolling" 
            @trigger="onRoll" 
          />
        </div>

      </div>
    </div>
    
  </div>
</template>
