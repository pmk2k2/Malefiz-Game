<template>
  <div class="einstellung-view">
    <h3>WÜRFELZEIT</h3>

    <div class="time-control">
      <button
        class="timeButton"
        @mousedown="startHold('decrease')"
        @mouseup="stopHold"
        @mouseleave="stopHold"
      >
        -
      </button>

      <div class="timeDisplay">
        {{ time }} s
      </div>

      <button
        class="timeButton"
        @mousedown="startHold('increase')"
        @mouseup="stopHold"
        @mouseleave="stopHold"
      >
        +
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const time = ref(1)

onMounted(async () => {
  const res = await fetch("/api/daten/cooldown")
  time.value = await res.json()
})

let interval: number | null = null

function increase() {
  time.value++
}

function decrease() {
  if (time.value > 1) time.value--
}

async function sendCooldown() {
  await fetch("/api/daten/cooldown?seconds=" + time.value, {
    method: "Post"
  });
  
}


function startHold(action: 'increase' | 'decrease') {
  if (interval) return

  // Sofort einmal ausführen
  action === 'increase' ? increase() : decrease()

  // Dann beim Halten wiederholen
  interval = setInterval(() => {
    action === 'increase' ? increase() : decrease()
  }, 160) as unknown as number
}

function stopHold() {
  if (interval) {
    clearInterval(interval)
    interval = null
  }
  sendCooldown()
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
.time-control {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

/* Buttons */
.timeButton {
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

/* Hover Effekt */
.timeButton:hover {
  background: rgba(255,255,255,0.35);
}

.timeButton:active {
  transform: scale(0.95);
}

/* Zeit-Anzeige */
.timeDisplay {
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