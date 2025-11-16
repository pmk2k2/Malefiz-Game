<!--  Spielfeld-Szene -->
<script setup lang="ts">
import { useLoop, type TresObject } from '@tresjs/core'
import { onMounted, ref, shallowRef } from 'vue'
import { Align, OrbitControls } from '@tresjs/cientos'
import { MeshStandardMaterial } from 'three'
import ThePlayerFigure from './ThePlayerFigure.vue'


// Referenz zum Donut-Mesh
const donutRef = ref()

// Ref fuer die dargestellten Figuren
const figures = ref({})
figures.value = {
    "welt": {
        "position": [1,0,1],
        "color": '#ff0101',
        "model": '/Pawn.glb',
        "player": 'Spieler1',
        "orientation": 'north'
    },
    "hallo": {
        "position": [-1,0,-1],
        "color": '#aabccb',
        "model": '/Pawn.glb',
        "player": 'Spieler2',
        "orientation": 'south'
    },
    "wesh": {
        "position": [-4,0,2],
        "color": '#aabccb',
        "model": '/Pawn.glb',
        "player": 'Spieler2',
        "orientation": 'west'
    }
}

// Render bzw Animations Loop
const { onBeforeRender } = useLoop()

// TESTWEISE Keyinput um Figurbewegung etc zu testen
let figureControlInd = 0
let egoPersp = false
function keyPress(event: any) {
  if(event.key >= '0' && event.key <= '9') {
    figureControlInd = Number.parseInt(event.key)
    console.log("Kontrollierende Figur: " + figureControlInd)
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
  if(event.key === 'Tab') {
    egoPersp = !egoPersp
    console.log("Ego-Perspektive: " + egoPersp)
  }
}
onMounted(() => document.addEventListener('keydown', keyPress))

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
    <TresAxesHelper />
    <TresGridHelper />
    <OrbitControls />

    <!-- Camera -->
    <TresPerspectiveCamera
        :position="[ 7, 7, 7]"
        :look-at="[ 0, 0, 0]"
    />

    <!-- Beleuchtung -->
    <TresAmbientLight :intensity="0.5" />
    <TresDirectionalLight :position="[5, 5, 2]" :intensity="2.5"/>

    <!-- Testweise ein Donut da -->
    <TresMesh ref="donutRef" :position="[0,2,0]">
        <TresTorusGeometry :args="[1, 0.4, 16, 32]" />
        <TresMeshStandardMaterial color="#ff1100" :metalness="0.7" :roughness="0" />
    </TresMesh>

    <ThePlayerFigure
        v-for="(props, id) in figures"
        :key="id"
        v-bind="props"
    />


</template>
