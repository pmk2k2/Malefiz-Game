<template>
  <div class="map-feature-wrapper">
    <div class="map-selector-wrapper">
      <div class="selection-row">
        <button class="left-arrow" @click="nextMap"><</button>
        <img :src="maps[currentIndex]?.image" alt="" />
        <button class="right-arrow" @click="prevMap">></button>
      </div>
      <h3 class="map-name">Map-Auswahl: {{ maps[currentIndex]?.name }}</h3>
    </div>
    <div class="button-group">
      <button class="btn create-btn" @click="openEditor">Map erstellen</button>
      <button class="btn import-btn" @click="importMap">Map Importieren</button>
    </div>
    <input
      type="file"
      ref="fileInput"
      accept=".json"
      style="display: none"
      @change="handleFileImport"
    />
  </div>
</template>

<script setup lang="ts">
import { useGameStore } from '@/stores/gamestore'
import { ref, onMounted } from 'vue'

const fileInput = ref<HTMLInputElement | null>(null)
const maps = ref<Array<{ id: string; name: string; image: string }>>([])

const currentIndex = ref(0)
const loading = ref(false)
const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'
const gameStore = useGameStore()
const presets = ref<string[]>([])
const selectedBoard = ref<string | null>(null)

const emit = defineEmits(['openEditor', 'settingsUpdated'])

onMounted(async () => {
  const gameCode = gameStore.gameData.gameCode
  if (!gameCode) return

  try {
    const res = await fetch(`${apiBase}/lobby/${gameCode}/boards/presets`)
    if (res.ok) {
      presets.value = await res.json()

      maps.value = presets.value.map((name) => ({
        id: name,
        name: formatBoardName(name),
        image: new URL('@/assets/chooseMap.png', import.meta.url).href,
      }))
    }
  } catch (err) {
    console.error('Initial loading of Maps failed', err)
  }
})

async function nextMap() {
  if (maps.value.length === 0) return
  currentIndex.value = (currentIndex.value + 1) % maps.value.length
  const map = maps.value[currentIndex.value]
  if (map) {
    await autoSelectBoard(map.id)
  }
}

async function prevMap() {
  if (maps.value.length === 0) return
  if (currentIndex.value === 0) {
    currentIndex.value = maps.value.length - 1
  } else {
    currentIndex.value--
  }
  const map = maps.value[currentIndex.value]
  if (map) {
    await autoSelectBoard(map.id)
  }
}

const importMap = () => fileInput.value?.click()
const handleFileImport = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  loading.value = true
  try {
    const res = await fetch(`${apiBase}/lobby/${gameStore.gameData.gameCode}/board/import`, {
      method: 'POST',
      body: formData,
    })

    if (res.ok) {
      alert('Map erfolgreich importiert!')
      const data = await res.json()
      const newBoardName = data.fileName

      // Die Liste mit dem neuen Map laden
      const presetRes = await fetch(
        `${apiBase}/lobby/${gameStore.gameData.gameCode}/boards/presets`,
      )
      if (presetRes.ok) {
        const freshPresets = await presetRes.json()

        maps.value = freshPresets.map((name: string) => ({
          id: name,
          name: formatBoardName(name),
          image: new URL('@/assets/chooseMap.png', import.meta.url).href,
        }))

        const newIndex = maps.value.findIndex((m) => m.id === newBoardName)
        if (newIndex !== -1) currentIndex.value = newIndex

        await autoSelectBoard(newBoardName)
      }
    } else {
      const errorData = await res.json().catch(() => ({}))
      alert('Fehler beim Import: ' + (errorData.message || 'Unbekkanter Fehler'))
    }
  } catch (err) {
    console.error('Upload fehlgeschlafen', err)
    alert('Verbindung zum Server fehlgeschlagen.')
  } finally {
    loading.value = false
    target.value = '' // Das ermöglicht, die selbe Datei nochmal zu wählen
  }
}
// --- Board Logic ---
const formatBoardName = (n: string) =>
  n
    .replace('.json', '')
    .replace(/([A-Z])/g, ' $1')
    .trim()

// Logik von vorherige Methode confirmAll in EinstellungView
const autoSelectBoard = async (presetName: string) => {
  const res = await fetch(
    `${apiBase}/lobby/${gameStore.gameData.gameCode}/board/select-preset?playerId=${gameStore.gameData.playerId}&presetName=${presetName}`,
    { method: 'POST' },
  )
}

const openEditor = () => {
  selectedBoard.value = 'custom'
  emit('openEditor')
}
</script>

<style scoped>
.map-feature-wrapper {
  display: flex;
  align-items: center;
  flex-direction: column;
}

.button-group {
  display: flex;
  flex-direction: row;
  gap: 15px;
}

.create-btn,
.import-btn {
  width: 190px;
  padding: 8px 0 !important;
  margin: 8px;
  border-bottom-width: 4px !important;
  font-size: 1rem !important;
}

.map-selector-wrapper {
  width: 400px;
  height: 260px;
  background-color: #3d2b1f;
  background-image:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 100%),
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 38px,
      rgba(0, 0, 0, 0.15) 39px,
      rgba(0, 0, 0, 0.15) 40px
    );
  border: 5px solid #2d1b0d;
  border-radius: 15px;
  padding: 15px;
  box-shadow:
    inset 0 0 20px rgba(0, 0, 0, 0.5),
    0 10px 20px rgba(0, 0, 0, 0.4);
}

.selection-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

img {
  width: 220px;
  height: 190px;
  object-fit: cover;
  border: 3px solid #2d1b0d;
  border-radius: 8px;
  background-color: #000;
}

.left-arrow,
.right-arrow {
  width: 45px;
  height: 45px;
  background-color: #634121;
  color: #ffcc66;
  font-size: 1.5rem;
  font-weight: bold;
  border: 3px solid #2d1b0d;
  border-bottom-width: 6px;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.1s ease;
}

.left-arrow:hover,
.right-arrow:hover {
  filter: brightness(1.2);
  transform: translateY(-2px);
}

.left-arrow:active,
.right-arrow:active {
  transform: translateY(3px);
  border-bottom-width: 3px;
}

.map-name {
  color: #ffcc66;
  text-shadow: 2px 2px 0px #2d1b0d;
  margin: 0;
  font-family: 'MedievalSharp', cursive, serif;
  text-align: center;
}
</style>
