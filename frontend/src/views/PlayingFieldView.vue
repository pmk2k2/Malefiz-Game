<!-- Einstiegspunkt fuer die Spielfeld-Szene -->

<script setup lang="ts">
import { TresCanvas } from '@tresjs/core';
import PlayingField from '@/components/playingfield/ThePlayingField.vue';
</script>

<template>
    <TresCanvas
        clear-color="#82DBC5"
        window-size
    >
        <PlayingField />
    </TresCanvas>
</template>