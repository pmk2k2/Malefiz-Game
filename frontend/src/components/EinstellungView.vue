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
<<<<<<< HEAD
=======
  
.einstellung-view h3 {

  text-align: center;
  font-size: 1.6rem;
  font-weight: 700;

  color: white;

  padding-bottom: 0.4rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);

  margin-bottom: 1.5rem;
}

>>>>>>> Merge_EnergieSammeln+DuellVorbereitung
.einstellung-view {
  position: fixed;
  top: 15%; 
  left: 70%;
  width: 280px;
  background-color: #3d2b1f;
  background-image: 
    linear-gradient(to bottom, rgba(0,0,0,0.3) 0%, transparent 100%),
    repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0,0,0,0.15) 39px, rgba(0,0,0,0.15) 40px);
  
  padding: 1.5rem;
  border: 5px solid #2d1b0d;
  border-radius: 15px;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.5), 0 15px 30px rgba(0,0,0,0.6);
  z-index: 1000;
  font-family: 'Kanit', sans-serif;
}

.einstellung-view h3 {
  text-align: center;
  font-size: 1.3rem;
  font-weight: 800;
  color: #ffc107; 
  text-transform: uppercase;
  letter-spacing: 2px;
  padding-bottom: 0.6rem;
  border-bottom: 2px solid #2d1b0d;
  margin-bottom: 1.5rem;
  text-shadow: 2px 2px 0px rgba(0,0,0,0.8);
}

<<<<<<< HEAD
.time-control {
=======
/* Eine Reihe */
.settings-control {
>>>>>>> Merge_EnergieSammeln+DuellVorbereitung
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.timeDisplay {
  min-width: 90px;
  padding: 0.6rem;
  font-size: 1.5rem;
  font-weight: 900;
  color: #2d1b0d;
  text-align: center;
  background-color: #f0e2d0; 
  border: 3px solid #2d1b0d;
  border-radius: 10px;
  box-shadow: inset 0 2px 5px rgba(0,0,0,0.2);
}

<<<<<<< HEAD
.timeButton {
  width: 50px;
  height: 50px;
  font-size: 1.8rem;
  font-weight: 900;
  color: #f0e2d0;
=======
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
>>>>>>> Merge_EnergieSammeln+DuellVorbereitung
  cursor: pointer;
  
  background-color: #4d3319;
  background-image: linear-gradient(to bottom, rgba(255,255,255,0.1), transparent);
  border: 3px solid #2d1b0d;
  border-bottom-width: 6px; 
  border-radius: 12px;
  
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.1s ease;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
}

<<<<<<< HEAD
.timeButton:hover {
  filter: brightness(1.2);
}

.timeButton:active {
  transform: translateY(2px);
  border-bottom-width: 3px;
=======

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
>>>>>>> Merge_EnergieSammeln+DuellVorbereitung
}
</style>