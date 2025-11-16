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
import { ref } from 'vue'
import type { ISpielerDTD } from '@/stores/ISpielerDTD'
import SpielerListeZeile from './SpielerListeZeile.vue'

const spielerListe = ref<ISpielerDTD[]>([
  {
    id: 1,
    name: 'Player 1',
    bereitschaft: true,
    spielfiguren: [{ icon: '/assets/icons/blau.png' }]
  },
  {
    id: 2,
    name: 'Player 2',
    bereitschaft: false,
    spielfiguren: [{ icon: '/assets/icons/gruen.png' }]
  },
  {
    id: 3,
    name: 'Player 3',
    bereitschaft: false,
    spielfiguren: [{ icon: '/assets/icons/gelb.png' }]
  },
  {
    id: 4,
    name: 'Player 4',
    bereitschaft: false,
    spielfiguren: [{ icon: '/assets/icons/rot.png' }]
  }
])
const selectedPlayer = ref<number | null>(null)

function handleDelete(id: number) {
  
  spielerListe.value = spielerListe.value.filter(item => item.id !== id)
}
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
