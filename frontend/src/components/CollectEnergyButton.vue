<template>
  <button
    class="roll-button flex items-center justify-center text-center"
    @click="handleClick"
  >
    <span>⚡ Energie sammeln</span>
  </button>
</template>

<script setup lang="ts">

const emit = defineEmits(['trigger'])

function handleClick() {
  // Feuert immer, unabhängig von Cooldowns oder States
  emit('trigger')
}
</script>