package de.hsrm.mi.swtpr.milefiz.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.service.CodeGeneratorService;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RestController;
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

        service.addPlayer(code, playerId, name, true, false);
        Game game = service.getGame(code);

        return Map.of(
                "gameCode", code,
                "playerName", name,
                "players", game.getPlayers(),
                "playerId", playerId,
                "isHost", true,
                "isReady", false);
    }

    @PostMapping("/join")
    public Map<String, Object> joinGame(@RequestBody Map<String, String> body, HttpSession session) {
        String name = body.get("name");
        String code = body.get("code");
        String playerId = session.getId();

        boolean success = service.addPlayer(code, playerId, name, false, false);
        if (!success) {
            return Map.of("error", "Invalid game code");
        }
        Game game = service.getGame(code);

        return Map.of(
                "gameCode", code,
                "playerName", name,
                "players", game.getPlayers(),
                "playerId", playerId,
                "isHost", false,
                "isReady", false);
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
    @PostMapping("/start")
   public ResponseEntity<Map<String, Object>> startGame(@RequestBody Map<String, String> body, HttpSession session) {
        String gameCode = body.get("code");
        String playerId = body.get("playerId");
        logger.info("The player with id " + playerId + " is leaving the game " + gameCode);

        //Game game = service.getGame(gameCode);

        boolean successByAdmin= service.triggerAdminStart(gameCode, playerId);
        boolean successByCounter= service.triggerCounterStart(gameCode, playerId);

        if(successByAdmin || successByCounter){
            return ResponseEntity.ok(Map.of("success", true));
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Only the host can start the game"));
                }
    }

    @GetMapping("/get")
    public Map<String, Object> getPlayers(@RequestParam("code") String code) {
        Game game = service.getGame(code);
        if (game == null)
            return Map.of("error", "Game not found");

        return Map.of("players", game.getPlayers());
    }

@PostMapping("/setReady")
public ResponseEntity<Map<String, Object>> setReady(@RequestBody Map<String, String> body, HttpSession session) {
    String gameCode = body.get("code");
    String playerId = body.get("playerId");
    boolean isReady = Boolean.parseBoolean(body.get("isReady"));

    Game game = service.getGame(gameCode);
    if (game == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Game not found"));
    }

    //  Spielerlimit prüfen (≤ 4)
    if (game.getPlayers().size() > 4) {
        service.publishPlayerLimitEvent(gameCode);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Player limit exceeded (max 4)"));
    }

    // Use the method that publishes READY_UPDATED and starts countdown when appropriate
    boolean updated = service.setPlayerReadyAndCheckStart(gameCode, playerId, isReady);
    if (!updated) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Player not found"));
    }

    // READY Updated Event broadcasten
    service.publishReadyUpdateEvent(gameCode, playerId, isReady, game.getPlayerById(playerId).getName());

    //  Countdown starten wenn ALLE Ready + WAITING
    if (game.getPlayers().stream().allMatch(Player::isReady)
            && game.getState() == GameState.WAITING) {

        game.startCountdown();

        service.publishCountdownStartEvent(gameCode, game.getCountdownStartedAt());
    }

    return ResponseEntity.ok(Map.of("playerId", playerId, "isReady", isReady));
}


}
