<script setup lang="ts">
import { useGameStore } from '@/stores/gamestore'
import { onMounted, ref } from 'vue'

const time = ref(1)
const maxEnergy = ref(10)
let interval: number | null = null
const presets = ref<string[]>([])

const gameStore = useGameStore()
const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'

onMounted(async () => {
  const gameCode = gameStore.gameData.gameCode
  if (!gameCode) return

  // Load Presets
  try {
    const res = await fetch(`${apiBase}/lobby/${gameCode}/boards/presets`)
    if (res.ok) presets.value = await res.json()
  } catch (err) {
    console.error('Presets failed', err)
  }

  // Load Current Settings
  try {
    const resEn = await fetch(`${apiBase}/game/get?code=${gameCode}`)
    const data = await resEn.json()
    maxEnergy.value = data.maxCollectableEnergy

    const resTime = await fetch(`${apiBase}/daten/cooldown`)
    time.value = await resTime.json()
  } catch (err) {
    console.error('Settings load failed', err)
  }
})

const startTimer = (type: 'time' | 'energy', action: 'increase' | 'decrease') => {
  if (interval) return
  const run = () => {
    if (type === 'time') {
      action === 'increase' ? time.value++ : time.value > 1 && time.value--
    } else {
      action === 'increase' ? maxEnergy.value++ : maxEnergy.value > 1 && maxEnergy.value--
    }
  }
  run()
  interval = window.setInterval(run, 160)
}

const stopTimer = (type: 'time' | 'energy') => {
  if (interval) {
    clearInterval(interval)
    interval = null
    // Sync to server immediately on release
    if (type === 'time') syncTime()
    else syncEnergy()
  }
}

const syncTime = () =>
  fetch(`${apiBase}/game/updateSettings`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: gameStore.gameData.gameCode,
      playerId: gameStore.gameData.playerId,
      cooldown: time.value,
    }),
  })

const syncEnergy = () => {
  fetch(`${apiBase}/game/updateSettings`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: gameStore.gameData.gameCode,
      playerId: gameStore.gameData.playerId,
      maxCollectableEnergy: maxEnergy.value,
    }),
  })
}
</script>

<template>
  <div class="settigs-panel-wrapper">
    <h3 class="panel-title">Spielregeln</h3>
    <div class="settings-section">
      <div class="setting-item">
        <span class="label">Dice Cooldown</span>
        <div class="control">
          <button
            class="arrow-btn"
            @mousedown="startTimer('time', 'decrease')"
            @mouseup="stopTimer('time')"
          >
            -
          </button>
          <div class="value-display">{{ time }}s</div>
          <button
            class="arrow-btn"
            @mousedown="startTimer('time', 'increase')"
            @mouseup="stopTimer('time')"
          >
            +
          </button>
        </div>
      </div>

      <div class="setting-item">
        <span class="label">Energy Limit</span>
        <div class="control">
          <button
            class="arrow-btn"
            @mousedown="startTimer('energy', 'decrease')"
            @mouseup="stopTimer('energy')"
          >
            -
          </button>
          <div class="value-display">{{ maxEnergy }}</div>
          <button
            class="arrow-btn"
            @mousedown="startTimer('energy', 'increase')"
            @mouseup="stopTimer('energy')"
          >
            +
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-section {
  display: flex;
  justify-content: space-around;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.setting-panel-wrapper {
  width: 250px;
  background-color: #3d2b1f;
  background-image:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%),
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 38px,
      rgba(0, 0, 0, 0.15) 39px,
      rgba(0, 0, 0, 0.15) 40px
    );
  border: 5px solid #2d1b0d;
  border-radius: 15px;
  padding: 30px 20px;
  display: flex;
  box-shadow:
    inset 0 0 20px rgba(0, 0, 0, 0.5),
    0 10px 20px rgba(0, 0, 0, 0.4);

  width: 350px;
  min-height: 260px;
}

.panel-title {
  color: #ffcc66;
  text-align: center;
  font-size: 2.4rem;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.label {
  color: #f0e2d0;
  font-size: 1rem;
  text-align: center;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.value-display {
  background: #f0e2d0;
  color: #2d1b0d;
  padding: 8px 15px;
  border-radius: 8px;
  font-weight: 900;
  min-width: 60px;
  text-align: center;
  border: 2px solid #2d1b0d;
}

.arrow-btn {
  width: 40px;
  height: 40px;
  background: #4d3319;
  color: white;
  border: 2px solid #2d1b0d;
  border-bottom-width: 4px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.2rem;
}

.arrow-Button:active {
  transform: translateY(2px);
  border-bottom-width: 2px;
}
</style>
