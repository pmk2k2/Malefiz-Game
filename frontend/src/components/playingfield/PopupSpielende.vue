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

const visible = computed(() => gameStore.gameData.gameOver);

const isWinner = computed(() => {
  return gameStore.gameData.winnerId === gameStore.gameData.playerId;
});

function goBack() {
  gameStore.resetGameCode()
  router.push("/main");
}
</script>
