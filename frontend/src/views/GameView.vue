<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { TresCanvas } from '@tresjs/core'
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import TheGrid from '@/components/playingfield/TheGrid.vue'
import PopupSpielende from '@/components/playingfield/PopupSpielende.vue'
import TheMapBarrierEditor from '@/components/playingfield/TheMapBarrierEditor.vue'
import { NodeFunctionInput } from 'three/webgpu';

const gridRef = ref<any>(null)
const sichtbar = ref(false)

const liveGrid = computed(() => {
  return gridRef.value?.grid || { cols: 11, rows: 8, cells: [] }
})

const liveFigures = computed(() => {
  return gridRef.value?.figures || []
})


function openCensoredMap() {
  sichtbar.value = true;
  console.log("CensoredMap geöffnet")
}

function closeCensoredMap() {
  sichtbar.value = false
  console.log("CensoredMap geschlossen")
}

import { useGameStore } from '@/stores/gamestore'


const gameStore = useGameStore()

// Steuert alles: Button-Animation, Disabled-State und Logik
const isBusy = ref(false) 
const cooldownSeconds = ref(3) // Default 3s, wird überschrieben

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL


// Cooldown-Zeit beim Start laden
onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/daten/cooldown`)
    if (res.ok) {
      cooldownSeconds.value = await res.json()
    }
  } catch (e) {
    console.error("Cooldown konnte nicht geladen werden", e)
  }
})

async function onRoll(id: string) {
  const { gameCode, playerId } = gameStore.gameData

  // Sicherheitscheck
  if (!gameCode || !playerId) return
  if(isBusy.value) return // Kein Doppelklick möglich


  isBusy.value = true

  try {
    // Würfelwurf (Backend & 3D Animation)
    // Warten auf die Animation, aber der Button bleibt gesperrt
    await rollDice(gameCode, playerId)

    // Der Timer startet für den restlichen Cooldown
    // Die Animation im Button läuft weiter, weil isBusy true bleibt
    startCooldownTimer()

  } catch (e) {
    console.error("Fehler beim Würfeln", e)
    // Bei Fehler: Button sofort wieder freigeben oder Fehlermeldung zeigen
    isBusy.value = false 
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
    <!-- 3D-Spielfeld -->
    <TresCanvas clear-color="#87CEEB" class="w-full h-full">
      <TheGrid ref="gridRef"/>
    </TresCanvas>

    <PopupSpielende /> 
    <div class="ui-panel-left">
      <div class="wood-panel dice-box">
        <div class="dice-container">
          <Dice3D />
        </div>
        <RollButton 
          :is-loading="isBusy" 
          @trigger="onRoll" 
        />

      </div>
    </div>

    <div class="ui-controls-bottom">
      <button class="map-btn" @click="openCensoredMap">
        <span class="icon">🗺️</span> Map öffnen
      </button>
    </div>

    <div v-if="sichtbar" class="modal-overlay" @click.self="closeCensoredMap">
      <div class="map-modal">
        <button class="close-seal" @click="closeCensoredMap">✕</button>
        <div class="map-content">
          <TheMapBarrierEditor 
              :grid="liveGrid" 
              :figures="liveFigures" 
          />
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
    linear-gradient(to bottom, rgba(0,0,0,0.3), transparent),
    repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0,0,0,0.1) 39px, rgba(0,0,0,0.1) 40px);
  
  border: 4px solid #2d1b0d;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.6), inset 0 0 15px rgba(0,0,0,0.5);
}

.dice-container {
  width: 160px;
  height: 160px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: inset 0 0 10px rgba(0,0,0,0.5);
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
  box-shadow: 0 5px 15px rgba(0,0,0,0.4);
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
  box-shadow: 0 0 50px rgba(0,0,0,1);
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
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
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
</style>
