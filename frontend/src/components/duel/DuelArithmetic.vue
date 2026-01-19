<template>
  <div class="arithmetic-game">
    <div class="header">SCHNELLES RECHNEN</div>
    
    <div v-if="gameData.duelQuestion" class="math-container">
      <div class="equation-box">
        {{ gameData.duelQuestion.text }}
      </div>

      <div class="answers-grid">
        <button
          v-for="(answer, index) in gameData.duelQuestion.answers"
          :key="index"
          class="math-btn"
          :disabled="buttonsDisabled"
          @click="submitAnswer(answer)"
        >
          {{ answer }}
        </button>
      </div>
      
      <div v-if="hasAnswered" class="status-msg">
        Antwort gesendet...
      </div>
    </div>
    
    <div v-else class="loading">
      Berechne Aufgaben...
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useGameStore } from '@/stores/gamestore'
import { storeToRefs } from 'pinia'

const gameStore = useGameStore()
const { gameData } = storeToRefs(gameStore)

const hasAnswered = ref(false)
const buttonsDisabled = ref(false)

async function submitAnswer(answerVal: string) {
  if (buttonsDisabled.value) return
  
  // Buttons sofort deaktivieren, damit man nicht doppelt klickt
  buttonsDisabled.value = true
  hasAnswered.value = true

  try {
    // Post an den neuen Controller-Endpunkt
    await fetch('/api/duel/arithmetic/answer', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        gameCode: gameData.value.gameCode,
        playerId: gameData.value.playerId,
        answer: answerVal // Den String senden (z.B. "12") und nicht den Index
      })
    })
  } catch (e) {
    console.error("Fehler beim Senden der Mathe-Antwort", e)
    buttonsDisabled.value = false 
    hasAnswered.value = false
  }
}
</script>

<style scoped>
.arithmetic-game {
  text-align: center;
  font-family: 'Kanit', sans-serif;
  color: white;
  padding: 10px;
  animation: fadeIn 0.3s ease-out;
}

.header {
  font-size: 1.8rem;
  font-weight: 900;
  color: #fbbf24;
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.equation-box {
  background: rgba(0, 0, 0, 0.4);
  border: 4px solid #fff;
  border-radius: 15px;
  padding: 20px;
  font-size: 2.5rem;
  font-weight: 900;
  margin-bottom: 30px;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.1);
  text-shadow: 2px 2px 0 #000;
}

.answers-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.math-btn {
  background: #3d2b1f; 
  color: #fff;
  border: 3px solid #5e4335;
  border-bottom-width: 6px;
  border-radius: 12px;
  font-size: 2rem;
  font-weight: bold;
  padding: 15px;
  cursor: pointer;
  transition: all 0.1s;
  text-shadow: 1px 1px 2px black;
}

.math-btn:hover:not(:disabled) {
  background: #5e4335;
  transform: translateY(-2px);
  filter: brightness(1.1);
}

.math-btn:active:not(:disabled) {
  transform: translateY(3px);
  border-bottom-width: 3px;
}

.math-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(0.8);
  transform: none;
  border-bottom-width: 3px;
}

.status-msg {
  margin-top: 20px;
  font-size: 1.2rem;
  color: #aaa;
  font-style: italic;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}
</style>