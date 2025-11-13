<template>
  <div class="container p-6">
    <h2 class="text-2xl font-bold mb-4">Lobby</h2>

    <section class="mb-4">
      <p><strong>Game Code:</strong> <b>{{ gameCode }}</b></p>
      <p><strong>Player Name:</strong> <b>{{ playerName }}</b></p>
    </section>

    <section>
      <h3 class="font-semibold mb-2">Aktive Spieler:</h3>
      <ul>
        <li v-for="p in players" :key="p.id">{{ p.name }}</li>
        <li v-if="players.length === 0">Waiting for players...</li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const gameCode = ref(route.query.gameCode as string || '');
const playerName = ref(route.query.playerName as string || '');
const players = ref<{ id: string, name: string }[]>([]);
</script>

<style scoped>
.container {
  max-width: 600px;
  margin: 0 auto;
}
</style>
