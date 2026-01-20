<script setup lang="ts">
import { useLoader, useTres } from '@tresjs/core';
import { shallowRef, watch } from 'vue';
import { BackSide, CubeTexture, CubeTextureLoader, LinearFilter, MeshBasicMaterial, TextureLoader } from 'three';

const { scene } = useTres()
const texturePaths = [
    '/skybox/attic/Attic2048-Cubemap/px.png',
    '/skybox/attic/Attic2048-Cubemap/nx.png',
    '/skybox/attic/Attic2048-Cubemap/py.png',
    '/skybox/attic/Attic2048-Cubemap/ny.png',
    '/skybox/attic/Attic2048-Cubemap/pz.png',
    '/skybox/attic/Attic2048-Cubemap/nz.png'
] as string[]
const { state: texture } = useLoader(CubeTextureLoader as any, texturePaths as any) // Textur fuer Env/Background
// Texturen fuer den Cube
const { state: cube_px }= useLoader(TextureLoader, texturePaths[0] as string)
const { state: cube_nx }= useLoader(TextureLoader, texturePaths[1] as string)
const { state: cube_py }= useLoader(TextureLoader, texturePaths[2] as string)
const { state: cube_ny }= useLoader(TextureLoader, texturePaths[3] as string)
const { state: cube_pz }= useLoader(TextureLoader, texturePaths[4] as string)
const { state: cube_nz }= useLoader(TextureLoader, texturePaths[5] as string)

const skyboxMats = shallowRef()
watch([cube_px, cube_nx, cube_py, cube_ny, cube_pz, cube_nz], (textures) => {
    if(textures.every(t => t)) {    // jede textur soll geladen sein
        // Material-Array bauen mit den einzelnen Texturen je Material
        const materials = textures.map(tex => {
            return new MeshBasicMaterial({
                color: '#AA9988',   // abdunkeln
                map: tex,
                side: BackSide,     // rendern im inneren des cubes
                depthWrite: false   // ignoriert tiefe fuer hintergrund
            })
        })
        skyboxMats.value = materials    // Update triggern
    }
})

watch(texture, (map) => {
    if(map && scene.value) {
        (map as CubeTexture).magFilter = LinearFilter;
        (map as CubeTexture).minFilter = LinearFilter    // ka ob das ueberhaupt was aendert
        scene.value.background = (map as CubeTexture)
        scene.value.environment = (map as CubeTexture)
        scene.value.backgroundBlurriness = 0.01
    }
})
</script>

<template>
    <!-- Spiegelung an x-Achse, damit Textur mit Env-Map uebereinstimmt -->
    <TresMesh :position="[0,0,0]"
        v-if="skyboxMats"
        :material="skyboxMats"
        :scale="[-1,1,1]"
    >
        <TresBoxGeometry
            :args="[350,350,350]"
        />
    </TresMesh>
</template>