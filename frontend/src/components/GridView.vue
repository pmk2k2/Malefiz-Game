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
type CellType = 'path' | 'start' | 'goal' | 'blocked'

// Zellenkoordinten
interface CellCoord {
  i: number
  j: number
  type: CellType
}

// Das Spielfeld, cells für aktiven Spielfelder
interface Grid {
  cols: number
  rows: number
  cells: CellCoord[]
}

// Demo-Spielfeld, Werte werden zukünftig vom Backend gefüllt
const dummyGrid: Grid = {
  cols: 11,
  rows: 8,
  cells: [
    { i: 0, j: 0, type: 'start'},
    { i: 1, j: 0, type: 'start'},
    { i: 2, j: 0, type: 'start' },
    { i: 3, j: 0, type: 'start' },
    { i: 4, j: 0, type: 'start' },
    { i: 5, j: 0, type: 'start' },
    { i: 6, j: 0, type: 'start' },
    { i: 7, j: 0, type: 'start' },
    { i: 8, j: 0, type: 'start' },
    { i: 9, j: 0, type: 'start' },
    { i: 10, j: 0, type: 'start' },
    { i: 0, j: 2, type: 'path' },
    { i: 1, j: 2, type: 'path' },
    { i: 2, j: 2, type: 'path' },
    { i: 3, j: 2, type: 'path' },
    { i: 4, j: 2, type: 'path' },
    { i: 5, j: 2, type: 'path' },
    { i: 6, j: 2, type: 'path' },
    { i: 7, j: 2, type: 'path' },
    { i: 8, j: 2, type: 'path' },
    { i: 9, j: 2, type: 'path' },
    { i: 10, j: 2, type: 'path' },
    { i: 0, j: 4, type: 'path' },
    { i: 1, j: 4, type: 'path' },
    { i: 2, j: 4, type: 'path' },
    { i: 3, j: 4, type: 'path' },
    { i: 4, j: 4, type: 'path' },
    { i: 5, j: 4, type: 'path' },
    { i: 6, j: 4, type: 'path' },
    { i: 7, j: 4, type: 'path' },
    { i: 8, j: 4, type: 'path' },
    { i: 9, j: 4, type: 'path' },
    { i: 10, j: 4, type: 'path' },
    { i: 0, j: 6, type: 'path' },
    { i: 1, j: 6, type: 'path' },
    { i: 2, j: 6, type: 'path' },
    { i: 3, j: 6, type: 'path' },
    { i: 4, j: 6, type: 'path' },
    { i: 5, j: 6, type: 'path' },
    { i: 6, j: 6, type: 'path' },
    { i: 7, j: 6, type: 'path' },
    { i: 8, j: 6, type: 'path' },
    { i: 9, j: 6, type: 'path' },
    { i: 10, j: 6, type: 'path' },
    { i: 0, j: 1, type: 'path' },
    { i: 0, j: 5, type: 'path' },
    { i: 10, j: 1, type: 'path' },
    { i: 10, j: 5, type: 'path' },
    { i: 5, j: 3, type: 'path' },
    { i: 3, j: 1, type: 'path' },
    { i: 7, j: 1, type: 'path' },
    { i: 5, j: 7, type: 'goal' },
  ],
}

const CELL_SIZE = 2

// Map aus den explizit gesetzten Feldern
const key = (i:number, j:number) => `${i},${j}`
// 2D-Array aller Zellen des Spielfelds mit Typ, wobei nicht gesetzte Zellen 'blocked' sind
const allCells = computed<CellCoord[]>(() => {
  const overrides = new Map<string, CellType>()
  for (const cell of dummyGrid.cells) overrides.set(key(cell.i, cell.j), cell.type)

  const out: CellCoord[] = []
  for (let j = 0; j < dummyGrid.rows; j++) {
    for (let i = 0; i < dummyGrid.cols; i++) {
      out.push({ i, j, type: overrides.get(key(i, j)) ?? 'blocked' })
    }
  }
  return out
})

// Zellen auf Map-Koordinaten (x, y, z) mappen
function cellToField(cell: CellCoord): [number, number, number] {
  const x = (cell.i - dummyGrid.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - dummyGrid.rows / 2 + 0.5) * CELL_SIZE)

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
          :args="[dummyGrid.cols * CELL_SIZE * 5, dummyGrid.rows * CELL_SIZE * 5]"
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
        <template v-if="cell.type === 'path' || cell.type === 'start'">
          <TheRock />
        </template>
        <template v-else-if="cell.type === 'blocked'">
          <TheTree />
          <TheGrass />
        </template>
        <template v-else-if="cell.type === 'goal'">
          <TheRock />
          <TheCrown />
        </template>
      </TresMesh>
    </TresCanvas>

    <!-- UI-оверлей слева сверху -->
    <div class="ui-overlay">
      <h1 class="title">Malefiz – Würfeltest</h1>
      <RollButton buttonId="diceButton" @trigger="onRoll" />
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
