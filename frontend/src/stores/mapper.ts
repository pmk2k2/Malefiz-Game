import type { ISpielerDTD } from "@/stores/ISpielerDTD";

export function mapBackendPlayersToDTD(players: any[]): ISpielerDTD[] {
  return players.map((p, index) => ({
    id: index + 1,
    name: p.name,
    bereitschaft: false,
    spielfiguren: [
      { icon: `/assets/icons/player${(index % 4) + 1}.png` }
    ],
    isHost: p.isHost ?? p.host ?? false
  }));
}
