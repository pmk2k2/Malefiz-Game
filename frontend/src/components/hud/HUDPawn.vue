<template>
  <div
    class="flex flex-col items-center transition-all duration-200"
    :class="active ? 'opacity-100 scale-100' : 'opacity-25 scale-90'"
  >
    <!-- Kopf -->
    <div
      class="w-3 h-3 rounded-full"
      :style="{ backgroundColor: color }"
    />

    <!-- Körper -->
    <div
      class="w-2.5 h-4 -mt-0.5 rounded-sm"
      :style="{ backgroundColor: color }"
    />

  </div>
</template>

<script setup lang="ts">
defineProps<{
  color: string
  active: boolean
}>()
</script>
