<template>
  <div class="login-container">
    <header class="header">
      <h1>Login</h1>
    </header>

    <main class="main-content">
      <div class="button-group">
        <input class="input-name" v-model="name" placeholder="Dein Name" required />

        <button class="btn create" @mouseover="playHover" @click="login">Weiter</button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import hoverSoundFile from '../assets/button_hover.mp3'

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

  localStorage.setItem('playerName', name.value)
  router.push('/main')
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-image: url('../assets/MainMenu_Background.jpeg');
  background-size: cover;
}
.input-name {
  width: 420px;
  padding: 12px;
  font-size: 1.5rem;
  border-radius: 10px;
  border: none;
}
.create {
  width: 420px;
  padding: 12px 0;
  font-size: 2.1rem;
  font-weight: bold;
  border-radius: 10px;
  border: none;
  background: linear-gradient(to bottom, #b77a48, #8a5c32);
  color: #111827;
}
.create:hover {
  background: linear-gradient(to bottom, #c88b58, #a86e3c);
}
</style>
