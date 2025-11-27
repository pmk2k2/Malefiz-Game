import { defineStore } from "pinia";
import { reactive, watch } from "vue";
import SockJS from "sockjs-client";
import { Client } from '@stomp/stompjs';
import type { IFrontendNachrichtEvent } from '@/services/IFrontendNachrichtEvent';
//import { useInfo } from "@/composable/useInfo";
import type { ISpielerDTD } from "./ISpielerDTD";
import type { LobbyID } from './LobbyID'; 
import { mapBackendPlayersToDTD } from '@/stores/mapper';

//const { setzeInfo } = useInfo();

export const useGameStore = defineStore("gamestore", () => { 
  const gameData = reactive<{
    ok: boolean;
    players: ISpielerDTD[];
    gameCode: string | null;
    playerId: string | null;
    playerName: string | null;
    isHost: boolean | null;
  }>({
    ok: false,
    players: [],
    gameCode: null,
    playerId: null,
    playerName: null,
    isHost: null
  });

  loadFromLocalStorage();
  watch(
  () => gameData,
  () => saveToLocalStorage(),
  { deep: true }
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
          if (event.typ === "LOBBY") { //bei event JOINED und LEFT
            updatePlayerList(gameCode);
          }
          //TODO: Mehrere if (FrontEndNacrichtEvents) aufzählen, wo die UI live geupdated werden muss)
        } catch (err) {
          //setzeInfo("Fehler beim Verarbeiten einer Live-Nachricht");
          console.error(err);
          console.log("FEHLER")
        }
      });
    };

    stompClient.activate();
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
      gameData.gameCode = gameCode;

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
    startLobbyLiveUpdate,
    updatePlayerList,
    disconnect,
    reset,
  };
});