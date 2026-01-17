<template>
  <div class="bg-[rgba(255,255,255,0.05)] p-4 rounded-xl shadow-inner ring-2 ring-[#8B5E3C]">

    <div class="flex items-center gap-2 mb-3">
      <span class="w-4 h-4 rounded-full" :style="{ backgroundColor: player.color }"></span>
      <span class="font-semibold text-white">{{ player.name }}</span>
    </div>


    <div class="flex items-center gap-3 mb-2">
      <span class="w-16 text-sm font-medium text-slate-200">Haus:</span>
      <div class="flex gap-2">
        <HudPawn
          v-for="fig in player.home"
          :key="fig.id"
          :color="player.color"
          :active="fig.id === selectedFigureId"
        />
        <span v-if="player.home.length === 0" class="text-xs text-slate-400 italic">
          leer
        </span>
      </div>
    </div>


    <div class="flex items-center gap-3">
      <span class="w-16 text-sm font-medium text-slate-200">Feld:</span>
      <div class="flex gap-2">
        <HudPawn
          v-for="fig in player.field"
          :key="fig.id"
          :color="player.color"
          :active="fig.id === selectedFigureId"
        />
        <span v-if="player.field.length === 0" class="text-xs text-slate-400 italic">
          keine
        </span>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import type { ISpielerDTD } from "@/stores/ISpielerDTD"
import HudPawn from "@/components/hud/HUDPawn.vue"

interface PlayerRow {
  playerId: string
  name: string
  color: string
  home: ISpielerDTD["spielfiguren"]
  field: ISpielerDTD["spielfiguren"]
  
}

const props = defineProps<{
  player: PlayerRow
  selectedFigureId: string | null
}>()
</script>
