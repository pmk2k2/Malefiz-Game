<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { watchEffect } from 'vue'

// Katana by dook [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/zV3WXbyjMf)
const scale = 0.5
const { state } = useGLTF('/Katana.glb', { draco: true })

watchEffect(() => {
    if(!state.value?.scene) return

    state.value?.scene.scale.set(scale, scale, scale)
})
</script>
<template>
    <primitive v-if="state?.scene" :object="state?.scene" />
</template>