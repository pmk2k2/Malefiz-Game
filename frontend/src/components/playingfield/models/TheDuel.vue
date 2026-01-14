<script setup lang="ts">
import { useGLTF } from '@tresjs/cientos'
import { useLoader } from '@tresjs/core'
import { BoxGeometry, Mesh, MeshBasicMaterial, PlaneGeometry, TextureLoader } from 'three'
import { THREE_GetGifTexture } from 'threejs-gif-texture'
import { shallowRef, watchEffect } from 'vue'

// Katana by dook [CC-BY] (https://creativecommons.org/licenses/by/3.0/) via Poly Pizza (https://poly.pizza/m/zV3WXbyjMf)
const scale = 5.85
const hoehe = 0.85
const rotX = (Math.PI / 2), rotY = 0, rotZ = 0
//const { state } = useGLTF('/Katana.glb', { draco: true })
/*
const { state: texture, isLoading, error } = useLoader(
  TextureLoader,
  '/FightCloud.gif'
  //'/skybox_raw.jpeg'
  //'https://raw.githubusercontent.com/Tresjs/assets/main/textures/black-rock/Rock035_2K_Color.jpg'
)*/

const material = shallowRef(new MeshBasicMaterial())
THREE_GetGifTexture("/FightCloud.gif").then( texture => {
  material.value = new MeshBasicMaterial({

    map: texture
  })
})

</script>

<template>
  <!-- primitive v-if="texturePlane" :object="texturePlane" :position="currPos" / -->
  <TresMesh :rotation-x="rotX" :position="[0,0,hoehe]">
    <TresPlaneGeometry :args="[scale, scale]" />
    <TresMeshBasicMaterial v-if="material" v-bind="material"/> <!-- :map="texture" / -->
  </TresMesh>
</template>
