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
import { ref, onMounted } from 'vue'
import type { ISpielerDTD } from '@/stores/ISpielerDTD'
import SpielerListeZeile from './SpielerListeZeile.vue'
import { mapBackendPlayersToDTD } from '@/stores/mapper'

const spielerListe = ref<ISpielerDTD[]>([])
const selectedPlayer = ref<string | null>(null)

const currentUserIsHost = ref(false)


onMounted(async () => {
  const gameCode = localStorage.getItem("gameCode")

  currentUserIsHost.value = localStorage.getItem('isHost') === 'true';

  if (!gameCode) {
    console.warn("No game code found!")
    return
  }

  try {

    const res = await fetch(`http://localhost:8080/api/game/get?code=${gameCode}`)
    if (!res.ok) throw new Error("HTTP error " + res.status)

    const backendData = await res.json()

    spielerListe.value = mapBackendPlayersToDTD(backendData.players)

    console.log("Spieler geladen:", spielerListe.value)

  } catch (err) {
    console.error("Failed to load players:", err)
  }
})



defineExpose({  spielerListe })

function handleDelete(id: string) {
  spielerListe.value = spielerListe.value.filter(item => item.id !== id)
}
defineExpose({ handleDelete, spielerListe })

</script>

<style scoped>
.spieler-listview {
  width: 600px;
  height: 300px;
  max-width: 800px;
  margin: 2rem auto;
  background: rgba(0, 64, 0, 0.3);
  padding: 2.5rem;
  border-radius: 20px;
  backdrop-filter: blur(8px);
}
</style>
