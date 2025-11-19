<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

// Skalierung, Rotation (in Radiant) und Anhebung des Modells
const scale: [number, number, number] = [1.6, 2, 1.6] // breit, hoch, tief
const rotation: [number, number, number] = [Math.PI / 2, 0, 0] // Modell um 90° um die X-Achse drehen
const lift = 0.02

const { state } = useGLTF('/Rock Path Round Small.glb', { draco: true })

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state.scene.clone()"
    :scale="scale"
    :rotation="rotation"
    :position="[0, lift, 0]" />
</template>