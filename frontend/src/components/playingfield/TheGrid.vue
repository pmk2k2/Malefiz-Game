<script setup lang="ts">
import { useLoop } from '@tresjs/core'
import { Align, OrbitControls } from '@tresjs/cientos'
import { computed, onMounted, onUnmounted, ref, shallowRef, watch, watchEffect } from 'vue'
import TheRock from './models/TheRock.vue'
import TheTree from './models/TheTree.vue'
import TheCrown from './models/TheCrown.vue'
import TheGrass from './models/TheGrass.vue'
import TheDuel from './models/TheDuel.vue'
import Barrier from './models/TheBarrier.vue'
import TheTable from './models/TheTable.vue'
import ThePlayerFigure from '@/components/playingfield/ThePlayerFigure.vue'
import { useGameStore } from '@/stores/gamestore'
import type { IPlayerFigure } from '@/stores/IPlayerFigure'
import type { IFigureMoveRequest } from '@/services/IFigureMoveRequest'
import { useAnimationQueue } from '@/composable/useAnimationQueue'
import { storeToRefs } from 'pinia'
import { DirectionalLight, type PerspectiveCamera } from 'three'
import TheSky from './TheSky.vue'
import { useShadowLights } from '@/composable/useShadowLights'
import { useInfo } from '@/composable/useInfo'

// Zellentypen
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL' | 'BARRIER' | 'DUEL'

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
const { setzeInfo } = useInfo()

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
const camRef = shallowRef<PerspectiveCamera | null>(null)
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
  // Layers der Kamera setzen
  if(camRef.value) {
    //camRef.value.layers.disable(1)
    camRef.value.layers.enable(1)
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
      console.log('Energieverbrauch bestätigt. Setze Anzeige auf 0.')
      gameStore.gameData.energy = 0
    } else {
      console.warn('Fehler: Server hat Energieverbrauch abgelehnt.')
    }
  } catch (e) {
    console.error('Netzwerkfehler beim Energieverbrauch:', e)
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

    //map old positions by id
    const oldState = new Map(
      figures.value.map((f) => [
        f.id,
        {
          position: f.position,
          orientation: f.orientation,
          viewDirRot: f.viewDirRot,
        },
      ]),
    )

    figures.value = backendFigures.map((fig) => {
      const playerIndex = players.indexOf(fig.playerId)
      const old = oldState.get(fig.id)
      // try to keep old position if exists, else fallback to home position
      const position = old?.position ?? calculateHomePosition(fig, playerIndex, players.length)
      const orientation = old?.orientation ?? fig.orientation
      const viewDirRot = old?.viewDirRot ?? 0

      return {
        ...fig,
        currentAnim: null,
        animQueue: [],
        position,
        orientation,
        viewDirRot,
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
  const cam = camRef.value as PerspectiveCamera
  if (!cam) return

  if (egoPersp.value) {
    const fig = ownFigures.value[figureControlInd.value]
    if (!fig) return
    const [x, y, z] = fig.position
    if (y == undefined) return

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
      setzeInfo('Figurwechsel gesperrt – Zug läuft noch', 'info')
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
      setzeInfo(`Nicht genug Energie für Sprung (${currentEnergy}/${MAX_ENERGY})`, 'info')
      return
    }

    // Figur finden und Sprung auslösen
    const fig = ownFigures.value[figureControlInd.value]
    if (fig) {
      const globalIndex = figures.value.findIndex((f) => f.id === fig.id)
      if (globalIndex !== -1) {
        // Animation starten
        console.log('Energie voll - Sprung wird ausgeführt!')
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
    case 'north':
      figureViewDir.value = 0
      break
    case 'east':
      figureViewDir.value = 1
      break
    case 'south':
      figureViewDir.value = 2
      break
    case 'west':
      figureViewDir.value = 3
      break
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
    case 0:
      fig.orientation = 'north'
      targetRot = 0
      break
    case 1:
      fig.orientation = 'east'
      targetRot = -0.5
      break
    case 2:
      fig.orientation = 'south'
      targetRot = 1
      break
    case 3:
      fig.orientation = 'west'
      targetRot = 0.5
      break
  }
  const index = figures.value.findIndex(
    (figInd) => figInd.id === fig.id && figInd.playerId === fig.playerId,
  )
  if (targetRot === undefined) return
  queueRotation(index, startRot, targetRot, ANIMATION_DURATION / 2)
}

async function sendMoveDirection() {
  if (isCurrentlyJumping.value) {
    console.log('Blockiert: Figur springt gerade.')
    return
  }

  if (!gameStore.gameData.moveChoiceAllowed) {
    console.log('---> Bewegung gerade nicht erlaubt')
    return
  }

  if (gameStore.gameData.movingFigure) {
    if (gameStore.gameData.movingFigure !== ownFigures.value[figureControlInd.value]?.id) {
      setzeInfo('Du musst deinen Zug mit der Figur beenden!', 'info')
      return
    }
  }

  if (gameStore.gameData.forbiddenDir) {
    if (
      gameStore.gameData.forbiddenDir.toLowerCase() ===
      ownFigures.value[figureControlInd.value]?.orientation
    ) {
      setzeInfo('Du darfst nicht zurueck gehen!', 'error')
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
    if (!response.ok) {
      const errData = await response.json().catch(() => ({}))
      throw new Error(errData.message || 'Zug ungültig')
    }

    const data = await response.json()

    if (data.success === false) {
      gameStore.gameData.requireInput = true
      gameStore.gameData.moveChoiceAllowed = true

      setzeInfo(data.message || 'Überprüfe deine Richtungseingabe!', 'error')
    }
  } catch (err: any) {
    setzeInfo(err.message, 'error')
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

defineExpose({
  board,
  figures,
})

// Schattensachen
const { staticLightRef, dynamicLightRef } = useShadowLights()
watchEffect(() => {
  console.log("LICHT: Checke beide Lichter")
  if(staticLightRef.value) {
    console.log("LICHT: Setze statisches Licht auf Layer 0")
    staticLightRef.value.layers.set(0)  // statisches licht auf layer 0 setze
    staticLightRef.value.shadow.autoUpdate = false  // auto update ausschalten (fuer performance)
  }
  if(dynamicLightRef.value) {
    dynamicLightRef.value.layers.set(1) // dynamisches licht auf layer 1
  }
})

const lightPos = [3.5,12,8] as [number, number, number]
const lightLookAt = [0,1,-2]
const lightIntensity = 1
const lightDistance = 40
const lightShadowParam = {
  width: 50,
  height: 50,
  near: 0.5,
  far: 40,
  zoom: 0.2,
  blurRadius: 2.5
}


// Expose fetchGameState globally for store to call on figures update event
if (typeof window !== 'undefined') {
  // @ts-ignore
  window.fetchGameState = fetchGameState
}
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


  <TheSky />
  <TresAmbientLight :intensity="0.45" />
  <!-- ungenutzte Params
   
    :distance="lightDistance"
    :shadow-camera-zoom="lightShadowParam.zoom"
  -->
  <TresDirectionalLight
    ref="staticLightRef"
    cast-shadow
    :layers="0"
    :position="lightPos" 
    :look-at="lightLookAt"
    :intensity="lightIntensity" 

    :shadow-mapSize-width="1024"
    :shadow-mapSize-height="1024"
    :shadow-auto-update="false"

    :shadow-camera-width="lightShadowParam.width"
    :shadow-camera-height="lightShadowParam.height"
    :shadow-camera-near="lightShadowParam.near"
    :shadow-camera-far="lightShadowParam.far"
    :shadow-camera-zoom="lightShadowParam.zoom"
  />
  <!-- TresDirectionalLight
    ref="dynamicLightRef"
    cast-shadow
    :layers="1"
    :position="[lightPos[0], lightPos[1], lightPos[2]]" 
    :look-at="lightLookAt"
    :intensity="lightIntensity" 

    :shadow-mapSize-width="512"
    :shadow-mapSize-height="512"

    :shadow-camera-width="lightShadowParam.width"
    :shadow-camera-height="lightShadowParam.height"
    :shadow-camera-near="lightShadowParam.near"
    :shadow-camera-far="lightShadowParam.far"
  / -->

  <template v-if="board">
    <TheTable :scale="(board.cols > board.rows) ? board.cols * CELL_SIZE * 0.17 : board.rows * CELL_SIZE * 0.17"/>
    <Align bottom>
      <TresMesh receive-shadow :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]" :layers="0" >
        <TresBoxGeometry :args="[board.cols * CELL_SIZE * 1.3, board.rows * CELL_SIZE * 1.5, 0.1]" />
        <TresMeshStandardMaterial color="#b6e3a5" :roughness="0.13" :metalness="0.05" />
      </TresMesh>
    </Align>

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
      <template v-else-if="cell.type === 'DUEL'">
        <TheRock />
        <TheDuel />
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
