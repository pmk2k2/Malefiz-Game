package de.hsrm.mi.swtpr.milefiz.messaging;

import de.hsrm.mi.swtpr.milefiz.model.Direction;

// Event fuer Bewegungen von Spielfiguren
public class IngameMoveEvent {

    // Infos zum Spiel
    private String gameCode; // an die bestimmte Lobby
    private String playerId;
    private String figureId;
    private int steps;
    private Direction dir;

    public IngameMoveEvent(String gameCode, String playerId, String figureId, int steps, Direction dir) {
        this.gameCode = gameCode;
        this.playerId = playerId;
        this.figureId = figureId;
        this.steps = steps;
        this.dir = dir;
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

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

}
