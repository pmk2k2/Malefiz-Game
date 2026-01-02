<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { TresCanvas } from '@tresjs/core'
import RollButton from '@/components/RollButton.vue'
import CollectEnergyButton from '@/components/CollectEnergyButton.vue' 
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import TheGrid from '@/components/playingfield/TheGrid.vue'
import TheMapBarrierEditor from '@/components/playingfield/TheMapBarrierEditor.vue'
import { NodeFunctionInput } from 'three/webgpu';

const gridRef = ref<any>(null)
const sichtbar = ref(false)

const liveGrid = computed(() => {
  return gridRef.value?.grid || { cols: 11, rows: 8, cells: [] }
})

const liveFigures = computed(() => {
  return gridRef.value?.figures || []
})


function openCensoredMap() {
  sichtbar.value = true;
  console.log("CensoredMap geöffnet")
}

function closeCensoredMap() {
  sichtbar.value = false
  console.log("CensoredMap geschlossen")
}

import { useGameStore } from '@/stores/gamestore'


const gameStore = useGameStore()

// Steuert alles: Button-Animation, Disabled-State und Logik
const isBusy = ref(false)
const isSavingEnergy = ref(false) // Nur für Energie
const cooldownSeconds = ref(3) // Default 3s, wird überschrieben

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL


// Cooldown-Zeit beim Start laden
onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE_URL}/daten/cooldown`)
    if (res.ok) {
      cooldownSeconds.value = await res.json()
    }
  } catch (e) {
    console.error("Cooldown konnte nicht geladen werden", e)
  }
})

async function onRoll(id: string) {
  const { gameCode, playerId } = gameStore.gameData

  // Sicherheitscheck
  if (!gameCode || !playerId) return
  if(isBusy.value || isSavingEnergy.value) return // Kein Doppelklick möglich und nicht während energie gesammelt wird


  isBusy.value = true

    try {
    // Würfelwurf (Backend & 3D Animation)
    // Warten auf die Animation, aber der Button bleibt gesperrt
    await rollDice(gameCode, playerId)

    // Der Timer startet für den restlichen Cooldown
    // Die Animation im Button läuft weiter, weil isBusy true bleibt
    startCooldownTimer()

  } catch (e) {
    console.error("Fehler beim Würfeln", e)
    // Bei Fehler: Button sofort wieder freigeben oder Fehlermeldung zeigen
    isBusy.value = false 
  }
}

//funktion zum Speichern der Energie
async function saveEnergy() {
  const { gameCode, playerId } = gameStore.gameData
  if (!gameCode || !playerId) return

  //prüfen ob Button blockiert ist
  if(isBusy.value || isSavingEnergy.value) return

  // Sucht nach Figuren der Spieler
  const myFigure = liveFigures.value.find((f: any) => f.playerId === playerId)
  isSavingEnergy.value = true 

  try {
    //Ziel URL 
    const url = `${API_BASE_URL}/move/${gameCode}`;

    //aufruf an Backend um im Spiel Energie zu sammeln anstatt zu ziehen
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        playerId: playerId,
        direction: "north", 
        collectEnergy: true 
      })
    })
  } catch(e) {
    console.error(e)
  } finally {
    isSavingEnergy.value = false
  }
}

function startCooldownTimer() {
  // Zeit vom Backend
  setTimeout(() => {
    // Erst hier hört die Animation auf und der Button wird wieder klickbar
    isBusy.value = false
  }, cooldownSeconds.value * 1000) 
}

</script>

<template>
  <div class="relative h-screen w-screen overflow-hidden bg-[#111827]">
    <!-- 3D-Spielfeld -->
    <TresCanvas clear-color="#87CEEB" class="w-full h-full">
      <TheGrid ref="gridRef" />
    </TresCanvas>

    <div class="pointer-events-none absolute inset-0 flex items-start m-2 z-50">
      
      <div class="pointer-events-auto flex w-96 flex-col gap-6 rounded-2xl bg-black/40 p-4 backdrop-blur-sm border border-white/10">
        
        <div class="flex flex-col items-center gap-4">
          <div class="h-40 w-40 relative">
            <Dice3D />
          </div>
          
          <div class="flex flex-row gap-2 w-full justify-center">
            <RollButton 
              :is-loading="isBusy" 
              @trigger="onRoll" 
            />
            
            <CollectEnergyButton
              :is-loading="isSavingEnergy"
              @trigger="saveEnergy"
            />
          </div>

        </div>
      </div>
    </div>

    <div class="absolute bottom-4 right-4 z-10">
      <button class="open p-2 bg-green-600 text-white rounded-lg" @click="openCensoredMap">open map</button>
    </div>

    <div v-if="sichtbar" class="absolute inset-0 bg-black/80 z-20 flex items-center justify-center">
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
