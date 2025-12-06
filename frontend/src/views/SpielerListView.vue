<template>
  <div class="spieler-listview">
    <SpielerListeZeile
      v-for="spieler in spielerListe"
      :key="spieler.id"
      :spieler="spieler"
      :selected="selectedPlayer === spieler.id"
      @deletezeile="handleDelete"
      @select="selectedPlayer = spieler.id"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { ISpielerDTD } from '@/stores/ISpielerDTD'
import SpielerListeZeile from '@/components/SpielerListeZeile.vue'
import { mapBackendPlayersToDTD } from '@/stores/mapper'

const spielerListe = ref<ISpielerDTD[]>([])
const selectedPlayer = ref<number | null>(null)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

onMounted(async () => {
  const gameCode = localStorage.getItem('gameCode')

  if (!gameCode) {
    console.warn('No game code found!')
    return
  }

  try {
    const res = await fetch(`${API_BASE_URL}/game/get?code=${gameCode}`)
    if (!res.ok) throw new Error('HTTP error ' + res.status)

    const backendData = await res.json()

    spielerListe.value = mapBackendPlayersToDTD(backendData.players)

    console.log('Spieler geladen:', spielerListe.value)
  } catch (err) {
    console.error('Failed to load players:', err)
  }
})

function handleDelete(id: number) {
  spielerListe.value = spielerListe.value.filter((item) => item.id !== id)
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
