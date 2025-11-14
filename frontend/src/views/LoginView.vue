<template>
  <div class="container">

    <header class="header">
      <h1 v-if="mode === 'create'">Spiel erstellen</h1>
      <h1 v-if="mode === 'join'">Spiel beitreten</h1>
    </header>

    <main class="main-content">
      <div class="button-group">

        <input
          v-model="name"
          placeholder="Dein Name"
          class="input-name"
          required
        />

        <input
          v-if="mode === 'join'"
          v-model="code"
          placeholder="Spielcode"
          class="input-name"
          required
        />

        <button
          class="btn create"
          v-if="mode === 'create'"
          @mouseover="playHover"
          @click="createGame"
        >
          Spiel erstellen
        </button>

        <button
          class="btn join"
          v-if="mode === 'join'"
          @mouseover="playHover"
          @click="joinGame"
        >
          Beitreten
        </button>

        <button class="btn logout" @mouseover="playHover" @click="goBack">
          Zurück
        </button>

        <p style="color: red; font-weight: bold" v-if="error">{{ error }}</p>

      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import hoverSoundFile from '@/assets/button_hover.mp3';

const route = useRoute();
const router = useRouter();

const mode = route.query.mode as string;
const name = ref("");
const code = ref("");
const error = ref("");

function playHover() {
  new Audio(hoverSoundFile).play();
}

function goBack() {
  router.push("/");
}

async function createGame() {
  if (!name.value.trim()) {
    error.value = "Bitte Namen eingeben!";
    return;
  }

  const res = await fetch("http://localhost:8080/api/game/create", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name: name.value })
  });

  const data = await res.json();
  router.push({ path: "/lobby", query: { gameCode: data.gameCode, playerName: name.value } });
}

async function joinGame() {
  if (!name.value.trim() || !code.value.trim()) {
    error.value = "Name und Code erforderlich!";
    return;
  }

  const res = await fetch("http://localhost:8080/api/game/join", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name: name.value, code: code.value })
  });

  const data = await res.json();

  if (data.error) {
    error.value = data.error;
  } else {
    router.push({ path: "/lobby", query: { gameCode: code.value, playerName: name.value } });
  }
}
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-image: url('../assets/MainMenu_Background.jpeg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.header {
  padding: 20px;
  text-align: center;
}

.header h1 {
  font-size: 3rem;
  margin: 0;
  color: #ff0000;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-name {
  width: 420px;
  padding: 12px;
  font-size: 1.5rem;
  border-radius: 10px;
  border: none;
}

.btn {
  width: 420px;
  padding: 12px 0;
  font-size: 2.1rem;
  font-weight: bold;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn:hover {
  transform: scale(1.20);
}

.logout {
  background-color: #f44336;
  color: white;
  margin-top: 20px;
  font-size: 1.2rem;
}
</style>
