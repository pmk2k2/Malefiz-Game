import type { ISpielerDTD } from "@/stores/ISpielerDTD";

export function mapBackendPlayersToDTD(players: any[]): ISpielerDTD[] {
  return players.map((p) => ({
    id: p.id ?? p.playerId ?? p.playerID,
    name: p.name ?? p.playerName,
    isReady: p.isReady ?? p.ready ?? false,
    spielfiguren: [],
    isHost: p.isHost ?? p.host ?? false,
    color: p.color ?? p.farbe ?? "#cccccc"
  }));
}