<script setup lang="ts">
import { Align, useGLTF } from '@tresjs/cientos';
import { watchEffect } from 'vue';

// Etwas Variation in der Groesse, Skalierung und Platzierung vom Gras
const gras1RandX = Math.random() * (0.55 - (-0.55)) + (-0.55)
const gras1RandY = Math.random() * (0.55 - (-0.55)) + (-0.55)
const gras1RandScale = Math.random() * (1.2 - (0.9)) + (0.9)
const gras1RandRot = Math.random() * 2

const gras2RandScale = Math.random() * (1.15 - (0.85)) + (0.85)
const gras2RandRot = Math.random() * 2


const gras1Pos: [number, number, number] = [gras1RandX, gras1RandY, 0]
const gras1Scale: [number, number, number] = [gras1RandScale * 2, gras1RandScale * 2, gras1RandScale * 2]
const gras1Rot: [number, number, number] = [Math.PI / 2, Math.PI * gras1RandRot, 0]

const gras2Pos: [number, number, number] = [-gras1RandX, -gras1RandY, 0]
const gras2Scale: [number, number, number] = [gras2RandScale * 2, gras2RandScale * 2.2, gras2RandScale * 2]
const gras2Rot: [number, number, number] = [Math.PI / 2, Math.PI * gras2RandRot, 0]

const lift = -0.04
//const { state } = useGLTF('/Grass Patch.glb', { draco: true })
// grass green by Steve B [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/8q6D0D_SuBE)
const { state } = useGLTF('/grass_green-opt.glb', { draco: true })

watchEffect(() => {
  if (!state.value?.scene) return

  state.value.scene.traverse((child) => {
    // @ts-expect-error three.js Mesh hat isMesh und material zur Laufzeit
    if (child.isMesh) {
      // Schatten deaktivieren
      child.castShadow = false
      child.receiveShadow = false
    }
  })
})
</script>

<template>
<TresMesh :position="[0,0,lift]">
    <Align top>
        <primitive v-if="state?.scene"
        :object="state?.scene"
        :position="gras1Pos"
        :rotation="gras1Rot"
        :scale="gras1Scale"
        />
        <primitive v-if="state?.scene"
        :object="state?.scene.clone()"
        :position="gras2Pos"
        :rotation="gras2Rot"
        :scale="gras2Scale"
        />
    </Align>
</TresMesh>
</template>