<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

// Etwas Variation in der Groesse, Skalierung und Platzierung des Weges
const randomX = Math.random() * (0.1 - (-0.1)) + (-0.1)
const randomY = Math.random() * (0.1 - (-0.1)) + (-0.1)
const randomScale = Math.random() * (1.1 - (0.9)) + (0.9)
const randomRot = Math.random() * 2

// Skalierung, Rotation (in Radiant) und Anhebung des Modells
const scale: [number, number, number] = [1.6 * randomScale, 2, 1.6 * randomScale] // breit, hoch, tief
const rotation: [number, number, number] = [Math.PI / 2, Math.PI * randomRot, 0] // Modell um 90° um die X-Achse drehen
const lift = -0.05
const position: [number, number, number] = [randomX, randomY, lift]

const { state } = useGLTF('/Rock Path Round Small.glb', { draco: true })

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state?.scene.clone()"
    :scale="scale"
    :rotation="rotation"
    :position="position" />
</template>