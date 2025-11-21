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

const router = useRouter()

const playerName = localStorage.getItem('playerName')
const code = ref('')

function playHover() {
  new Audio(hoverSoundFile).play()
}

async function beitreten() {
  if (!code.value) {
    alert('Bitte Spielcode eingeben!')
    return
  }

  const res = await fetch('http://localhost:8080/api/game/join', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: playerName, code: code.value }),
  })

  const data = await res.json()

  if (!data.error) {
    localStorage.setItem('playerId', data.playerId)
    localStorage.setItem('gameCode', data.gameCode)
    router.push('/lobby')
  } else {
    alert(data.error)
  }
}

function zurueck() {
  router.push('/main')
}
</script>

<style>
.lobby-container {
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

.back {
  width: 420px;
  padding: 14px;
  font-size: 2rem;
  font-weight: bold;
  border-radius: 10px;
  cursor: pointer;
  background: linear-gradient(to bottom, #b77a48, #8a5c32);
}
.back:hover {
  transform: scale(1.2);
  background: linear-gradient(to bottom, #c88b58, #a86e3c);
}
</style>
