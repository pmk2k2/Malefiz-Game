import { defineStore } from "pinia";
import { reactive } from "vue";
import SockJS from "sockjs-client";
import { Client } from '@stomp/stompjs';
import type { IFrontendNachrichtEvent } from '@/services/IFrontendNachrichtEvent';
//import { useInfo } from "@/composable/useInfo";
import type { ISpielerDTD } from "./ISpielerDTD";
import type { LobbyID } from './LobbyID'; 

//const { setzeInfo } = useInfo();

export const useGameStore = defineStore("gamestore", () => { 
  const gameData = reactive<{
    ok: boolean;
    players: ISpielerDTD[];
    lobby: LobbyID[];
    gameCode: string | null;
  }>({
    ok: false,
    players: [],
    lobby: [],
    gameCode: null
  });

  let stompClient: Client | null = null;

  function startLobbyLiveUpdate(gameCode: string) {
    if (stompClient && stompClient.active) {
      console.log("STOMP-Client existiert bereits oder ist aktiv.");
      return;
    }

    stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/stompbroker'),
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
        redirect: 'error'
      });
      console.log("[updatePlayerList] Response-Status:", response.status);
      if (!response.ok) throw new Error(response.statusText);

      const jsonData = await response.json();
      gameData.players = jsonData.players || []; 
      gameData.lobby = jsonData.lobby || [];
      gameData.ok = true;
      gameData.gameCode = gameCode;
      console.log("[updatePlayerList] Daten aktualisiert:", jsonData);
    } catch (error) {
      console.error("[updatePlayerList] Fehler:", error);
      if (error instanceof Error) {
        //setzeInfo(error.message);
      } else {
        //setzeInfo("Unbekannter Fehler");
      }
      gameData.players = [];
      gameData.lobby = [];
      gameData.ok = false;
    }
  }

  function disconnect() {
    if (stompClient) {
      stompClient.deactivate();
      stompClient = null;
    }
  }

  return {
    gameData,
    startLobbyLiveUpdate,
    updatePlayerList,
    disconnect
  };
});