<template>
  <div class="menu-container">
    <header class="header">
      <h1>Willkommen, {{ playerName }}</h1>
    </header>

    <main class="main-content">
      <div class="button-group">
        <button class="btn create" @mouseover="playHover" @click="spielErstellen">
          Spiel erstellen
        </button>
        <button class="btn join" @mouseover="playHover" @click="goJoin">Spiel beitreten</button>
        <button class="btn info" @mouseover="playHover" @click="toggleRules">Regeln / Info</button>
        <button class="btn settings" @mouseover="playHover">Einstellungen</button>
      </div>
    </main>

    <div v-if="showRules" class="rules-overlay" @click.self="toggleRules">
      <div class="rules-container">
        <button class="close-rules-btn" @click="toggleRules">✕</button>
        <InfoView @close="toggleRules" />
      </div>
    </div>

    <footer class="footer">
      <button class="btn logout" @mouseover="playHover" @click="logout">Ausloggen</button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import hoverSoundFile from '../assets/button_hover.mp3'
import { useGameStore } from '@/stores/gamestore'
import InfoView from '@/components/InfoView.vue'

const gameStore = useGameStore()
const showRules = ref(false)
const route = useRoute()
const router = useRouter()
const playerName = computed(() => gameStore.gameData.playerName ?? '')

const players = ref<{ id: string; name: string }[]>([])

function playHover() {
  new Audio(hoverSoundFile).play()
}

function goJoin() {
  router.push('/join')
}

function toggleRules() {
  showRules.value = !showRules.value
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL


async function spielErstellen() {
  const res = await fetch(`${API_BASE_URL}/game/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: playerName.value }),
  })

  const data = await res.json()

  gameStore.gameData.playerId = data.playerId
  gameStore.gameData.gameCode = data.gameCode
  gameStore.gameData.isHost = true
  router.push('/lobby')
}

async function logout() {
  const { playerId, gameCode } = gameStore.gameData

  if (playerId && gameCode) {
    await fetch(`${API_BASE_URL}/game/leave`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        playerId,
        code: gameCode,
      }),
    })
  }
  gameStore.reset()
  router.push('/')
}
</script>

<style>
.menu-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-image: linear-gradient(rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.4)), url('../assets/backg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  font-family: 'Kanit', sans-serif;
}

.header h1 {
  font-size: 4rem;
  font-weight: 900;
  background: linear-gradient(to bottom, #cefba0 20%, #4caf50 80%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;


  filter: drop-shadow(2px 4px 2px rgba(0, 0, 0, 0.8));


  text-shadow: 0 0 15px rgba(167, 255, 131, 0.5);

  text-transform: uppercase;
  letter-spacing: 4px;
  margin-top: 50px;
  text-align: center;

  transform: perspective(500px) rotateX(10deg);
}

.header h1 span {
  display: block;
  font-size: 1.2rem;
  color: #ffc107;
  letter-spacing: 8px;
  margin-bottom: -10px;
  -webkit-text-fill-color: #ffc107;
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
  gap: 18px;
  align-items: center;
}

.btn {
  width: 320px;
  padding: 15px 0;
  font-size: 1.5rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  border-radius: 12px;

  background-color: #4d3319;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.2) 0%, transparent 100%),
    repeating-linear-gradient(90deg, transparent, transparent 40px, rgba(0, 0, 0, 0.1) 41px, rgba(0, 0, 0, 0.1) 42px);
  background-size: 100% 100%, 100% 100%, 100% 100%, 50px 100%;

  border: 4px solid #3d2b1f;
  border-bottom-width: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #f0e2d0;
  text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.5);
  position: relative;
}


.btn:not(.create):not(.logout) {
  background-color: #2d4d19;
  border-color: #1e3311;
  color: #e0f2d8;
}

.btn:hover {
  transform: translateY(2px);
  border-bottom-width: 4px;
  filter: brightness(1.1);
}

/*
.info {
  background-color: #2e5a6e !important; 
  border-color: #1b3a47 !important;
  color: #d1ecf1 !important;
}

.settings {
  background-color: #2e5a6e !important; 
  border-color: #1b3a47 !important;
  color: #d1ecf1 !important;
}
*/
.create {
  background-color: #634121;
  border-color: #3d2b1f;
  color: #ffcc66;
}

.logout {
  background-color: #6d2d2d;
  border-color: #421a1a;
  width: 180px;
  font-size: 1.1rem;
  margin: 20px auto;
  display: block;
}

.footer {
  padding-bottom: 50px;
  text-align: center;
}

/* Rules Overlay */
.rules-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  backdrop-filter: blur(10px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
}

.rules-container {
  position: relative;
  width: 85vw;
  height: 85vh;
  max-width: 900px;
  background: #1a1a1a;
  border: 8px solid #3d2b1f;
  border-radius: 15px;
  box-shadow: 0 0 50px rgba(0, 0, 0, 1);
  overflow: auto;
  padding: 20px;
}

.close-rules-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 45px;
  height: 45px;
  background: #6d2d2d;
  color: white;
  border: 3px solid #421a1a;
  border-radius: 50%;
  font-weight: bold;
  cursor: pointer;
  z-index: 210;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.5);
  transition: transform 0.2s;
}

.close-rules-btn:hover {
  transform: scale(1.1) rotate(90deg);
  background: #a33535;
}
</style>