<template>
  <div class="lobby-container">
    <header class="header">
      <h1>Spiel beitreten</h1>
      <h2>Willkommen, {{ playerName }}</h2>
    </header>

    <main class="main-content">
      <div class="form-box">
        <input v-model="code" placeholder="Spielcode eingeben" class="input" />

        <button class="btn join" @mouseover="playHover" @click="beitreten">Beitreten</button>

        <button class="btn back" @mouseover="playHover" @click="zurueck">Zurück</button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import hoverSoundFile from '../assets/button_hover.mp3'
import { useGameStore } from '@/stores/gamestore'

const router = useRouter()
const gameStore = useGameStore()

const playerName = gameStore.gameData.playerName
console.log(playerName)
const code = ref('')

function playHover() {
  new Audio(hoverSoundFile).play()
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

async function beitreten() {
  if (!code.value) {
    alert('Bitte Spielcode eingeben!')
    return
  }

  const res = await fetch(`${API_BASE_URL}/game/join`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: playerName, code: code.value }),
  })

  const data = await res.json()

  if (!data.error) {
    gameStore.gameData.playerId = data.playerId
    gameStore.gameData.gameCode = data.gameCode
    gameStore.gameData.isHost = false
    router.push('/lobby')
  } else {
    alert(data.error)
  }
}

function zurueck() {
  router.push('/main')
}
</script>

<style scoped>
.lobby-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('../assets/backg.jpg');
  background-size: cover;
  background-position: center;
  font-family: 'Kanit', sans-serif;
}

.header h1 {
  font-size: 3.5rem;
  font-weight: 900;
  background: linear-gradient(to bottom, #cefba0 20%, #4caf50 80%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(2px 4px 2px rgba(0, 0, 0, 0.8));
  text-transform: uppercase;
  margin-top: 40px;
}

.header h2 {
  color: #ffc107; 
  font-size: 1.4rem;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 2px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}


.form-box {
  display: flex;
  flex-direction: column;
  gap: 25px;
  background: rgba(20, 40, 20, 0.6);
  padding: 50px;
  border-radius: 20px;
  border: 2px solid rgba(167, 255, 131, 0.2);
  backdrop-filter: blur(5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
}


.input {
  width: 420px;
  padding: 15px;
  font-size: 1.8rem;
  font-family: 'Kanit', sans-serif;
  font-weight: 600;
  text-align: center;
  background-color: #f0e2d0; 
  border: 4px solid #3d2b1f;
  border-radius: 12px;
  color: #2d1b0d;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.2);
  outline: none;
  transition: all 0.3s ease;
}

.input:focus {
  border-color: #4caf50;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.2), 0 0 15px rgba(76, 175, 80, 0.5);
}

.btn {
  width: 100%;
  padding: 15px 0; 
  font-size: 1.6rem; 
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  border-radius: 12px; 
  border: 4px solid #2d1b0d; 
  border-bottom-width: 8px;   
  cursor: pointer;
  transition: all 0.2s ease;
  color: #f0e2d0; 
  text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.5);
  background-image: 
    repeating-linear-gradient(90deg, transparent, transparent 40px, rgba(0,0,0,0.1) 41px, rgba(0,0,0,0.1) 42px);
}

.join {
  background-color: #2d4d19 !important; 
  border-color: #1a3010;
}

.join:hover {
  background-color: #3a6320 !important;
  transform: translateY(2px);
  border-bottom-width: 4px;
  box-shadow: 0 10px 25px rgba(167, 255, 131, 0.3);
}

.back {
  background-color: #6d2d2d !important;
  border-color: #421a1a !important;
}

.back:hover {
  transform: translateY(2px);
  border-bottom-width: 4px;
  box-shadow: 0 10px 25px rgba(244, 67, 54, 0.3);
}
</style>
