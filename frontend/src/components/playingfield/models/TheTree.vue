<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

// Etwas Variation in der Groesse, Skalierung und Platzierung der Baeume
const randomX = Math.random() * (0.15 - (-0.15)) + (-0.15)
const randomY = Math.random() * (0.15 - (-0.15)) + (-0.15)
const randomScale = Math.random() * (1.17 - (0.95)) + (0.95)
const randomRot = Math.random() * 2

const scale: [number, number, number] = [0.8 * randomScale, 1 * randomScale, 0.8 * randomScale]
const rotation: [number, number, number] = [Math.PI / 2, Math.PI * randomRot, 0]
const lift = 1.6
const position: [number, number, number] = [randomX, randomY, lift]

const { state } = useGLTF('/Pine Tree.glb', { draco: true })

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state?.scene.clone()"
    :scale="scale"
    :rotation="rotation"
    :position="position" />
</template>