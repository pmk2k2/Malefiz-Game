<script setup lang="ts">
import { useLoop, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import { computed, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import TheRock from './models/TheRock.vue'
import TheTree from './models/TheTree.vue'
import TheCrown from './models/TheCrown.vue'
import TheGrass from './models/TheGrass.vue'
import Barrier from './models/Barrier.vue'
import ThePlayerFigure from '@/components/playingfield/ThePlayerFigure.vue'
import { useGameStore } from '@/stores/gamestore'
import type { IPlayerFigure } from '@/stores/IPlayerFigure'
import type { IFigureMoveRequest } from '@/services/IFigureMoveRequest'
import { useAnimationQueue } from '@/composable/useAnimationQueue'
import { storeToRefs } from 'pinia'

// Zellentypen
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL' | 'BARRIER'

// Zellenkoordinten
interface Field {
  i: number
  j: number
  type?: CellType
}

// Das Spielfeld
interface Board {
  cols: number
  rows: number
  grid: Field[][]
}
console.log('Erstelle Grid')

const gameStore = useGameStore()
const CELL_SIZE = 2
const board = ref<Board | null>(null)
const isLoading = ref(true)
const gameCode = gameStore.gameData.gameCode

// Player figures
const { figures } = storeToRefs(gameStore)
const currentPlayerId = gameStore.gameData.playerId
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// Animationqueue
const ANIMATION_DURATION = 300 
const { queueMove, queueRotation, vertJump } = useAnimationQueue()

// Maximale Energie
const MAX_ENERGY = 10

// Camera controls
const camRef = shallowRef<TresObject | null>(null)
const default_cam_pos: [number, number, number] = [0, 15, 18]
const camHeight = 1.2
const figureControlInd = ref(0) 
const egoPersp = ref(true)
const figureViewDir = ref(-1)

const { onBeforeRender } = useLoop()

const ownFigures = computed(() => {
  if (!currentPlayerId) return []
  return figures.value.filter((fig) => fig.playerId === currentPlayerId)
})

const allPlayers = computed(() => {
  const playersMap = new Map<string, { id: string; color: string }>()
  figures.value.forEach((fig) => {
    if (!playersMap.has(fig.playerId)) {
      playersMap.set(fig.playerId, { id: fig.playerId, color: fig.color })
    }
  })
  return Array.from(playersMap.values())
})

const isFigureLocked = computed(() => {
  return (
    gameStore.gameData.remainingSteps !== null &&
    gameStore.gameData.remainingSteps > 0 &&
    gameStore.gameData.movingFigure !== null
  )
})

const isCurrentlyJumping = computed(() => {
  // Wichtig: .value nutzen
  const currentFig = ownFigures.value[figureControlInd.value]
  return currentFig?.currentAnim?.isJump === true
})

onMounted(async () => {
  isLoading.value = true

  const fetched = await getBoardFromBackend()
  if (fetched) {
    board.value = fetched
    await fetchGameState()
  } else {
    console.error('Board konnte nich geladen werden, Figuren-Rendering wird übersprungen.')
  }
  window.addEventListener('keydown', onKeyDown)
  isLoading.value = false
  const gameCode = gameStore.gameData.gameCode
  const playerId = gameStore.gameData.playerId
  if (gameCode != null && playerId != null) {
    gameStore.startIngameLiveUpdate(gameCode, playerId)
    gameStore.startLobbyLiveUpdate(gameCode)
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown)
})

watch(
  () => gameStore.gameData.requireInput,
  (newVal) => {
    console.log('Eingriff durch Nutzer erforderlich? ', newVal)
  },
)

watch(
  () => gameStore.ingameMoveEvent,
  async (newEv) => {
    console.log('Neues Move-Event eingetroffen: ', newEv)
    if (!newEv) return
    if (newEv.operation === 'BARRIER_PLACED') {
      const fetched = await getBoardFromBackend()
      if (fetched) {
        board.value = { ...fetched }
      }
      return
    }

    const index = figures.value.findIndex(
      (fig) => fig.id === newEv.figureId && fig.playerId === newEv.id,
    )
  
    if (newEv.bewegung.startX == null || newEv.bewegung.startZ == null) return
    if (!(newEv.bewegung.startX < 0 || newEv.bewegung.startZ < 0)) {
      const startPosField = cellToField({ i: newEv.bewegung.startX, j: newEv.bewegung.startZ })
      newEv.bewegung.startX = startPosField[0]
      newEv.bewegung.startZ = startPosField[2]
    } else {
      newEv.bewegung.startX = null
      newEv.bewegung.startZ = null
    }
    const endPosField = cellToField({ i: newEv.bewegung.endX, j: newEv.bewegung.endZ })
    newEv.bewegung.endX = endPosField[0]
    newEv.bewegung.endZ = endPosField[2]

    queueMove(index, newEv.bewegung, ANIMATION_DURATION)
    if (!figures.value[index]) return
    figures.value[index].orientation = newEv.bewegung.dir.toLowerCase()
  },
)

async function sendBoard(boardData: Board) {
  try {
    const response = await fetch(`/api/board?code=${gameCode}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(boardData),
    })
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const data = await response.json()
    console.log('Board sent successfully:', data)
  } catch (err) {
    console.error('Error sending board:', err)
  }
}

async function getBoardFromBackend(): Promise<Board | null> {
  try {
    const response = await fetch(`${API_BASE_URL}/board?code=${gameCode}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    })
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const text = await response.text()
    if (!text) return null
    const data = JSON.parse(text) as Board
    console.log('Board received', data)
    return data
  } catch (err) {
    console.error('Error getting board:', err)
    return null
  }
}

// Funktion, um dem Server den Verbrauch zu melden
async function consumeEnergy() {
  const { gameCode, playerId } = gameStore.gameData
  
  if (!gameCode || !playerId) return

  try {
    // equest an den neuen Backend-Endpunkt senden
    const response = await fetch(`${API_BASE_URL}/move/${gameCode}/consume-energy`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ playerId: playerId }),
    })

    // Wenn der Server "OK" sagt (Status 200), setzen wir die Energie lokal auf 0
    if (response.ok) {
        console.log("Energieverbrauch bestätigt. Setze Anzeige auf 0.")
        gameStore.gameData.energy = 0
    } else {
        console.warn("Fehler: Server hat Energieverbrauch abgelehnt.")
    }

  } catch (e) {
    console.error("Netzwerkfehler beim Energieverbrauch:", e)
  }
}

async function fetchGameState() {
  const gameCode = gameStore.gameData.gameCode
  if (!gameCode) return
  try {
    const res = await fetch(`${API_BASE_URL}/game/${gameCode}/figures`)
    if (!res.ok) throw new Error('Failed to fetch figures')
    const backendFigures: IPlayerFigure[] = await res.json()
    const players = [...new Set(backendFigures.map((f) => f.playerId))].sort()
    figures.value = backendFigures.map((fig) => {
      const playerIndex = players.indexOf(fig.playerId)
      const [x, y, z] = calculateHomePosition(fig, playerIndex, players.length)
      return {
        ...fig,
        currentAnim: null,
        animQueue: [],
        position: [x, y, z],
        viewDirRot: 0,
      } as IPlayerFigure
    })
  } catch (error) {
    console.error('Fehler beim Laden der Figuren:', error)
  }
}

function _calculateHomeBaseOffset(playerIndex: number, playersCount: number) {
  const b = board.value
  if (!b) return { x: 0, z: 0 }
  const boardWidth = b.cols * CELL_SIZE
  let x_offset = 0
  const z_offset = (b.rows / 2) * CELL_SIZE + 2

  if (playersCount === 2) {
    x_offset = playerIndex === 0 ? -boardWidth / 3 : boardWidth / 3
  } else if (playersCount === 3) {
    if (playerIndex === 0) x_offset = -boardWidth / 3
    else if (playerIndex === 1) x_offset = boardWidth / 3
    else if (playerIndex === 2) x_offset = 0
  } else if (playersCount === 4) {
    const figSpacing = 0.5
    const totalHomeWidth = playersCount * (figSpacing * 5) + (playersCount - 1) * 2.5
    const base_x = -totalHomeWidth / 2 + (figSpacing * 5) / 2
    x_offset = base_x + playerIndex * (figSpacing * 5 + 2.5)
  }
  return { x: x_offset, z: z_offset }
}

function calculateHomePosition(
  figure: IPlayerFigure,
  playerIndex: number,
  playersCount: number,
): [number, number, number] {
  const figIndex = Number(figure.id.split('-').pop()!) - 1
  const figSpacing = 0.45
  const y = 0
  const { x: x_offset, z: z_offset } = _calculateHomeBaseOffset(playerIndex, playersCount)
  const x_pos = x_offset + (figIndex - 2) * figSpacing * 2
  return [x_pos, y, z_offset]
}

function calculateHomeCenter(playerId: string) {
  const players = allPlayers.value.map((p) => p.id).sort()
  const playerIndex = players.indexOf(playerId)
  const playersCount = players.length
  return _calculateHomeBaseOffset(playerIndex, playersCount)
}

onBeforeRender(({ delta }) => {
  const cam = camRef.value as any
  if (!cam) return

  if (egoPersp.value) {
    const fig = ownFigures.value[figureControlInd.value]
    if (!fig) return
    const [x, y, z] = fig.position
    if(y == undefined) return

    cam.position.set(x, camHeight + (y - 0.2), z)

    // Check auf Sprung
    const isJumping = fig.currentAnim?.isJump === true
    if (!isJumping) {
      cam.rotation.set(0, Math.PI * fig.viewDirRot, 0)
    } 
  } else {
    cam.position.set(default_cam_pos[0], default_cam_pos[1], default_cam_pos[2])
    cam.lookAt(0, 0, 0)
  }
})

function onKeyDown(event: KeyboardEvent) {
  // 1. Wenn Sprung läuft: Blockieren
  if (isCurrentlyJumping.value) {
    return 
  }

  const key = event.key
  const numOwnFigures = ownFigures.value.length

  // 2. Figur wechseln
  if (key === 'ArrowRight' || key === 'ArrowLeft') {
    event.preventDefault()

    // Regelwerk-Check
    if (isFigureLocked.value) {
      alert('Figurwechsel gesperrt – Zug läuft noch')
      return
    }

    if (numOwnFigures === 0) return

    if (key === 'ArrowRight') {
      figureControlInd.value = (figureControlInd.value + 1) % numOwnFigures
    } else if (key === 'ArrowLeft') {
      figureControlInd.value = (figureControlInd.value - 1 + numOwnFigures) % numOwnFigures
    }
  }

  // 3. Sprung 
  if (key === ' ') { 
    event.preventDefault() 
    
    // Prüfen, ob genug Energie da ist
    // 0, falls undefined
    const currentEnergy = gameStore.gameData.energy || 0 

    if (currentEnergy < MAX_ENERGY) {
      console.log(`Nicht genug Energie für Sprung (${currentEnergy}/${MAX_ENERGY})`)
      return 
    }

    // Figur finden und Sprung auslösen
    const fig = ownFigures.value[figureControlInd.value]
    if (fig) {
      const globalIndex = figures.value.findIndex(f => f.id === fig.id)
      if (globalIndex !== -1) {
        
        // Animation starten
        console.log("Energie voll - Sprung wird ausgeführt!")
        vertJump(globalIndex, 2000) 
        
        // Energie verbrauchen (Backend call + lokaler Reset)
        consumeEnergy()
      }
    }
  }

  // 4. Drehung
  if (key === 'a' || key === 'A') rotateCurrentFigure(-1)
  if (key === 'd' || key === 'D') rotateCurrentFigure(1)

  // 5. Move
  if (key === 'w' || key === 'W') sendMoveDirection()

  // 6. Perspektive
  if (key === 'e' || key === 'E') {
    egoPersp.value = !egoPersp.value
   
  }
}

const allCells = computed<Field[]>(() => {
  if (!board.value) return []
  return board.value.grid.flat()
})

function cellToField(cell: Field): [number, number, number] {
  if (!board.value) return [0, 0, 0]
  const x = (cell.i - board.value.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - board.value.rows / 2 + 0.5) * CELL_SIZE)
  return [x, 0.05, z]
}

function getCurrentFigureRot() {
  if (!ownFigures.value[figureControlInd.value]) return
  switch (ownFigures.value[figureControlInd.value]?.orientation) {
    case 'north': figureViewDir.value = 0; break;
    case 'east': figureViewDir.value = 1; break;
    case 'south': figureViewDir.value = 2; break;
    case 'west': figureViewDir.value = 3; break;
  }
}

function rotateCurrentFigure(rot: number) {
  const fig = ownFigures.value[figureControlInd.value]
  if (!fig) return

  getCurrentFigureRot()
  if (rot <= 0) {
    figureViewDir.value = (figureViewDir.value - 1 + 4) % 4
  } else {
    figureViewDir.value = (figureViewDir.value + 1) % 4
  }
  const startRot = fig.viewDirRot
  let targetRot
  switch (figureViewDir.value) {
    case 0: fig.orientation = 'north'; targetRot = 0; break
    case 1: fig.orientation = 'east'; targetRot = -0.5; break
    case 2: fig.orientation = 'south'; targetRot = 1; break
    case 3: fig.orientation = 'west'; targetRot = 0.5; break
  }
  const index = figures.value.findIndex(
    (figInd) => figInd.id === fig.id && figInd.playerId === fig.playerId,
  )
  if (targetRot === undefined) return
  queueRotation(index, startRot, targetRot, ANIMATION_DURATION / 2)
}

async function sendMoveDirection() {
  if (isCurrentlyJumping.value) {
    console.log("Blockiert: Figur springt gerade.")
    return
  }

  if (!gameStore.gameData.moveChoiceAllowed) {
    console.log('---> Bewegung gerade nicht erlaubt')
    return
  }

  if (gameStore.gameData.movingFigure) {
    if (gameStore.gameData.movingFigure !== ownFigures.value[figureControlInd.value]?.id) {
      alert('Du musst deinen Zug mit der Figur beenden!')
      return
    }
  }

  if (gameStore.gameData.forbiddenDir) {
    if (
      gameStore.gameData.forbiddenDir.toLowerCase() ===
      ownFigures.value[figureControlInd.value]?.orientation
    ) {
      alert('Du darfst nicht zurueck gehen!')
      return
    }
  }

  gameStore.gameData.requireInput = false
  gameStore.gameData.moveChoiceAllowed = false

  if (currentPlayerId === null) return
  const figureIdReq = ownFigures.value[figureControlInd.value]?.id
  const directionReq = ownFigures.value[figureControlInd.value]?.orientation

  if (!figureIdReq || !directionReq) return
  const moveReq: IFigureMoveRequest = {
    playerId: currentPlayerId,
    figureId: figureIdReq,
    direction: directionReq,
  }

  try {
    const response = await fetch(`/api/move/${gameCode}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(moveReq),
    })
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
    const data = await response.json()
    console.log('Move-Request sent successfully:', data)
    if (data.success === false) {
      gameStore.gameData.requireInput = true
      gameStore.gameData.moveChoiceAllowed = true
      alert('Ueberprufe deine Richtungseingabe!')
    }
  } catch (err) {
    console.error('Error sending move request:', err)
  }
}

const selectedFigure = computed(() => {
  return ownFigures.value[figureControlInd.value] ?? null
})

watch(
  selectedFigure,
  (fig) => {
    gameStore.selectedFigureId = fig?.id ?? null
  },
  { immediate: true },
)

defineExpose({ board, figures })
</script>

<template>
  <TresPerspectiveCamera ref="camRef" :position="default_cam_pos" :look-at="[0, 0, 0]" />
  
  <OrbitControls
    ref="orbitRef"
    v-if="!egoPersp || (egoPersp && ownFigures[figureControlInd]?.currentAnim?.isJump)"
    :enablePan="false"
    :enableZoom="false"
    :enableDamping="true"
    :minPolarAngle="0"
    :maxPolarAngle="Math.PI"
    :rotateSpeed="0.6"
  />

  <TresDirectionalLight :position="[20, 40, 10]" :intensity="1.5" />

  <template v-if="board">
    <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
      <TresPlaneGeometry :args="[board.cols * CELL_SIZE * 5, board.rows * CELL_SIZE * 5]" />
      <TresMeshStandardMaterial color="#b6e3a5" :roughness="1" :metalness="0" />
    </TresMesh>

    <TresMesh
      v-for="cell in allCells"
      :key="`cell-${cell.i}-${cell.j}-${cell.type}`"
      :position="cellToField(cell)"
      :rotation="[-Math.PI / 2, 0, 0]"
    >
      <template v-if="cell.type === 'PATH' || cell.type === 'START'">
        <TheRock />
      </template>
      <template v-if="cell.type === 'BARRIER'">
        <Barrier />
        <TheRock />
      </template>
      <template v-else-if="cell.type === 'BLOCKED'">
        <TheTree />
        <TheGrass />
      </template>
      <template v-else-if="cell.type === 'GOAL'">
        <TheRock />
        <TheCrown />
      </template>
    </TresMesh>

    <template v-for="(player, index) in allPlayers" :key="`home-${player.id}`">
      <TresMesh
        :position="[calculateHomeCenter(player.id).x, -0.01, calculateHomeCenter(player.id).z]"
        :rotation="[-Math.PI / 2, 0, 0]"
      >
        <TresPlaneGeometry :args="[6, 3]" />
        <TresMeshStandardMaterial :color="player.color || '#cccccc'" />
      </TresMesh>
    </template>

    <ThePlayerFigure
      v-for="fig in figures"
      :key="fig.id"
      :position="fig.position"
      :color="fig.color"
      :orientation="fig.orientation"
    />
  </template>
</template>