<template>
  <div
    class="flex flex-col items-center transition-all duration-200"
    :class="active
    ? 'opacity-100 scale-110 drop-shadow-[0_0_6px_rgba(255,255,255,0.8)]'
    : 'opacity-30 scale-90'"
  >
    <!-- Kopf -->
    <div
      class="w-4  h-4 rounded-full"
      :style="{ backgroundColor: color }"
    ></div>

    <!-- Hals -->
    <div
      class="w-3 h-2 -mt-0.5 rounded-sm"
      :style="{ backgroundColor: color }"
    ></div>


    <!-- Körper -->
    <div
      class="w-2.5 h-5 -mt-1.5 rounded-sm"
      :style="{ backgroundColor: color }"
    ></div>

    <!-- Basis -->
    <div
      class="w-4  h-2 -mt-0.5 rounded-sm"
      :style="{ backgroundColor: color }"
    ></div>

    <!-- Fuß -->
     <div
      class="w-5 h-2 -mt-0.5 rounded-sm"
      :style="{ backgroundColor: color }"
    ></div>

  </div>
</template>

<script setup lang="ts">
defineProps<{
  color: string
  active: boolean
}>()
</script>
