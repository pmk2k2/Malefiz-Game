package de.hsrm.mi.swtpr.milefiz.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import de.hsrm.mi.swtpr.milefiz.controller.dto.FigureDto;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.exception.GameNotFoundException;
import de.hsrm.mi.swtpr.milefiz.exception.GameStartException;
import de.hsrm.mi.swtpr.milefiz.exception.NotHostException;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@CrossOrigin(
    origins = {
        "http://localhost:5173",
        "https://dev1.mi.hs-rm.de"
    }
)
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

        if (!removed) {
            logger.warn("Player {} konnte nicht vom Spiel {} gelöscht werden.", playerId, gameCode);
        }

        return Map.of("removed", removed);
    }

    @PostMapping("/setReady")
    public ResponseEntity<Map<String, Object>> setReady(@RequestBody Map<String, String> body, HttpSession session) {
        String gameCode = body.get("code");
        String playerId = body.get("playerId");

        if (gameCode == null || playerId == null || body.get("isReady") == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Fehlende 'code', 'playerId' or 'isReady' in Request-Body"));
        }

        boolean isReady = Boolean.parseBoolean(body.get("isReady"));

        // Use the method that publishes READY_UPDATED and starts countdown when appropriate
        boolean updated = service.setPlayerReadyAndCheckStart(gameCode, playerId, isReady);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Player not found"));
        }

        return ResponseEntity.ok(Map.of("playerId", playerId, "isReady", isReady));
    }

    @PostMapping("/start")
   public ResponseEntity<Map<String, Object>> startGame(@RequestBody Map<String, String> body) {
        String gameCode = body.get("code");
        String playerId = body.get("playerId");
        
        if (gameCode == null || playerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Fehlende 'code' or 'playerId' im Request-Body."));
        }

        try {
            service.startOrTriggerGame(gameCode, playerId);
        
            // Erfolg
            return ResponseEntity.ok(Map.of("success", true, "gameCode", gameCode));

        } catch (GameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Spiel nicht gefunden."));
        } catch (NotHostException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Nur der Host darf das Spiel starten."));
        } catch (GameStartException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Spiel konnte nicht gestartet werden: " + e.getMessage()));
        }
    }

    @GetMapping("/get")
    public Map<String, Object> getPlayers(@RequestParam("code") String code) {
        Game game = service.getGame(code);
        if (game == null)
            return Map.of("error", "Spiel nicht gefunden.");

        return Map.of("players", game.getPlayers());
    }

    @GetMapping("/{code}/figures")
    public List<FigureDto> getFigures(@PathVariable String code) {
        logger.info("Request figures for game code: {}", code);
        List<FigureDto> figures = service.getFiguresasDto(code);

        if (figures.isEmpty() && service.getGame(code) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Spiel nicht gefunden.");
        }
        return figures;
    }

}
