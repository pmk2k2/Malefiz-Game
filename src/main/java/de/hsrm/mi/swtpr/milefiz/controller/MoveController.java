package de.hsrm.mi.swtpr.milefiz.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    private static final Logger logger = LoggerFactory.getLogger(MoveController.class);

    @Autowired
    private GameService gameService;

    @Autowired
    private MovementLogicService movementLogic;
    // Methode reagiert auf das Ergebnis "BARRIER_HIT" von MovementLogicService und triggert das Event "BARRIER_WAIT" über GameService
    @PostMapping("/{gameCode}")
    public FigureMoveResult move(@PathVariable String gameCode, @RequestBody FigureMoveRequest request) {

        Game game = gameService.getGame(gameCode);
        logger.info("Move-Request-Antwort erhalten: {}", request);

        if (game == null) return FigureMoveResult.fail("Game nicht gefunden");

        FigureMoveResult result = movementLogic.moveFigure(game, gameCode, request);

        if (result.success && "BARRIER_HIT".equals(result.message)) {
            logger.info("Barriere getroffen! Sende BARRIER_WAIT Event für Spiel {}", gameCode);
            gameService.publischBarrierWaitEvent(gameCode, request.playerId);
        }
        
        return result;
    }

    // Energie verbrauchen (Sprung)
    @PostMapping("/{gameCode}/consume-energy")
    public ResponseEntity<?> consumeEnergy(@PathVariable String gameCode, @RequestBody Map<String, String> body) {
        String playerId = body.get("playerId");
        Game game = gameService.getGame(gameCode);
        
        if (game == null) return ResponseEntity.notFound().build();

        // Setzt Energie auf 0
        movementLogic.resetPlayerEnergy(game, playerId);

        // Sendet einfach ein leeres "OK" zurück
        return ResponseEntity.ok().build();
    }

}
