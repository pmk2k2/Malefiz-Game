<template>
  <div 
    v-if="visible"
    class="fixed inset-0 bg-black/60 flex items-center justify-center z-50"
  >
    <div
      class="p-10 rounded-2xl shadow-2xl text-center max-w-md w-full bg-black/30 backdrop-blur-md border border-white/10"
    >
      <h2
        class="text-4xl font-extrabold mb-8 drop-shadow-lg"
        :class="isWinner 
          ? 'text-[#FFD85E]'
          : 'text-[#E03B3B]'
        "
      >
        {{ isWinner ? "Du hast gewonnen!" : "Du hast verloren!" }}
      </h2>

      <button
        @click="goBack"
        class="mt-4 px-8 py-4 rounded-xl text-xl font-extrabold tracking-wide text-white bg-[#B77A48] transition duration-200 shadow-lg"
      >
        Zurück
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useGameStore } from '@/stores/gamestore'

const gameStore = useGameStore();
const router = useRouter();

const visible = computed(() => gameStore.gameData.gameOver === true);

const isWinner = computed(() => {
  return gameStore.gameData.winnerId === gameStore.gameData.playerId;
});

function goBack() {
  gameStore.resetGameCode()
  router.push("/main");
}
</script>
<style scoped>
.fixed.inset-0 {
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
}


.bg-black\/30 {
  background-color: #3d2b1f !important;
  background-image: 
    linear-gradient(to bottom, rgba(0,0,0,0.3), transparent),
    repeating-linear-gradient(90deg, transparent, transparent 38px, rgba(0,0,0,0.1) 39px, rgba(0,0,0,0.1) 40px) !important;
  
 
  border: 6px solid #2d1b0d !important;
  border-radius: 25px !important;
  box-shadow: 0 20px 60px rgba(0,0,0,0.8), inset 0 0 20px rgba(0,0,0,0.5) !important;
  
  max-width: 500px !important;
  padding: 50px !important;
}


.bg-black\/30:has(.text-\[\#FFD85E\]) {
  border-color: #ffc107 !important;
  box-shadow: 0 0 30px rgba(255, 193, 7, 0.3), 0 20px 60px rgba(0,0,0,0.8) !important;
}


.bg-black\/30:has(.text-\[\#E03B3B\]) {
  border-color: #6d2d2d !important;
}

h2 {
  font-family: 'Kanit', sans-serif;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: 40px !important;
}

.text-\[\#FFD85E\] {
  color: #ffc107 !important; 
  text-shadow: 2px 4px 0px rgba(0,0,0,0.8);
}

.text-\[\#E03B3B\] {
  color: #e03b3b !important; 
  text-shadow: 2px 4px 0px rgba(0,0,0,0.8);
}


.bg-\[\#B77A48\] {
  background-color: #2d4d19 !important; 
  background-image: linear-gradient(to bottom, rgba(255,255,255,0.1), transparent) !important;
  
  border: 3px solid #1e3311 !important;
  border-bottom-width: 8px !important; 
  
  color: #f0e2d0 !important;
  font-family: 'Kanit', sans-serif;
  text-transform: uppercase;
  transition: all 0.1s ease !important;
}

.bg-\[\#B77A48\]:hover {
  filter: brightness(1.2);
  transform: translateY(-2px);
}

.bg-\[\#B77A48\]:active {
  transform: translateY(4px) !important;
  border-bottom-width: 2px !important;
}
</style>