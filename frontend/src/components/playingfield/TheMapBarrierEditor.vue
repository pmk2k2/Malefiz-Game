<script setup lang="ts">
import { TresCanvas, type TresObject } from '@tresjs/core'
import { computed, shallowRef } from 'vue'
import TheRock from './models/TheRock.vue'
import TheTree from './models/TheTree.vue'
import TheCrown from './models/TheCrown.vue'
import TheGrass from './models/TheGrass.vue'
import ThePlayerFigureCensored from './ThePlayerFigureCensored.vue'

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

interface ThePlayerFigureCensored {
    id: string | number
    position: [number, number, number]
}

const props = defineProps <{
    dummyGrid: Grid,
    figures: ThePlayerFigureCensored[]
}>()

const CELL_SIZE = 2

// Zellen auf Map-Koordinaten (x, y, z) mappen
function cellToField(cell: CellCoord): [number, number, number] {
  const x = (cell.i - props.dummyGrid.cols / 2 + 0.5) * CELL_SIZE // props.
  const z = -((cell.j - props.dummyGrid.rows / 2 + 0.5) * CELL_SIZE) // props.
 return [x, 0, z]
}

const camRef = shallowRef<TresObject | null>(null)
const camWidth = props.dummyGrid.cols * CELL_SIZE + 4
const camHeight = props.dummyGrid.rows * CELL_SIZE + 4
const distanz = 200
const default_cam_pos: [number, number, number] = [0, distanz, 0]

// Map aus den explizit gesetzten Feldern
const cellKey = (i: number, j: number) => `${i},${j}`
// 2D-Array aller Zellen des Spielfelds mit Typ, wobei nicht gesetzte Zellen 'blocked' sind
const allCells = computed<CellCoord[]>(() => {
  // Vom dummyGrid überschriebene Zellen
  const definedCells = new Map<string, CellType>()

  for (const cell of props.dummyGrid.cells) {
    definedCells.set(cellKey(cell.i, cell.j), cell.type)
  }

  // vollständiges Grid
  const gridOut: CellCoord[] = []
  for (let j = 0; j < props.dummyGrid.rows; j++) {
    for (let i = 0; i < props.dummyGrid.cols; i++) {
      gridOut.push({ i, j, type: definedCells.get(cellKey(i, j)) ?? 'blocked' })
    }
  }
  return gridOut
})

</script>

<template>
  <TresCanvas clear-color="#87CEEB" class="w-full h-full">
    <TresOrthographicCamera 
    ref="camRef" 
    :position="default_cam_pos" 
    :args=" [
        camWidth / -2,
        camWidth / 2,
        camHeight / 2,
        camHeight / -2,
        0.1,
        200
        ]"
        :look-at="[0, 0, 0]"
        />

    <!-- Licht -->
    <TresDirectionalLight :position="[20, 40, 10]" :intensity="2" />

    <!-- Boden -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
      <TresPlaneGeometry :args="[props.dummyGrid.cols * CELL_SIZE * 5, props.dummyGrid.rows * CELL_SIZE * 5]" />
      <TresMeshStandardMaterial color="#b6e3a5" :roughness="1" :metalness="0" />
    </TresMesh>

    <!-- Zellen mit Objekten je nach Typ -->
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

    <!-- Spielfigur(en) -->
    <ThePlayerFigureCensored
      v-for="(fig, index) in figures"
      :key="index"
      :position="fig.position"
    />
  </TresCanvas>
</template>
