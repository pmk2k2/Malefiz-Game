<script setup lang="ts">
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import { computed, ref, shallowRef, onMounted, onUnmounted } from 'vue'
import TheRock from './models/TheRock.vue'
import TheTree from './models/TheTree.vue'
import TheCrown from './models/TheCrown.vue'
import TheGrass from './models/TheGrass.vue'

import ThePlayerFigure from './ThePlayerFigure.vue'
import type { IPlayerFigure } from '@/stores/IPlayerFigure'

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
    { i: 0, j: 0, type: 'start' },
    { i: 1, j: 0, type: 'start' },
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

// Zellen auf Map-Koordinaten (x, y, z) mappen
function cellToField(cell: CellCoord): [number, number, number] {
  const x = (cell.i - dummyGrid.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - dummyGrid.rows / 2 + 0.5) * CELL_SIZE)

  return [x, 0, z]
}

const startCell: CellCoord = {
  i: 5,
  j: 2,
  type: 'start',
}

// Weltkoordinaten für diese Startzelle bestimmen
const [startX, , startZ] = cellToField(startCell)

// Figuren-Liste (später vom Backend befüllen)
const figures = ref<IPlayerFigure[]>([
  {
    id: 'fig-1',
    position: [startX, 0.2, startZ],
    color: '#ff0101',
    playerId: 'Player1',
    orientation: 'east',
  },
])

const camRef = shallowRef<TresObject | null>(null)
const default_cam_pos: [number, number, number] = [0, 15, 18]
// feste Kamera-Höhe
const camHeight = 1.2
// Perspektive von welcher Figur
let figureControlInd = 0
const egoPersp = ref(false)

function updateCam() {
  const cam = camRef.value as any
  if (!cam) return

  if (egoPersp.value) {
    // Kamera auf Figur setzen
    const fig = figures.value[figureControlInd]
    if (!fig) return

    const [x, , z] = fig.position

    cam.position.set(x, camHeight, z)

    // Blickrichtung anhand der Orientierung
    let lookDir = 0
    switch (fig.orientation) {
      case 'north':
        lookDir = 0
        break
      case 'east':
        lookDir = -0.5
        break
      case 'south':
        lookDir = 1
        break
      case 'west':
        lookDir = 0.5
        break
    }

    cam.rotation.set(0, Math.PI * lookDir, 0)
  } else {
    cam.position.set(default_cam_pos[0], default_cam_pos[1], default_cam_pos[2])
    cam.lookAt(0, 0, 0)
  }
}

function onKeyDown(event: KeyboardEvent) {
  const key = event.key

  if (key >= '0' && key <= '9') {
    const index = Number.parseInt(key, 10)
    if (index < figures.value.length) {
      figureControlInd = index
      if (egoPersp.value) {
        updateCam()
      }
    }
    return
  }

  if (key === 'e' || key === 'E') {
    egoPersp.value = !egoPersp.value
    updateCam()
  }
}

// Map aus den explizit gesetzten Feldern
const cellKey = (i: number, j: number) => `${i},${j}`
// 2D-Array aller Zellen des Spielfelds mit Typ, wobei nicht gesetzte Zellen 'blocked' sind
const allCells = computed<CellCoord[]>(() => {
  // Vom dummyGrid überschriebene Zellen
  const definedCells = new Map<string, CellType>()

  for (const cell of dummyGrid.cells) {
    definedCells.set(cellKey(cell.i, cell.j), cell.type)
  }

  // vollständiges Grid
  const gridOut: CellCoord[] = []
  for (let j = 0; j < dummyGrid.rows; j++) {
    for (let i = 0; i < dummyGrid.cols; i++) {
      gridOut.push({ i, j, type: definedCells.get(cellKey(i, j)) ?? 'blocked' })
    }
  }
  return gridOut
})

onMounted(() => {
  window.addEventListener('keydown', onKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown)
})

defineExpose({
  dummyGrid,
  figures
})
</script>

<template>
  <TresCanvas clear-color="#87CEEB" class="w-full h-full">
    <TresPerspectiveCamera ref="camRef" :position="default_cam_pos" :look-at="[0, 0, 0]" />
    <OrbitControls v-if="!egoPersp" />

    <!-- Licht -->
    <TresDirectionalLight :position="[20, 40, 10]" :intensity="2" />

    <!-- Boden -->
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
      <TresPlaneGeometry :args="[dummyGrid.cols * CELL_SIZE * 5, dummyGrid.rows * CELL_SIZE * 5]" />
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
    <ThePlayerFigure
      v-for="fig in figures"
      :key="fig.id"
      :position="fig.position"
      :color="fig.color"
      :orientation="fig.orientation"
    />
  </TresCanvas>
</template>
