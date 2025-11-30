package de.hsrm.mi.swtpr.milefiz.service;

import java.time.Instant;
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
import de.hsrm.mi.swtpr.milefiz.model.GameState;

@Service
public class GameService {
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private Map<String, Game> games = new HashMap<>();
    private CodeGeneratorService codeService;
    private final int MAX_PLAYERS = 2;
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

    public synchronized boolean setPlayerReady(String gameCode, String playerId, boolean isReady) {
        Game game = games.get(gameCode);
        if (game == null)
            return false;

        Player player = game.getPlayerById(playerId);
        if (player == null)
            return false;

        player.setReady(isReady);

        //  Prüfen, ob alle Ready sind
        boolean allReady = game.getPlayers().size() > 0 &&
                game.getPlayers().stream().allMatch(Player::isReady);

        if (allReady) {
            // Countdown starten + WS Event an alle Spieler senden
            startCountdown(gameCode);
        }
        return true;
    }

    private static FrontendNachrichtEvent limitEvent(String gameCode) {
        return new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.PLAYER_LIMIT_ERROR,
                gameCode,
                null);
    }

    public synchronized boolean setPlayerReadyAndCheckStart(String gameCode, String playerId, boolean isReady) {
        Game game = games.get(gameCode);
        if (game == null)
            return false;

        if (game.getPlayers().size() > MAX_PLAYERS) {
            publisher.publishEvent(limitEvent(gameCode));
            return false;
        }

        Player player = game.getPlayerById(playerId);
        if (player == null)
            return false;

        player.setReady(isReady);

        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                playerId,
                Operation.READY_UPDATED,
                gameCode,
                player.getName()));

        if (game.getPlayers().stream().allMatch(Player::isReady)
                && game.getState() == GameState.WAITING) {

            game.startCountdown();
            publisher.publishEvent(countdownEvent(gameCode, game.getCountdownStartedAt()));
        }

        return true;
    }

 public void startCountdown(String gameCode) {
    Game game = games.get(gameCode);
    if (game == null) return;

    game.setState(GameState.COUNTDOWN);
    Instant now = Instant.now();
    game.setCountdownStartedAt(now);

    publisher.publishEvent(new FrontendNachrichtEvent(
        FrontendNachrichtEvent.Nachrichtentyp.LOBBY,
        "server",
        FrontendNachrichtEvent.Operation.COUNTDOWN_STARTED,
        gameCode,
        null,
        now, null
    ));
}

    private FrontendNachrichtEvent countdownEvent(String gameCode, Instant startedAt) {
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.COUNTDOWN_STARTED,
                gameCode,
                null);
        e.setCountdownStartedAt(startedAt);
        return e;
    }

    public synchronized boolean triggerAdminStart(String gameCode, String adminId) {
        Game game = games.get(gameCode);
        if (game == null)
            return false;

        if (game.getPlayers().size() > MAX_PLAYERS) {
            publisher.publishEvent(limitEvent(gameCode));
            return false;
        }

        boolean start = game.adminStart();
        if (!start)
            return false;

        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                adminId,
                Operation.GAME_STARTED_BY_ADMIN,
                gameCode,
                null));

        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.GAME_RUNNING,
                gameCode,
                null));

        return true;
    }

    public void publishPlayerLimitEvent(String gameCode) {
        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.PLAYER_LIMIT_ERROR,
                gameCode,
                null));
    }

    public void publishReadyUpdateEvent(String gameCode, String playerId, boolean ready, String name) {
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                playerId,
                Operation.READY_UPDATED,
                gameCode,
                name);
        publisher.publishEvent(e);
    }

    public void publishCountdownStartEvent(String gameCode, Instant startedAt) {
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.COUNTDOWN_STARTED,
                gameCode,
                null);
        e.setCountdownStartedAt(startedAt);
        e.setGameState(GameState.COUNTDOWN);
        publisher.publishEvent(e);
    }

}
