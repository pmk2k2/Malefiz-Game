<script setup lang="ts">
import { TresCanvas } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'

// Das Spielfeld, cells für aktiven Spielfelder
interface Grid {
  cols: number
  rows: number
}

// Demo-Spielfeld, Werte werden zukünftig vom Backend gefüllt
const dummyGrid: Grid = {
  cols: 11,
  rows: 8,
}

const LINE_THICKNESS = 0.02
const LINE_Y = 0.011
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
          :position="[n - 1 - dummyGrid.cols / 2, LINE_Y, 0]"
        >
          <TresBoxGeometry :args="[LINE_THICKNESS, 0.001, dummyGrid.rows]" />
          <TresMeshBasicMaterial color="black" />
        </TresMesh>

        <!-- Horizontale Linien -->
        <TresMesh
          v-for="n in dummyGrid.rows + 1"
          :key="`h-${n}`"
          :position="[0, LINE_Y, n - 1 - dummyGrid.rows / 2]"
        >
          <TresBoxGeometry :args="[dummyGrid.cols, 0.001, LINE_THICKNESS]" />
          <TresMeshBasicMaterial color="black" />
        </TresMesh>
      </TresCanvas>
    </div>
  </div>
</template>
