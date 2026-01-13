<template>
    <div class="mash">
        <div class="tutorial-mash">SPAMME [T] BIS DIE LEISTE VOLL IST!</div>

        <div class="mash-container">
        <div class="player-label left">GEGNER</div>
        <div class="bar-background">
        <div class="indicator" :style="{ left: barPosition + '%' }">
        <div class="glow"></div>
        </div>
      </div>
      <div class="player-label right">DU</div>
    </div>

    <div class="score-info">Power: {{ gameData.mashScore }}</div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useGameStore } from '@/stores/gamestore' 
import { storeToRefs } from 'pinia'

const gameStore = useGameStore()
const { gameData } = storeToRefs(gameStore)
const LIMIT = 20
let keyDown = false;

// mashScore in Prozent umwandeln
const barPosition = computed(() => {
    const score = gameData.value.mashScore || 0
    return ((score + LIMIT) / (LIMIT * 2)) * 100
})

async function sendMash() {
    await fetch('/api/duel/mashing', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
        gameCode: gameData.value.gameCode,
        playerId: gameData.value.playerId,
   })
  })
}

// Bei Tastenklick 'T' wird es über sendMash ans Backend geschickt mit gameCode und PlayerId um zu zeigen wer geklickt Player1 oder 2
function onKey(e: KeyboardEvent) {
    if (e.key.toLowerCase() === 't') {
        if (keyDown) { // Wenn gedrückt gehalten wird returnen (Sperre damit man nicht gedrückt hält)
            return
        }
        keyDown = true; 
        sendMash()
    }
}

// Immer wenn wir nicht T drücken wieder keyDown freigeben
function keyUp(e: KeyboardEvent) {
    if (e.key.toLowerCase() === 't') {
        keyDown = false;
    }
}

onMounted(() => {
    window.addEventListener('keydown', onKey)
    window.addEventListener('keyup', keyUp)
})

onUnmounted(() => {
    window.addEventListener('keydown', onKey)
    window.addEventListener('keyup', keyUp)
})

</script>

<style scoped>
.mash-zone { text-align: center; padding: 20px; }
.instruction { font-size: 32px; font-weight: 900; color: #ffcc00; margin-bottom: 30px; animation: blink 0.5s infinite; }

.mash-container {
  display: flex; align-items: center; gap: 15px; margin: 40px 0;
}

.bar-background {
  position: relative; flex-grow: 1; height: 30px;
  background: linear-gradient(90deg, #ff4d4d 0%, #4dff4d 100%);
  border-radius: 15px; border: 3px solid #000;
}

.indicator {
  position: absolute; top: -10px; width: 10px; height: 50px;
  background: #fff; border: 2px solid #000;
  transition: left 0.1s ease-out; transform: translateX(-50%);
}

.glow {
  width: 100%; height: 100%; box-shadow: 0 0 20px #fff;
}

.player-label { font-weight: 900; font-size: 18px; text-transform: uppercase; }

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>