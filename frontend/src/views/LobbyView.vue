<template>
  <div class="background">
    <h1>Malefiz</h1>
    <h2>{{ lobbyID?.LobbyID ?? 'kein LobbyID vorhanden' }}</h2>

    <div class="icon-button">
      <button type="button">
        <img :src="infoIcon" alt="Info" />
      </button>
      <button type="button" @click="toggleSettingsView">
        <img :src="einstellungIcon" alt="Einstellungen" />
      </button>
    </div>

    <EinstellungView v-if="showSettings" />

    <SpielerListeView ref="spielerListeRef" />

    <div class="buttons">
      <button @click="clearRoll">Löschen</button>
      <button @click="isBereit">Bereit</button>
      <button @click="goBack">Verlassen</button>
    </div>

    <div v-if="roll !== null" class="roll-result">Würfel: {{ roll }}</div>
  </div>
</template>

<script setup lang="ts">
import einstellungIcon from '@/assets/einsetllung.png'
import infoIcon from '@/assets/info.png'
import { ref } from 'vue'
import SpielerListeView from '@/components/SpielerListView.vue'
import EinstellungView from '@/components/EinstellungView.vue'
import { useRoute, useRouter } from 'vue-router'
import type { ISpielerDTD } from '@/stores/ISpielerDTD'

const lobbyID = ref({
  LobbyID: localStorage.getItem('gameCode'),
})

const router = useRouter()

const roll = ref<number | null>(null)
const spielerListeRef = ref<InstanceType<typeof SpielerListeView> | null>(null)

function rollDice() {
  router.push('/game')
}

function clearRoll() {
  // Würfel zurücksetzen
  roll.value = null

}

async function goBack() {
  const playerId = localStorage.getItem("playerId");
  const gameCode = localStorage.getItem("gameCode");

  if (playerId && gameCode) {
    await fetch("http://localhost:8080/api/game/leave", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        playerId,
        code: gameCode
      })
    });
  localStorage.removeItem("gameCode")
  router.push("/main");
}
}


const props = defineProps<{ spieler: ISpielerDTD }>();

const emit = defineEmits<{
  (e: "isReady", value: boolean): void
}>();



async function isBereit() {
  const playerId = localStorage.getItem("playerId");
  const gameCode = localStorage.getItem("gameCode");
  if (!playerId || !gameCode) {
    console.warn("Keine playerId oder gameCode vorhanden");
    return;
  }

  try {
    // Backend-Call
    const res = await fetch("http://localhost:8080/api/game/setReady", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ playerId, code: gameCode, isReady: true })
    });

    if (!res.ok) throw new Error("Failed to set ready");

    const data = await res.json();

    // Update lokal, falls die Spieler-Liste verfügbar ist
    if (spielerListeRef.value?.spielerListe) {
      const player = spielerListeRef.value.spielerListe.find((s: any) => s.id === playerId);
      if (player) {
        player.isReady = data.isReady;
      }
    }

    // Event an Parent senden
    emit("isReady", true);
  } catch (err) {
    console.error(err);
  }
}

const showSettings = ref(false)

function toggleSettingsView() {
  showSettings.value = !showSettings.value
}

</script>

<style scoped>
.background {
  background-image: url('@/assets/hintergrund.jpeg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  height: 100vh;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-direction: column;
  padding-top: 2rem;
}

.buttons {
  margin-top: 2rem;
}

button {
  margin: 10px;
  padding: 12px 24px;
  background-color: rgb(131, 102, 21);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 26px;
  color: rgb(247, 247, 247);
}

button:hover {
  background-color: rgba(255, 255, 255, 1);
}

button img {
  width: 62px;
  height: 62px;
  margin: 0 300px;
}

button:has(img) {
  background-color: transparent;
  padding: 0;
  border-radius: 0;
}
.icon-button button {
  background-color: transparent;
  padding: 0;
  border-radius: 0;
  cursor: pointer;
}

.icon-button button:hover {
  background-color: transparent; /* verhindert den weißen Hover */
}

.roll-result {
  margin-top: 1rem;
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
  text-shadow: 1px 1px 2px black;
}
</style>
