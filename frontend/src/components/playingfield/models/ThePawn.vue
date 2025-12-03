<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
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

watchEffect(() => {
  if (!state.value?.scene) return

  state.value?.scene.scale.set(scale, scale, scale)

  state.value.scene.traverse((child) => {
    // @ts-expect-error three.js Mesh hat isMesh und material zur Laufzeit
    if (child.isMesh && child.material && child.material.color) {
      // @ts-expect-error material.color existiert zur Laufzeit
      child.material.color.set(props.color)
    }
  })
})
</script>

<template>
  <primitive v-if="state?.scene" :object="state?.scene" />
</template>
