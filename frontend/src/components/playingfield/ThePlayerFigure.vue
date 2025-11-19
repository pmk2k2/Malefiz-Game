<script setup lang="ts">
import { shallowRef } from 'vue'
import ThePawn from './models/ThePawn.vue'
import { Align } from '@tresjs/cientos'

// Definieren und festlegen der Props und Defaultwerte
const props = defineProps({
  position: {
    // Position der Figur
    type: [Number, Number, Number],
    default: () => [0, 0, 0]
  },
  color: {
    // Farbe der Figur (Server sollte die eig geben)
    type: String,
    default: () => '#ffaacc'
  },
  model: {
    // falls man spaeter verschiedene auswaehlbare Models haben will
    type: String,
    default: () => 'Pawn'
  },
  player: {
    // zu welchem Spieler die Figur gehoert
    // keine Ahnung, ob tatsaechlich noetig ???
    type: String,
    default: () => 'Player'
  },
  orientation: {
    // Richtung, wohin die Figur schaut (fuer Egoperspektive relevant)
    type: String,
    default: () => 'north'
  }
})
</script>

<template>
  <TresMesh :position="props.position">
    <Align top> <!-- setzt die Figur auf den Boden -->
      <ThePawn :color="props.color"/>
    </Align>
  </TresMesh>
</template>
