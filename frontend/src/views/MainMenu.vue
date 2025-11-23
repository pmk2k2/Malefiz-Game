<template>
 <div class="container">
    
    <header class="header">
        <h1>Willkommen, {{ playerName }}</h1>
    </header>

    <main class="main-content">
      <div class="button-group">

        <button class="btn create" @mouseover="playHover" @click="spielErstellen">Spiel erstellen</button>
        <button class="btn join" @mouseover="playHover" @click="goJoin">Spiel beitreten</button>
        <button class="btn info" @mouseover="playHover">Regeln / Info</button>
        <button class="btn settings" @mouseover="playHover">Einstellungen</button>

      </div>

      <div class="player-list">
        <h2 style="text-align: center;">Aktive Spieler</h2>
        <ul>
          <li v-for="p in players" :key="p.id">{{ p.name }}</li>
        </ul>
      </div>
    </main>

    <footer class="footer">
      <button class="btn logout" @mouseover="playHover" @click="logout">Ausloggen</button>
    </footer>

 </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'; 
import { useRoute, useRouter } from 'vue-router';
import hoverSoundFile from '../assets/button_hover.mp3';

const route = useRoute();
const router = useRouter();
const playerName = ref(localStorage.getItem("playerName") || "");

const players = ref<{ id: string, name: string }[]>([]);

function playHover() {
  new Audio(hoverSoundFile).play();
}

function goJoin() {
  router.push("/join");
}


async function spielErstellen() {
  const res = await fetch('/api/game/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: playerName.value })
  });

  const data = await res.json();

  localStorage.setItem("playerId", data.playerId);
  localStorage.setItem("gameCode", data.gameCode);
  router.push("/lobby");
}

async function logout() {
  const playerId = localStorage.getItem("playerId");
  const gameCode = localStorage.getItem("gameCode");

  if (playerId && gameCode) {
    await fetch("/api/game/leave", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        playerId,
        code: gameCode
      })
    });
  }
  localStorage.clear();
  router.push("/");
}
</script>

<style>
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
  gap: 50px;
}

.player-list {
    background-color: rgba(0,0,0,0.5);
    color: white;
    padding: 20px;
    border-radius: 10px;
    min-width: 200px;
    min-height: 300px;
    overflow-y: auto;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.btn {
  width: 420px;
  padding: 12px 0;
  font-size: 2.1rem;
  font-weight: bold;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
}

.logout {
  background-color: #f44336;
  color: white;
  margin: 20px;
  padding: 10px 16px; 
  width: 120px;      
  font-size: 1rem;   
}

.btn:hover {
  transform: scale(1.20);
}
</style>