package de.hsrm.mi.swtpr.milefiz.service;

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
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Nachrichtentyp;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Operation;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final Map<String, Game> games = new HashMap<>();
    private final CodeGeneratorService codeService;
    private final ApplicationEventPublisher publisher;

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
        if (game == null) {
            return false;
        }

        // Farbe zuweisen
        int colorIndex = game.getPlayers().size();
        if (colorIndex >= PLAYER_COLORS.size()) {
            logger.error("Max Anzahl von Spielern erreicht.");
            return false;
        }
        String assignedColor = PLAYER_COLORS.get(colorIndex);

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

        // LOBBY EVENT
        publisher.publishEvent(new FrontendNachrichtEvent(
                Nachrichtentyp.LOBBY,
                playerId,
                Operation.LEFT,
                gameCode,
                removedPlayer.getName()));

        List<Player> players = game.getPlayers();

        // Wenn niemand mehr da -> Spiel löschen
        if (players.isEmpty()) {
            games.remove(gameCode);
            logger.info("Game {} removed (no players left).", gameCode);
            return true;
        }

        // Host neu bestimmen
        if (wasHost) {
            players.get(0).setHost(true);
            logger.info("New host: {}", players.get(0).getName());
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

    public boolean startGame(String gameCode) {
        Game game = games.get(gameCode);

        // Spiel existiert nicht ODER Figuren wurden bereits erstellt
        if (game == null || !game.getFigures().isEmpty()) {
            return false;
        }

        final int FIGURES_PER_PLAYER = 5;
        final int START_I = 5;
        final int START_J = 2;

        for (Player player : game.getPlayers()) {
            String playerId = player.getId();
            String color = player.getColor();

            for (int i = 1; i <= FIGURES_PER_PLAYER; i++) {
                String figureId = playerId + "-fig-" + i;
                Figure newFigure = new Figure(figureId, playerId, color, START_J, START_I);
                game.addFigure(newFigure);
            }
        }

        logger.info("Spiel {} gestartet: {} Figuren für {} Spieler",
                gameCode, game.getFigures().size(), game.getPlayers().size());

        return true;
    }

    public Game getGame(String code) {
        return games.get(code);
    }

    public List<FigureDto> getFiguresasDto(String gameCode) {
        Game game = getGame(gameCode);
        if (game == null) {
            return Collections.emptyList();
        }

        return game.getFigures().stream()
            .map(f -> new FigureDto(f.getId(), f.getColor(), f.getOwnerPlayerId(), f.getOrientation(), f.getGridI(), f.getGridJ()))
            .collect(Collectors.toList());
    }
}
