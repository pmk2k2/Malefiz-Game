<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos';

/*
const scale: [number, number, number] = [0.8, 1, 0.8]
const rotation: [number, number, number] = [Math.PI / 2, 0, 0]
const lift = 1.7
const { state } = useGLTF('/Pine Tree.glb', { draco: true })
*/

</script>

<template>  <
    <TresMesh :position="[0, 0, 0.5]">    <!-- z position ändern wenn wir uns zu entscheiden, das zensierte barriere auf path liegen soll-->
        <TresCircleGeometry :args="[0.5, 64]" />
        <TresMeshStandardMaterial color="#000000"/>
    </TresMesh>
</template>