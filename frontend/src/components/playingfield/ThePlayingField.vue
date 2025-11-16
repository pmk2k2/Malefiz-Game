<!--  Spielfeld-Szene -->
<script setup lang="ts">
import { useLoop, type TresObject } from '@tresjs/core'
import { computed, onMounted, ref, shallowRef } from 'vue'
import { Align, OrbitControls } from '@tresjs/cientos'
import { MeshStandardMaterial } from 'three'
import ThePlayerFigure from './ThePlayerFigure.vue'
import type { IPlayerFigure } from '@/IPlayerFigure'

// Referenz zur Kamera
const camRef = shallowRef<TresObject | null>(null)
const orbitRef = shallowRef<TresObject | null>(null)

// Referenz zum Donut-Mesh
const donutRef = ref()

// Ref fuer die dargestellten Figuren
const figures = ref(new Map<number, IPlayerFigure>())
figures.value.set(0, {
        "position": [1,0,1],
        "color": '#ff0101',
        //"model": '/Pawn.glb',
        "player": 'Spieler1',
        "orientation": 'north'
    })
figures.value.set(1, {
        "position": [-1,0,-1],
        "color": '#0101FF',
        //"model": '/Pawn.glb',
        "player": 'Spieler2',
        "orientation": 'south'
    })
figures.value.set(2, {
        "position": [-4,0,2],
        "color": '#0101FF',
        //"model": '/Pawn.glb',
        "player": 'Spieler2',
        "orientation": 'west'
    }) 
// Array fuer figur-Map erstellen
const figureEntries = computed(() => {
    return Array.from(figures.value.entries())
})


// Ego-Perspektive
function toggleEgoPersp() {
    if(camRef.value) {
        if(egoPersp) {
            console.log("In Figur")
            const kameraHoehe = 0.8

            // Position der aktuellen Figur kriegen
            let figAttr = figures.value.get(figureControlInd)

            // Position und lookAt-Richtung fuer Kamera anpassen
            let lookDir = [...figAttr?.position]
            let camPos = figAttr?.position
            console.log(figAttr?.orientation)
            switch(figAttr?.orientation) {
                case 'north':
                    lookDir[0] = lookDir[0] + 1;
                    break;
                case 'east':
                    lookDir[2] = lookDir[2] + 1;
                    break;
                case 'south':
                    lookDir[0] = lookDir[0] - 1;
                    break;
                case 'west':
                    lookDir[2] = lookDir[2] - 1;
                    break;
            }

            // Kamera platzieren 
            camRef.value.position.x = camPos[0]
            camRef.value.position.y = kameraHoehe
            camRef.value.position.z = camPos[2]
            /*
            camRef.value.lookAt.x = lookDir[0]
            camRef.value.lookAt.y = lookDir[1]
            camRef.value.lookAt.z = lookDir[2]
            */
           camRef.value.lookAt = lookDir
        } else {
            console.log("Ausserhalb Figur")
            // Kamera zurueckpacken
            camRef.value.position.x = 7
            camRef.value.position.y = 7
            camRef.value.position.z = 7
            camRef.value.lookAt.x = 0
            camRef.value.lookAt.y = 0
            camRef.value.lookAt.z = 0
        }
    }
}

function moveFigure(id: number, newX: number, newY: number, newZ: number) {
    if(figures.value.get(id)) {
        let fig = figures.value.get(id)
        fig.position = [newX, newY, newZ]
    }
}

// Render bzw Animations Loop
const { onBeforeRender } = useLoop()

// TESTWEISE Keyinput um Figurbewegung etc zu testen
let figureControlInd = 0
let egoPersp = false

function keyPress(event: any) {
  console.log(event.key)
  if(event.key >= '0' && event.key <= '9') {
    figureControlInd = Number.parseInt(event.key)
    console.log("Kontrollierende Figur: " + figureControlInd)
  }
  // Figur des jeweiligen Index bewegen dies das
  if(event.key === 'w') {
    let fig = figures.value.get(figureControlInd)
    let pos = [...fig?.position]

    pos[0] = pos[0] + 1
    fig.orientation = 'north'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 'a') {
    let fig = figures.value.get(figureControlInd)
    let pos = [...fig?.position]

    pos[2] = pos[2] - 1
    fig.orientation = 'west'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 's') {
    let fig = figures.value.get(figureControlInd)
    let pos = [...fig?.position]

    pos[0] = pos[0] - 1
    fig.orientation = 'south'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 'd') {
    let fig = figures.value.get(figureControlInd)
    let pos = [...fig?.position]

    pos[2] = pos[2] + 1
    fig.orientation = 'east'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 'e') {
    egoPersp = !egoPersp
    console.log("Ego-Perspektive: " + egoPersp)
    toggleEgoPersp();
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
    <!--
    <OrbitControls ref="orbitRef"/>
-->

    <!-- Camera -->
    <TresPerspectiveCamera
        ref="camRef"
        :position="[7,7,7]"
        :lookAt="[0,0,0]"
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
        v-for="[id, fig] in figureEntries"
        :key="id"
        v-bind="fig"
    />


</template>
