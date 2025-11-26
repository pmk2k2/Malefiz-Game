package de.hsrm.mi.swtpr.milefiz.entities.board;

import java.util.Arrays;

public class TemporaryBoardDTO {
    private int cols;
    private int rows;
    private FieldDTO[][] grid; // 2D array

    public TemporaryBoardDTO() {
    }

    public TemporaryBoardDTO(int cols, int rows, FieldDTO[][] grid) {
        this.cols = cols;
        this.rows = rows;
        this.grid = grid;
    }

    // getters & setters
    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public FieldDTO[][] getGrid() {
        return grid;
    }

    public void setGrid(FieldDTO[][] grid) {
        this.grid = grid;
    }

    @Override
    public String toString() {
        return "TemporaryBoardDTO [cols=" + cols + ", rows=" + rows + ", grid=" + Arrays.toString(grid) + "]";
    }

}
