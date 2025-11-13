<template>
 <div class="container">
    
    <header class="header">
        <h1> Willkommen bei Mi’lefiz</h1>
    </header>

    <main class="main-content">
      <div class="button-group">
        <button class="btn create" @mouseover="playHover" @click="spielErstellen">Spiel erstellen</button>
        <button class="btn join" @mouseover="playHover" @click="spielBeitreten">Spiel beitreten</button>
        <button class="btn info" @mouseover="playHover">Regeln / Info</button>
        <button class="btn settings" @mouseover="playHover">Einstellungen</button>
        <input v-model="playerName" placeholder="Dein Name" required/>
      </div>

      <div class="player-list">
        <h2 style="text-align: center;">Aktive Spieler</h2>
        <ul>
          <li v-for="p in players" :key="p.id">{{ p.name }}</li>
        </ul>
      </div>
    </main>

    <footer class = "footer">
      <button class="btn logout" @mouseover="playHover">Ausloggen</button>
    </footer>

 </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'; 
import router from '@/router';
import hoverSoundFile from '../assets/button_hover.mp3'

const playerName = ref('');
const players = ref<{ id: string, name: string }[]>([]);

function playHover() {
  const audio = new Audio(hoverSoundFile)
  audio.play()
}

async function spielErstellen() {
  if (!playerName.value) {
    alert('Bitte gib deinen Namen ein!');
    return;
  }

  const res = await fetch('http://localhost:8080/api/game/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: playerName.value })
  });

  const data = await res.json();
  console.log(data);

  if (data.gameCode) {
    router.push({
      path: '/lobby',
      query: { gameCode: data.gameCode, playerName: playerName.value }
    });
  } else {
    alert('Fehler beim Erstellen des Spiels');
  }
}

async function spielBeitreten() {
  if (!playerName.value) {
    alert('Bitte gib deinen Namen ein!');
    return;
  }

  const code = prompt('Bitte Spielcode eingeben:');
  if (!code) return;

  const res = await fetch('http://localhost:8080/api/game/join', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: playerName.value, code })
  });

  const data = await res.json();
  console.log(data);

  if (data.error) {
    alert(data.error);
  } else {
    router.push({
      path: '/lobby',
      query: { gameCode: code, playerName: playerName.value }
    });
  }
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