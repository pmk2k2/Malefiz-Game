package de.hsrm.mi.swtpr.milefiz.entities.board;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    private int cols;
    private int rows;
    private Field[][] grid = {};
    // Startfelder fuer Spieler
    private ArrayList<Field> startFields = new ArrayList<Field>();

    public Board() {

    }

    public Board(int cols, int rows, Field[][] grid) {
        this.cols = cols;
        this.rows = rows;
        this.grid = grid;
    }

    public Field get(int i, int j) {
        return grid[j][i];
    }

    public int getWidth() {
        return grid[0].length;
    }

    public int getHeight() {
        return grid.length;
    }

    @Override
    public String toString() {
        return "Board [rows=" + rows + ", cols=" + cols + ", grid=" + Arrays.toString(grid) + "]";
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public Field[][] getGrid() {
        return grid;
    }

    public void setGrid(Field[][] grid) {
        this.grid = grid;
    }

    public ArrayList<Field> getStartFields() {
        return startFields;
    }

    public Field getStartFieldByIndex(int index) {
        return startFields.get(index);
    }

    public void addStartField(Field startField) {
        this.startFields.add(startField);
    }

}