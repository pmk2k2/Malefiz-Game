<template>
  <button
    class="roll-button flex items-center justify-center text-center"
    :disabled="isLoading"
    @click="handleClick"
  >
    <span v-if="!isLoading">🎲 Würfel werfen</span>
    <span v-else class="dots">⏳</span>
  </button>
</template>

<script setup lang="ts">

const props = defineProps<{
  isLoading: boolean
}>()


const emit = defineEmits(['trigger'])

function handleClick() {
  //Wenn props.isLoading true, dann läuft cooldwon und es darf nicht gewürfelt werden
  if (!props.isLoading){
    emit('trigger')
  }
}
</script>
