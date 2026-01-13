<!-- Infobox (useInfo) 
<template>
  <div class="lobby-container">
    <div v-if="infoText" class="info-box"> 
      {{ infoText }}
    </div>
  </div>
</template>

-->

<template>
  <div class="menu-container">
    <header class="header">
      <h1>Malefiz</h1>
    </header>

    <main class="main-content">
      <div class="login-card">
        <div class="button-group">
          <label for="loginname" class="input-label">Gib deinen Namen ein:</label>
          <input
            id="loginname"
            class="input-name"
            v-model="name"
            placeholder="..."
            required
            @keyup.enter="login"
          />

          <button class="btn create" @mouseover="playHover" @click="login">
            Abenteuer starten
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import hoverSoundFile from '../assets/button_hover.mp3'
import { useGameStore } from '@/stores/gamestore'

const gameStore = useGameStore()

const router = useRouter()
const name = ref('')

function playHover() {
  new Audio(hoverSoundFile).play()
}

function login() {
  if (!name.value.trim()) {
    alert('Bitte Namen eingeben!')
    return
  }

  gameStore.gameData.playerName = name.value
  router.push('/main')
}
</script>

<style scoped>
.login-card {
  background: rgba(20, 40, 20, 0.6);
  padding: 40px;
  border-radius: 20px;
  border: 2px solid rgba(167, 255, 131, 0.2);
  backdrop-filter: blur(5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
}

.input-label {
  color: #ffc107;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 2px;
  margin-bottom: -10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.input-name {
  width: 320px;
  padding: 15px;
  font-size: 1.5rem;
  font-family: 'Kanit', sans-serif;
  font-weight: 600;
  text-align: center;
  background-color: #f0e2d0;
  background-image: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.2), transparent);
  border: 4px solid #3d2b1f;
  border-radius: 12px;
  color: #2d1b0d;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.2);
  outline: none;
  transition: all 0.3s ease;
}

.input-name:focus {
  border-color: #4caf50;
  box-shadow:
    inset 0 2px 10px rgba(0, 0, 0, 0.2),
    0 0 15px rgba(76, 175, 80, 0.5);
  transform: scale(1.02);
}

.input-name::placeholder {
  color: rgba(45, 27, 13, 0.4);
}

.create {
  margin-top: 10px;
}
</style>
