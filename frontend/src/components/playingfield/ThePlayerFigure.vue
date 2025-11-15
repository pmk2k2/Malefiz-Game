<script setup lang="ts">
import ThePawn from './models/ThePawn.vue'

// Definieren und festlegen der Props und Defaultwerte
interface Props {
  position?: [number, number, number]
  color?: string
  model?: string
  player?: string
  figureNr?: number
  orientation?: string
}
const props = withDefaults(defineProps<Props>(), {
  position: () => [0, 0, 0],
  color: '#ffaacc',
  model: 'ThePawn',
  player: 'Player',
  figureNr: 0,
  orientation: 'north'
})

function setPosition(x: number, y: number, z: number) {
  props.value.position = [x, y, z]
}

</script>

<template>
  <TresMesh :position="props.position">
    <ThePawn />
    <TresMeshStandardMaterial :color="props.color" />
  </TresMesh>
</template>
