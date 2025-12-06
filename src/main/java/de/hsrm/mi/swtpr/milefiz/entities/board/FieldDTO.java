package de.hsrm.mi.swtpr.milefiz.entities.board;

public class FieldDTO {
    private int i;
    private int j;
    private CellType type;

    public FieldDTO() {
    } // Jackson needs this

    public FieldDTO(int i, int j, CellType type) {
        this.i = i;
        this.j = j;
        this.type = type;
    }

    // getters & setters
    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FieldDTO [i=" + i + ", j=" + j + ", type=" + type + "]";
    }

}
