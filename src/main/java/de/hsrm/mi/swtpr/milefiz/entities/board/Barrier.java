package de.hsrm.mi.swtpr.milefiz.entities.board;


public class Barrier {
    private int gridI;
    private int gridJ;

    public Barrier() {
    }

    public Barrier(int gridI, int gridJ) {
        this.gridI = gridI;
        this.gridJ = gridJ;
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
}