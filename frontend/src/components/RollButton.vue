<script setup lang="ts">
import { ref } from 'vue'

const rolling = ref(false)
const emit = defineEmits<{
  (e: 'trigger', id: string): void
}>()

function rollDice() {
  if (rolling.value) return
  rolling.value = true
  emit('trigger', 'diceButton')
  setTimeout(() => (rolling.value = false), 1500)
}
</script>

<template>
  <button
    class="roll-button flex items-center justify-center text-center"
    :disabled="rolling"
    @click="rollDice"
  >
    <span v-if="!rolling">🎲 Würfel werfen</span>
    <span v-else class="dots">würfelt</span>
  </button>
</template>
