package de.hsrm.mi.swtpr.milefiz.controller.dto;

public class FigureDto {
    private String id;
    private String color;
    private String playerId;
    private String orientation;
    private int gridI;
    private int gridJ;
    
    public FigureDto(String id, String color, String playerId, String orientation, int gridI, int gridJ) {
        this.id = id;
        this.color = color;
        this.playerId = playerId;
        this.orientation = orientation;
        this.gridI = gridI;
        this.gridJ = gridJ;
    }

    public String getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getOrientation() {
        return orientation;
    }

    public int getGridI() {
        return gridI;
    }

    public int getGridJ() {
        return gridJ;
    }
}
