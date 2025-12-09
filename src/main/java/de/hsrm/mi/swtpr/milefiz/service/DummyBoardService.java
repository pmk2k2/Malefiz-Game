package de.hsrm.mi.swtpr.milefiz.service;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;

@Service
public class DummyBoardService {
    public Board createDummyBoard() {
        int cols = 11;
        int rows = 8;

        Field[][] grid = new Field[rows][cols];

        // Row 0
        grid[0] = new Field[] {
                new Field(0, 0, CellType.START),
                new Field(1, 0, CellType.BLOCKED),
                new Field(2, 0, CellType.BLOCKED),
                new Field(3, 0, CellType.START),
                new Field(4, 0, CellType.BLOCKED),
                new Field(5, 0, CellType.START),
                new Field(6, 0, CellType.BLOCKED),
                new Field(7, 0, CellType.START),
                new Field(8, 0, CellType.BLOCKED),
                new Field(9, 0, CellType.BLOCKED),
                new Field(10, 0, CellType.START)
        };

        // Row 1
        grid[1] = new Field[] {
                new Field(0, 1, CellType.PATH),
                new Field(1, 1, CellType.BLOCKED),
                new Field(2, 1, CellType.BLOCKED),
                new Field(3, 1, CellType.PATH),
                new Field(4, 1, CellType.BLOCKED),
                new Field(5, 1, CellType.PATH),
                new Field(6, 1, CellType.BLOCKED),
                new Field(7, 1, CellType.PATH),
                new Field(8, 1, CellType.BLOCKED),
                new Field(9, 1, CellType.BLOCKED),
                new Field(10, 1, CellType.PATH)
        };

        // Row 2
        grid[2] = new Field[] {
                new Field(0, 2, CellType.PATH),
                new Field(1, 2, CellType.PATH),
                new Field(2, 2, CellType.PATH),
                new Field(3, 2, CellType.PATH),
                new Field(4, 2, CellType.PATH),
                new Field(5, 2, CellType.PATH),
                new Field(6, 2, CellType.PATH),
                new Field(7, 2, CellType.PATH),
                new Field(8, 2, CellType.PATH),
                new Field(9, 2, CellType.PATH),
                new Field(10, 2, CellType.PATH)
        };

        // Row 3
        grid[3] = new Field[] {
                new Field(0, 3, CellType.BLOCKED),
                new Field(1, 3, CellType.BLOCKED),
                new Field(2, 3, CellType.BLOCKED),
                new Field(3, 3, CellType.BLOCKED),
                new Field(4, 3, CellType.BLOCKED),
                new Field(5, 3, CellType.PATH),
                new Field(6, 3, CellType.BLOCKED),
                new Field(7, 3, CellType.BLOCKED),
                new Field(8, 3, CellType.BLOCKED),
                new Field(9, 3, CellType.BLOCKED),
                new Field(10, 3, CellType.BLOCKED)
        };

        // Row 4
        grid[4] = new Field[] {
                new Field(0, 4, CellType.PATH),
                new Field(1, 4, CellType.PATH),
                new Field(2, 4, CellType.PATH),
                new Field(3, 4, CellType.PATH),
                new Field(4, 4, CellType.PATH),
                new Field(5, 4, CellType.PATH),
                new Field(6, 4, CellType.PATH),
                new Field(7, 4, CellType.PATH),
                new Field(8, 4, CellType.PATH),
                new Field(9, 4, CellType.PATH),
                new Field(10, 4, CellType.PATH)
        };

        // Row 5
        grid[5] = new Field[] {
                new Field(0, 5, CellType.PATH),
                new Field(1, 5, CellType.BLOCKED),
                new Field(2, 5, CellType.BLOCKED),
                new Field(3, 5, CellType.BLOCKED),
                new Field(4, 5, CellType.BLOCKED),
                new Field(5, 5, CellType.BLOCKED),
                new Field(6, 5, CellType.BLOCKED),
                new Field(7, 5, CellType.BLOCKED),
                new Field(8, 5, CellType.BLOCKED),
                new Field(9, 5, CellType.BLOCKED),
                new Field(10, 5, CellType.PATH)
        };

        // Row 6
        grid[6] = new Field[] {
                new Field(0, 6, CellType.PATH),
                new Field(1, 6, CellType.PATH),
                new Field(2, 6, CellType.PATH),
                new Field(3, 6, CellType.PATH),
                new Field(4, 6, CellType.PATH),
                new Field(5, 6, CellType.PATH),
                new Field(6, 6, CellType.PATH),
                new Field(7, 6, CellType.PATH),
                new Field(8, 6, CellType.PATH),
                new Field(9, 6, CellType.PATH),
                new Field(10, 6, CellType.PATH)
        };

        // Row 7
        grid[7] = new Field[] {
                new Field(0, 7, CellType.BLOCKED),
                new Field(1, 7, CellType.BLOCKED),
                new Field(2, 7, CellType.BLOCKED),
                new Field(3, 7, CellType.BLOCKED),
                new Field(4, 7, CellType.BLOCKED),
                new Field(5, 7, CellType.GOAL),
                new Field(6, 7, CellType.BLOCKED),
                new Field(7, 7, CellType.BLOCKED),
                new Field(8, 7, CellType.BLOCKED),
                new Field(9, 7, CellType.BLOCKED),
                new Field(10, 7, CellType.BLOCKED)
        };

        Board dummyBoard = new Board(cols, rows, grid);

        return dummyBoard;
    }
}
