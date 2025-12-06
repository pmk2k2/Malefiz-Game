<script setup lang="ts">
import ThePawn from './models/ThePawn.vue'
import { Align } from '@tresjs/cientos'
import type { IPlayerFigure } from '@/stores/IPlayerFigure'

type PlayerFigureProps = Pick<IPlayerFigure, 'position' | 'color' | 'orientation'> & {
  model?: string
  player?: string
}

// Definieren und festlegen der Props und Defaultwerte
const props = withDefaults(defineProps<PlayerFigureProps>(), {
  position: () => [0, 0, 0],
  color: '#ffaacc',
  model: 'Pawn',
  player: 'Player',
  orientation: 'north',
})
</script>

<template>
  <TresMesh :position="props.position">
    <Align top>
      <!-- setzt die Figur auf den Boden -->
      <ThePawn :color="props.color" />
    </Align>
  </TresMesh>
</template>
