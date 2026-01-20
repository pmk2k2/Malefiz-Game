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

// Zellentypen
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL' | 'BARRIER' | 'DUEL'

// Zellenkoordinten
interface Field {
  i: number
  j: number
  type?: CellType
}

// Das Spielfeld, grid für aktiven Spielfelder
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
const ANIMATION_DURATION = 300 // laenge der Animation in ms
const { queueMove, queueRotation } = useAnimationQueue()

// Camera controls
const camRef = shallowRef<PerspectiveCamera | null>(null)
const default_cam_pos: [number, number, number] = [0, 15, 18]
const camHeight = 1.2
const figureControlInd = ref(0) //figureControlInd als ref geändert, um änderungen reacktiv für HUD und Store zu machen
const egoPersp = ref(true)
const figureViewDir = ref(-1)

// Abspielen von Animation etc
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

onMounted(async () => {
  isLoading.value = true

  const fetched = await getBoardFromBackend()
  if (fetched) {
    board.value = fetched
    await fetchGameState()
  } else {
    console.error('Board konnte nich geladen werden, Figuren-Rendering wird übersprungen.')
  }
  // Add keyboard listener
  window.addEventListener('keydown', onKeyDown)
  isLoading.value = false
  const gameCode = gameStore.gameData.gameCode
  const playerId = gameStore.gameData.playerId
  if (gameCode != null && playerId != null) {
    // Websockets fuer Gameupdates und persoenliche Requests
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

// Schau, ob Eingriff durch Nutzer erforderlich ist
watch(
  () => gameStore.gameData.requireInput,
  (newVal) => {
    // Irgendwas in der UI anzeigen lassen ig
    console.log('Eingriff durch Nutzer erforderlich? ', newVal)
  },
)

// moveEvents ueberwachen und ausfuehren
watch(
  () => gameStore.ingameMoveEvent,
  async (newEv) => {
    console.log('Neues Move-Event eingetroffen: ', newEv)
    if (!newEv) return
    // Live-Aktualisierung des Boards, wenn eine Barriere neu positioniert wird
    if (newEv.operation === 'BARRIER_PLACED') {
      const fetched = await getBoardFromBackend()
      if (fetched) {
        board.value = { ...fetched }
      }
      return
    }

    // FrontendNachricht mit Bewegung drin behandeln
    // Anzusteuernde Figur finden
    const index = figures.value.findIndex(
      (fig) => fig.id === newEv.figureId && fig.playerId === newEv.id,
    )
    // Logikkoordinaten in Spielkoordinaten umwandeln
    //console.log("Startpos aus Ev: ", newEv?.bewegung.startX, newEv?.bewegung.startZ)
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
    /*
  console.log("Startpos aus Ev neu: ", newEv?.bewegung.startX, newEv?.bewegung.startZ)
  console.log("endpos aus Ev neu: ", newEv?.bewegung.endX, newEv?.bewegung.endZ)
  */
    // Bewegung in Queue anhaengen
    queueMove(index, newEv.bewegung, ANIMATION_DURATION)
    // Orientierung richtig setzen
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
      headers: {
        'Content-Type': 'application/json',
      },
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

async function fetchGameState() {
  const gameCode = gameStore.gameData.gameCode
  if (!gameCode) return

  try {
    const res = await fetch(`${API_BASE_URL}/game/${gameCode}/figures`)
    if (!res.ok) throw new Error('Failed to fetch figures')

    const backendFigures: IPlayerFigure[] = await res.json()
    console.log('FIGURES WE GET FROM THE BACKEND: ' + backendFigures.length)

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
    console.log('Figure geladen und positioniert:', figures.value.length)
  } catch (error) {
    console.error('Fehler beim Laden der Figuren:', error)
  }
}

function _calculateHomeBaseOffset(playerIndex: number, playersCount: number) {
  const b = board.value
  if (!b) {
    console.error(
      'FIGURE STACKING ERROR: Board data (b.cols/b.rows) is missing or NULL. Using (0, 0).',
    )
    // board not loaded yet — return sensible default so callers don't crash
    return { x: 0, z: 0 }
  }

  const boardWidth = b.cols * CELL_SIZE
  // Position des gesamten Häuschens
  let x_offset = 0
  // Häuschen unterhalb des Spielfelds (+ 2 Einheiten Abstand)
  const z_offset = (b.rows / 2) * CELL_SIZE + 2

  if (playersCount === 2) {
    // Spieler 1 kommt Links und Spieler 2 rechts
    x_offset = playerIndex === 0 ? -boardWidth / 3 : boardWidth / 3
  } else if (playersCount === 3) {
    // Spieler 3 kommt hier in der Mitte
    if (playerIndex === 0) x_offset = -boardWidth / 3
    else if (playerIndex === 1) x_offset = boardWidth / 3
    else if (playerIndex === 2) x_offset = 0
  } else if (playersCount === 4) {
    // Alle Häuschen unter nebeneinander
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
  // Abstand zwischen Fiuren in einem Häuschen
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

// war: updateCam()
// so ists besser, da nicht jedes mal updateCam() aufgerufen werden muss
onBeforeRender(({ delta }) => {
  const cam = camRef.value as PerspectiveCamera
  if (!cam) return

  if (egoPersp.value) {
    const fig = ownFigures.value[figureControlInd.value]
    if (!fig) return

    const [x, y, z] = fig.position
    if (y == undefined) return
    cam.position.set(x, camHeight + (y - 0.2), z)

    /*
    let lookDir = 0
    console.log("Blickrichtung Kamera/Figur: ", fig.orientation)
    switch (fig.orientation) {
      case 'north':
        lookDir = 0
        figureViewDir.value = 0
        break
      case 'east':
        lookDir = -0.5
        figureViewDir.value = 1
        break
      case 'south':
        lookDir = 1
        figureViewDir.value = 2
        break
      case 'west':
        lookDir = 0.5
        figureViewDir.value = 3
        break
    }
    */
    //console.log("Figur Rotation fuer Kamera: ", ownFigures.value[figureControlInd].viewDirRot)
    cam.rotation.set(0, Math.PI * fig.viewDirRot, 0)
  } else {
    cam.position.set(default_cam_pos[0], default_cam_pos[1], default_cam_pos[2])
    cam.lookAt(0, 0, 0)
  }
  //}
})

function onKeyDown(event: KeyboardEvent) {
  const key = event.key
  const numOwnFigures = ownFigures.value.length

  if (key === 'ArrowRight' || key === 'ArrowLeft') {
    event.preventDefault()

    if (numOwnFigures === 0) return

    if (key === 'ArrowRight') {
      figureControlInd.value = (figureControlInd.value + 1) % numOwnFigures
    } else if (key === 'ArrowLeft') {
      figureControlInd.value = (figureControlInd.value - 1 + numOwnFigures) % numOwnFigures
    }

    if (egoPersp.value) {
      //updateCam()
    }
  }

  // Figur drehen
  if (key === 'a' || key === 'A') {
    rotateCurrentFigure(-1)

    if (egoPersp.value) {
      //updateCam()
    }
  }
  if (key === 'd' || key === 'D') {
    rotateCurrentFigure(1)

    if (egoPersp.value) {
      //updateCam()
    }
  }

  if (key === 'w' || key === 'W') {
    sendMoveDirection()
  }

  if (key === 'e' || key === 'E') {
    egoPersp.value = !egoPersp.value
    if (egoPersp.value && numOwnFigures > 0) {
      figureControlInd.value = 0
    }
    //updateCam()
  }
}

// 2D-Array aller Zellen des Spielfelds mit Typ
const allCells = computed<Field[]>(() => {
  if (!board.value) return []
  return board.value.grid.flat()
})

// Zellen auf Map-Koordinaten (x, y, z) mappen
function cellToField(cell: Field): [number, number, number] {
  if (!board.value) return [0, 0, 0]

  const x = (cell.i - board.value.cols / 2 + 0.5) * CELL_SIZE
  const z = -((cell.j - board.value.rows / 2 + 0.5) * CELL_SIZE)

  return [x, 0.05, z]
}

/*
function onRoll(id: string) {
  console.log('Button pressed:', id)
  rollDice()
}
*/

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
  //const startRot = ownFigures.value[figureControllInd].viewDirRot
  const startRot = fig.viewDirRot
  let targetRot
  switch (figureViewDir.value) {
    case 0:
      //ownFigures.value[figureControlInd].orientation = 'north'
      fig.orientation = 'north'
      targetRot = 0
      break
    case 1:
      //ownFigures.value[figureControlInd].orientation = 'east'
      fig.orientation = 'east'
      targetRot = -0.5
      break
    case 2:
      //ownFigures.value[figureControlInd].orientation = 'south'
      fig.orientation = 'south'
      targetRot = 1
      break
    case 3:
      //ownFigures.value[figureControlInd].orientation = 'west'
      fig.orientation = 'west'
      targetRot = 0.5
      break
  }
  console.log('Rotation von Figur: ', startRot, targetRot)
  const index = figures.value.findIndex(
    (figInd) => figInd.id === fig.id && figInd.playerId === fig.playerId,
  )
  if (targetRot === undefined) return
  queueRotation(index, startRot, targetRot, ANIMATION_DURATION / 2)
}

// Methode, damit die Bewegungsrichtung an den Server geschickt wird
async function sendMoveDirection() {
  // Wenn move nicht erlaubt ist -> Abbruch
  if (!gameStore.gameData.moveChoiceAllowed) {
    console.log('---> Bewegung gerade nicht erlaubt')
    return
  }

  // Wenn der versuchte Move nicht der zu bewegenden Figur entspricht -> Abbruch

  console.log(
    gameStore.gameData.moveDone,
    gameStore.gameData.movingFigure,
    ownFigures.value[figureControlInd.value]?.id,
  )
  if (gameStore.gameData.movingFigure) {
    if (gameStore.gameData.movingFigure !== ownFigures.value[figureControlInd.value]?.id) {
      alert('Du musst deinen Zug mit der Figur beenden!')
      return
    }
  }

  // Wenn versucht wird in "verbotene" Richtung zu gehen

  console.log(
    `--- ${gameStore.gameData.forbiddenDir} ${ownFigures.value[figureControlInd.value]?.orientation}`,
  )
  if (gameStore.gameData.forbiddenDir) {
    if (
      gameStore.gameData.forbiddenDir.toLowerCase() ===
      ownFigures.value[figureControlInd.value]?.orientation
    ) {
      alert('Du darfst nicht zurueck gehen!')

      return
    }
  }

  // weitere Moves zunaechst blockieren
  gameStore.gameData.requireInput = false
  gameStore.gameData.moveChoiceAllowed = false

  /*
  gameStore.gameData.moveDone = true
  gameStore.gameData.movingFigure = null
  */

  // Orientierung etc von aktueller Figur kriegen

  if (currentPlayerId === null) return
  const figureIdReq = ownFigures.value[figureControlInd.value]?.id
  const directionReq = ownFigures.value[figureControlInd.value]?.orientation

  if (!figureIdReq || !directionReq) return
  const moveReq: IFigureMoveRequest = {
    playerId: currentPlayerId,
    figureId: figureIdReq,
    direction: directionReq,
  }

  // Datenobjekt schicken
  try {
    const response = await fetch(`/api/move/${gameCode}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(moveReq),
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data = await response.json()
    console.log('Move-Request sent successfully:', data)

    // wenn Antwort negativ -> nochmal input geben
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

defineExpose({
  board,
  figures
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

const lightPos = [3.5,12,8]
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

</script>

<template>
  <!-- TresCanvas clear-color="#87CEEB" class="w-full h-full" -->
  <TresPerspectiveCamera ref="camRef" :position="default_cam_pos" :look-at="[0, 0, 0]" />
  <OrbitControls v-if="!egoPersp" />

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
    :position="lightPos" 
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
      <TresMesh receive-shadow :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]" :layers="[0,1]" >
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

    <!-- Home bases for players -->
    <template v-for="(player, index) in allPlayers" :key="`home-${player.id}`">
      <TresMesh
        :position="[calculateHomeCenter(player.id).x, -0.01, calculateHomeCenter(player.id).z]"
        :rotation="[-Math.PI / 2, 0, 0]"
      >
        <TresPlaneGeometry :args="[6, 3]" />
        <TresMeshStandardMaterial :color="player.color || '#cccccc'" />
      </TresMesh>
    </template>

    <!-- Player figures -->
    <ThePlayerFigure
      v-for="fig in figures"
      :key="fig.id"
      :position="fig.position"
      :color="fig.color"
      :orientation="fig.orientation"
    />
  </template>
  <!-- /TresCanvas -->
</template>
