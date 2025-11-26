<template>
  <div 
    class="spieler-zeile"
    :class="{ selected: selected }"
    @click="selectRow"
  >
    <div class="spieler-info">
      <img
        v-if="spieler.spielfiguren.length"
        :src="spieler.spielfiguren[0].icon"
        alt="Spielfigur"
        class="spielfigur"
      />
      <span class="spieler-name">{{ spieler.name }}</span>
    </div>

    <div class="spieler-status">
      <span v-if="spieler.bereitschaft" class="status bereit">bereit</span>
      <span v-else class="status nicht-bereit">Nicht bereit</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ISpielerDTD } from '@/stores/ISpielerDTD'

const props = defineProps<{
  spieler: ISpielerDTD,
  selected: boolean
}>()

const emit = defineEmits<{
  deletezeile: [id:number],
  select: []
}>()

function selectRow() {
  emit('select')  
}
</script>

<style scoped>
.spieler-zeile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  color: #fff;
  font-family: 'Inter', sans-serif;
  font-size: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
}

/* Hover-Effekt */
.spieler-zeile:hover {
  background: rgba(255, 255, 255, 0.11);
}

/* Aktive Auswahl */
.spieler-zeile.selected {
  background-color: rgba(0, 128, 255, 0.3); /* Hervorhebung */
  border-radius: 8px;
  font-weight: bold;
}

.spieler-info {
  display: flex;
  align-items: center;
  gap: 5.7rem;
}

.spielfigur {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.spieler-name {
  font-weight: 600;
  color: #fff;
}

.spieler-status {
  font-size: 1.5rem;
}

.status.bereit {
  color: #a3e635;
}

.status.nicht-bereit {
  color: #d1d5db;
}
</style>
