<script setup lang="ts">
import { DoubleSide, SpriteMaterial } from 'three'
import { THREE_GetGifTexture } from 'threejs-gif-texture'
import { shallowRef } from 'vue'

const scale = 2.5
const hoehe = 1

const material = shallowRef()
THREE_GetGifTexture("/FightCloud.gif").then( texture => {
  material.value = new SpriteMaterial(
    {
      map: texture,
      transparent: true,
      side: DoubleSide
    }
  )
})
</script>

<template>
  <TresSprite :position="[0,0,hoehe]" :scale="[scale, scale, 1]">
    <TresSpriteMaterial v-if="material" v-bind="material" />
  </TresSprite>
</template>
