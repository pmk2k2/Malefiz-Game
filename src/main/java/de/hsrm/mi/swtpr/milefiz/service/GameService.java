package de.hsrm.mi.swtpr.milefiz.service;

import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.controller.dto.FigureDto;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.exception.GameNotFoundException;
import de.hsrm.mi.swtpr.milefiz.exception.GameStartException;
import de.hsrm.mi.swtpr.milefiz.exception.NotHostException;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Nachrichtentyp;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Operation;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtService;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private Map<String, Game> games = new HashMap<>();
    private CodeGeneratorService codeService;
    private final int MAX_PLAYERS = 4;
    private final long COUNTDOWN_DURATION_SECONDS = 10; // Countdown Dauer in Sekunden
    private ApplicationEventPublisher publisher;

    @Autowired
    private BoardService boardService;

    // Farben für Spieler
    private static final List<String> PLAYER_COLORS = List.of(
            "#ff0000",
            "#ffff00",
            "#0000ff",
            "#00ff00");

    public GameService(CodeGeneratorService codeService, ApplicationEventPublisher publisher) {
        this.codeService = codeService;
        this.publisher = publisher;
    }

    public String createGame() {
        String gameCode = codeService.generateCode();
        Game newGame = new Game();
        newGame.setBoard(boardService.getBoardFromJson("DummyBoard.json"));
        games.put(gameCode, newGame);

        logger.info("All the games: {}", Arrays.toString(games.keySet().toArray()));
        return gameCode;
    }

    public boolean addPlayer(String gameCode, String playerId, String name, boolean isHost) {
        Game game = games.get(gameCode);
        if (game == null || game.getPlayers().size() >= PLAYER_COLORS.size()) {
            if (game != null && game.getPlayers().size() >= PLAYER_COLORS.size()) {
                logger.error("Max Anzahl von Spielern erreicht.");
                publishPlayerLimitEvent(gameCode);
            }
            return false;
        }

        // Prevent duplicate playerId
        if (game.getPlayerById(playerId) != null) {
            logger.warn("Player with id {} already exists in game {}", playerId, gameCode);
            return false;
        }

        // if player reconnect, cancel remove figure and timestamp
        Timer t = scheduledFigureRemovals.remove(playerId);
        if (t != null)
            t.cancel();
        playerRemovalTimestamps.remove(playerId);

        String assignedColor = PLAYER_COLORS.get(game.getPlayers().size());
        Player player = new Player(name, playerId, isHost, assignedColor);
        player.setReady(false);

        boolean playerAdded = game.addPlayer(player, playerId);
        if (!playerAdded) {
            return false;
        }
        // LOBBY EVENT
        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                playerId,
                Operation.JOINED,
                gameCode,
                name));

        return true;
    }

    //schedule game remove
    private final Map<String, Timer> gameRemovalTimers = new HashMap<>();

    // schedule player remove
    private final Map<String, Long> playerRemovalTimestamps = new HashMap<>();
    // schedule figure remove
    private final Map<String, Timer> scheduledFigureRemovals = new HashMap<>();
    private static final long FIGURE_REMOVAL_GRACE_PERIOD_MS = 5_000;

    public boolean removePlayer(String gameCode, String playerId) {
        Game game = games.get(gameCode);
        if (game == null) {
            return false;
        }

        Player removedPlayer = game.getPlayerById(playerId);
        if (removedPlayer == null) {
            return false;
        }

        boolean wasHost = removedPlayer.isHost();
        boolean removed = game.removePlayer(playerId);
        if (!removed) {
            return false;
        }

        // time when player removed to grace period for figure
        playerRemovalTimestamps.put(playerId, System.currentTimeMillis());

        // schedual remove figure after 5s if player nott reconnected
        Timer figureTimer = new Timer();
        scheduledFigureRemovals.put(playerId, figureTimer);
        figureTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // if player not reconneted after 5s-> remove figure
                if (game.getPlayerById(playerId) == null) {
                    int before = game.getFigures().size();
                    List<Figure> toRemove = new ArrayList<>();
                    for (Figure f : game.getFigures()) {
                        if (playerId.equals(f.getOwnerPlayerId())) {
                            toRemove.add(f);
                        }
                    }
                    for (Figure f : toRemove) {
                        game.removeFigure(f);
                    }
                    int after = game.getFigures().size();
                    logger.info("Removed {} figures of player {} from game {}", (before - after), playerId, gameCode);
                    playerRemovalTimestamps.remove(playerId);
                    // notify frontend to refetch figures
                    publisher.publishEvent(
                            new FrontendNachrichtEvent(
                                    FrontendNachrichtEvent.Nachrichtentyp.INGAME,
                                    playerId,
                                    FrontendNachrichtEvent.Operation.READY_UPDATED,
                                    gameCode,
                                    null));
                }
                scheduledFigureRemovals.remove(playerId);
            }
        }, FIGURE_REMOVAL_GRACE_PERIOD_MS);

        // Stop countdown, wenn ein Spieler gekickt wurde während des Countdowns
        if (game.getState() == GameState.COUNTDOWN) {
            stopCountdown(gameCode);
            publishCountdownAbortEvent(gameCode);
        }

        // LOBBY EVENT
        publisher.publishEvent(new FrontendNachrichtEvent(
                FrontendNachrichtEvent.Nachrichtentyp.LOBBY,
                playerId,
                FrontendNachrichtEvent.Operation.LEFT,
                gameCode,
                removedPlayer.getName()));

        List<Player> players = game.getPlayers();

        // Wenn niemand mehr da -> Spiel nach 20s löschen (nicht sofort)
        // websocket disconnect when lobby-gameview -> remove player if disconnect make
        // player list empty-> remove gamecode, so we have to wait about 20s till
        // webcket reconnected and player rejoin
        if (players.isEmpty()) {
            if (!gameRemovalTimers.containsKey(gameCode)) {
                Timer timer = new Timer();
                gameRemovalTimers.put(gameCode, timer);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Game g = games.get(gameCode);
                        if (g != null && g.getPlayers().isEmpty()) {
                            games.remove(gameCode);
                            logger.info("Game {} removed (no players left) after 20s grace period.", gameCode);
                        }
                        gameRemovalTimers.remove(gameCode);
                    }
                }, 20_000);
                logger.info("Scheduled removal for game {} in 20s (no players left).", gameCode);
            }
            return true;
        } else {
            Timer t = gameRemovalTimers.remove(gameCode);
            if (t != null) {
                t.cancel();
                logger.info("Cancelled scheduled removal for game {} (player rejoined).", gameCode);
            }
        }

        // Host neu bestimmen
        if (wasHost && !players.isEmpty()) {
            players.get(0).setHost(true);
            logger.info("New host: {}", players.get(0).getName());
        }

        return true;
    }
    // Redundant ?
    /*
     * public synchronized boolean setPlayerReady(String gameCode, String playerId,
     * boolean isReady) {
     * Game game = games.get(gameCode);
     * if (game == null)
     * return false;
     * 
     * Player player = game.getPlayerById(playerId);
     * if (player == null)
     * return false;
     * 
     * player.setReady(isReady);
     * 
     * // Prüfen, ob alle Ready sind
     * boolean allReady = game.getPlayers().size() > 0 &&
     * game.getPlayers().stream().allMatch(Player::isReady);
     * 
     * if (allReady) {
     * // Countdown starten + WS Event an alle Spieler senden
     * startCountdown(gameCode);
     * }
     * return true;
     * }
     */

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

        if (!isReady) {
            stopCountdown(gameCode);
            // Informiere Frontend, dass der Countdown abgebrochen wurde
            publishCountdownAbortEvent(gameCode);
        }

        publishReadyUpdateEvent(gameCode, playerId, isReady, player.getName());

        if (game.getPlayers().stream().allMatch(Player::isReady) && game.getState() == GameState.WAITING) {
            startCountdown(gameCode);
        }

        return true;
    }

    public void startOrTriggerGame(String gameCode, String playerId)
            throws GameNotFoundException, NotHostException, GameStartException {
        Game game = games.get(gameCode);

        if (game == null) {
            throw new GameNotFoundException("Spiel nicht gefunden.");
        }

        Player callingPlayer = game.getPlayerById(playerId);
        // Prüfe Host
        if (callingPlayer == null || !callingPlayer.isHost()) {
            throw new NotHostException("Nur der Host darf das Spiel starten.");
        }
        // Start triggern
        boolean success = triggerAdminStart(gameCode, playerId) || triggerCounterStart(gameCode, playerId);

        if (!success) {
            String issue = (game.getState() != GameState.COUNTDOWN)
                    ? "Spiel ist nicht im Zustand -Countdown-; " + game.getState()
                    : "Unbekannter Fehler beim Starten.";
            throw new GameStartException("Spiel konnte nicht gestartet werden: " + issue);
        }
    }

    public void startCountdown(String gameCode) {
        Game game = games.get(gameCode);
        if (game == null || game.getState() != GameState.WAITING)
            return;

        game.setState(GameState.COUNTDOWN);
        Instant now = Instant.now();
        game.setCountdownStartedAt(now);

        publishCountdownStartEvent(gameCode, now);
    }

    public void stopCountdown(String gameCode) {
        Game game = games.get(gameCode);
        if (game == null || game.getState() != GameState.COUNTDOWN)
            return;

        game.setState(GameState.WAITING);
        game.setCountdownStartedAt(null);

        publishCountdownAbortEvent(gameCode);
    }

    public synchronized boolean triggerAdminStart(String gameCode, String adminId) {
        Game game = games.get(gameCode);
        if (game == null || game.getState() != GameState.COUNTDOWN)
            return false;

        if (game.getPlayers().size() > MAX_PLAYERS) {
            publisher.publishEvent(limitEvent(gameCode));
            return false;
        }

        // Reset steps for all players before starting
        resetPlayerSteps(game);

        boolean start = game.adminStart();
        if (!start)
            return false;

        if (!initialiatizeFigures(game))
            return false;

        game.setState(GameState.RUNNING);
        publishGameRunningEvent(gameCode);
        return true;
    }

    public synchronized boolean triggerCounterStart(String gameCode, String adminId) {
        Game game = games.get(gameCode);
        if (game == null || game.getState() != GameState.COUNTDOWN)
            return false;

        if (game.getPlayers().size() == MAX_PLAYERS) {
            publisher.publishEvent(limitEvent(gameCode));
            return false;
        }

        // Reset steps for all players before starting
        resetPlayerSteps(game);

        boolean start = game.counterStart(COUNTDOWN_DURATION_SECONDS);
        if (!start)
            return false;

        if (!initialiatizeFigures(game))
            return false;

        game.setState(GameState.RUNNING);
        publishGameRunningEvent(gameCode);
        return true;
    }

    // Helper to reset steps for all players
    private void resetPlayerSteps(Game game) {
        for (Player p : game.getPlayers()) {
            p.setStepsTaken(0);
            p.setRemainingSteps(0);
            p.setTotalSteps(0);
        }
    }

    private boolean initialiatizeFigures(Game game) {
        if (game.getFigures().isEmpty() == false)
            return false;

        final int FIGURES_PER_PLAYER = 5;
        // Problem: 0|0 kann gueltiges Feld sein
        final int START_I = 0;
        final int START_J = 0;

        for (Player player : game.getPlayers()) {
            String playerId = player.getId();
            String color = player.getColor();

            for (int i = 1; i <= FIGURES_PER_PLAYER; i++) {
                String figureId = playerId + "-fig-" + i;
                Figure newFigure = new Figure(figureId, playerId, color, START_J, START_I);
                newFigure.setOnField(false);
                game.addFigure(newFigure);
            }
        }

        logger.info("Spiel {} gestartet: {} Figuren für {} Spieler",
                game.getFigures().size(), game.getPlayers().size());

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

    private void publishGameRunningEvent(String gameCode) {
        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.GAME_RUNNING,
                gameCode,
                null));
    }

    public void publishPlayerLimitEvent(String gameCode) {
        publisher.publishEvent(limitEvent(gameCode));
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
        e.setCountdownDurationSeconds(COUNTDOWN_DURATION_SECONDS);
        publisher.publishEvent(e);
    }

    public void publishCountdownAbortEvent(String gameCode) {
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.COUNTDOWN_ABORTED,
                gameCode,
                null);
        publisher.publishEvent(e);
    }

    public Game getGame(String code) {
        return games.get(code);
    }

    public List<FigureDto> getFiguresasDto(String gameCode) {
        Game game = getGame(gameCode);
        if (game == null) {
            return Collections.emptyList();
        }

        // only return figure if player in game or disconnected < 5s
        return game.getFigures().stream()
                .filter(f -> {
                    String pid = f.getOwnerPlayerId();
                    if (game.getPlayerById(pid) != null)
                        return true;
                    Long removedAt = playerRemovalTimestamps.get(pid);
                    if (removedAt == null)
                        return false;
                    return (System.currentTimeMillis() - removedAt) < FIGURE_REMOVAL_GRACE_PERIOD_MS;
                })
                .map(f -> new FigureDto(f.getId(), f.getColor(), f.getOwnerPlayerId(), f.getOrientation(), f.getGridI(),
                        f.getGridJ()))
                .collect(Collectors.toList());
    }

    // spieleinstwllungen aktualisieren
    public void updateGameSettings(String gameCode, String playerId, int maxEnergy) throws NotHostException {
        Game game = games.get(gameCode);
        if (game == null)
            return;

        // Nur der Host darf die Einstellungen ändern
        Player player = game.getPlayerById(playerId);
        if (player == null || !player.isHost()) {
            throw new NotHostException("Nur der Host kann Einstellungen ändern.");
        }

        game.setMaxCollectableEnergy(maxEnergy);

        // Informiere Frontend über die eingestellte Energie
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                "server",
                Operation.READY_UPDATED,
                gameCode,
                null);
        publisher.publishEvent(e);
    }

    public void publischBarrierWaitEvent(String gameCode, String playerId) {
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, playerId, Operation.BARRIER_WAIT,
                gameCode, null);
        e.setGameState(GameState.BARRIER_PLACEMENT);
        publisher.publishEvent(e);
    }

    public void publishBarrierPlacedEvent(String gameCode) {
        FrontendNachrichtEvent e = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, "server", Operation.BARRIER_PLACED,
                gameCode, null);
        e.setGameState(GameState.RUNNING);
        publisher.publishEvent(e);
    }

    public Set<String> getAllGameCodes() {
        return games.keySet();
    }
}
