<script setup lang="ts">
import { Align, useGLTF } from '@tresjs/cientos';

// Table by jeremy [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/8RW134iS2gW)
const { state } = useGLTF('/Table.glb', { draco: true })
const props = defineProps({ scale: Number })

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