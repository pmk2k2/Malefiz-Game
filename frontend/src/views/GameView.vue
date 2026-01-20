<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { TresCanvas } from '@tresjs/core'
import RollButton from '@/components/RollButton.vue'
import CollectEnergyButton from '@/components/CollectEnergyButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import TheGrid from '@/components/playingfield/TheGrid.vue'
import PopupSpielende from '@/components/playingfield/PopupSpielende.vue'
import TheMapBarrierEditor from '@/components/playingfield/TheMapBarrierEditor.vue'
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/gamestore'
import PauseMenu from '@/components/PauseMenu.vue'
import EnergyBar from '@/components/playingfield/EnergyBar.vue'
import HUDInfoView from '@/components/hud/HUDInfoView.vue'

const gameStore = useGameStore()
const { figures } = storeToRefs(gameStore)
const gridRef = ref<any>(null)
const sichtbar = ref(false)

const liveBoard = computed(() => {
  return gridRef.value?.board ?? null
})

const liveFigures = computed(() => gridRef.value?.figures ?? [])

// Spielstatus ueberwachen, um OEffnen und Schließen des Maps zu steuern
watch(
  () => gameStore.gameState,
  (newState) => {
    if (newState === 'BARRIER_PLACEMENT') {
      sichtbar.value = true
      console.log('Auto Öffnen: Barriere versetzen...')
    } else if (newState === 'RUNNING') {
      sichtbar.value = false
      console.log('Auto Schließen: Spiel läuft weiter...')
    }
  },
)

function openCensoredMap() {
  sichtbar.value = true
  console.log('CensoredMap geöffnet')
}

function closeCensoredMap() {
  sichtbar.value = false
  console.log('CensoredMap geschlossen')
}

// Steuert alles: Button-Animation, Disabled-State und Logik
const isBusy = ref(false)
const isSavingEnergy = ref(false) // Nur für Energie
const cooldownSeconds = ref(3) // Default 3s, wird überschrieben

// Energie aus Stores gameData aber reaktiv
const energy = computed(() => gameStore.gameData.energy)
const maxEnergy = 10 // !!! Max-Energie sollte eigentlich irgendwo anders herkommen

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// Cooldown-Zeit beim Start laden
onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/daten/cooldown`)
    if (res.ok) {
      cooldownSeconds.value = await res.json()
    }
  } catch (e) {
    console.error('Cooldown konnte nicht geladen werden', e)
  }
})

async function onRoll(id: string) {
  const { gameCode, playerId } = gameStore.gameData

  // Sicherheitscheck
  if (!gameCode || !playerId) return
  if (isBusy.value || isSavingEnergy.value) return // Kein Doppelklick möglich und nicht während energie gesammelt wird

  isBusy.value = true

  try {
    // Würfelwurf (Backend & 3D Animation)
    // Warten auf die Animation, aber der Button bleibt gesperrt
    await rollDice(gameCode, playerId)

    // Der Timer startet für den restlichen Cooldown
    // Die Animation im Button läuft weiter, weil isBusy true bleibt
    startCooldownTimer()
  } catch (e) {
    console.error('Fehler beim Würfeln', e)
    // Bei Fehler: Button sofort wieder freigeben oder Fehlermeldung zeigen
    isBusy.value = false
  }
}

//funktion zum Speichern der Energie
async function saveEnergy() {
  const { gameCode, playerId } = gameStore.gameData
  if (!gameCode || !playerId) return

  //prüfen ob Button blockiert ist
  if (isBusy.value || isSavingEnergy.value) return

  // Sucht nach Figuren der Spieler
  const myFigure = liveFigures.value.find((f: any) => f.playerId === playerId)
  isSavingEnergy.value = true

  try {
    //Ziel URL
    const url = `${API_BASE_URL}/move/${gameCode}`

    //aufruf an Backend um im Spiel Energie zu sammeln anstatt zu ziehen
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        playerId: playerId,
        direction: 'north',
        collectEnergy: true,
      }),
    })
  } catch (e) {
    console.error(e)
  } finally {
    isSavingEnergy.value = false
  }
}

function startCooldownTimer() {
  // Zeit vom Backend
  setTimeout(() => {
    // Erst hier hört die Animation auf und der Button wird wieder klickbar
    isBusy.value = false
  }, cooldownSeconds.value * 1000)
}
</script>

<template>
  <div class="game-scene">
    <!-- 3D поле -->
    <TresCanvas clear-color="#87CEEB" class="w-full h-full" shadows>
      <TheGrid ref="gridRef" />
    </TresCanvas>

    <!-- Popup конца игры -->
    <PopupSpielende />
    <PauseMenu />

    <!-- DUEL / MINIGAME OVERLAY -->
    <div v-if="gameStore.gameData.duelActive" class="minigame-overlay">
      <div class="minigame-box">
        <h2>DUELL STARTET!</h2>
        <p>Bereite dich auf das Minispiel vor...</p>
        <div class="loader"></div>
      </div>
    </div>

    <!-- UI Overlay -->
    <div class="pointer-events-none absolute inset-0 flex items-start m-2 z-50">
      <div
        class="pointer-events-auto flex w-80 flex-col gap-6 rounded-2xl bg-black/40 p-4 backdrop-blur-sm border border-white/10">
        <!-- Dice -->
        <div class="flex flex-col items-center gap-4">
          <div class="h-40 w-40 relative">
            <Dice3D />
          </div>

          <!-- Buttons -->
          <div class="flex flex-row gap-2 w-full justify-center">
            <RollButton :is-loading="isBusy" @trigger="onRoll" />

            <CollectEnergyButton :is-loading="isSavingEnergy" @trigger="saveEnergy" />
          </div>

          <!-- Map button -->
          <button class="mt-2 rounded-xl bg-green-700 px-4 py-2 text-white font-bold" @click="openCensoredMap">
            🗺️ Map öffnen
          </button>
        </div>
      </div>
    </div>

    <div class="ui-panel-right">
      <div class="hud-wrapper">
        <HUDInfoView />
      </div>
    </div>
    <div class="energy-bar-container pointer-events-none absolute m-2 z-50">
      <EnergyBar :max-energy="maxEnergy" :current-energy="energy"></EnergyBar>
    </div>

    <!-- Map Modal -->
    <div v-if="sichtbar" class="modal-overlay" @click.self="closeCensoredMap">
      <div class="map-modal">
        <button class="close-seal" @click="closeCensoredMap">✕</button>
        <div class="map-content">
          <TheMapBarrierEditor :board="liveBoard" :figures="figures" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.game-scene {
  position: relative;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #0a0f1a;
}

.ui-panel-left {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 50;
  pointer-events: none;
}

.wood-panel {
  pointer-events: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 25px;

  background-color: #3d2b1f;
  background-image:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent),
    repeating-linear-gradient(90deg,
      transparent,
      transparent 38px,
      rgba(0, 0, 0, 0.1) 39px,
      rgba(0, 0, 0, 0.1) 40px);

  border: 4px solid #2d1b0d;
  border-radius: 20px;
  box-shadow:
    0 10px 30px rgba(0, 0, 0, 0.6),
    inset 0 0 15px rgba(0, 0, 0, 0.5);
}

.dice-container {
  width: 160px;
  height: 160px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
}

.ui-controls-bottom {
  position: absolute;
  bottom: 30px;
  right: 30px;
  z-index: 10;
}

.map-btn {
  background: #2d4d19;
  color: #f0e2d0;
  border: 3px solid #1e3311;
  border-bottom-width: 6px;
  padding: 12px 25px;
  font-family: 'Kanit', sans-serif;
  font-size: 1.2rem;
  font-weight: 800;
  text-transform: uppercase;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.4);
}

.map-btn:hover {
  transform: translateY(-3px);
  filter: brightness(1.2);
  box-shadow: 0 8px 20px rgba(76, 175, 80, 0.4);
}

.map-btn:active {
  transform: translateY(2px);
  border-bottom-width: 3px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.map-modal {
  position: relative;
  width: 85vw;
  height: 85vh;
  background: #1a1a1a;
  border: 8px solid #3d2b1f;
  border-radius: 15px;
  box-shadow: 0 0 50px rgba(0, 0, 0, 1);
  overflow: hidden;
}

.close-seal {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 45px;
  height: 45px;
  background: #6d2d2d;
  color: white;
  border: 3px solid #421a1a;
  border-radius: 50%;
  font-weight: bold;
  cursor: pointer;
  z-index: 110;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  transition: transform 0.2s;
}

.close-seal:hover {
  transform: scale(1.1) rotate(90deg);
  background: #a33535;
}

.map-content {
  width: 100%;
  height: 100%;
  padding: 20px;
}

.energy-bar-container {
  bottom: 2%;
  left: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.minigame-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 0.5s ease;
}

.minigame-box {
  text-align: center;
  color: white;
  font-family: 'Kanit', sans-serif;
  animation: scaleUp 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.minigame-box h2 {
  font-size: 4rem;
  color: #fbbf24;
  text-shadow: 0 0 20px rgba(251, 191, 36, 0.5);
  margin-bottom: 1rem;
}

.minigame-box p {
  font-size: 2rem;
  color: #cbd5e1;
}

.loader {
  width: 48px;
  height: 48px;
  border: 5px solid #fff;
  border-bottom-color: #fbbf24;
  border-radius: 50%;
  display: inline-block;
  box-sizing: border-box;
  animation: rotation 1s linear infinite;
  margin-top: 2rem;
}

.ui-panel-right {
  position: absolute;
  bottom: 20px;
  right: 20px;
  z-index: 50;
  pointer-events: none;
}

.hud-wrapper {
  pointer-events: auto;
  margin: 0;
}

@keyframes rotation {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes scaleUp {
  from {
    transform: scale(0.8);
  }

  to {
    transform: scale(1);
  }
}
</style>
