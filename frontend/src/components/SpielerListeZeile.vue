<template>
  <div class="spieler-zeile"  @click="selectRow">
    <div class="spieler-info">
     <span
        class="spieler-farbe"
        :style="{ backgroundColor: spieler.color}"
      ></span>


      <span class="spieler-name">
        {{ spieler.name }}
        <span v-if="spieler.isHost">(Host)</span>
      </span>
    </div>

    <div class="spieler-status">
      <span v-if="spieler.isReady" class="status bereit">bereit</span>
      <span v-else class="status nicht-bereit">Nicht bereit</span>
    </div>

    <div class="spieler-kicken" v-if="meHost && !spieler.isHost">
      <button class="kicken" @click.stop="kicken">kicken</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ISpielerDTD } from '@/stores/ISpielerDTD'
import { useGameStore } from '@/stores/gamestore'

const gameStore = useGameStore()

const props = defineProps<{
  spieler: ISpielerDTD
  selected: boolean
  meHost: boolean
}>()


const emit = defineEmits<{
  deletezeile: [id: string]
  select: []
}>()

function selectRow() {
  emit('select')
}

async function kicken() {
  const gameCode = gameStore.gameData.gameCode
  const playerIdKick = props.spieler.id

  try {
    const res = await fetch('/api/game/leave', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        code: gameCode,
        playerId: playerIdKick,
        requesterId: gameStore.gameData.playerId,
      }),
    })

    if (res.ok) {
      emit('deletezeile', playerIdKick)
    } else {
      console.log('Fehler beim Kicken (res nicht ok)')
    }
  } catch (err) {
    console.error('Fehler beim Kicken:', err)
  }
}
</script>

<style scoped>
.spieler-zeile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1rem;
  margin-bottom: 5px;
  font-family: 'Kanit', sans-serif;
  color: #f0e2d0; 
  background: rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  
  cursor: pointer;
  transition: all 0.2s ease;
}

.spieler-zeile:hover {
  background: rgba(76, 175, 80, 0.15);
  transform: translateX(5px); 
}


.spieler-zeile.selected {
  background-color: rgba(167, 255, 131, 0.1);
  border: 1px solid #4caf50;
  box-shadow: inset 0 0 10px rgba(76, 175, 80, 0.2);
}

.spieler-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.spielfigur {
  width: 32px;
  height: 32px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
}

.spieler-name {
  font-weight: 600;
  font-size: 1.1rem;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
}


.spieler-name span {
  color: #ffc107;
  font-size: 0.8rem;
  margin-left: 5px;
}

.spieler-status {
  font-size: 0.9rem;
  font-weight: 800;
  text-transform: uppercase;
}

.status.bereit {
  color: #a7ff83; 
  text-shadow: 0 0 8px rgba(167, 255, 131, 0.5);
}

.status.nicht-bereit {
  color: rgba(255, 255, 255, 0.4);
}


.kicken {
  background: #6d2d2d;
  border: 1px solid #421a1a;
  border-radius: 4px;
  color: #ffcdd2;
  font-size: 0.7rem;
  padding: 4px 8px;
  text-transform: uppercase;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
}

.kicken:hover {
  background: #c62828;
  color: white;
}
.spieler-farbe {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #fff;
  display: inline-block;
}

</style>
