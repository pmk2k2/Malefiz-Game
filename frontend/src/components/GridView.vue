<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import { computed, onMounted, ref } from 'vue'
import TheRock from './TheRock.vue'
import TheTree from './TheTree.vue'
import TheCrown from './TheCrown.vue'
import TheGrass from './TheGrass.vue'
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import { useGameStore } from '@/stores/gamestore'


// Zellentypen
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL'

// Zellenkoordinten
interface Field {
  i: number
  j: number
  type: CellType
}

// Das Spielfeld, grid für aktiven Spielfelder
interface Board {
  cols: number
  rows: number
  grid: Field[][]
}

const gameStore = useGameStore()
const CELL_SIZE = 2
const board = ref<Board | null>(null)
const isLoading = ref(true)
const gameCode = gameStore.gameData.gameCode

onMounted(async () => {
  isLoading.value = true
  const fetched = await getBoardFromBackend()
  console.log(fetched)
  if (fetched) {
    board.value = fetched
  }
  isLoading.value = false
})

async function sendBoard(boardData: Board) {
  try {
    const response = await fetch(`/api/board?code=${gameCode}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(boardData),
    })

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log('Board sent successfully:', data)
  } catch (err) {
    console.error('Error sending board:', err)
  }
}

async function getBoardFromBackend(): Promise<Board | null> {
  try {
    const response = await fetch(`/api/board?code=${gameCode}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    // Check if body exists before parsing
    const text = await response.text()
    if (!text) return null

    const data = JSON.parse(text) as Board
    console.log('Board received', data)
    return data
  } catch (err) {
    console.error('Error getting board:', err)
    return null
  }
}

// 2D-Array aller Zellen des Spielfelds mit Typ
const allCells = computed<Field[]>(() => {
  if (!board.value) return []
  return board.value.grid.flat()
})

// Zellen auf Map-Koordinaten (x, y, z) mappen
function cellToField(cell: Field): [number, number, number] {
  if (!board.value) return [0, 0, 0]

  const x = (cell.i - board.value.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - board.value.rows / 2 + 0.5) * CELL_SIZE)

  return [x, 0.05, z]
}

function onRoll(id: string) {
  console.log('Button pressed:', id)
  rollDice()
}
</script>

<template>
  <div class="game-wrapper">
    <TresCanvas clear-color="#87CEEB" class="game-canvas">
      <TresPerspectiveCamera :position="[0, 15, 18]" :look-at="[0, 0, 0]" />
      <OrbitControls />

      <TresDirectionalLight :position="[20, 40, 10]" :intensity="1.5" />

      <template v-if="board">
        <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
          <TresPlaneGeometry :args="[board.cols * CELL_SIZE * 5, board.rows * CELL_SIZE * 5]" />
          <TresMeshStandardMaterial color="#b6e3a5" :roughness="1" :metalness="0" />
        </TresMesh>

        <TresMesh v-for="cell in allCells" :key="`cell-${cell.i}-${cell.j}`" :position="cellToField(cell)"
          :rotation="[-Math.PI / 2, 0, 0]">
          <template v-if="cell.type === 'PATH' || cell.type === 'START'">
            <TheRock />
          </template>
          <template v-else-if="cell.type === 'BLOCKED'">
            <TheTree />
            <TheGrass />
          </template>
          <template v-else-if="cell.type === 'GOAL'">
            <TheRock />
            <TheCrown />
          </template>
        </TresMesh>
      </template>

    </TresCanvas>

    <div class="ui-overlay">
      <h1 class="title">Malefiz – Würfeltest</h1>

      <div v-if="isLoading">Loading Board...</div>
      <div v-else>
        <RollButton buttonId="diceButton" @trigger="onRoll" />
        <Dice3D />
      </div>
    </div>
  </div>
</template>

<style scoped>
.game-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.game-canvas {
  width: 100%;
  height: 100%;
}

/* панель поверх канвы, в левом верхнем углу */
.ui-overlay {
  position: absolute;
  top: 1rem;
  left: 1rem;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
  color: #fff;
  backdrop-filter: blur(4px);
}

.title {
  margin: 0;
  font-size: 1.2rem;
}
</style>