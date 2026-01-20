<template>
  <div class="spieler-listview">
    <SpielerListeZeile
      v-for="spieler in spielerListe"
      :key="spieler.id"
      :spieler="spieler"
      :selected="selectedPlayer === spieler.id"
      :meHost="currentUserIsHost"
      @deletezeile="handleDelete"
      @select="selectedPlayer = spieler.id"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import SpielerListeZeile from '@/components/SpielerListeZeile.vue'
import { useGameStore } from '@/stores/gamestore'
const gameStore = useGameStore()
const currentUserIsHost = computed(() => gameStore.gameData.isHost === true)
watch(
  () => gameStore.gameData.isHost,
  (newVal) => {
    console.log('Updated host value:', newVal)
  },
)
const spielerListe = computed(() => {
  return gameStore.gameData.players.map(player => {
    return {
      ...player
    }
  })
})

const selectedPlayer = ref<string | null>(null)

function handleDelete(id: string) {
  gameStore.gameData.players = gameStore.gameData.players.filter((p) => p.id !== id)
}
defineExpose({ handleDelete, spielerListe })
</script>

<style scoped>
.spieler-listview {
  width: 600px;
  height: 300px;
  max-width: 800px;
  margin: 2rem auto;
}
</style>
