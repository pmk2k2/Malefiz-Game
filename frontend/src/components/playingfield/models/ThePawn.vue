<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';
import { watchEffect } from 'vue';

const props = defineProps({
  color: {
    type: String,
    default: '#ff01ff'
  }
})
// Skalierung des Models
const scale = 1;
const { state } = useGLTF("/Pawn.glb", {draco: true})
watchEffect((child) => {
  state.value?.scene.scale.set(scale, scale, scale)
  state.value?.scene.traverse( (child) => {
    if(child.isMesh) {
      child.material.color.set(props.color)
    }
  })
}
)
</script>

<template>
  <primitive v-if="state" :object="state?.scene" />
</template>
