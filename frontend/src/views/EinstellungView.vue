<template>
  <div class="einstellung-view">
    <h3>WÜRFELZEIT wählen:</h3>

    <div class="time-options">
      <div
        class="time-box"
        v-for="time in times"
        :key="time"
        :class="{ active: selectedTime === time }"
        @click="selectedTime = time"
      >
        {{ time }} s
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const times = [20, 30, 40, 50]
const selectedTime = ref(20)
</script>

<style scoped>
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

/* Container für die Buttons */
.time-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* Runder Viereck (runde Boxen) */
.time-box {
  background: rgba(255, 255, 255, 0.2);
  padding: 1rem;
  border-radius: 15px;     /* rundes Viereck */
  text-align: center;
  font-size: 1.2rem;
  cursor: pointer;
  transition: 0.2s ease;
  border: 2px solid transparent;
}

/* Hover-Effekt */
.time-box:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* Ausgewählte Option */
.time-box.active {
  border-color: gold;
  background: rgba(255, 255, 255, 0.45);
  font-weight: bold;
}
</style>
