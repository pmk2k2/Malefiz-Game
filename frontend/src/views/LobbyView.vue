<template>
  <div class="menu-container">
    <header class="header">
      <h1><span>Lobby</span>Malefiz</h1>
      <div class="code-display">
        <span class="label">Lobby ID</span>
        <h2 class="code-number">{{ gameStore.gameData.gameCode ?? '---' }}</h2>
      </div>
    </header>

    <div class="notifications-container">
      <div v-for="n in nachrichten" :key="n.id" class="info-box" :class="n.typ">
        <button @click="loescheInfo(n.id)" class="cancel-button">✕</button>
        <span class="info-text">{{ n.text }}</span>
      </div>
    </div>

    <div class="icon-nav">
      <button class="icon-btn" type="button" @click="toggleRulesView">
        <img :src="infoIcon" alt="Info" />
      </button>
    </div>

    <main class="main-content-lobby">
      <InfoView v-if="showRules" @close="toggleRulesView" />

      <div class="lobby-selection-area">
        <div class="map-box-container">
          <MapAuswahl v-if="isHost" @openEditor="openBoardEditor" />

          <div v-else class="map-selector-wrapper readonly-preview">
            <div class="selection-row" style="justify-content: center">
              <img :src="currentMapImage" alt="selected Map" />
            </div>
            <h3 class="map-name">Ausgewählt: {{ currentMapName }}</h3>
            <p class="host-info-text">Warte auf Host-Auswahl...</p>
          </div>
        </div>

        <div class="middle-column-wrapper">
          <div class="spieler-liste-container">
            <SpielerListeView ref="spielerListeRef" @deleteZeile="onDeleteZeile" />
          </div>
        </div>

        <div class="right-spacer">
          <LobbySettingsPanel v-if="isHost" />

          <div v-else class="rules-display-readonly">
            <h3 class="section-subtitle">Spielregeln</h3>
            <div class="rule-item">
              <span>Würfel Cooldown:</span>
              <div class="value-badge">{{ gameStore.gameData.cooldown }}</div>
            </div>
            <div class="rule-item">
              <span>Energie Limit:</span>
              <div class="value-badge">{{ gameStore.gameData.maxCollectableEnergy }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="button-group-lobby">
        <button class="btn ready-btn small-btn" :class="{ 'is-ready': gameStore.gameData.isBereit }" @click="isReady">
          {{ gameStore.gameData.isBereit ? '✓ Bereit' : 'Bereit' }}
        </button>

        <button v-if="isHost" class="btn create small-btn" @click="gameStartenByAdmin">
          Starten
        </button>
      </div>

      <button class="btn logout exit-btn" @click="goBack">Verlassen</button>
    </main>

    <Counter v-if="showCounter" />
    <div v-if="roll !== null" class="roll-result">Würfel: {{ roll }}</div>

    <!-- NEW: Board Editor -->
    <BoardEditor :isVisible="showBoardEditor" @close="handleEditorClose" @boardSaved="handleBoardSaved" />
  </div>
</template>

<script setup lang="ts">
import InfoView from '@/components/InfoView.vue'
import infoIcon from '@/assets/info.png'
import { computed, onUnmounted, ref } from 'vue'
import SpielerListeView from './SpielerListeView.vue'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'
import { useGameStore } from '@/stores/gamestore'
import Counter from '@/components/playingfield/models/Counter.vue'
import { useInfo } from '@/composable/useInfo'
import BoardEditor from '@/components/BoardEditor.vue'
import MapAuswahl from '@/components/MapAuswahl.vue'
import LobbySettingsPanel from '@/components/LobbySettingsPanel.vue'

const { nachrichten, loescheInfo, setzeInfo } = useInfo()
const gameStore = useGameStore()
const isHost = computed(() => gameStore.gameData.isHost)
const router = useRouter()

const roll = ref<number | null>(null)
const spielerListeRef = ref<InstanceType<typeof SpielerListeView> | null>(null)

const showCounter = computed(
  () => gameStore.gameData.players.length > 0 && gameStore.gameData.players.every((p) => p.isReady),
)

const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'

// NEW: Board selection state
const showBoardSelection = ref(false)
const showBoardEditor = ref(false)

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

const currentMapName = computed(() => {
  const rawName = gameStore.gameData.boardName || 'DummyBoard.json'

  return rawName
    .replace('.json', '')
    .replace(/([A-Z])/g, ' $1')
    .trim()
})

const currentMapImage = computed(() => {
  return new URL('@/assets/chooseMap.png', import.meta.url).href
})

function onDeleteZeile(playerId: string) {
  if (spielerListeRef.value) {
    spielerListeRef.value.spielerListe = spielerListeRef.value.spielerListe.filter(
      (spieler) => spieler.id !== playerId,
    )

    if (gameStore.countdown !== null) {
      gameStore.stopCountdown
    }
  }
}

function clearRoll() {
  roll.value = null
}

async function goBack() {
  const { playerId, gameCode } = gameStore.gameData

  if (playerId && gameCode) {
    await fetch(`${apiBase}/game/leave`, {
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

async function gameStartenByAdmin() {
  const gameCode = gameStore.gameData.gameCode
  const playerId = gameStore.gameData.playerId
  if (!gameCode) {
    console.warn('Kein gameCode vorhanden')
    return
  }
  try {
    const res = await fetch(`${apiBase}/game/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code: gameCode, playerId }),
    })
    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      throw new Error('Fehler beim Starten des Spiels: ' + (err.error || res.statusText))
    }
    router.push('/game')
  } catch (err: any) {
    setzeInfo(err.message, 'error')
    console.error(err)
  }
}

async function isReady() {
  const playerId = gameStore.gameData.playerId
  const gameCode = gameStore.gameData.gameCode
  const isCurrentlyReady = !gameStore.gameData.isBereit

  if (!playerId || !gameCode) {
    console.warn('Keine playerId oder gameCode vorhanden')
    return
  }

  try {
    const res = await fetch(`${apiBase}/game/setReady`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ playerId, code: gameCode, isReady: isCurrentlyReady }),
    })

    if (!res.ok) {
      const err = await res.json().catch(() => ({}))
      const errorMessage = err.error || res.statusText || 'Fehler beim Setzen des Ready-Status.'
      setzeInfo(errorMessage, 'error')
      throw new Error(errorMessage)
    }

    gameStore.gameData.isBereit = isCurrentlyReady
    if (isCurrentlyReady == true) {
      gameStore.countdown
    } else {
      gameStore.stopCountdown
    }
  } catch (err) {
    console.error(err)
  }
}

const showRules = ref(false)

function toggleRulesView() {
  showRules.value = !showRules.value
}

// NEW: Board selection handlers
function openBoardEditor() {
  showBoardSelection.value = false
  showBoardEditor.value = true
}

function onBoardSelected(boardName: string) {
  setzeInfo(`Board "${boardName.replace('.json', '')}" selected!`, 'info')
}

function onBoardSaved() {
  setzeInfo('Custom board saved successfully!', 'success')
}

function handleEditorClose() {
  showBoardEditor.value = false
  showBoardSelection.value = true
}

function handleBoardSaved() {
  showBoardEditor.value = false
  showBoardSelection.value = true

  setzeInfo("Spielfeld erfolgreich gespeichert!", "success")
}

</script>

<style scoped>
/* Keep all existing styles from LobbyView.vue */

.header {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.code-display {
  margin-top: 5px;
  background: rgba(45, 35, 10, 0.9);
  border: 3px solid #ffc107;
  border-radius: 10px;
  display: inline-block;
  padding: 10px 30px;
  margin-top: 10px;
  box-shadow: 0 0 20px rgba(255, 193, 7, 0.3);
  transform: rotate(-1deg);
}

.code-display .label {
  color: #ffc107;
  font-size: 0.6rem;
  text-transform: uppercase;
  display: block;
  letter-spacing: 2px;
}

.code-number {
  color: #ffffff;
  font-size: 1.4rem;
  margin: 0;
  letter-spacing: 3px;
  text-shadow: 2px 2px 0px #3d2b1f;
}

.menu-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
  background-image:
    linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('@/assets/backg.jpg');
  background-size: cover;
}

.main-content-lobby {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  padding: 20px;
  min-height: 0;
}

.lobby-selection-area {
  display: flex;
  flex-direction: row;
  width: 100%;
  margin: 20px auto;
}

.map-box-container {
  flex: 1;
  display: flex;
  justify-content: center;
}

.middle-column-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.right-spacer {
  display: flex;
  padding-left: 20px;
  flex: 1;
}

.spieler-liste-container {
  width: 100%;
  max-width: 500px;
  flex: 0 1 auto;
  max-height: 40vh;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  background-color: #3d2b1f;
  background-image:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%),
    repeating-linear-gradient(90deg,
      transparent,
      transparent 38px,
      rgba(0, 0, 0, 0.15) 39px,
      rgba(0, 0, 0, 0.15) 40px);

  padding: 15px;
  border: 5px solid #2d1b0d;
  border-radius: 15px;
  box-shadow:
    inset 0 0 20px rgba(0, 0, 0, 0.5),
    0 10px 20px rgba(0, 0, 0, 0.4);
  overflow-y: auto;
}

.spieler-liste-container::-webkit-scrollbar {
  width: 10px;
}

.spieler-liste-container::-webkit-scrollbar-track {
  background: #2d1b0d;
  border-radius: 10px;
}

.spieler-liste-container::-webkit-scrollbar-thumb {
  background: #4caf50;
  border: 2px solid #2d1b0d;
  border-radius: 10px;
}

.spieler-liste-container::-webkit-scrollbar-thumb:hover {
  background: #a7ff83;
}

.button-group-lobby {
  display: flex;
  flex-direction: row;
  gap: 15px;
  flex-shrink: 0;
  padding-bottom: 10px;
  justify-content: center;
  width: 100%;
}

.button-group-lobby .btn {
  width: 180px;
  font-size: 1.1rem;
  padding: 10px 0px;
}

.icon-nav {
  position: absolute;
  top: 25px;
  right: 25px;
  display: flex;
  flex-direction: row;
  gap: 20px;
  z-index: 100;
}

.icon-btn {
  width: 65px;
  height: 65px;
  border-radius: 15px;
  background-color: #3d2b1f;
  background-image: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border: 3px solid #2d1b0d;
  border-bottom-width: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}

.icon-btn img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  filter: sepia(1) saturate(5) hue-rotate(-10deg) drop-shadow(0 0 5px rgba(255, 193, 7, 0.5));
  transition: all 0.3s ease;
}

.icon-btn:hover {
  transform: translateY(-3px) scale(1.02);
  background-color: #4d3319;
}

.icon-btn:hover img {
  transform: rotate(10deg);
}

.icon-btn:active {
  transform: translateY(2px);
  border-bottom-width: 2px;
}

.ready-btn.is-ready {
  background-color: #2d4d19;
  border-color: #a7ff83;
  color: #a7ff83;
  box-shadow: 0 0 20px rgba(167, 255, 131, 0.4);
}

.info-box {
  background: #6d2d2d;
  color: white;
  padding: 15px 40px;
  border-radius: 8px;
  border: 2px solid #f44336;
  position: relative;
  animation: slideIn 0.3s ease-out;
}

.cancel-button {
  position: absolute;
  right: 5px;
  top: 5px;
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.btn {
  padding: 12px 20px;
  font-size: 1.2rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  border-radius: 12px;

  background-color: #4d3319;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.2) 0%, transparent 100%),
    repeating-linear-gradient(90deg,
      transparent,
      transparent 40px,
      rgba(0, 0, 0, 0.1) 41px,
      rgba(0, 0, 0, 0.1) 42px);
  background-size:
    100% 100%,
    100% 100%,
    100% 100%,
    50px 100%;

  border: 4px solid #3d2b1f;
  border-bottom-width: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #f0e2d0;
  text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.5);
  position: relative;
}

.btn:not(.create):not(.logout):not(.board-btn) {
  background-color: #2d4d19;
  border-color: #1e3311;
  color: #e0f2d8;
}

.btn:hover {
  transform: translateY(2px);
  border-bottom-width: 4px;
  filter: brightness(1.1);
}

.create {
  background-color: #634121;
  border-color: #3d2b1f;
  color: #ffcc66;
}

/* NEW: Board button styling */
.board-btn {
  background-color: #4a4a2d;
  border-color: #2d2d1b;
  color: #ffd700;
}

.btn.logout {
  background-color: #6d2d2d;
  border-color: #421a1a;
}

.exit-btn {
  position: absolute;
  bottom: 20px;
  left: 20px;
  width: 150px !important;
  padding: 8px 0 !important;
  font-size: 1rem !important;
  border-bottom-width: 4px !important;
  z-index: 10;
}

.readonly-preview,
.rules-display-readonly {
  width: 400px;
  height: 300px;
  background-color: #3d2b1f;
  background-image:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%),
    repeating-linear-gradient(90deg,
      transparent,
      transparent 38px,
      rgba(0, 0, 0, 0.15) 39px,
      rgba(0, 0, 0, 0.15) 40px);
  border: 5px solid #2d1b0d;
  border-radius: 15px;
  padding: 15px;
  text-align: center;
  box-shadow: inset 0 0 20px rhba(0, 0, 0, 0.5);
}

.selection-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.host-info-text {
  color: #ffc107;
  font-size: 0.8rem;
  font-style: italic;
  margin-top: 10px;
}

.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #f0e2d0;
  margin: 15px 0;
  padding: 0 20px;
}

.value-badge {
  background: #f0e2d0;
  color: #2d1b0d;
  padding: 5px 15px;
  border-radius: 8px;
  font-weight: 900;
  border: 2px solid #2d1b0d;
}

.section-subtitle {
  color: #ffcc66;
  text-transform: uppercase;
  font-size: 1rem;
  margin-bottom: 20px;
  text-shadow: 1px 1px 2px black;
}

img {
  width: 220px;
  height: 190px;
  object-fit: cover;
  border: 3px solid #2d1b0d;
  border-radius: 8px;
  background-color: #000;
}

.info-box.error {
  background: #6d2d2d;
  border-color: #f44336;
}

.info-box.success {
  background: #2d4d19;
  border-color: #a7ff83;
}

.info-box.info {
  background: #3d2b1f;
  border-color: #ffc107;
}

.notifications-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
