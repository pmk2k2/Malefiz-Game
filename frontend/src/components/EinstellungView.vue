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

.time-control {
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

.timeButton {
  width: 50px;
  height: 50px;
  font-size: 1.8rem;
  font-weight: 900;
  color: #f0e2d0;
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

.timeButton:hover {
  filter: brightness(1.2);
}

.timeButton:active {
  transform: translateY(2px);
  border-bottom-width: 3px;
}
</style>