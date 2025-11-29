package de.hsrm.mi.swtpr.milefiz.entities.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.board.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;

public class Game {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    // @Size(min = 1, max = )
    private Map<String, Player> playerList;

    // temporäres Feld
    private Board board;
    // Figuren in Backend
    private List<Figure> figures = new ArrayList<>();

    public Game() {
        playerList = new HashMap<>();
        this.board = new Board(); // Board direkt anlegen
    }

    public boolean addPlayer(Player player, String playerId) {
        if (playerList.containsKey(playerId)) {
            logger.info("Player " + player.getName() + "already exist!!!!!!");
            return false;
        }
        playerList.put(playerId, player);
        logger.info("The game now has players: "
                + Arrays.toString(playerList.values().stream().map(Player::getName).toArray(String[]::new)));
        return true;
    }

    public Player getPlayer(String playerId) {
        return playerList.get(playerId);
    }

    public List<Player> getPlayers() {
        return playerList.values().stream().toList();
    }

    public Player getPlayerById(String playerId) {
        return playerList.get(playerId);
    }

    public boolean removePlayer(String playerId) {
        Player removed = playerList.remove(playerId);
        logger.info("The game now has players: "
                + Arrays.toString(playerList.values().stream().map(Player::getName).toArray(String[]::new)));
        return removed != null;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public List <Figure> getFigures () {
        return figures;
    }

    public void addFigure(Figure fig) {
        figures.add(fig);
        board.get(fig.getI(), fig.getJ()).addFigure(fig);   // Auf Feld setzen
    }


}
