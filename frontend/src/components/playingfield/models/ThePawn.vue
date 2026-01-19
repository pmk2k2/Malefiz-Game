<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { Color, MeshStandardMaterial } from 'three'
import { watchEffect } from 'vue'

const props = defineProps({
  color: {
    type: String,
    default: '#ff01ff',
  },
})
// Skalierung des Models
const scale = 2.5
const { state } = useGLTF('/Pawn.glb', { draco: true })

const shinyMat = new MeshStandardMaterial({
  color: new Color(props.color),
  roughness: 0.0,
  metalness: 0.45
})

watchEffect(() => {
  if (!state.value?.scene) return

  state.value?.scene.scale.set(scale, scale, scale)

  state.value.scene.traverse((child) => {
    // @ts-expect-error three.js Mesh hat isMesh und material zur Laufzeit
    if (child.isMesh) {
      // Schatten werfen und empfangen
      child.castShadow = true
      child.receiveShadow = true
      // Material setzen
      child.material = shinyMat
    }
  })
})
</script>

<template>
  <primitive v-if="state?.scene" :object="state?.scene" />
</template>
