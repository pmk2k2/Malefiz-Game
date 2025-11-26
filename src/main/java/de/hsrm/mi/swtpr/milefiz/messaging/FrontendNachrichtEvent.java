package de.hsrm.mi.swtpr.milefiz.messaging;

public class FrontendNachrichtEvent {

    public enum Nachrichtentyp {
        LOBBY
        // weitere typen können hier spätre ergänzt werden
    }

    public enum Operation {
        LEFT,
        JOINED
        // weitere Operationen können hier später ergänzt werden.
    }

    private Nachrichtentyp typ;
    private String id;
    private Operation operation;
    private String gameCode; // an die bestimmte Lobby
    private String playerName;

    public FrontendNachrichtEvent(Nachrichtentyp typ, String id, Operation operation, String gameCode,
            String playerName) {
        this.typ = typ;
        this.id = id;
        this.operation = operation;
        this.gameCode = gameCode;
        this.playerName = playerName;
    }

    public Nachrichtentyp getTyp() {
        return typ;
    }

    public void setTyp(Nachrichtentyp typ) {
        this.typ = typ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        return "FrontendNachrichtEvent{" +
                "typ=" + typ +
                ", id=" + id +
                ", operation=" + operation +
                ", lobby=" + gameCode +
                ", playerName=" + playerName +
                '}';
    }
}
