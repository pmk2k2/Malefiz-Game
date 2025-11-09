<!--  Spielfeld-Szene -->
<script setup lang="ts">
import { useLoop } from '@tresjs/core'
import { ref } from 'vue'
import { OrbitControls } from '@tresjs/cientos'
import { MeshStandardMaterial } from 'three'

// Referenz zum Donut-Mesh
const donutRef = ref()
const donutMatRef = ref(new MeshStandardMaterial({
    color: 0xFF1100,
    metalness: 1.0,
    roughness: 0.1
}))

// Render bzw Animations Loop 
const { onBeforeRender } = useLoop()

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
    <TresPerspectiveCamera
        :position="[ 7, 7, 7]" 
        :look-at="[ 0, 0, 0]" 
    />

    <TresAmbientLight :intensity="0.5" />
    <TresDirectionalLight :position="[5, 5, 2]" :intensity="2.5"/>

    <TresMesh ref="donutRef" :position="[0,2,0]">
        <TresTorusGeometry :args="[1, 0.4, 16, 32]" />
        <!-- TresMeshStandardMaterial ref="donutMatRef" / -->
        <TresMeshStandardMaterial color="#ff1100" :metalness="0.7" :roughness="0" />
    </TresMesh>

    <TresAxesHelper />
    <TresGridHelper />
    <OrbitControls />

</template>