<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'

// Zellenkoordinten
interface CellCoord {
  i: number
  j: number
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
    { i: 5, j: 0 },
    { i: 0, j: 7 },
    { i: 1, j: 7 },
    { i: 2, j: 7 },
    { i: 3, j: 7 },
    { i: 4, j: 7 },
    { i: 5, j: 7 },
    { i: 6, j: 7 },
    { i: 7, j: 7 },
    { i: 8, j: 7 },
    { i: 9, j: 7 },
    { i: 10, j: 7 },
    { i: 0, j: 5 },
    { i: 1, j: 5 },
    { i: 2, j: 5 },
    { i: 3, j: 5 },
    { i: 4, j: 5 },
    { i: 5, j: 5 },
    { i: 6, j: 5 },
    { i: 7, j: 5 },
    { i: 8, j: 5 },
    { i: 9, j: 5 },
    { i: 10, j: 5 },
    { i: 0, j: 6 },
    { i: 10, j: 6 },
    { i: 3, j: 6 },
    { i: 7, j: 6 },
    { i: 5, j: 4 },
    { i: 0, j: 3 },
    { i: 1, j: 3 },
    { i: 2, j: 3 },
    { i: 3, j: 3 },
    { i: 4, j: 3 },
    { i: 5, j: 3 },
    { i: 6, j: 3 },
    { i: 7, j: 3 },
    { i: 8, j: 3 },
    { i: 9, j: 3 },
    { i: 10, j: 3 },
    { i: 0, j: 1 },
    { i: 1, j: 1 },
    { i: 2, j: 1 },
    { i: 3, j: 1 },
    { i: 4, j: 1 },
    { i: 5, j: 1 },
    { i: 6, j: 1 },
    { i: 7, j: 1 },
    { i: 8, j: 1 },
    { i: 9, j: 1 },
    { i: 10, j: 1 },
    { i: 0, j: 2 },
    { i: 10, j: 2 },
  ],
}

function cellToField(cell: CellCoord): [number, number, number] {
  const x = cell.i - dummyGrid.cols / 2 + 0.5
  const z = cell.j - dummyGrid.rows / 2 + 0.5

  return [x, 0.05, z]
}
</script>

<template>
  <div class="main-div">
    <h1>Milefiz - Game</h1>
    <div class="grid-div">
      <TresCanvas clear-color="#82DBC5">
        <TresPerspectiveCamera :position="[0, 10, 8]" :look-at="[0, 0, 0]" />

        <!-- Maussteuerung: Dreh/Zoom-Funktion -->
        <OrbitControls />

        <!-- Beleuchtung -->
        <TresAmbientLight :intensity="0.5" />
        <TresDirectionalLight :position="[5, 10, 5]" :intensity="0.8" />

        <!--Vertikale Linien -->
        <TresMesh
          v-for="n in dummyGrid.cols + 1"
          :key="`v-${n}`"
          :position="[n - 1 - dummyGrid.cols / 2, 0.011, 0]"
        >
          <TresBoxGeometry :args="[0.02, 0.001, dummyGrid.rows]" />
          <TresMeshBasicMaterial color="black" />
        </TresMesh>

        <!-- Horizontale Linien -->
        <TresMesh
          v-for="n in dummyGrid.rows + 1"
          :key="`h-${n}`"
          :position="[0, 0.011, n - 1 - dummyGrid.rows / 2]"
        >
          <TresBoxGeometry :args="[dummyGrid.cols, 0.011, 0.02]" />
          <TresMeshBasicMaterial color="black" />
        </TresMesh>

        <!--Path-Steine-->
        <TresMesh
          v-for="cell in dummyGrid.cells"
          :key="`path-${cell.i}-${cell.j}`"
          :position="cellToField(cell)"
          :rotation="[-Math.PI / 2, 0, 0]"
        >
          <TresCircleGeometry :args="[0.35, 32]" />
          <TresMeshStandardMaterial color="#444444" />
        </TresMesh>
      </TresCanvas>
    </div>
  </div>
</template>
