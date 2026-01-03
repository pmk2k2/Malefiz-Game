export interface IIngameRequestEvent {
  type: string // Nachrichtentyp (z.B. DIRECTION)
  playerId: string // Player-ID
  figureId: string // ID der Figur
  gameCode: string // Game-Code der Lobby
  forbiddenDir: string  // verbotene Richtung
}
