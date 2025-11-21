package de.hsrm.mi.swtpr.milefiz.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        logger.info("All the games:  "
                + Arrays.toString(games.keySet().stream().toArray(String[]::new)));
        return gameCode;
    }

    public boolean addPlayer(String gameCode, String playerId, String name, boolean isHost) {
        Game game = games.get(gameCode);
        if (games.get(gameCode) == null) {
            return false;
        }
        //
        boolean playerAdded = game.addPlayer(new Player(name, playerId, isHost), playerId);
        if (!playerAdded) {
            return false;
        }
        return true;
    }

    public Game getGame(String code) {
        return games.get(code);
    }

    public boolean removePlayer(String gameCode, String playerId) {
        Game game = games.get(gameCode);
        if (game == null)
            return false;

        Player removedPlayer = game.getPlayerById(playerId);
        logger.info(playerId);
        if (removedPlayer == null)
            return false;

        boolean wasHost = removedPlayer.isHost();

        boolean removed = game.removePlayer(playerId);
        if (!removed)
            return false;

        List<Player> players = game.getPlayers();

        if (players.isEmpty()) {
            games.remove(gameCode);
            return true;
        }

        if (wasHost) {
            players.get(0).setHost(true);
            logger.info("new host" + players.get(0).getName());
        }

        return true;
    }

}
