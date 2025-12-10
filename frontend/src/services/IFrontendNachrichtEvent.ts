export interface IFrontendNachrichtEvent {
  typ: string // Nachrichtentyp (z.B. LOBBY)
  operation: string // Operation (z.B. JOINED, LEFT, COUNTDOWN_STARTED, etc.)
  id: string // Player-ID
  gameCode: string // Game-Code der Lobby
  playerName: string // Name des Spielers
  countdownStartedAt: string
  countdownDurationSeconds: number
  gameState: string
}
