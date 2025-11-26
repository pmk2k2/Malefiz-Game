package de.hsrm.mi.swtpr.milefiz.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Nachrichtentyp;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Operation;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private Map<String, Game> games = new HashMap<>();
    private CodeGeneratorService codeService;

    private ApplicationEventPublisher publisher;

    public GameService(CodeGeneratorService codeService, ApplicationEventPublisher publisher) {
        this.codeService = codeService;
        this.publisher = publisher;
    }

    public String createGame() {
        String gameCode = codeService.generateCode();
        games.put(gameCode, new Game());
        logger.info("All the games:  "
                + Arrays.toString(games.keySet().stream().toArray(String[]::new)));
        return gameCode;
    }

    public boolean addPlayer(String gameCode, String playerId, String name, boolean isHost, boolean isReady) {
        Game game = games.get(gameCode);
        if (games.get(gameCode) == null) {
            return false;
        }
        //
        boolean playerAdded = game.addPlayer(new Player(name, playerId, isHost, isReady), playerId);

        // Event nach add veröffentlichen
        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                playerId,
                Operation.JOINED,
                gameCode,
                name));

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
        if (game == null) {
            return false;
        }

        Player removedPlayer = game.getPlayerById(playerId);
        logger.info("Player leaving: " + playerId);
        if (removedPlayer == null) {
            return false;
        }

        boolean wasHost = removedPlayer.isHost();

        boolean removed = game.removePlayer(playerId); // huer wird der player removed
        if (!removed) {
            return false;
        }
        // Event nach Entfernung veröffentlichen
        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                playerId,
                Operation.LEFT,
                gameCode,
                removedPlayer.getName()));

        List<Player> players = game.getPlayers();
        if (players.isEmpty()) {
            games.remove(gameCode);
            logger.info("Game " + gameCode + " removed as no players left.");
            return true;
        }

        if (wasHost) {
            players.get(0).setHost(true);
            logger.info("New host: " + players.get(0).getName());
        }

        return true;
    }

    public boolean setPlayerReady(String gameCode, String playerId, boolean isReady) {
        Game game = games.get(gameCode);
        if (game == null)
            return false;

        Player player = game.getPlayerById(playerId);
        if (player == null)
            return false;

        player.setReady(isReady);
        return true;
    }

}
