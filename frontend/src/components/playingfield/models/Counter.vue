<template>
  <div class="counter-container">
    <div class="counter-label">SPIEL STARTET IN</div>
    <div class="counter-number">
      {{ selectedTime }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { useGameStore } from '@/stores/gamestore'
import { computed, ref } from 'vue'
const store = useGameStore()

// Computed aus dem Store nutzen
const selectedTime = computed(() => store.countdown ?? 10)
</script>

<style scoped>
.counter-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  
  width: 240px;
  height: 240px;
  
  background-color: #a06f2f;
  background-image: 
    repeating-radial-gradient(circle at center, #d2b48c 0px, #c19a6b 20px, #d2b48c 40px),
    linear-gradient(to bottom, rgba(255,255,255,0.2), transparent);
  background-blend-mode: multiply;

  border: 10px solid #3d2b1f; 
  border-radius: 30px; 
  
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 2000;

  box-shadow: 
    0 20px 50px rgba(0, 0, 0, 0.9),
    inset 0 0 40px rgba(0, 0, 0, 0.2),
    0 0 30px rgba(255, 193, 7, 0.3);
  
  animation: magic-pulse 1s infinite alternate ease-in-out;
}

.counter-label {
  font-family: 'Kanit', sans-serif;
  color: #3d2b1f; 
  font-size: 1.1rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: -15px;
  text-shadow: 1px 1px 0px rgba(255,255,255,0.5);
}

.counter-number {
  font-family: 'Kanit', sans-serif;
  font-size: 8rem;
  font-weight: 900;
  color: #2d1b0d; 
  text-shadow: 
    1px 1px 0px rgba(255,255,255,0.4),
    -1px -1px 0px rgba(0,0,0,0.5);
}

@keyframes magic-pulse {
  0% { 
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.9), 0 0 20px rgba(255, 193, 7, 0.2);
  }
  100% { 
    transform: translate(-50%, -50%) scale(1.06);
    border-color: #4d3319;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.9), 0 0 40px rgba(255, 193, 7, 0.4);
  }
}
</style>
