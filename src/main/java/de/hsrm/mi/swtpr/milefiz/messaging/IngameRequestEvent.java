package de.hsrm.mi.swtpr.milefiz.messaging;

// Event, damit Antwort von Frontend zu gegebenen Anlaessen erwartet wird 
public class IngameRequestEvent {
    public enum Aktion {
        DIRECTION,
        // BARRIERE,
    }

    private Aktion type;
    private String playerId;
    private String figureId;    // fuer welche Figur request ankam
    private String gameCode;

    // Ohne bestimmte Figur (zb nach Wuerfeln)
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

    
}
