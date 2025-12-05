package de.hsrm.mi.swtpr.milefiz.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import de.hsrm.mi.swtpr.milefiz.controller.dto.FigureDto;
import de.hsrm.mi.swtpr.milefiz.controller.dto.PlayerDto;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
                "playerId", playerId,
                "isHost", true);
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
                "playerId", playerId,
                "isHost", false);
    }

    @PostMapping("/leave")
    public Map<String, Object> leaveGame(@RequestBody Map<String, String> body, HttpSession session) {
        String gameCode = body.get("code");
        String playerId = body.get("playerId");
        logger.info("The player with id " + playerId + " is leaving the game " + gameCode);
        boolean removed = service.removePlayer(gameCode, playerId);
        if (removed) {
            logger.info(playerId + "LEFFFFTTTTTT");
        } else {
            logger.info("NOOOOOOOTTTTTTT");
        }
        return Map.of("removed", removed);
    }

    @GetMapping("/get")
    public Map<String, Object> getPlayers(@RequestParam("code") String code) {
        Game game = service.getGame(code);
        if (game == null)
            return Map.of("error", "Game not found");

        return Map.of("players", game.getPlayers());
    }

    @GetMapping("/{code}/figures")
    public List<FigureDto> getFigures(@PathVariable String code) {
        logger.info("Request figures for game code: {}", code);
        try {
            return service.getFiguresasDto(code);
        } catch (Exception e) {
            logger.error("Error fetching figures for game {}: {}", code, e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or error occurred");
        }
    }

    @PostMapping("/{code}/start")
    public Map<String, Object> startGame(@PathVariable String code, @RequestBody PlayerDto player) {
        String playerId = player.getPlayerId();
        Game game = service.getGame(code);

        if (game == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Spiel nicht gefunden.");
        }

        Player callingPlayer = game.getPlayerById(playerId);

        // Nur der Host des Spiels darf ihn starten
        if (callingPlayer == null || !callingPlayer.isHost()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nur der Host darf das Spiel starten.");
        }

        boolean success = service.startGame(code);

        if (success) {
            return Map.of("success", true, "gameCode", code);
        } else {
            // Spiel läuft bereit oder keine Spiele verfügbar
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Spiel konnte nicht gestartet werden.");
        }
    }

    @PostMapping("/setReady")
    public ResponseEntity<Map<String, Object>> setReady(@RequestBody Map<String, String> body, HttpSession session) {
        String gameCode = body.get("code");
        String playerId = body.get("playerId");
        boolean isReady = Boolean.parseBoolean(body.get("isReady"));

        Game game = service.getGame(gameCode);
        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Game not found"));
        }

        boolean updated = service.setPlayerReady(gameCode, playerId, isReady);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Player not found"));
        }

        return ResponseEntity.ok(Map.of("playerId", playerId, "isReady", isReady));
    }

}
