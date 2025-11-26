export interface IFrontendNachrichtEvent {
    typ: string; // Nachrichtentyp (z.B. VERBINDUNGSABBRUCH)
    id: string;  // Player-ID
    operation: string; // Operation (z.B. LEFT)
    gameCode: string; // Game-Code der Lobby
    playerName: string; // Name des Spielers
}