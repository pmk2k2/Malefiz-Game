<template>
  <div class="w-[300px] max-w-[90vw] mx-auto ] rounded-2xl  space-y-1 text-slate-100">

    <div class="space-y-4">
      <HUDInfoZeile
        v-for="player in groupedByPlayer"
        :key="player.playerId"
        :player="player"
        :selectedFigureId="selectedFigureId"
      />
    </div>
  </div>
</template>




<script setup lang="ts">
import { computed, type Ref } from "vue"
import { useGameStore } from "@/stores/gamestore"
import type { ISpielerDTD } from "@/stores/ISpielerDTD"
import HUDInfoZeile from "./HUDInfoZeile.vue"
import { storeToRefs } from "pinia"



const gameStore = useGameStore()
const { selectedFigureId, figures } = storeToRefs(gameStore)




// Prüfen, ob Figuren im Haus sind
function isFigureInHome(fig: ISpielerDTD["spielfiguren"][0]) {
  if (!fig.position) return true
  const [, , z] = fig.position
  return z > 5
}

//Sammeln der Figuren pro Spieler
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
