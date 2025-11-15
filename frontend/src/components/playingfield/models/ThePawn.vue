<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';
import { watchEffect } from 'vue';

// Skalierung des Models
const scale = 1;
const { state, materials } = useGLTF("/Pawn.glb", {draco: true})
watchEffect(() => state.value?.scene.scale.set(scale, scale, scale))
</script>

<template>
  <primitive v-if="state" :object="state?.scene" />
</template>
