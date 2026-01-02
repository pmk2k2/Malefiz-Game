<template>
  <div class="einstellung-view">
    <h3>WÜRFELZEIT</h3>

    <div class="settings-control">
      <button
        class="settingsButton"
        @mousedown="startTime('decrease')"
        @mouseup="stopTime"
        @mouseleave="stopTime"
      >
        -
      </button>

      <div class="valueDisplay">
        {{ time }} s
      </div>

      <button
        class="settingsButton"
        @mousedown="startTime('increase')"
        @mouseup="stopTime"
        @mouseleave="stopTime"
      >
        +
      </button>
    </div>

    <h3 style="margin-top: 6%;">ENERGIELIMIT</h3>

    <div class="settings-control">
      <button
        class="settingsButton"
        @mousedown="startEnergy('decrease')"
        @mouseup="stopEnergy"
        @mouseleave="stopEnergy"
      >
        -
      </button>

      <div class="valueDisplay">
        {{ maxEnergy }}
      </div>

      <button
        class="settingsButton"
        @mousedown="startEnergy('increase')"
        @mouseup="stopEnergy"
        @mouseleave="stopEnergy"
      >
        +
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useGameStore } from '@/stores/gamestore'

const gameStore = useGameStore()
const time = ref(1)
const maxEnergy = ref(10) //Standardwert für Maximale Energie
let interval: number | null = null

onMounted(async () => {
  // Zeit
  const res = await fetch("/api/daten/cooldown")
  time.value = await res.json()

  //energie vom server
  const code = gameStore.gameData.gameCode
  const resEn = await fetch("/api/game/get?code=" + code)
  const data = await resEn.json()
  maxEnergy.value = data.maxCollectableEnergy
})

// Umsortiert in Zeit Block
function increaseTime() {
  time.value++
}

function decreaseTime() {
  if (time.value > 1) time.value--
}

function startTime(action: string) {
  if (interval) return

  const run = () => {
    if (action === 'increase') increaseTime()
    else decreaseTime()
  }

  run()
  interval = window.setInterval(run, 160)
}

function stopTime() {
  if (interval) {
    clearInterval(interval)
    interval = null
    fetch("/api/daten/cooldown?seconds=" + time.value, {
      method: "Post" 
    })
  }
}

// Energie Block
function increaseEnergy() {
  maxEnergy.value++
}

function decreaseEnergy() {
  if (maxEnergy.value > 1) maxEnergy.value--
}

// Start vom hoch und runterzählen wenn man maus gedrückt hält
function startEnergy(action: string) {
  if (interval) return

  const run = () => {
    if (action === 'increase') increaseEnergy()
    else decreaseEnergy()
  }

  run()
  interval = window.setInterval(run, 160)
}

// Stoppt das zählen und speichert den Wert
function stopEnergy() {
  if (interval) {
    clearInterval(interval)
    interval = null
    sendEnergy()
  }
}

// Sendet das gesetzte limit als an den Server
async function sendEnergy() {
  const { gameCode, playerId } = gameStore.gameData

  await fetch("/api/game/updateSettings", {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ 
      code: gameCode, 
      playerId: playerId, 
      maxEnergy: maxEnergy.value 
    })
  })
}
</script>

<style scoped>
  
.einstellung-view h3 {

  text-align: center;
  font-size: 1.6rem;
  font-weight: 700;

  color: white;

  padding-bottom: 0.4rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);

  margin-bottom: 1.5rem;
}

.einstellung-view {
  position: fixed;
  top: 5cm;
  left: 65%;
  width: 260px;
  background: rgba(0, 64, 0, 0.3);
  padding: 2rem;
  border-radius: 20px;
  backdrop-filter: blur(8px);
  z-index: 1000;
}

/* Eine Reihe */
.settings-control {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

/* Buttons */
.settingsButton {
  width: 55px;
  height: 55px;
  font-size: 2rem;
  font-weight: bold;
  color: white;

  border: 2px solid rgba(255,255,255,0.6);
  border-radius: 15px;

  background: rgba(255,255,255,0.15);
  cursor: pointer;
  transition: background 0.25s ease, transform 0.15s ease;
}


.settingsButton:hover {
  background: rgba(255,255,255,0.35);
}

.settingsButton:active {
  transform: scale(0.95);
}

/* Wert-Anzeige */
.valueDisplay {
  min-width: 100px;
  padding: 0.8rem 1rem;

  font-size: 1.4rem;
  font-weight: bold;
  color: white;
  text-align: center;

  border: 2px solid rgba(255,255,255,0.6);
  border-radius: 15px;

  background: rgba(255,255,255,0.20);
}
</style>