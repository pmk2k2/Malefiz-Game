<template>
  <div class="w-[300px] max-w-[90vw] mx-auto bg-[rgba(0,64,0,0.25)] 
              backdrop-blur-[10px] rounded-2xl shadow-lg p-6 space-y-1 text-slate-100">

    <h2 class="text-lg font-bold text-white mb-4 text-center">Spielerübersicht</h2>

    <div class="space-y-4">
      <HUDInfoZeile
        v-for="player in groupedByPlayer"
        :key="player.playerId"
        :player="player"
      />
    </div>
  </div>
</template>




<script setup lang="ts">
import { computed } from "vue"
import { useGameStore } from "@/stores/gamestore"
import type { ISpielerDTD } from "@/stores/ISpielerDTD"
import HUDInfoZeile from "./HUDInfoZeile.vue"

const gameStore = useGameStore()
const figures = computed(() => gameStore.figures)

/**
 * Prüft, ob Figur im Haus ist
 */
function isFigureInHome(fig: ISpielerDTD["spielfiguren"][0]) {
  if (!fig.position) return true
  const [, , z] = fig.position
  return z > 5
}

/**
 * Spielerzeilen für HUD vorbereiten
 */
const groupedByPlayer = computed(() => {
  return gameStore.gameData.players.map(player => {
    const playerFigures = figures.value.filter(f => f.playerId === player.id)
    const color = playerFigures[0]?.color ?? "#808080"

    return {
      playerId: player.id,
      name: player.name,
      color,
      home: playerFigures.filter(isFigureInHome),
      field: playerFigures.filter(f => !isFigureInHome(f))
    }
  })
})

</script>
