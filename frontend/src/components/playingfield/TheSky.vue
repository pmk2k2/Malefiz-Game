<script setup lang="ts">
import { useLoader, useTres } from '@tresjs/core';
import { shallowRef, watch, watchEffect } from 'vue';
import { BackSide, CubeTextureLoader, LinearFilter, MeshBasicMaterial, TextureLoader } from 'three';
import { materialSheen } from 'three/tsl';

const { scene } = useTres()
const texturePaths = [
    '/skybox/attic/Attic2048-Cubemap/px.png',
    '/skybox/attic/Attic2048-Cubemap/nx.png',
    '/skybox/attic/Attic2048-Cubemap/py.png',
    '/skybox/attic/Attic2048-Cubemap/ny.png',
    '/skybox/attic/Attic2048-Cubemap/pz.png',
    '/skybox/attic/Attic2048-Cubemap/nz.png'
]
const { state: texture } = useLoader(CubeTextureLoader, texturePaths)

watch(texture, (map) => {
    if(map && scene.value) {
        map.magFilter = LinearFilter 
        map.minFilter = LinearFilter
        scene.value.background = map
        scene.value.backgroundBlurriness = 0.04
    }
})
/*
watchEffect(() => {
    if(scene.value) {
        scene.value.backgroundIntensity = 0.05
        scene.value.backgroundBlurriness = 0.02
    }
})
*/
</script>

<template>
</template>