<script setup lang="ts">
import { GLTFModel, useGLTF } from '@tresjs/cientos';

// Skalierung und Position anpassen, damit die Barriere gut auf dem Feld steht
const scale: [number, number, number] = [1.7, 1.7, 1.7] 
const rotation: [number, number, number] = [1.5, (Math.random() * 2) * Math.PI, 0] 
const shiftY = 0.2 // Anhebung, falls der Ursprung des Modells mittig ist
const shiftX = - 0.2
const lift = 0.5

//const { state } = useGLTF('/Barrier.glb', { draco: true })

</script>

<template>
    <GLTFModel
    path="/Barrier.glb"
    receive-shadow
    cast-shadow
    :scale="scale"
    :rotation="rotation"
    :position="[shiftX , shiftY, lift]" />
</template>