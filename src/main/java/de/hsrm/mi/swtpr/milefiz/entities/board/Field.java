package de.hsrm.mi.swtpr.milefiz.entities.board;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;

public class Field {
    private int i;
    private int j;
    private CellType type;
    private List<Figure> figures = new ArrayList<>();
    private Barrier barrier;

    public Field() {

    }

    public Field(int i, int j, CellType type) {
        this.i = i;
        this.j = j;
        this.type = type;
    }

    public boolean hasBarrier() {
        // Ein Feld hat eine Barriere, wenn entweder das Objekt gesetzt ist oder der Zelltyp explizit BARRIER ist.
        return this.barrier != null || this.type == CellType.BARRIER;
    }

    public Barrier getBarrier() {
        return barrier;
    }

    public void setBarrier(Barrier barrier) {
        this.barrier = barrier;
    }

    public void addFigure(Figure fig) {
        figures.add(fig);
    }

    public void removeFigure(Figure figure) {
        figures.remove(figure);
    }

    public List<Figure> getFigures() {
        // Nur Figuren zaehlen, die tatsaechlich auf Feld und nicht "im Aus" stehen
        return figures
                .stream()
                .filter(f -> f.isOnField())
                .collect(Collectors.toList());
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public CellType getType() {
        return type;
    }

    public boolean isBlocked() {
        if (this.type == CellType.BLOCKED) return true;
        if (this.hasBarrier()) return true;
        if (!this.getFigures().isEmpty()) return true;

        return false;
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

    // Duell: deutet an, ob das Feld derzeit ein Duellfeld ist
    public boolean isDuelField() {
        List<Figure> figs = getFigures();

        if(figs.size() != 2) {
            return false;
        }
        // Nur wenn 2 Figuren auf einem Feld 
        return !figs.get(0).getOwnerPlayerId().equals(figs.get(1).getOwnerPlayerId());
    }

}
