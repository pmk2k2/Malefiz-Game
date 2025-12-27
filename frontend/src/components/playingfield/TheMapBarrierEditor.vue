<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { computed, onMounted, ref, } from 'vue'
import TheRock from './models/TheRock.vue'
import TheTree from './models/TheTree.vue'
import TheCrown from './models/TheCrown.vue'
import TheGrass from './models/TheGrass.vue'
import ThePlayerFigureCensored from './ThePlayerFigureCensored.vue'
import Barrier from './models/Barrier.vue'
import { useGameStore } from '@/stores/gamestore'
import type { IPlayerFigure } from '@/stores/IPlayerFigure'
import ThePlayerFigure from './ThePlayerFigure.vue'

const figures = ref<IPlayerFigure[]>([])
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL' | 'BARRIER'

interface Field {
  i: number
  j: number
  type: CellType
}

interface Board {
  cols: number
  rows: number
  grid: Field[][]
}

const props = defineProps <{
  board: Board | null
  figures: IPlayerFigure[]
}>()

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const gameStore = useGameStore()
const gameCode = gameStore.gameData.gameCode
const isLoading = ref(true)
const CELL_SIZE = 2
const board = ref<Board | null>(null)
const myId = gameStore.gameData.playerId

/*
async function getBoardFromBackend(): Promise<Board | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/board?code=${gameCode}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const text = await response.text()
    if (!text) return null

    const data = JSON.parse(text) as Board
    console.log('MapBarrierEditor: Board received', data)
    return data
  } catch (err) {
    console.error('MapBarrierEditor: Error getting board:', err)
    return null
  }
}

onMounted(async () => {
  if (board.value) {
    console.log('Spielfeld ist bereits gefetcht worden (Wenn das in Konsole steht wird mehrmals das Spielfeld gefetcht statt einmalig zu beginn also problem)')
    return
  }
  isLoading.value = true
  const fetched = await getBoardFromBackend()
  if (fetched) {
    board.value = fetched
  } else {
    console.error('MapBarrierEditor: Board konnte nicht geladen werden.')
  }
  isLoading.value = false
})
*/

const allCells = computed<Field[]>(() => {
  if (!props.board) return []
  return props.board.grid.flat()
})

function cellToField(cell: Field): [number, number, number] {
  if (!props.board) return [0, 0, 0]

  const x = (cell.i - props.board.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - props.board.rows / 2 + 0.5) * CELL_SIZE)

  return [x, 0.05, z]
}

const camWidth = computed(() => (props.board?.cols || 1) * CELL_SIZE)
const camHeight = computed(() => (props.board?.rows || 1) * CELL_SIZE)

</script>
  
<template>
  <TresCanvas>
    <TresOrthographicCamera
      v-if="props.board"
      :args="[
      -camWidth / 1, 
      camWidth / 1, 
      camHeight / 1.5, 
      -camHeight / 1.5, 
      0.1, 
      1000
      ]"
      :position="[0, 50, 0]"
      :look-at="[0, 0, 0]"
    />

    <TresAmbientLight :intensity="1" />
    <TresDirectionalLight :position="[10, 20, 10]" :intensity="1" />

    <template v-if="props.board">
      <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
        <TresPlaneGeometry :args="[props.board.cols * CELL_SIZE * 5, props.board.rows * CELL_SIZE * 5]" />
        <TresMeshStandardMaterial color="#b6e3a5" :roughness="1" :metalness="0" />
      </TresMesh>

      <TresMesh
        v-for="cell in allCells"
        :key="`cell-${cell.i}-${cell.j}`"
        :position="cellToField(cell)"
        :rotation="[-Math.PI / 2, 0, 0]"
      >
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
        <template v-else-if="cell.type === 'BARRIER'">
          <Barrier />
        </template>
      </TresMesh>

      <template v-for="fig in props.figures" :key="fig.id">
        <ThePlayerFigure
          v-if="fig.playerId === myId"
          :position="fig.position"
          :color="fig.color"
          :orientation="fig.orientation"
        />

        <ThePlayerFigureCensored
          v-else
          :position="fig.position"
          :color="fig.color"
          :playerId="fig.playerId"
        />
      </template>
      
    </template>
  </TresCanvas>
</template>
