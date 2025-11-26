package de.hsrm.mi.swtpr.milefiz.entities.board;

public class TemporaryBoard implements Board {

    private final Field[][] grid;

    public TemporaryBoard() {
        int rows = 8;
        int cols = 11;
        grid = new Field[rows][cols];

        
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                // Reihen 0, 2, 4, 6 sind begehbar (PATH), wie im Frontend
                boolean isPathRow = (j == 0 || j == 2 || j == 4 || j == 6);

                CellType type = isPathRow ? CellType.PATH : CellType.BLOCKED;

                // Feld mit passendem type anlegen
                grid[j][i] = new Field(i, j, type);
            }
        }

        // Ziel-Feld wie in GameBoard.vue (i=5, j=7)
        grid[7][5].setType(CellType.GOAL);


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
}