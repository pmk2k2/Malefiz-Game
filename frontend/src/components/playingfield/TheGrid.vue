<script setup lang="ts">
import { TresCanvas, type TresObject } from '@tresjs/core'
import { OrbitControls } from '@tresjs/cientos'
import { computed, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue'
import TheRock from './models/TheRock.vue'
import TheTree from './models/TheTree.vue'
import TheCrown from './models/TheCrown.vue'
import TheGrass from './models/TheGrass.vue'
import ThePlayerFigure from '@/components/playingfield/ThePlayerFigure.vue'
import RollButton from '@/components/RollButton.vue'
import Dice3D, { rollDice } from '@/components/Dice3D.vue'
import { useGameStore } from '@/stores/gamestore'
import type { IPlayerFigure } from '@/stores/IPlayerFigure'
import type { IFigureMoveRequest } from '@/services/IFigureMoveRequest'

// Zellentypen
type CellType = 'START' | 'PATH' | 'BLOCKED' | 'GOAL'

// Zellenkoordinten
interface Field {
  i: number
  j: number
  type: CellType
}

// Das Spielfeld, grid für aktiven Spielfelder
interface Board {
  cols: number
  rows: number
  grid: Field[][]
}

const gameStore = useGameStore()
const CELL_SIZE = 2
const board = ref<Board | null>(null)
const isLoading = ref(true)
const gameCode = gameStore.gameData.gameCode

// Player figures
const figures = ref(gameStore.figures) //ref<IPlayerFigure[]>([])
const currentPlayerId = gameStore.gameData.playerId
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// Camera controls
const camRef = shallowRef<TresObject | null>(null)
const default_cam_pos: [number, number, number] = [0, 15, 18]
const camHeight = 1.2
let figureControlInd = 0
const egoPersp = ref(false)
const figureViewDir = ref(-1)

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
  if(gameCode != null && playerId != null) {
    // Websockets fuer Gameupdates und persoenliche Requests
    gameStore.startIngameLiveUpdate(gameCode, playerId)
    gameStore.startLobbyLiveUpdate(gameCode)
  }

})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown)
})

// Schau, ob Eingriff durch Nutzer erforderlich ist
watch(() => gameStore.gameData.requireInput, (newVal) => {
  console.log("Eingriff durch Nutzer erforderlich? {}", newVal)
})

// moveEvents ueberwachen und ausfuehren
watch(() => gameStore.ingameMoveEvent, (newEv) => {
  console.log("Neues Moveevent im Grid verarbeiten")
  console.log(newEv)
  // FrontendNachricht mit Bewegung drin behandeln
  // Anzusteuernde Figur finden
  const index = figures.value.findIndex((fig) => fig.id === newEv.figureId && fig.playerId === newEv.id)

  // Wenn Steps = 0 -> keine Schritte laufen
  const bew = newEv.bewegung
  if(bew.steps === 0) {
    moveFigure(index, bew.endX, bew.endZ)
  } else {
    moveFigureSteps(index, bew.steps, bew.dir.toLowerCase)
  }
})

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
        position: [x, y, z],
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
  let z_offset = (b.rows / 2) * CELL_SIZE + 2

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

// evtll lieber in ein watch(...) packen?
function updateCam() {
  const cam = camRef.value as any
  if (!cam) return

  if (egoPersp.value) {
    const fig = ownFigures.value[figureControlInd]
    if (!fig) return

    const [x, , z] = fig.position
    cam.position.set(x, camHeight, z)

    let lookDir = 0
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

    cam.rotation.set(0, Math.PI * lookDir, 0)
  } else {
    cam.position.set(default_cam_pos[0], default_cam_pos[1], default_cam_pos[2])
    cam.lookAt(0, 0, 0)
  }
}

function onKeyDown(event: KeyboardEvent) {
  const key = event.key
  const numOwnFigures = ownFigures.value.length

  if (key === 'ArrowRight' || key === 'ArrowLeft') {
    event.preventDefault()

    if (numOwnFigures === 0) return

    if (key === 'ArrowRight') {
      figureControlInd = (figureControlInd + 1) % numOwnFigures
    } else if (key === 'ArrowLeft') {
      figureControlInd = (figureControlInd - 1 + numOwnFigures) % numOwnFigures
    }



    if (egoPersp.value) {
      updateCam()
    }
  }

  // Figur drehen
  if (key === 'a' || key === 'A') {
    rotateCurrentFigure(-1)

    if (egoPersp.value) {
      updateCam()
    }
  }
  if (key === 'd' || key === 'D') {
    rotateCurrentFigure(1)

    if (egoPersp.value) {
      updateCam()
    }
  }

  if (key === 'w' || key === 'W') {
    sendMoveDirection()
  }


  if (key === 'e' || key === 'E') {
    egoPersp.value = !egoPersp.value
    if (egoPersp.value && numOwnFigures > 0) {
      figureControlInd = 0
    }
    updateCam()
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


// Asynchrone Funktion, die eine Figur nach Index um x Schritte nach vorne bewegt
// Schritt fuer Schritt Animation
async function moveFigureSteps(index = 0, wuerfelSchritte = 1, direction) {
  // Wenn Schrittanzahl 0 -> ignorieren
  if(wuerfelSchritte === 0) return;

  // Fuer Anzahl der angegeben Schritte
  for(let i = 0; i < wuerfelSchritte; i++){
    // momentane und Zielposition halten
    const currentPos = figures.value[index].position

    // ein Feld = 2 Einheiten
    // je nach direction anpassen
    let moveNS = 0
    let moveWE = 0
    switch(direction) {
      case 'north':
        moveNS = 2
        break;
      case 'south':
        moveNS = -2
        break;
      case 'west':
        moveWE = 2
        break;
      case 'east':
        moveWE = -2
        break;
    }
    const targetPos = [currentPos[0] + moveWE, currentPos[1], currentPos[2] + moveNS]

    // Aufruf der Animationsfunktion
    await animateMovement(currentPos, targetPos, index)
  }
}

// von einer Pos direkt zur anderen Pos springen (ohne viel huepfen)
async function moveFigure(index = 0, newX = 0, newZ = 0) {
  const currentPos = figures.value[index].position
  const targetField = cellToField({i: newX, j: newZ})
  console.log(targetField)
  const targetPos = [targetField[0], 0.2, targetField[2]]

  await animateMovement(currentPos, targetPos, index)
}

// Funktion zur Animation der Bewegung der Spielfiguren
function animateMovement(currentPos, targetPos, index) {
  let duration = 450  // Dauer der Animation in ms
  let startPos = currentPos
  const startTime = performance.now();  // Startzeit zum Berechnen der Differenz benoetigt

  // Promise, damit ein jeweils ein Animationszyklus zunaechst beendet wird
  return new Promise<void>(resolve => {
    function step(now){

      // Animation laeuft im Grunde von 0-100% durch
      // Dabei ist "(now-startTime) / duration" der Anteil, wie viel von der Animation abgeschlossen wurde
      // Da es zeitbasiert ist, sollte es kein Problem mit verschiedenen Framerates geben
      // Wenn mehr Zeit vergangen ist, als ein Zyklus eigentlich geht, so wird time auf 1
      // und somit die Animation als fertig gesehen
      const time = Math.min((now - startTime) / duration, 1);

      let newPos = [0,0,0]
      newPos[0] = startPos[0] + (targetPos[0] - startPos[0]) * time;  // lineare Bewegung auf x-Koordinate
      newPos[1] = startPos[1] + Math.sin(Math.PI * time);             // "Huepfen" bei Bewegung auf y-Achse nach halber Sinuskurve
      newPos[2] = startPos[2] + (targetPos[2] - startPos[2]) * time;  // lineare Bewegung auf z-Koordinate

      figures.value[index].position = newPos  // Figur auf Position bewegen
      if(time < 1){
        // bis Animation fertig abgespielt ist, Animation aufrufen
        requestAnimationFrame(step)
      } else {
        // Wenn time > 1 (also Animation 100% durch ist) -> Promise resolven
        resolve()
      }

      // Kamera stetig updaten
      updateCam()
    }

    requestAnimationFrame(step)
  })
}

function getCurrentFigureRot() {
  switch(ownFigures.value[currentPlayerId].orientation) {
    case 'north':
      figureViewDir.value = 0
      break;
    case 'east':
      figureViewDir.value = 1
      break;
    case 'south':
      figureViewDir.value = 2
      break;
    case 'west':
      figureViewDir.value = 3
      break;
  }
}

function rotateCurrentFigure(rot: number) {
  if(rot <= 0) {
    figureViewDir.value = (figureViewDir.value - 1 + 4) % 4
  } else {
    figureViewDir.value = (figureViewDir.value + 1) % 4
  }
  switch(figureViewDir.value) {
    case 0:
      ownFigures.value[figureControlInd].orientation = 'north'
      break;
    case 1:
      ownFigures.value[figureControlInd].orientation = 'east'
      break;
    case 2:
      ownFigures.value[figureControlInd].orientation = 'south'
      break;
    case 3:
      ownFigures.value[figureControlInd].orientation = 'west'
      break;
  }
}

// Methode, damit die Bewegungsrichtung an den Server geschickt wird
async function sendMoveDirection() {
  // Wenn move nicht erlaubt ist -> Abbruch
  if(!gameStore.gameData.moveChoiceAllowed) return;

  // Wenn der versuchte Move nicht der zu bewegenden Figur entspricht -> Abbruch
  if(!gameStore.gameData.moveDone) {
    if(gameStore.gameData.movingFigure !== ownFigures.value[figureControlInd]?.id)
      return
  }
  // weitere Moves blockieren
  gameStore.gameData.requireInput = false
  gameStore.gameData.moveChoiceAllowed = false

  // wenn Antwort positiv -> wuerfel etc freigeben
  gameStore.gameData.moveDone = true
  gameStore.gameData.movingFigure = null

  // Orientierung etc von aktueller Figur kriegen
  const moveReq: IFigureMoveRequest = {
    playerId: currentPlayerId,
    figureId:  ownFigures.value[figureControlInd].id,
    direction: ownFigures.value[figureControlInd].orientation
  }

  // Datenobjekt schicken
  try {
    const response = await fetch(`/api/move/${gameCode}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(moveReq),
    })

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)

    const data = await response.json()
    console.log('Move-Request sent successfully:', data)

    // wenn Antwort positiv -> wuerfel etc freigeben

  } catch (err) {
    console.error('Error sending move request:', err)
  }
}

</script>

<template>
  <TresCanvas clear-color="#87CEEB" class="w-full h-full">
    <TresPerspectiveCamera ref="camRef" :position="default_cam_pos" :look-at="[0, 0, 0]" />
    <OrbitControls v-if="!egoPersp" />

    <TresDirectionalLight :position="[20, 40, 10]" :intensity="1.5" />

    <template v-if="board">
      <TresMesh :rotation="[-Math.PI / 2, 0, 0]" :position="[0, 0, 0]">
        <TresPlaneGeometry :args="[board.cols * CELL_SIZE * 5, board.rows * CELL_SIZE * 5]" />
        <TresMeshStandardMaterial color="#b6e3a5" :roughness="1" :metalness="0" />
      </TresMesh>

      <TresMesh
        v-for="cell in allCells"
        :key="`cell-${cell.i}-${cell.j}`"
        :position="cellToField(cell)"
        :rotation="[-Math.PI / 2, 0, 0]"
      >
        <template v-if="cell.type === 'PATH' || cell.type === 'START'">
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
  </TresCanvas>
</template>
