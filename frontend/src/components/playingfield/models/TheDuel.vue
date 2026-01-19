<script setup lang="ts">
import { FrontSide, SpriteMaterial } from 'three'
// https://github.com/bandinopla/threejs-gif-texture
import { THREE_GetGifTexture } from 'threejs-gif-texture'
import { shallowRef } from 'vue'

const scale = 2.75
const hoehe = 1

const material = shallowRef()
THREE_GetGifTexture("/FightCloud.gif").then( texture => {
  material.value = new SpriteMaterial(
    {
      map: texture,
      transparent: true,
      side: FrontSide 
    }
  )
})
</script>

<template>
  <!-- Sprite statt Mesh, damit Textur immer die Kamera anschaut -->
  <TresSprite :position="[0,0,hoehe]" :scale="[scale, scale, 1]" :cast-shadow="false">
    <TresSpriteMaterial v-if="material" v-bind="material" />
  </TresSprite>
</template>
