package de.hsrm.mi.swtpr.milefiz.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.board.FieldDTO;
import de.hsrm.mi.swtpr.milefiz.entities.board.TemporaryBoard;
import de.hsrm.mi.swtpr.milefiz.entities.board.TemporaryBoardDTO;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api")
public class BoardController {
    public TemporaryBoardDTO thisBoard;

    @PostMapping("/temporary-board")
    public TemporaryBoard receiveBoard(@RequestBody TemporaryBoardDTO dto) {
        Field[][] board = new Field[dto.getRows()][dto.getCols()];
        System.err.println(dto);

        for (int j = 0; j < dto.getRows(); j++) {
            for (int i = 0; i < dto.getCols(); i++) {
                FieldDTO f = dto.getGrid()[j][i];
                board[j][i] = new Field(f.getI(), f.getJ(), f.getType());
            }
        }

        TemporaryBoard temporaryBoard = new TemporaryBoard(dto.getCols(), dto.getRows(), board);
        return temporaryBoard;
    }

}
