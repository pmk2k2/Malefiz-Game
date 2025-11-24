package de.hsrm.mi.swtpr.milefiz.entities.board;

public class Figure {
    private String id;
    private String playerId;
    private int i;
    private int j;

    public Figure(String id, String playerId, int i, int j) {
        this.id = id;
        this.playerId = playerId;
        this.i = i;
        this.j = j;
    }

    public String getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public void setPosition(int i, int j) {
        this.i = i;
        this.j = j;
    }
}
