package de.hsrm.mi.swtpr.milefiz.messaging;

import de.hsrm.mi.swtpr.milefiz.model.Direction;

// Event, damit Antwort von Frontend zu gegebenen Anlaessen erwartet wird 
public class IngameRequestEvent {
    public enum Aktion {
        DIRECTION,
        DICE_ROLL
        // BARRIERE,
    }

    private Aktion type;
    private String playerId;
    private String figureId;    // fuer welche Figur request ankam
    private String gameCode;
    private Direction forbiddenDir; // verbotene Richtung (bei Richtungsanfragen)

    // Ohne bestimmte Figur (zb vor/nach Wuerfeln)
    public IngameRequestEvent(Aktion type, String playerId, String gameCode) {
        this.type = type;
        this.playerId = playerId;
        this.gameCode = gameCode;
    }

    // Fuer bestimmte Figur
    public IngameRequestEvent(Aktion type, String playerId, String figureId, String gameCode) {
        this.type = type;
        this.playerId = playerId;
        this.figureId = figureId;
        this.gameCode = gameCode;
    }
    // Fuer Richtungsanfragen
    public IngameRequestEvent(Aktion type, String playerId, String figureId, String gameCode, Direction forbiddenDir) {
        this.type = type;
        this.playerId = playerId;
        this.figureId = figureId;
        this.gameCode = gameCode;
        this.forbiddenDir = forbiddenDir;
    }

    public Aktion getType() {
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getFigureId() {
        return figureId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setType(Aktion type) {
        this.type = type;
    }
}
