package de.hsrm.mi.swtpr.milefiz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import de.hsrm.mi.swtpr.milefiz.entities.board.Barrier;
import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.BarrierMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.service.GameService;

@RestController
@RequestMapping("/api/game")
public class BarrierController {

    private static final Logger logger = LoggerFactory.getLogger(BarrierController.class);

    @Autowired
    private GameService gameService;

    // Trigger für das Platzieren der Barriere (Pickup and Place).
    // Wird aufgerufen, wenn der Spieler eine Barriere platzieren will.
    @PostMapping("/{gameCode}/barrier/place")
    public FigureMoveResult placeBarrier(@PathVariable("gameCode") String gameCode,
            @RequestBody BarrierMoveRequest request) {
        Game game = gameService.getGame(gameCode);
        if (game == null)
            return FigureMoveResult.fail("Spiel nicht gefunden.");

        Field targetField = game.getBoard().get(request.toI, request.toJ);
        // Platzieren einer Barriere darf nur auf einer freien Zelle geschehen
        if (targetField.getType() == CellType.PATH && !targetField.isBlocked()) {
            targetField.setBarrier(new Barrier(request.toI, request.toJ));
            targetField.setType(CellType.BARRIER);
            // Game serverseitig auf RUNNING setzen
            game.setState(GameState.RUNNING);
            // Clients informieren (Map schließen)
            gameService.publishBarrierPlacedEvent(gameCode);

            return FigureMoveResult.ok("Barriere gesetzt.");
        }

        return FigureMoveResult.fail("Dieses Feld ist besetzt oder ungültig.");

    }
}