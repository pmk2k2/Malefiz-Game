package de.hsrm.mi.swtpr.milefiz.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.service.GameService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class BoardController {
    @Autowired
    private GameService service;
    private Logger logger = LoggerFactory.getLogger(BoardController.class);

    @PostMapping("/board")
    public void receiveBoard(@RequestBody Board board, @RequestParam("code") String code) {
        if (board.getGrid() != null) {
            logger.info("BACKEND HAT DAS SPIELFELD GENOMMEN");
            Game theGame = service.getGame(code);
            theGame.setBoard(board);
        } else {
            logger.info("NICHTS GENOMMEN");
        }
    }

    @GetMapping("/board")
    public Board sendBoard(@RequestParam("code") String code) {
        try {
            Game theGame = service.getGame(code);
            Board b = theGame.getBoard();
            // logger.info("THE GAME IS LOADED:" + theGame.toString() + "and its board: " + theGame.getBoard().toString());
            if (b.getCols() == 0) { // Board is leer
                return null;
            }
            // Prüfen, warum sich die alte Barriere nicht verschwindet.
            logger.info("REST-Anfrage Board: Feld (5,1) ist Typ: {}", b.get(5, 1).getType());
            return b;
        } catch (Exception e) {
            return null;
        }
    }

}
