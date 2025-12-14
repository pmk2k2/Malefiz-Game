import type { IBewegung } from "./IBewegung"

export interface IFrontendNachrichtEvent {
  typ: string // Nachrichtentyp (z.B. LOBBY)
  id: string // Player-ID
  operation: string // Operation (z.B. LEFT)
  gameCode: string // Game-Code der Lobby
  playerName: string // Name des Spielers
  countdownStartedAt: string

  countdownDurationSeconds: number
  gameState: string
  //Muhanad: warum gameState

  // Felder fuer Movementupdates
  figureId: string
  bew: IBewegung
}
