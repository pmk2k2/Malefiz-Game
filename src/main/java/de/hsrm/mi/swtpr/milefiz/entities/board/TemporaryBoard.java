package de.hsrm.mi.swtpr.milefiz.entities.board;

import java.util.Arrays;

public class TemporaryBoard implements Board {
    private int rows;
    private int cols;
    private final Field[][] grid;

    public TemporaryBoard() {
        grid = new Field[1][1];
        // rows = 8;
        // cols = 11;
        // grid = new Field[rows][cols];

        // for (int j = 0; j < rows; j++) {
        // for (int i = 0; i < cols; i++) {
        // // Reihen 0, 2, 4, 6 sind begehbar (PATH), wie im Frontend
        // boolean isPathRow = (j == 0 || j == 2 || j == 4 || j == 6);

        // CellType type = isPathRow ? CellType.PATH : CellType.BLOCKED;

        // // Feld mit passendem type anlegen
        // grid[j][i] = new Field(i, j, type);
        // }
        // }

        // // Ziel-Feld wie in GameBoard.vue (i=5, j=7)
        // grid[7][5].setType(CellType.GOAL);

    }

    public TemporaryBoard(int rows, int cols, Field[][] grid) {
        this.rows = rows;
        this.cols = cols;
        this.grid = grid;
    }

    @Override
    public Field get(int i, int j) {
        return grid[j][i];
    }

    @Override
    public int getWidth() {
        return grid[0].length;
    }

    @Override
    public int getHeight() {
        return grid.length;
    }

    @Override
    public String toString() {
        return "TemporaryBoard [rows=" + rows + ", cols=" + cols + ", grid=" + Arrays.toString(grid) + "]";
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

}