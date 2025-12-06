<script setup lang="ts">
import { Align } from '@tresjs/cientos'

type CensoredFiguresProps = {
    position?: [number,number, number]
}

// Definieren und festlegen der Props und Defaultwerte
const props = withDefaults(defineProps<CensoredFiguresProps>(), {
  position: () => [0, 0, 0],
})
</script>

<template>
  <TresMesh :position="props.position">
    <Align top>
      <TresMesh :position="[0, 0.2, 0]"> 
        <TresCylinderGeometry :args="[0.4, 0.4, 0.8, 16]" />
        <TresMeshStandardMaterial color="#A9A9A9" /> </TresMesh>
    </Align>
  </TresMesh>
</template>
