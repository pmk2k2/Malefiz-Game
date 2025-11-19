<!--  Spielfeld-Szene -->
<script setup lang="ts">
import { useLoop, type TresObject } from '@tresjs/core'
import { computed, onMounted, ref, shallowRef, watchEffect } from 'vue'
import { Align, OrbitControls } from '@tresjs/cientos'
import { MeshStandardMaterial } from 'three'
import ThePlayerFigure from './ThePlayerFigure.vue'
import type { IPlayerFigure } from '@/IPlayerFigure'
import { update } from 'three/examples/jsm/libs/tween.module.js'

// Referenz zur Kamera
const camRef = shallowRef<TresObject | null>(null)
const orbitRef = shallowRef<TresObject | null>(null)

// Referenz zum Donut-Mesh
//const donutRef = ref()

// Feste Kamerahoehe
const kameraHoehe = 0.75

// Ref fuer die dargestellten Figuren
// Wird am Ende tatsaechlich aus WebSocket dingern bezogen
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
figures.value.set(3, {
        "position": [-1,0,5],
        "color": '#01FF01',
        //"model": '/Pawn.glb',
        "player": 'Spieler3',
        "orientation": 'east'
    }) 
figures.value.set(4, {
        "position": [3,0,3],
        "color": '#01FF01',
        //"model": '/Pawn.glb',
        "player": 'Spieler3',
        "orientation": 'south'
    }) 
// Array fuer figur-Map erstellen
const figureEntries = computed(() => {
    return Array.from(figures.value.entries())
})


// Ego-Perspektive
function updateCam() {
    if(camRef.value) {
        if(egoPersp) {
            console.log("In Figur")

            // Position der aktuellen Figur kriegen
            let figAttr = figures.value.get(figureControlInd)

            console.log(figAttr?.orientation)

            // Position und lookAt-Richtung fuer Kamera anpassen
            const camPos = figures.value.get(figureControlInd).position
            let lookDir = 0
            switch(figAttr?.orientation) {
                case 'north':
                    lookDir = -0.5;
                    break;
                case 'east':
                    lookDir = 1;
                    break;
                case 'south':
                    lookDir = 0.5;
                    break;
                case 'west':
                    lookDir = 0;
                    break;
            }

            // Kamera platzieren 
            camRef.value.position.x = camPos[0] // figureControlInd.camPos.value[0]
            camRef.value.position.y = kameraHoehe
            camRef.value.position.z = camPos[2] // figureControlInd.camPos.value[0]
            camRef.value.rotation.set(0, Math.PI * lookDir, 0)
        } else {
            console.log("Ausserhalb Figur")
            // Kamera zurueckpacken
            camRef.value.position.x = 7
            camRef.value.position.y = 7
            camRef.value.position.z = 7
            // Hardcoded Rotation, damit man auf 0,0,0 guckt
            camRef.value.rotation.set(-0.7853981633974481, 0.6154797086703871, 0.5235987755982987)
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
  if(event.key >= '0' && event.key <= '9') {
    figureControlInd = Number.parseInt(event.key)
    console.log("Kontrollierende Figur: " + figureControlInd)
  }
  // Figur des jeweiligen Index bewegen dies das
  let pos = null
  if(event.key === 'w') {
    let fig = figures.value.get(figureControlInd)
    pos = [...fig?.position]

    pos[0] = pos[0] + 1
    fig.orientation = 'north'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 'a') {
    let fig = figures.value.get(figureControlInd)
    pos = [...fig?.position]

    pos[2] = pos[2] - 1
    fig.orientation = 'west'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 's') {
    let fig = figures.value.get(figureControlInd)
    pos = [...fig?.position]

    pos[0] = pos[0] - 1
    fig.orientation = 'south'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 'd') {
    let fig = figures.value.get(figureControlInd)
    pos = [...fig?.position]

    pos[2] = pos[2] + 1
    fig.orientation = 'east'
    moveFigure(figureControlInd, pos[0], pos[1], pos[2])
  }
  if(event.key === 'e') {
    egoPersp = !egoPersp
    console.log("Ego-Perspektive: " + egoPersp)
    updateCam()
  }
  if(pos !== null) {
    //moveCam(pos[0], kameraHoehe, pos[2])
    updateCam()
  }
}

onMounted(() => document.addEventListener('keydown', keyPress))

// elapsed -> Renderdelta?
// donutRef.value -> wenn donutRef da ist ?
// onBeforeRender -> BEVOR jeder Frame gerendert wird
onBeforeRender(({ elapsed }) => {
    /*
    if(donutRef.value) {
        donutRef.value.rotation.x = elapsed * 0.5
        donutRef.value.rotation.y = elapsed * 0.25
    } */

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
    <!--
    <TresMesh ref="donutRef" :position="[0,2,0]">
        <TresTorusGeometry :args="[1, 0.4, 16, 32]" />
        <TresMeshStandardMaterial color="#ff1100" :metalness="0.7" :roughness="0" />
    </TresMesh> -->

    <ThePlayerFigure
        v-for="[id, fig] in figureEntries"
        :key="id"
        v-bind="fig"
    />


</template>
