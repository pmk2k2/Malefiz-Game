package de.hsrm.mi.swtpr.milefiz.entities.board;

public class Figure {
    private String id;
    private String playerId;
    private int i;
    private int j;
    private String color;

    public Figure(String id, String playerId, int i, int j, String color) {
        this.id = id;
        this.playerId = playerId;
        this.i = i;
        this.j = j;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String color() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
