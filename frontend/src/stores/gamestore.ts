import { defineStore } from "pinia";
import { reactive } from "vue";
import { Client } from '@stomp/stompjs';
import type { IFrontendNachrichtEvent } from '@/services/IFrontendNachrichtEvent';
import { useInfo } from "@/composable/useInfo";
import type { ISpielerDTD } from "./ISpielerDTD";
import type { LobbyID } from './LoppyID';

const { setzeInfo } = useInfo();

const DEST = '/topic/lobby';

export const useDoenerStore = defineStore("doenerstore", () => {
  const spielerdata = reactive<{ ok: boolean; spielerinfo: ISpielerDTD[], lobby: LobbyID[] }>({
    ok: false,
    spielerinfo: [],
    lobbyid:[]
  });
  let stompClient: Client | null = null;

  function startDoenerLiveUpdate() {
    if (stompClient && stompClient.active) {
      console.log("STOMP-Client existiert bereits oder ist aktiv.");
      return;
    }

    stompClient = new Client({
      brokerURL: 'ws://localhost:8080/stompbroker/websocket',
      reconnectDelay: 5000,
      debug: str => console.log('[STOMP]', str),
    });

    stompClient.onConnect = () => {
      console.log("STOMP verbunden, abonniere /topic/lobby");
      stompClient!.subscribe(DEST, message => {
        try {
          const event: IFrontendNachrichtEvent = JSON.parse(message.body);
          console.log("Empfangenes Event:", JSON.stringify(event));
          if (event.typ === "DOENER") {
            updateDoenerListe();
          }
        } catch (err) {
          setzeInfo("Fehler beim Verarbeiten einer Live-Nachricht");
          console.error(err);
        }
      });
    };

    async function updateDoenerListe() {
    try {
        setzeInfo("Doener Daten wurden Aktualisiert");
        //const loginStore = useLoginStore();
        console.log("updateDoenerListe aufgerufen");
        const response = await fetch('/api/doener', {
            headers: {
                //'Authorization': `Bearer ${loginStore.jwt}`,
            },
            redirect: 'error'
        });
        console.log("[updateDoenerListe] Response-Status:", response.status);
        if (!response.ok) throw new Error(response.statusText);

        const jsonData = await response.json();
        //doenerdata.doenerliste = jsonData;
        //doenerdata.ok = true;
        console.log("[updateDoenerListe] Dönerliste aktualisiert:", jsonData);

        startDoenerLiveUpdate();
    } catch (error) {
        console.error("[updateDoenerListe] Fehler:", error);
        if (error instanceof Error) {
            setzeInfo(error.message);
        } else {
            setzeInfo("Unbekannter Fehler");
        }
        //doenerdata.doenerliste = [];
        //doenerdata.ok = false;
    }
  }
}