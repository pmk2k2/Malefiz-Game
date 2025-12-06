package de.hsrm.mi.swtpr.milefiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import de.hsrm.mi.swtpr.milefiz.service.MovementLogicService;

@RestController
@RequestMapping("/api/move")
public class MoveController {

    @Autowired
    private GameService gameService;

    @Autowired
    private MovementLogicService movementLogic;

    @PostMapping("/{gameCode}")
    public FigureMoveResult move(
            @PathVariable String gameCode,
            @RequestBody FigureMoveRequest reqest) {

        Game game = gameService.getGame(gameCode);

        if (game == null) {
            return FigureMoveResult.fail("Game nicht gefunden");
        }

        return movementLogic.moveFigure(game, reqest);
    }
}
