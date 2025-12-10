<template>
  <div class="background">
    <h1>Malefiz</h1>
    <h2>{{ gameStore.gameData.gameCode ?? 'kein LobbyID vorhanden' }}</h2>

    <div class="info-box" v-if="info.inhalt">
      <button @click="loescheInfo" class="cancel-button">✕</button>
      <span class="info-text">{{ info.inhalt }}</span>
    </div>

    <div class="icon-button">
      <button type="button" @click="toggleRulesView">
        <img :src="infoIcon" alt="Info" />
      </button>
      <button type="button" @click="toggleSettingsView">
        <img :src="einstellungIcon" alt="Einstellungen" />
      </button>
    </div>

    <InfoView v-if="showRules" @close="toggleRulesView" />

    <EinstellungView v-if="showSettings" />

    <SpielerListeView ref="spielerListeRef" @deleteZeile="onDeleteZeile" />

    <div class="buttons">
      <button @click="isReady"> {{ gameStore.gameData.isBereit ? "Bereitschaft zurücknehmen" : "Bereit" }}</button>
      <button v-if="isHost" @click="gameStartenByAdmin">Starten</button>
      <button @click="goBack">Verlassen</button>
    </div>
    <Counter v-if="showCounter" />

    <div v-if="roll !== null" class="roll-result">Würfel: {{ roll }}</div>
  </div>
</template>

<script setup lang="ts">
import InfoView from '@/components/InfoView.vue'
import einstellungIcon from '@/assets/einsetllung.png'
import infoIcon from '@/assets/info.png'
import { computed, onUnmounted, ref, watch } from 'vue'
import SpielerListeView from './SpielerListeView.vue'
import EinstellungView from '@/components/EinstellungView.vue'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'
import { useGameStore } from '@/stores/gamestore'
import type { ISpielerDTD } from '@/stores/ISpielerDTD'
import Counter from '@/components/playingfield/models/Counter.vue'
import { useInfo } from '@/composable/useInfo'


const { info, loescheInfo } = useInfo()
const gameStore = useGameStore()
const isHost = computed(() => gameStore.gameData.isHost)
const router = useRouter()

const roll = ref<number | null>(null)
const spielerListeRef = ref<InstanceType<typeof SpielerListeView> | null>(null)

const showCounter = computed(
  () => gameStore.gameData.players.length > 0 && gameStore.gameData.players.every((p) => p.isReady),
)

onMounted(() => {
  const code = gameStore.gameData.gameCode
  if (!code) {
    router.push('/main')
    return
  }
  gameStore.startLobbyLiveUpdate(code)
  gameStore.updatePlayerList(code)
  console.log(code)
})

onUnmounted(() => {
  gameStore.disconnect()
})
function onDeleteZeile(playerId: string) {
  if (spielerListeRef.value) {
    spielerListeRef.value.spielerListe = spielerListeRef.value.spielerListe.filter(
      (spieler) => spieler.id !== playerId,
    )
    
    if(gameStore.countdown!== null){
      gameStore.stopCountdown}
  }
}

function clearRoll() {
  // Würfel zurücksetzen
  roll.value = null
}

async function goBack() {
  const { playerId, gameCode } = gameStore.gameData

  if (playerId && gameCode) {
    await fetch('/api/game/leave', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        playerId,
        code: gameCode,
      }),
    })
    gameStore.disconnect()
    gameStore.resetGameCode()
    console.log(gameStore.gameData.gameCode)
    console.log('game code rm' + gameCode)
    router.push('/main')
  }
}

const props = defineProps<{ spieler: ISpielerDTD; meHost: boolean }>()

async function gameStartenByAdmin() {
  const gameCode = gameStore.gameData.gameCode
  const playerId = gameStore.gameData.playerId
  if (!gameCode) {
    console.warn('Kein gameCode vorhanden')
    return
  }
  try {
    const res = await fetch(`/api/game/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code: gameCode, playerId }),
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error('Fehler beim Starten des Spiels: ' + (err.error || res.statusText))
    }
    router.push('/game')
  } catch (err) {
    console.error(err)
  }
}

const emit = defineEmits<{
  (e: 'isReady', value: boolean): void
}>()


async function isReady() {
  const playerId = gameStore.gameData.playerId
  const gameCode = gameStore.gameData.gameCode
   const isCurrentlyReady = !gameStore.gameData.isBereit

  if (!playerId || !gameCode) {
    console.warn('Keine playerId oder gameCode vorhanden')
    return
  }

  try {
    // Backend-Call
    const res = await fetch('/api/game/setReady', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ playerId, code: gameCode, isReady: isCurrentlyReady }),
    })

    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      const errorMessage = err.error || res.statusText || 'Fehler beim Setzen des Ready-Status.'
      info.inhalt = errorMessage
      throw new Error(errorMessage)
    }
    // Start or stop countdown basierend auf dem aktuellen Ready-Status
    gameStore.gameData.isBereit= isCurrentlyReady 
    if( isCurrentlyReady == true) {
      gameStore.countdown
    }else {gameStore.stopCountdown}

  } catch (err) {
    console.error(err)
  }
}

const showSettings = ref(false)

const showRules = ref(false)

function toggleSettingsView() {
  showSettings.value = !showSettings.value
}

function toggleRulesView() {
  showRules.value = !showRules.value
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
