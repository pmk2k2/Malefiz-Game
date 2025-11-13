package de.hsrm.mi.swtpr.milefiz.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.service.CodeGeneratorService;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173") 
@RestController
@RequestMapping("/api/game")

public class GameRestController {
    
    @Autowired
    private GameService service;

    @Autowired
    private CodeGeneratorService codeService;

    @PostMapping("/create")
    public Map<String, Object> createGame(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String code = service.createGame();
        String playerId = codeService.generateCode();

        service.addPlayer(code, playerId, name, true);
        Game game = service.getGame(code);

        return Map.of(
            "gameCode", code, 
            "playerName", name,
            "players", game.getPlayers()
        );
    }

    @PostMapping("/join")
    public Map<String, Object> joinGame(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String code = body.get("code");
        String playerId = codeService.generateCode();

        boolean success = service.addPlayer(code, playerId, name, false);
        if (!success) {
            return Map.of("error", "Invalid game code");
        }

        Game game = service.getGame(code);
        return Map.of(
            "gameCode", code,
            "playerName", name,
            "players", game.getPlayers()
        );
    }
  
}
