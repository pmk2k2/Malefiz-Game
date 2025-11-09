<!--  Spielfeld-Szene -->
<script setup lang="ts">
import { OrbitControls } from '@tresjs/cientos'
</script>

<template>
    <TresPerspectiveCamera
        :position="[ 7, 7, 7]" 
        :look-at="[ 0, 0, 0]" 
    />

    <TresAxesHelper />
    <TresGridHelper />
    <OrbitControls />

</template>