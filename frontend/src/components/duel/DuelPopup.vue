<template>
  <div v-if="gameData.duelActive" class="overlay">
    <div class="popup">
      <div class="top">
        <div class="title">DUELL</div> 
        <!-- <div class="timer">{{ gameData.duelTimeLeft }}s</div> -->
      </div>
      <!-- 
      <div v-if="gameData.duelQuestion" class="content">
        <div class="question">{{ gameData.duelQuestion.text }}</div>

        <div class="answers">
          <button
            v-for="(a, i) in gameData.duelQuestion.answers"
            :key="i"
            class="answer"
            :disabled="gameData.duelAnswered || gameData.duelTimeLeft <= 0"
            @click="select(i)"
          >
            {{ a }}
          </button>
        </div>

        <div v-if="gameData.duelAnswered" class="status">Antwort gesendet…</div>
        <div v-else-if="gameData.duelTimeLeft <= 0" class="status">Zeit abgelaufen…</div>
      </div>
  
      <div v-else class="content"> 
        <div class="status">Frage wird geladen…</div>
      </div>
      --> 

      <div class="content">
        <DuelMash />
      </div>

    </div>
  </div>  
</template>

<script setup lang="ts">
import { onBeforeUnmount, watch } from 'vue'
import { useGameStore } from '@/stores/gamestore'
import { storeToRefs } from 'pinia'
import DuelMash from './DuelMash.vue'

const gameStore = useGameStore()
const { gameData } = storeToRefs(gameStore)

let interval: number | null = null

function startTimer() {
  stopTimer()
  interval = window.setInterval(() => {
    if (!gameData.value.duelActive) return

    if (gameData.value.duelTimeLeft <= 0) {
        stopTimer()

        if (!gameData.value.duelAnswered) {
        gameData.value.duelAnswered = true
        fetch('/api/duel/answer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
            gameCode: gameData.value.gameCode,
            playerId: gameData.value.playerId,
            answerIndex: -1,
            }),
        })
        }

        return
    }

    gameData.value.duelTimeLeft -= 1
    }, 1000)

}

function stopTimer() {
  if (interval) {
    clearInterval(interval)
    interval = null
  }
}

function select(index: number) {
  if (!gameData.value.duelQuestion) return
  if (gameData.value.duelAnswered) return
  if (gameData.value.duelTimeLeft <= 0) return

  gameData.value.duelAnswered = true

  fetch('/api/duel/answer', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      gameCode: gameData.value.gameCode,
      playerId: gameData.value.playerId,
      answerIndex: index,
    }),
  })
}

watch(
  () => gameData.value.duelQuestion,
  (q) => {
    if (q && gameData.value.duelActive) {
      gameData.value.duelTimeLeft = 10
      startTimer()
    } else {
      stopTimer()
    }
  },
  { immediate: true }
)


onBeforeUnmount(() => stopTimer())
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
}

.popup {
  width: 720px;
  max-width: 92vw;
  background: #1f1f1f;
  border: 4px solid #3d2b1f;
  border-radius: 18px;
  padding: 22px;
  color: #fff;
  font-family: 'Kanit', sans-serif;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.title {
  font-size: 28px;
  font-weight: 900;
  letter-spacing: 2px;
}

.timer {
  font-size: 22px;
  font-weight: 900;
  padding: 6px 12px;
  border-radius: 12px;
  background: rgba(255,255,255,0.12);
  border: 2px solid rgba(255,255,255,0.18);
}

.question {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 14px;
}

.answers {
  display: grid;
  gap: 12px;
}

.answer {
  padding: 14px;
  font-size: 18px;
  font-weight: 800;
  border-radius: 12px;
  cursor: pointer;
  border: 3px solid #2d1b0d;
  background: #4d3319;
  color: #fff;
}

.answer:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.status {
  margin-top: 14px;
  font-size: 16px;
  opacity: 0.85;
}
</style>
