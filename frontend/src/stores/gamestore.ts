import { defineStore } from "pinia";
import { reactive, ref, watch } from "vue";
import SockJS from "sockjs-client";
import { Client } from '@stomp/stompjs';
import type { IFrontendNachrichtEvent } from '@/services/IFrontendNachrichtEvent';
import { useInfo } from "@/composable/useInfo";
import type { ISpielerDTD } from "./ISpielerDTD";
import type { LobbyID } from './LobbyID';
import { mapBackendPlayersToDTD } from '@/stores/mapper';
import { useRouter } from "vue-router";


export const useGameStore = defineStore("gamestore", () => {
  
  const router = useRouter();
  const countdown = ref<number | null>(null);
  const gameState = ref<string>("WAITING");
  let countdownInterval: any = null;

  const gameData = reactive<{
    ok: boolean;
    players: ISpielerDTD[];
    gameCode: string | null;
    playerId: string | null;
    playerName: string | null;
    isHost: boolean | null;
    counterWert: number | null;
  }>({
    ok: false,
    players: [],
    gameCode: null,
    playerId: null,
    playerName: null,
    isHost: null,
    counterWert: null
  });

  loadFromLocalStorage();
  watch(
    () => ({
      players: gameData.players,
      gameCode: gameData.gameCode,
      playerId: gameData.playerId,
      playerName: gameData.playerName,
      isHost: gameData.isHost
    }),
    saveToLocalStorage
  );

  let stompClient: Client | null = null;

  function startLobbyLiveUpdate(gameCode: string) {
    if (stompClient && stompClient.active) {
      console.log("STOMP-Client existiert bereits oder ist aktiv.");
      return;
    }

    stompClient = new Client({
      webSocketFactory: () => new SockJS('/stompbroker'),
      reconnectDelay: 5000,
      debug: str => console.log('[STOMP]', str),
    });

    stompClient.onConnect = () => {
      console.log("STOMP verbunden, abonniere /topic/gameSession/" + gameCode);

      stompClient!.subscribe(`/topic/gameSession/${gameCode}`, message => {
        try {
          const event: IFrontendNachrichtEvent = JSON.parse(message.body);
          console.log("Empfangenes Event:", JSON.stringify(event));

          //  Lobby update wenn Spieler joint/left/ready ändert
          if (event.operation === "JOINED" || event.operation === "LEFT" || event.operation === "READY_UPDATED") {
            updatePlayerList(gameCode);
          }

          //  Countdown starten
          if (event.operation === "COUNTDOWN_STARTED") {
            gameState.value = "COUNTDOWN";

            const duration = event.countdownDurationSeconds || 10; // 30 als Fallback
            countdown.value = duration;
            //countdown.value = 10;

            if (countdownInterval) clearInterval(countdownInterval);

            const start = new Date(event.countdownStartedAt!).getTime();

            countdownInterval = setInterval(() => {
              const elapsed = Math.floor((Date.now() - start) / 1000);
              countdown.value = 10 - elapsed;

              if (countdown.value <= 0) {
                clearInterval(countdownInterval);
                countdownInterval = null;
                triggerGameStart(gameCode);
              }
            }, 500);
          }

          // Admin oder Server startet Spiel → kein Countdown, direkt rein
          if (event.operation === "GAME_RUNNING") {
            gameState.value = "RUNNING";
            disconnect();
            router.push(`/field`);
          }


          //  Spielerlimit überschritten
          if (event.operation === "PLAYER_LIMIT_ERROR") {
            alert("Lobby ist voll! Max 4 Spieler erlaubt.");
          }

        } catch (err) {
          console.error("WS Fehler:", err);
        }
      });
    };

    stompClient.activate();
  }
  async function triggerGameStart(gameCode: string) {
    const playerId = gameData.playerId;
    // use the router instance from the store scope
    if (!gameCode || !playerId) {
      console.warn("Fehlende Daten für den Spielstart.");
      return;
    }

    try {
      const res = await fetch("/api/game/start", {
        method: "POST",
        body: JSON.stringify({
          code: gameCode,
          playerId: playerId
        }),
        headers: { "Content-Type": "application/json" }
      });

      if (!res.ok) {
        throw new Error(`Fehler beim Starten des Spiels: ${res.statusText}`);
      }


      router.push('/field');



    } catch (err) {
      console.error("Fehler beim Starten des Spiels durch Counter:", err);
    }
  }


  async function updatePlayerList(gameCode: string) {
    try {
      //setzeInfo("Lobby-Daten wurden aktualisiert");
      console.log("updatePlayerList aufgerufen für Code:", gameCode);
      const response = await fetch(`/api/game/get?code=${gameCode}`, {
        headers: {
          // 'Authorization': `Bearer ${loginStore.jwt}`, // Falls JWT benötigt
        },
        redirect: 'error',

      });
      console.log("[updatePlayerList] Response-Status:", response.status);
      if (!response.ok) throw new Error(response.statusText);

      const jsonData = await response.json();

      gameData.players = mapBackendPlayersToDTD(jsonData.players || []);
      gameData.ok = true;

      if (gameData.playerId) {
        const me = (gameData.players as any[]).find(p => p.id === gameData.playerId);
        gameData.isHost = !!(me && me.isHost);
      }

      console.log("[updatePlayerList] Daten aktualisiert:", jsonData);
    } catch (error) {
      console.error("[updatePlayerList] Fehler:", error);
      if (error instanceof Error) {
        //setzeInfo(error.message);
      } else {
        //setzeInfo("Unbekannter Fehler");
      }
      gameData.players = [];
      gameData.ok = false;
    }
  }



  function disconnect() {
    if (stompClient) {
      stompClient.deactivate();
      stompClient = null;
    }
  }

  function reset() {
    console.log("Reset gameData");
    gameData.playerId = null;
    gameData.playerName = null;
    gameData.gameCode = null;
    gameData.isHost = null;
    gameData.players = [];
    gameData.ok = false;

    localStorage.removeItem("gameData");
  }

  function resetGameCode() {
    gameData.gameCode = null;
    console.log(JSON.stringify(gameData));
  }

  function saveToLocalStorage() {
    localStorage.setItem("gameData", JSON.stringify(gameData));
  }

  function loadFromLocalStorage() {
    const raw = localStorage.getItem("gameData");
    if (!raw) return;

    try {
      const data = JSON.parse(raw);
      Object.assign(gameData, data);
    } catch (e) {
      console.error("load fail", e);
    }
  }




  return {
    gameData,
    countdown,
    gameState,
    startLobbyLiveUpdate,
    updatePlayerList,
    disconnect,
    reset,
    resetGameCode,
    triggerGameStart
  };
});