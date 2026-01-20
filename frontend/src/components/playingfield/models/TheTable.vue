<script setup lang="ts">
import { Align, useGLTF } from '@tresjs/cientos';
import { Color, MeshStandardMaterial } from 'three';
import { watchEffect } from 'vue';

// Table by jeremy [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/8RW134iS2gW)
const { state } = useGLTF('/Table.glb', { draco: true })
const props = defineProps({ scale: Number })

const shinyWoodMat = new MeshStandardMaterial({
    color: new Color('#3d200d'),
    roughness: 0.07,
    metalness: 0.05
})

watchEffect(() => {
  if (!state.value?.scene) return

  state.value.scene.traverse((child) => {
    // @ts-expect-error three.js Mesh hat isMesh und material zur Laufzeit
    if (child.isMesh) {
        child.receiveShadow = true
        child.material = shinyWoodMat
    }
  })
})
</script>

<template>
<Align bottom :position="[0,-0.1,0]">
    <TresMesh>
        <primitive v-if="state?.scene"
            :object="state?.scene"
            :scale="[props.scale, props.scale, props.scale]"
        />
    </TresMesh>
</Align>
</template>