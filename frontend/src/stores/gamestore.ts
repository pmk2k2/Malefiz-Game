import { defineStore } from 'pinia'
import { reactive, ref, watch } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import type { IFrontendNachrichtEvent } from '@/services/IFrontendNachrichtEvent'
import { useInfo } from '@/composable/useInfo'
import type { ISpielerDTD } from './ISpielerDTD'
import { mapBackendPlayersToDTD } from '@/stores/mapper'
import { useRouter } from 'vue-router'
import type { IIngameRequestEvent } from '@/services/IIngameRequestEvent'
import type { IBewegung } from '@/services/IBewegung'
import type { IPlayerFigure } from './IPlayerFigure'

export const useGameStore = defineStore('gamestore', () => {
  console.log('Erstelle Gamestore')
  const { setzeInfo } = useInfo()
  const router = useRouter()
  const countdown = ref<number | null>(null)
  const gameState = ref<string>('WAITING')
  let countdownInterval: any = null
  // ID der aktuell ausgewählten Figur (für HUD)
  const selectedFigureId = ref<string | null>(null)

  const gameData = reactive<{
    ok: boolean
    players: ISpielerDTD[]
    gameCode: string | null
    playerId: string | null
    playerName: string | null
    isHost: boolean | null
    counterWert: number | null
    isBereit: boolean | null
    gameOver: boolean | null
    winnerId: string | null
    moveDone: boolean | null
    moveChoiceAllowed: boolean
    movingFigure: string | null
    requireInput: boolean
    forbiddenDir: string | null
    stepsTaken: number
    remainingSteps: number
    totalSteps: number
    energy: number
    duelActive: boolean
    currentMinigame: string | null
    mashScore: number
    duelTimeLeft: number
    duelAnswered: boolean
    duelQuestion: null | {
      text: string
      answers: string[]
    }

  }>({
    ok: false,
    players: [],
    gameCode: null,
    playerId: null,
    playerName: null,
    isHost: null,
    counterWert: null,
    isBereit: false,
    gameOver: null,
    winnerId: null,
    moveDone: true,
    moveChoiceAllowed: false,
    movingFigure: null,
    requireInput: false,
    forbiddenDir: null,
    stepsTaken: 0,
    remainingSteps: 0,
    totalSteps: 0,
    energy: 0,
    duelActive: false,
    currentMinigame: null,
    mashScore: 0,
    duelTimeLeft: 10,
    duelAnswered: false,
    duelQuestion: null,
  })
  const figures = ref<IPlayerFigure[]>([])
  const ingameMoveEvent = ref<IFrontendNachrichtEvent>()

  loadFromLocalStorage()
  watch(
    () => ({
      players: gameData.players,
      gameCode: gameData.gameCode,
      playerId: gameData.playerId,
      playerName: gameData.playerName,
      isHost: gameData.isHost,
      isBereit: gameData.isBereit,
      winnerId: gameData.winnerId,
      gameOver: gameData.gameCode,
    }),
    saveToLocalStorage,
  )

  let stompClient: Client | null = null
  let persStompClient: Client | null = null

  const apiBase = (import.meta.env.VITE_API_BASE_URL as string) || '/api'
  const stompEnv = (import.meta.env.VITE_STOMP_URL as string) || ''

  function computeSockJsUrl(target: string) {
    if (stompEnv && stompEnv.length) {
      return stompEnv
    }
    return `${location.protocol}//${location.host}/${target}`
  }

  function startLobbyLiveUpdate(gameCode: string) {
    //Websocket anknüpfung zum backend (FrontendNachrichtenService API) Webbasierte Anwendungen Praktikum-Blatt10
    if (stompClient && stompClient.active) {
      console.log('STOMP-Client existiert bereits oder ist aktiv.')
      return
    }

    const sockJsUrl = computeSockJsUrl('stompbroker')

    stompClient = new Client({
      webSocketFactory: () => new SockJS(sockJsUrl),
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str),
    })

    stompClient.onConnect = () => {
      console.log('STOMP verbunden, abonniere /topic/gameSession/' + gameCode)

      stompClient!.subscribe(`/topic/gameSession/${gameCode}`, (message) => {
        try {
          const event: IFrontendNachrichtEvent = JSON.parse(message.body)
          console.log('Empfangenes Event:', JSON.stringify(event))

          //  Ausschließlich Lobby updates (Joined, left, Countdown usw...)
          if (event.typ === 'INGAME') {
            if (event.operation === 'MOVE') {
              console.log('DING DONG Figur bewegen')
              console.log(event)
              ingameMoveEvent.value = event
            } else if (event.operation === 'GAME_OVER') {
              gameData.gameOver = true
              gameData.winnerId = event.id
              disconnect()
            } else if (event.operation === 'DUEL_NEW_QUESTION' && gameData.duelActive) {
              const q = event.quizQuestion
              if (!q) return

              gameData.duelAnswered = false
              gameData.duelTimeLeft = 10
              gameData.duelQuestion = {
                text: q.text,
                answers: q.answers,
              }
            }

            else if (event.operation === 'DUEL_RESULT' && gameData.duelActive) {
              gameData.duelActive = false
              gameData.duelQuestion = null
              gameData.duelAnswered = false
              gameData.duelTimeLeft = 0
              gameData.mashScore = 0
            }


            //aktualisiert die energie des lokalen Spielers falls die Event und Player ID übereinstimmt
            else if (event.operation === 'ENERGY_UPDATED') {
              console.log('Energie Update empfangen:', event)
              if (event.id === gameData.playerId) {
                const newVal = (event as any).newEnergyValue
                gameData.energy = newVal ?? 0
                console.log(`Neue Sprungenergie gespeichert: ${gameData.energy}`)
              }
            } else if (event.operation === 'BARRIER_WAIT') {
              if (event.id === gameData.playerId) {
                gameState.value = 'BARRIER_PLACEMENT'
              } else {
                console.log('Aus GameStore: Ein anderer Spieler setzt gerade eine Barriere.')
              }
            } else if (event.operation === 'BARRIER_PLACED') {
              gameState.value = 'RUNNING'
              ingameMoveEvent.value = event
            }
            if (event.operation === 'STEP_UPDATE' && event.step && event.id === gameData.playerId) {
                gameData.stepsTaken = event.step.totalSteps
                gameData.remainingSteps = event.step.remainingSteps
              }
            // DUEL / MASHUPDATE  
            else if (event.operation == 'DUEL_MASH_UPDATE') {
              gameData.mashScore = event.countdownDurationSeconds || 0
              console.log("Server Mashscore: ", gameData.mashScore)
            }
              // DUEL / MINIGAME START
            else if (event.operation === 'DUEL_PREPARE') {
              console.log('DUEL_PREPARE Event empfangen!')
              gameData.mashScore = 0
            }
            // DUEL / MINIGAME START
            else if (event.operation === 'DUEL') {
              console.log('DUEL Event empfangen!')
              gameData.duelActive = true
              gameData.duelQuestion = null
              gameData.duelAnswered = false
              gameData.duelTimeLeft = 10
              gameData.mashScore = 0
              
              fetch(`/api/duel/start?gameCode=${gameData.gameCode}`)

              // Nur fuer beteiligte Spieler
              if (
                gameData.playerId &&
                (event.id === gameData.playerId || event.opponentId === gameData.playerId)
              ) {
                gameData.duelActive = true
              }
            }
            // <--- NEU: Reagieren auf Minigame Auswahl
            else if (event.operation === 'MINIGAME_SELECTED') {
              const { minigameType, id, opponentId } = event
              
              console.log(`!!! MINIGAME SELECTED: ${minigameType} !!!`)
              
              //HIER DIE DUELL Minigame anzeige einbinden bei 'gameData.currentMinigame'
              gameData.currentMinigame = minigameType || null
              
              if (gameData.playerId === id || gameData.playerId === opponentId) {
                console.log(`Spieler ${gameData.playerName} startet nun Minispiel UI für ${event.minigameType}`)
              }
            }
          }
          else if (event.typ === 'LOBBY') {
            
            updatePlayerList(gameCode)

            //  Countdown starten

            if (event.operation === 'JOINED' && event.playerName) {
              setzeInfo(`${event.playerName} ist der Lobby beigetreten.`) //InfoBox setzen wenn Player
            }

            if (event.operation === 'LEFT' && event.playerName) {
              setzeInfo(`${event.playerName} hat die Lobby verlassen.`) //InfoBox setzen wenn Player die Lobby verlässt
            }
            if (event.operation === 'KICKED') {
              stopCountdown()
            }

            if (event.operation === 'COUNTDOWN_STARTED') {
              gameState.value = 'COUNTDOWN'

              const duration = event.countdownDurationSeconds || 10 // 10 als Fallback
              //countdown.value = duration
              const startTimeMs = new Date(event.countdownStartedAt!).getTime()

              if (countdownInterval) clearInterval(countdownInterval)

              countdownInterval = setInterval(() => {
                const elapsed = Math.floor((Date.now() - startTimeMs) / 1000)
                countdown.value = duration - elapsed

                if (countdown.value <= 0) {
                  clearInterval(countdownInterval)
                  countdownInterval = null
                  triggerGameStart(gameCode)
                }
              }, 500)
            }
            if (event.operation === 'COUNTDOWN_ABORTED') {
              stopCountdown()
            }

            // Admin oder Server startet Spiel → kein Countdown, direkt rein
            if (event.operation === 'GAME_RUNNING') {
              gameState.value = 'RUNNING'
              disconnect()
              router.push(`/game`)
            }

            //  Spielerlimit überschritten
            if (event.operation === 'PLAYER_LIMIT_ERROR') {
              alert('Lobby ist voll! Max 4 Spieler erlaubt.')
            }
          }
        } catch (err) {
          console.error('WS Fehler:', err)
        }
      })
    }

    stompClient.activate()
  }

  function startIngameLiveUpdate(gameCode: string, playerId: string) {
    //Websocket anknüpfung zum backend (FrontendNachrichtenService API) Webbasierte Anwendungen Praktikum-Blatt10
    if (persStompClient && persStompClient.active) {
      console.log('STOMP-Client existiert bereits oder ist aktiv.')
      return
    }

    const sockJsUrl = computeSockJsUrl('persstomp')

    persStompClient = new Client({
      webSocketFactory: () => new SockJS(sockJsUrl),
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str),
    })

    persStompClient.onConnect = () => {
      console.log(`STOMP verbunden, abonniere /queue/gameSession/${gameCode}/${playerId}`)

      persStompClient!.subscribe(`/queue/gameSession/${gameCode}/${playerId}`, (message) => {
        try {
          // Nur IIngameRequests behandeln
          const event: IIngameRequestEvent = JSON.parse(message.body)
          console.log('Empfangenes Event:', JSON.stringify(event))

          if (event.type === 'DIRECTION') {
            console.log('DIRECTION Event empfangen')
            gameData.requireInput = true
            gameData.moveChoiceAllowed = true
            if (event.figureId) {
              console.log(`Figur ${event.figureId} soll was machen`)
              console.log(`Aber nicht in Richtung ${event.forbiddenDir}`)
              gameData.movingFigure = event.figureId
              gameData.forbiddenDir = event.forbiddenDir
            } else {
              console.log('Irgendeine Figur soll was machen')
              gameData.movingFigure = null
              gameData.forbiddenDir = null
            }
          }
          if (event.type === 'DICE_ROLL') {
            gameData.requireInput = true
            gameData.moveChoiceAllowed = true
            // Wuerfel freigeben
            gameData.stepsTaken = 0
            gameData.remainingSteps = event.result
            gameData.totalSteps = event.result
          }
        } catch (err) {
          console.error('WS Fehler:', err)
        }
      })
    }

    persStompClient.activate()
  }

  async function triggerGameStart(gameCode: string) {
    const playerId = gameData.playerId

    if (!gameCode || !playerId) {
      console.warn('Fehlende Daten für den Spielstart.')
      return
    }

    try {
      const res = await fetch(`${apiBase}/game/start`, {
        method: 'POST',
        body: JSON.stringify({
          code: gameCode,
          playerId: playerId,
        }),
        headers: { 'Content-Type': 'application/json' },
      })

      if (!res.ok) {
        throw new Error(`Fehler beim Starten des Spiels: ${res.statusText}`)
      }

      router.push('/game')
    } catch (err) {
      console.error('Fehler beim Starten des Spiels durch Counter:', err)
    }
  }

  async function updatePlayerList(gameCode: string) {
    try {
      //setzeInfo("Lobby-Daten wurden aktualisiert");
      console.log('updatePlayerList aufgerufen für Code:', gameCode)
      const response = await fetch(`${apiBase}/game/get?code=${gameCode}`, {
        headers: {
          // 'Authorization': `Bearer ${loginStore.jwt}`, // Falls JWT benötigt
        },
        redirect: 'error',
      })
      console.log('[updatePlayerList] Response-Status:', response.status)
      if (!response.ok) throw new Error(response.statusText)

      const jsonData = await response.json()

      gameData.players = mapBackendPlayersToDTD(jsonData.players || [])
      gameData.ok = true

      if (gameData.playerId && !gameData.players.some((p) => p.id === gameData.playerId)) {
        console.warn('Du wurdest aus der Lobby entfernt')
        gameData.gameCode = null
        gameData.players = []
        gameData.isHost = false
        gameData.ok = false
        router.push('/main')
        return
      }

      if (gameData.playerId) {
        const me = (gameData.players as any[]).find((p) => p.id === gameData.playerId)
        gameData.isHost = !!(me && me.isHost)
      }

      console.log('[updatePlayerList] Daten aktualisiert:', jsonData)
    } catch (error) {
      console.error('[updatePlayerList] Fehler:', error)
      if (error instanceof Error) {
        //setzeInfo(error.message);
      } else {
        //setzeInfo("Unbekannter Fehler");
      }
      gameData.players = []
      gameData.ok = false
    }
  }

  function stopCountdown() {
    if (countdownInterval) {
      clearInterval(countdownInterval)
      countdownInterval = null
    }
    countdown.value = null
    gameState.value = 'WAITING'
  }

  function disconnect() {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    stopCountdown()
  }

  function reset() {
    console.log('Reset gameData')
    gameData.playerId = null
    gameData.playerName = null
    gameData.gameCode = null
    gameData.isHost = null
    gameData.players = []
    gameData.ok = false
    gameData.isBereit = false
    gameData.gameOver = null
    gameData.stepsTaken=0
    gameData.remainingSteps=0
    gameData.totalSteps=0
    gameData.duelActive = false
    gameData.currentMinigame = null
    stopCountdown()

    localStorage.removeItem('gameData')
  }

  function resetGameCode() {
    gameData.gameCode = null
    gameData.isHost = null
    gameData.playerId = null
    gameData.isBereit = false
    gameData.gameOver = null
    gameData.stepsTaken=0
    gameData.remainingSteps=0
    gameData.totalSteps=0

    stopCountdown()
    console.log(JSON.stringify(gameData))
  }

  function saveToLocalStorage() {
    localStorage.setItem('gameData', JSON.stringify(gameData))
  }

  function loadFromLocalStorage() {
    const raw = localStorage.getItem('gameData')
    if (!raw) return

    try {
      const data = JSON.parse(raw)
      Object.assign(gameData, data)
    } catch (e) {
      console.error('load fail', e)
    }
  }

  return {
    gameData,
    figures,
    ingameMoveEvent,
    countdown,
    gameState,
    startLobbyLiveUpdate,
    startIngameLiveUpdate,
    updatePlayerList,
    disconnect,
    reset,
    resetGameCode,
    triggerGameStart,
    stopCountdown,
    selectedFigureId,
  }
})