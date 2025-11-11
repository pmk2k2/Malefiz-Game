package de.hsrm.mi.swtpr.milefiz.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private Map<String, Game> games = new HashMap<>();
    private CodeGeneratorService codeService;

    public GameService(CodeGeneratorService codeService) {
        this.codeService = codeService;
    }

    public String createGame() {
        String gameCode = codeService.generateCode();
        games.put(gameCode, new Game());
        return gameCode;
    }

    public boolean addPlayer(String gameCode, String playerId, String name, boolean isHost) {
        Game game = games.get(gameCode);
        if (games.get(gameCode) == null) {
            return false;
        }
        //
        game.addPlayer(new Player(name, playerId, isHost));
        return true;
    }

    public Game getGame(String code) {
        return games.get(code);
    }
}
