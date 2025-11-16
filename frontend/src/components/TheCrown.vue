<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

const scale: [number, number, number] = [2, 2, 2]    // non-uniform scale
const rotation: [number, number, number] = [Math.PI / 2, 0, 0] // radians
const lift = 1

const { state } = useGLTF('/Crown.glb', { draco: true })

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state.scene.clone()"
    :scale="scale"
    :rotation="rotation"
    :position="[0, 0, lift]" />
</template>