package de.hsrm.mi.swtpr.milefiz.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/game")

public class GameRestController {
    private static Logger logger = LoggerFactory.getLogger(GameRestController.class);

    @Autowired
    private GameService service;

    @PostMapping("/create")
    public Map<String, Object> createGame(@RequestBody Map<String, String> body, HttpSession session) {
        String name = body.get("name");
        String code = service.createGame();
        String playerId = session.getId();

        service.addPlayer(code, playerId, name, true);
        Game game = service.getGame(code);

        return Map.of(
                "gameCode", code,
                "playerName", name,
                "players", game.getPlayers(),
                "playerId", playerId);
    }

    @PostMapping("/join")
    public Map<String, Object> joinGame(@RequestBody Map<String, String> body, HttpSession session) {
        String name = body.get("name");
        String code = body.get("code");
        String playerId = session.getId();

        boolean success = service.addPlayer(code, playerId, name, false);
        if (!success) {
            return Map.of("error", "Invalid game code");
        }

        Game game = service.getGame(code);
        return Map.of(
                "gameCode", code,
                "playerName", name,
                "players", game.getPlayers(),
                "playerId", playerId);
    }

    @PostMapping("/leave")
    public Map<String, Object> leaveGame(@RequestBody Map<String, String> body, HttpSession session) {
        String gameCode = body.get("code");
        String playerId = body.get("playerId");
        logger.info("The player with id " + playerId + " is leaving the game " + gameCode);
        boolean removed = service.removePlayer(gameCode, playerId);
        if (removed) {
            logger.info("LEFFFFTTTTTT");
        } else {
            logger.info("NOOOOOOOTTTTTTT");
        }
        return Map.of("removed", removed);
    }

    @GetMapping("/get")
    public Map<String, Object> getPlayers(@RequestParam String code) {
        Game game = service.getGame(code);
        if (game == null)
            return Map.of("error", "Game not found");

        return Map.of("players", game.getPlayers());
    }
    
}
