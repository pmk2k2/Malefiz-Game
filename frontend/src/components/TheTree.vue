<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

const scale: [number, number, number] = [0.8, 1, 0.8]
const rotation: [number, number, number] = [Math.PI / 2, 0, 0]
const lift = 1.7

const { state } = useGLTF('/Pine Tree.glb', { draco: true })

</script>

<template>
    <primitive v-if="state?.scene"
    :object="state.scene.clone()"
    :scale="scale"
    :rotation="rotation"
    :position="[0, 0, lift]" />
</template>