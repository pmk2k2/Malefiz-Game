<!--  Spielfeld-Szene -->
<script setup lang="ts">
import { useLoop, type TresObject } from '@tresjs/core'
import { ref, shallowRef } from 'vue'
import { Align, OrbitControls } from '@tresjs/cientos'
import { MeshStandardMaterial } from 'three'
import ThePlayerFigure from './ThePlayerFigure.vue'

let figureControlInd = 0;

// Referenz zum Donut-Mesh
const donutRef = ref()
const donutMatRef = ref(new MeshStandardMaterial({
    color: 0xFF1100,
    metalness: 1.0,
    roughness: 0.1
}))
const playerFigureRef = shallowRef<TresObject | null>(null)

// Render bzw Animations Loop
const { onBeforeRender } = useLoop()


// Testweise Keyinput
function keyPress(event: any) {
  if(event.key >= '0' && event.key <= '9') {
    figureControlInd = Number.parseInt(event.key)
  }
  // Figur des jeweiligen Index bewegen dies das
  if(event.key === 'W') {

  }
  if(event.key === 'A') {

  }
  if(event.key === 'S') {

  }
  if(event.key === 'D') {

  }
}

// elapsed -> Renderdelta?
// donutRef.value -> wenn donutRef da ist ?
// onBeforeRender -> BEVOR jeder Frame gerendert wird
onBeforeRender(({ elapsed }) => {
    if(donutRef.value) {
        donutRef.value.rotation.x = elapsed * 0.5
        donutRef.value.rotation.y = elapsed * 0.25
    }
})
</script>

<template>
    <!-- Camera -->
    <TresPerspectiveCamera
        :position="[ 7, 7, 7]"
        :look-at="[ 0, 0, 0]"
    />

    <!-- Beleuchtung -->
    <TresAmbientLight :intensity="0.5" />
    <TresDirectionalLight :position="[5, 5, 2]" :intensity="2.5"/>

    <TresMesh ref="donutRef" :position="[0,2,0]">
        <TresTorusGeometry :args="[1, 0.4, 16, 32]" />
        <!-- TresMeshStandardMaterial ref="donutMatRef" / -->
        <TresMeshStandardMaterial color="#ff1100" :metalness="0.7" :roughness="0" />
    </TresMesh>

    <Align top :position="[0.5, 0, 0.5]"> <!-- Damit Models auf Boden stehen -->
      <TresMesh ref="playerFigureRef">
        <ThePlayerFigure />
      </TresMesh>
    </Align>

    <TresAxesHelper />
    <TresGridHelper />
    <OrbitControls />

</template>
