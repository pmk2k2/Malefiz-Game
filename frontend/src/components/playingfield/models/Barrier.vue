<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

// Skalierung und Position anpassen, damit die Barriere gut auf dem Feld steht
const scale: [number, number, number] = [5.5, 5.5, 5.5] 
const rotation: [number, number, number] = [0, 0.3, 0] 
const shiftY = 0.2 // Anhebung, falls der Ursprung des Modells mittig ist
const shiftX = - 0.2
const lift = -0.6

const { state } = useGLTF('/Barrier.glb', { draco: true })

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state.scene.clone()"
    :scale="scale"
    :rotation="rotation"
    :position="[shiftX , shiftY, lift]" />
</template>