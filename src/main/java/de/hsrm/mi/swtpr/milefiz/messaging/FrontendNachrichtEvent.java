package de.hsrm.mi.swtpr.milefiz.messaging;

import java.time.Instant;

import de.hsrm.mi.swtpr.milefiz.model.GameState;

public class FrontendNachrichtEvent {

    public enum Nachrichtentyp {
        LOBBY
        // weitere typen können hier spätre ergänzt werden
    }

    public enum Operation {
        LEFT,
        JOINED,
        READY_UPDATED,
        COUNTDOWN_STARTED,
        GAME_STARTED_BY_ADMIN,
        GAME_STARTED_BY_COUNTER,
        GAME_RUNNING,
        PLAYER_LIMIT_ERROR
    }

    private Nachrichtentyp typ;
    private String id;
    private Operation operation;
    private String gameCode; // an die bestimmte Lobby
    private String playerName;
    private Instant countdownStartedAt;
    private GameState gameState;
    private long countdownDurationSeconds;

    public FrontendNachrichtEvent(Nachrichtentyp typ, String id, Operation operation, String gameCode,
            String playerName) {
        this.typ = typ;
        this.id = id;
        this.operation = operation;
        this.gameCode = gameCode;
        this.playerName = playerName;
    }

    public FrontendNachrichtEvent(Nachrichtentyp typ, String id, Operation operation, String gameCode,
            String playerName, Instant countdownStartedAt, GameState gameState) {
        this(typ, id, operation, gameCode, playerName);
        this.countdownStartedAt = countdownStartedAt;
        this.gameState = gameState;
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

    public Instant getCountdownStartedAt() {
        return countdownStartedAt;
    }

    public void setCountdownStartedAt(Instant t) {
        this.countdownStartedAt = t;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState s) {
        this.gameState = s;
    }

    public long getCountdownDurationSeconds() {
        return countdownDurationSeconds;
    }

    public void setCountdownDurationSeconds(long seconds) {
        this.countdownDurationSeconds = seconds;
    }
}
