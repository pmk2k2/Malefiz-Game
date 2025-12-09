<template>
  <div class="clock">
    <!-- Aufzieh-Knopf oben -->
    <div class="clock-knob"></div>

    <!-- Zeit-Anzeige -->
    <div class="clock-number">
      {{ selectedTime }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGameStore } from '@/stores/gamestore'
import { computed, ref } from 'vue'
const store = useGameStore()

// Computed aus dem Store nutzen
const selectedTime = computed(() => store.countdown ?? 30)
</script>

<style scoped>
.clock {
  position: fixed;
  top: 5cm;
  left: 75%;
  width: 200px;
  height: 200px;
  background: #f5f0e1;
  border: 6px solid #333;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem auto;
  box-shadow:
    inset -4px -4px 8px rgba(0, 0, 0, 0.2),
    inset 4px 4px 8px rgba(255, 255, 255, 0.5),
    0 4px 15px rgba(0, 0, 0, 0.3);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Aufzieh-Knopf */
.clock-knob {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 25px;
  height: 12px;
  background: #333;
  border-radius: 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* Zahl in der Mitte */
.clock-number {
  font-size: 3rem;
  font-weight: bold;
  color: #333;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.2);
}
</style>
