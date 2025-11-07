<template>
  <button :id="buttonId" class="roll-button" @click="onClick">🎲 Würfeln</button>
</template>

<script setup lang="ts">
import { defineEmits, defineProps } from 'vue'

const emit = defineEmits(['trigger'])
const props = defineProps<{ buttonId: string }>()

function onClick() {
  emit('trigger', props.buttonId)
}
</script>
