package de.hsrm.mi.swtpr.milefiz.entities.board;

import java.util.Arrays;

public class Board {
    private int rows;
    private int cols;
    private Field[][] grid = {};

    public Board() {

    }

    public Board(int rows, int cols, Field[][] grid) {
        this.rows = rows;
        this.cols = cols;
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

}