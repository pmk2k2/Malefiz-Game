<template>
  <div class="container">

    <header class="header">
      <h1>Spiel beitreten</h1>
      <h2>Willkommen, {{ playerName }}</h2>
    </header>

    <main class="main-content">
      <div class="form-box">
        <input 
          v-model="code"
          placeholder="Spielcode eingeben"
          class="input"
        />

        <button class="btn join" @click="beitreten">
          Beitreten
        </button>

        <button class="btn back" @click="zurueck">
          Zurück
        </button>
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

async function beitreten() {
  if (!code.value) {
    alert("Bitte Spielcode eingeben!")
    return
  }

  const res = await fetch('/api/game/join', {
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
  router.push("/main")
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
}

.header {
  padding: 20px;
  text-align: center;
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
  gap: 20px;
}

.input {
  width: 420px;
  padding: 12px;
  font-size: 1.8rem;
}

.btn {
  width: 420px;
  padding: 14px;
  font-size: 2rem;
  font-weight: bold;
  border-radius: 10px;
  cursor: pointer;
}
</style>
