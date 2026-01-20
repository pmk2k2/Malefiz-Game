<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';
import { useLoop } from '@tresjs/core';

const scale: [number, number, number] = [1.75, 1.75, 1.75]
const rotation: [number, number, number] = [Math.PI / 2, 0, 0]
const lift = 1

// Fortschritt der Animation
let rotProgress = 0
let liftProgress = 0
let scaleProgress = 0

const { state } = useGLTF('/Crown.glb', { draco: true })
const { onBeforeRender } = useLoop()


// Animation der Krone
onBeforeRender(({ delta }) => {
    if(!state?.value?.scene) return

    // x * delta bestimmt Geschwindigkeit der jeweiligen Animation
    // delta % 2 dafuer, dass man Werte nur zwischen 0 und 2 annimmt
    rotProgress += (0.33 * delta) % 2
    liftProgress += delta % 2
    scaleProgress += (0.5 * delta) % 2

    // Schreibarbeit sparen
    const currPos = state.value.scene.position
    const currRot = state.value?.scene.rotation

    // neue pos, rot und scale setzen
    const newScale = scale[0] + 0.1 * Math.sin(scaleProgress * Math.PI)
    state.value.scene.scale.set(newScale, newScale, newScale)
    currPos.set(currPos.x, currPos.y, lift + 0.1 * Math.sin(liftProgress * Math.PI))
    currRot.y = Math.PI * rotProgress
})

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state?.scene"
    :position="[0,0,lift]"
    :rotation="rotation"
    :scale="scale"
    />
</template>