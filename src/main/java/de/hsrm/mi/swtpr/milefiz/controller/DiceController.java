package de.hsrm.mi.swtpr.milefiz.controller;

////import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.exception.CooldownException;
//import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent;
//import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent.Aktion;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.service.DiceService;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
//import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/daten")
public class DiceController {

    private final DiceService diceService;
    private final GameService gameService;
    private static final Logger logger = LoggerFactory.getLogger(DiceController.class);

    // //@Autowired
    public DiceController(DiceService diceService, GameService gameService) {
        this.diceService = diceService;
        this.gameService = gameService;
    }

    /*
    @GetMapping("/roll")
    public DiceResult roll(@RequestParam(name = "playerName", defaultValue = "Player 1") String playerName)
            throws CooldownException {
        return diceService.rollDice(playerName);
    } */

    @PostMapping("/cooldown")
    public void setCooldown(@RequestParam("seconds") long seconds) {
        diceService.setCooldown(seconds);
    }

    @GetMapping("/cooldown")
    public long getCooldown() {
        return diceService.getCooldown();
    }

    // Wuerfelwurf für spezifisches Spiel ("gameCode" als Pfadvariable)
    // Speichert die gewuerfelte Zahl im backend
    // "gameCode"
    @PostMapping("/game/{gameCode}/roll")
    public DiceResult roll(@PathVariable("gameCode") String gameCode, @RequestParam("playerId") String playerId) throws CooldownException {

        // Spiel laden
        Game game = gameService.getGame(gameCode);
        if (game == null)
            throw new RuntimeException("Spiel nicht gefunden!");

        String playerName = game.getPlayerById(playerId).getName();
        logger.info("Spielername {}, Spielerid {}", playerName, playerId);

        // Validierung:Prüfen, ob schon gewuerfelt wurde
        // Blockiert doppelte Wuerfe bevor man sich bewegt hat
        // wenn DiceResult null -> es ist der erste Wurf des Spielers
        DiceResult prevRoll = game.getDiceResultByName(playerName);
        if (prevRoll != null && prevRoll.getValue() > 0) {
            throw new RuntimeException("Du hast schon gewürfelt! Beweg dich erst mal.");
        }

        // Wuerfeln
        DiceResult result = diceService.rollDice(playerName, playerId, gameCode);

        // Ergebnis im Backend festhalten
        game.updateDiceResult(playerName, result);

        // Ergebnis für den Spieler speichern
        //game.setRollForPlayer(playerId, result.getValue()); noetig??

        return result;
    }
}
