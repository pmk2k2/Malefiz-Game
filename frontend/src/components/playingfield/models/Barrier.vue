<script setup lang="ts">
import { GLTFModel, useGLTF } from '@tresjs/cientos';
import { watchEffect } from 'vue';

// Skalierung und Position anpassen, damit die Barriere gut auf dem Feld steht
const scale: [number, number, number] = [1.7, 1.7, 1.7] 
const rotation: [number, number, number] = [1.5, (Math.random() * 2) * Math.PI, 0] 
const shiftY = 0.2 // Anhebung, falls der Ursprung des Modells mittig ist
const shiftX = - 0.2
const lift = 0.5

const { state } = useGLTF('/Barrier.glb', { draco: true })
watchEffect(() => {
  if (!state.value?.scene) return

  state.value.scene.traverse((child) => {
    // @ts-expect-error three.js Mesh hat isMesh und material zur Laufzeit
    if (child.isMesh) {
      child.layers.set(1) // auf Licht im layer1 reagieren

      // Schatten werfen und empfangen
      child.castShadow = true
      child.receiveShadow = true
    }
  })
})

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state?.scene"
    :scale="scale"
    :rotation="rotation"
    :position="[shiftX , shiftY, lift]" />
</template>