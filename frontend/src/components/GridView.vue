<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import { computed } from 'vue'
import TheRock from './TheRock.vue'
import TheTree from './TheTree.vue'
import TheCrown from './TheCrown.vue'
import TheGrass from './TheGrass.vue'
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'

//Zellentypen
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL';

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

// Demo-Spielfeld, Werte werden zukünftig vom Backend gefüllt
const dummyBoard: Board = {
  cols: 11,
  rows: 8,
  grid: [
    // Row 0 (j=0)
    [
      { i: 0, j: 0, type: 'START' },
      { i: 1, j: 0, type: 'BLOCKED' },
      { i: 2, j: 0, type: 'START' },
      { i: 3, j: 0, type: 'START' },
      { i: 4, j: 0, type: 'BLOCKED' },
      { i: 5, j: 0, type: 'START' },
      { i: 6, j: 0, type: 'BLOCKED' },
      { i: 7, j: 0, type: 'START' },
      { i: 8, j: 0, type: 'BLOCKED' },
      { i: 9, j: 0, type: 'BLOCKED' },
      { i: 10, j: 0, type: 'START' },
    ],
    // Row 1 (j=1)
    [
      { i: 0, j: 1, type: 'PATH' },
      { i: 1, j: 1, type: 'BLOCKED' },
      { i: 2, j: 1, type: 'BLOCKED' },
      { i: 3, j: 1, type: 'PATH' },
      { i: 4, j: 1, type: 'BLOCKED' },
      { i: 5, j: 1, type: 'BLOCKED' },
      { i: 6, j: 1, type: 'BLOCKED' },
      { i: 7, j: 1, type: 'PATH' },
      { i: 8, j: 1, type: 'BLOCKED' },
      { i: 9, j: 1, type: 'BLOCKED' },
      { i: 10, j: 1, type: 'PATH' },
    ],
    // Row 2 (j=2)
    [
      { i: 0, j: 2, type: 'PATH' },
      { i: 1, j: 2, type: 'PATH' },
      { i: 2, j: 2, type: 'PATH' },
      { i: 3, j: 2, type: 'PATH' },
      { i: 4, j: 2, type: 'PATH' },
      { i: 5, j: 2, type: 'PATH' },
      { i: 6, j: 2, type: 'PATH' },
      { i: 7, j: 2, type: 'PATH' },
      { i: 8, j: 2, type: 'PATH' },
      { i: 9, j: 2, type: 'PATH' },
      { i: 10, j: 2, type: 'PATH' },
    ],
    // Row 3 (j=3)
    [
      { i: 0, j: 3, type: 'BLOCKED' },
      { i: 1, j: 3, type: 'BLOCKED' },
      { i: 2, j: 3, type: 'BLOCKED' },
      { i: 3, j: 3, type: 'BLOCKED' },
      { i: 4, j: 3, type: 'BLOCKED' },
      { i: 5, j: 3, type: 'PATH' },
      { i: 6, j: 3, type: 'BLOCKED' },
      { i: 7, j: 3, type: 'BLOCKED' },
      { i: 8, j: 3, type: 'BLOCKED' },
      { i: 9, j: 3, type: 'BLOCKED' },
      { i: 10, j: 3, type: 'BLOCKED' },
    ],
    // Row 4 (j=4)
    [
      { i: 0, j: 4, type: 'PATH' },
      { i: 1, j: 4, type: 'PATH' },
      { i: 2, j: 4, type: 'PATH' },
      { i: 3, j: 4, type: 'PATH' },
      { i: 4, j: 4, type: 'PATH' },
      { i: 5, j: 4, type: 'PATH' },
      { i: 6, j: 4, type: 'PATH' },
      { i: 7, j: 4, type: 'PATH' },
      { i: 8, j: 4, type: 'PATH' },
      { i: 9, j: 4, type: 'PATH' },
      { i: 10, j: 4, type: 'PATH' },
    ],
    // Row 5 (j=5)
    [
      { i: 0, j: 5, type: 'PATH' },
      { i: 1, j: 5, type: 'BLOCKED' },
      { i: 2, j: 5, type: 'BLOCKED' },
      { i: 3, j: 5, type: 'BLOCKED' },
      { i: 4, j: 5, type: 'BLOCKED' },
      { i: 5, j: 5, type: 'BLOCKED' },
      { i: 6, j: 5, type: 'BLOCKED' },
      { i: 7, j: 5, type: 'BLOCKED' },
      { i: 8, j: 5, type: 'BLOCKED' },
      { i: 9, j: 5, type: 'BLOCKED' },
      { i: 10, j: 5, type: 'PATH' },
    ],
    // Row 6 (j=6)
    [
      { i: 0, j: 6, type: 'PATH' },
      { i: 1, j: 6, type: 'PATH' },
      { i: 2, j: 6, type: 'PATH' },
      { i: 3, j: 6, type: 'PATH' },
      { i: 4, j: 6, type: 'PATH' },
      { i: 5, j: 6, type: 'PATH' },
      { i: 6, j: 6, type: 'PATH' },
      { i: 7, j: 6, type: 'PATH' },
      { i: 8, j: 6, type: 'PATH' },
      { i: 9, j: 6, type: 'PATH' },
      { i: 10, j: 6, type: 'PATH' },
    ],
    // Row 7 (j=7)
    [
      { i: 0, j: 7, type: 'BLOCKED' },
      { i: 1, j: 7, type: 'BLOCKED' },
      { i: 2, j: 7, type: 'BLOCKED' },
      { i: 3, j: 7, type: 'BLOCKED' },
      { i: 4, j: 7, type: 'BLOCKED' },
      { i: 5, j: 7, type: 'GOAL' },
      { i: 6, j: 7, type: 'BLOCKED' },
      { i: 7, j: 7, type: 'BLOCKED' },
      { i: 8, j: 7, type: 'BLOCKED' },
      { i: 9, j: 7, type: 'BLOCKED' },
      { i: 10, j: 7, type: 'BLOCKED' },
    ],
  ],
};

const CELL_SIZE = 2

async function sendBoard(board: typeof dummyBoard) {
  try {
    const response = await fetch('/api/temporary-board', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(board)
    });

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const data = await response.json();
    console.log('Board sent successfully:', data);
  } catch (err) {
    console.error('Error sending board:', err);
  }
}

// async function getBoardFromBackend(){
//   try{
//     const response = await fetch("api/temporary-board", {
//       method: "GET",
//       headers: {
//         'Content-Type': 'application/json'
//       }
//     });

//     const temporaryBoard: Board = await 
//   }
// }

function onSend(id: string) {
  console.log('Button pressed:', id);
  sendBoard(dummyBoard);
}

// Map aus den explizit gesetzten Feldern
const key = (i:number, j:number) => `${i},${j}`
// 2D-Array aller Zellen des Spielfelds mit Typ, wobei nicht gesetzte Zellen 'BLOCKED' sind
const allCells = computed<Field[]>(() => {
  return dummyBoard.grid.flat()
})

// Zellen auf Map-Koordinaten (x, y, z) mappen
function cellToField(cell: Field): [number, number, number] {
  const x = (cell.i - dummyBoard.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - dummyBoard.rows / 2 + 0.5) * CELL_SIZE)

  return [x, 0.05, z]
}

function onRoll(id: string) {
  console.log('Button pressed:', id)
  rollDice()
}
</script>

<template>
  <div class="game-wrapper">
    <!-- 3D-сцена на фоне -->
    <TresCanvas clear-color="#87CEEB" class="game-canvas">
      <TresPerspectiveCamera :position="[0, 15, 18]" :look-at="[0, 0, 0]" />
      <OrbitControls />

      <!-- Licht -->
      <TresDirectionalLight :position="[20, 40, 10]" :intensity="1.5" />

      <!-- Boden -->
      <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
        <TresPlaneGeometry
          :args="[dummyBoard.cols * CELL_SIZE * 5, dummyBoard.rows * CELL_SIZE * 5]"
        />
        <TresMeshStandardMaterial
          color="#b6e3a5"
          :roughness="1"
          :metalness="0"
        />
      </TresMesh>

      <!-- клетки -->
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
      </TresMesh>
    </TresCanvas>

    <!-- UI-оверлей слева сверху -->
    <div class="ui-overlay">
      <h1 class="title">Malefiz – Würfeltest</h1>
      <RollButton buttonId="diceButton" @trigger="onRoll" />
      <RollButton buttonId="sendButton" @trigger="onSend" />
      <Dice3D />
    </div>
  </div>
</template>

<style scoped>
.game-wrapper {
  position: relative;
  width: 100%;
  height: 100vh; /* полноэкранная сцена */
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
