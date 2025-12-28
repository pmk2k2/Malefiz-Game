package de.hsrm.mi.swtpr.milefiz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import de.hsrm.mi.swtpr.milefiz.model.BarrierMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;
import de.hsrm.mi.swtpr.milefiz.service.GameService;

@RestController
@RequestMapping("/api/game")
public class BarrierController {

    private static final Logger logger = LoggerFactory.getLogger(BarrierController.class);

    @Autowired
    private GameService gameService;

    //Trigger für das Platzieren der Barriere (Pickup and Place).
    //Wird aufgerufen, wenn der Spieler eine Barriere platzieren will.
    //Hier muss das Sprint Backlog Item #16 Implementiert werden
    @PostMapping("/{gameCode}/barrier/place")
    public FigureMoveResult placeBarrier(@PathVariable String gameCode, 
                                         @RequestBody BarrierMoveRequest request) {
        
        logger.info("Barrier-Place Trigger empfangen für Spiel: {} an Pos ({},{}) von Spieler {}", 
                gameCode, request.toI, request.toJ, request.playerId);

        return FigureMoveResult.ok();
    }
}