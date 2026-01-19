<script setup lang="ts">
import { Align, useGLTF } from '@tresjs/cientos';

const scale: [number, number, number] = [2, 2, 2]
const rotation: [number, number, number] = [Math.PI / 2, 0, 0]
const lift = -0.04
// grass mix by Steve B [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/2zt43AlwVoI)
// grass green by Steve B [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/8q6D0D_SuBE)
//const { state } = useGLTF('/Grass Patch.glb', { draco: true })
const { state } = useGLTF('/grass_green-opt.glb', { draco: true })

</script>

<template>
<TresMesh :position="[0,0,lift]" :rotation="rotation" :scale="scale">
    <Align top>
        <primitive v-if="state?.scene"
        :object="state.scene" />
    </Align>
</TresMesh>
</template>