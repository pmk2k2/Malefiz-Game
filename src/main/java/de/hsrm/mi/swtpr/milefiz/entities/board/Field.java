package de.hsrm.mi.swtpr.milefiz.entities.board;

import java.util.ArrayList;
import java.util.List;

import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;

public class Field {
    private int i;
    private int j;
    private CellType type;
    private List<Figure> figures = new ArrayList<>();

    public Field() {

    }

    public Field(int i, int j, CellType type) {
        this.i = i;
        this.j = j;
        this.type = type;
    }

    public void addFigure(Figure fig) {
        figures.add(fig);
    }

    public void removeFigure(Figure figure) {
        figures.remove(figure);
    }

    public List<Figure> getFigures() {
        return figures;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public CellType getType() {
        return type;
    }

    public boolean isBlocked() {
        return type == CellType.BLOCKED;
    }

    public boolean isBarrier() {
        return type == CellType.BARRIER;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    @Override
    public String toString() {
        return "Field [i=" + i + ", j=" + j + ", type=" + type + "]";
    }

}
