package de.hsrm.mi.swtpr.milefiz.entities.game;

public class Figure {
    private String id;
    private String ownerPlayerId;
    private String color;
    private int gridI;
    private int gridJ;
    private String orientation;
    private boolean isOnField;  // Ist Figur auf Feld oder im Haeusschen?

    public Figure(String id, String ownerPlayerId, String color, int startI, int startJ) {
        this.id = id;
        this.ownerPlayerId = ownerPlayerId;
        this.color = color;
        this.gridI = startI;
        this.gridJ = startJ;
        this.orientation = "north";
        this.isOnField = false;
    }

    // hier Validierung der Bewegung (Wegnetz, Sperren) ?
    public void move(int newI, int newJ, String newOrientation) {
        this.gridI = newI;
        this.gridJ = newJ;
        this.orientation = newOrientation;
    }

    public String getId() {
        return id;
    }

    public String getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public String getColor() {
        return color;
    }

    public int getGridI() {
        return gridI;
    }

    public void setGridI(int gridI) {
        this.gridI = gridI;
    }

    public int getGridJ() {
        return gridJ;
    }

    public void setGridJ(int gridJ) {
        this.gridJ = gridJ;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public void setPosition(int gridI, int gridJ) {
        this.gridI = gridI;
        this.gridJ = gridJ;
    }

    public boolean isOnField() {
        return isOnField;
    }

    public void setOnField(boolean isOnField) {
        this.isOnField = isOnField;
    }
}
