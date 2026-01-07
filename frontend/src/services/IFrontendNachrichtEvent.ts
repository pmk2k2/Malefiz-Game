import type { IBewegung } from "./IBewegung"
import type { IStep } from "./IStep"

export interface IFrontendNachrichtEvent {
  typ: string // Nachrichtentyp (z.B. LOBBY)
  operation: string // Operation (z.B. JOINED, LEFT, COUNTDOWN_STARTED, etc.)
  id: string // Player-ID
  gameCode: string // Game-Code der Lobby
  playerName: string // Name des Spielers
  countdownStartedAt: string
  countdownDurationSeconds: number
  gameState: string

  // Felder fuer Movementupdates
  figureId: string
  bewegung: IBewegung
  step: IStep
}
