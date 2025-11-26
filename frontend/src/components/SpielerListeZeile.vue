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
      <span class="spieler-name">
        {{ spieler.name }} 
        <span v-if="spieler.isHost">(Host)</span>
      </span>
    </div>

    <div class="spieler-status">
      <span v-if="spieler.bereitschaft" class="status bereit">bereit</span>
      <span v-else class="status nicht-bereit">Nicht bereit</span>
    </div>

    <div class="spieler-kicken" v-if="host && spieler.id.toString() !== myPlayerId">
      <button class="kicken" @click.stop="kicken">kick</button>
    </div>

  </div>
  
</template>

<script setup lang="ts">
import type { ISpielerDTD } from '@/stores/ISpielerDTD'

const myPlayerId = localStorage.getItem("playerId");
const host = localStorage.getItem("isHost") === "true";

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

console.log("--- DEBUG ZEILE ---");
console.log("Mein Status (isHost):", host);
console.log("Meine ID (localStorage):", myPlayerId);
console.log("Spieler ID (prop):", props.spieler.id);
console.log("Spieler Name:", props.spieler.name);
console.log("Vergleich:", host && (props.spieler.id !== myPlayerId));

async function kicken() {
  const gameCode = localStorage.getItem("gameCode");
  const playerId = props.spieler.id;

  if (!gameCode) {
    console.error("Kein gameCode gefunden debug");
    return;
  }

  try {
    const res = await fetch("http://localhost:8080/api/game/leave", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        code: gameCode,
        playerId: playerId
      })
    });
    
    const data = await res.json();
    if (data.removed) {
      console.log(`Spieler ${props.spieler.name} wurde gekickt`);
      emit("deletezeile", props.spieler.id); // entfernt die Zeile in der Lobby-Liste
    } else {
      console.warn("Spieler konnte nicht entfernt werden");
    }
  } catch (err) {
    console.error("Fehler beim Kicken:", err);
  }
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
