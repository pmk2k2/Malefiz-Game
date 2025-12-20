<script setup lang="ts">
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import TheGrid from '@/components/playingfield/TheGrid.vue'
import TheMapBarrierEditor from '@/components/playingfield/TheMapBarrierEditor.vue'
import { computed, ref } from 'vue';
import { NodeFunctionInput } from 'three/webgpu';

const gridRef = ref<any>(null)
const sichtbar = ref(false)

const liveGrid = computed(() => {
  return gridRef.value?.grid || { cols: 11, rows: 8, cells: [] }
})

const liveFigures = computed(() => {
  return gridRef.value?.figures || []
})

function onRoll(id: string) {
  console.log('Button pressed:', id)
  rollDice()
}

function openCensoredMap() {
  sichtbar.value = true;
  console.log("CensoredMap geöffnet")
}

function closeCensoredMap() {
  sichtbar.value = false
  console.log("CensoredMap geschlossen")
}

</script>

<template>
  <div class="relative h-screen w-screen overflow-hidden bg-[#111827]">
    <!-- 3D-Spielfeld -->
    <TheGrid ref="gridRef"/>

    <!-- Overlay-UI oben links -->
    <div class="pointer-events-none absolute inset-0 flex items-start m-2">
      <div class="pointer-events-auto flex w-80 flex-col gap-6 rounded-2xl bg-black/40 p-2">
        <div class="flex flex-col items-center">
          <div class="h-60 w-60">
            <Dice3D />
          </div>
          <RollButton buttonId="diceButton" @trigger="onRoll" />
        </div>
      </div>
    </div>

    <div class="absolute bottom-4 right-4 z-10">
      <button class="open p-2 bg-green-600 text-white rounded-lg" @click="openCensoredMap">open map</button>
    </div>

    <div v-show="sichtbar" class="absolute inset-0 bg-black/80 z-20 flex items-center justify-center">
      <div class="h-[80vh] w-[80vw] bg-[#222] rounded-xl relative">
        <TheMapBarrierEditor 
            :grid="liveGrid" 
            :figures="liveFigures" 
        />
        <button class="absolute top-4 right-4 bg-red-600 text-white p-2 rounded-full z-30" @click="closeCensoredMap">X</button>
          </div>      
        </div>
      </div>
</template>
