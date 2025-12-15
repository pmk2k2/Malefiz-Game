package de.hsrm.mi.swtpr.milefiz.entities.game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.model.GameState;

public class Game {
    private final int numberOfPlayers = 4;

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    // @Size(min = 1, max = )
    private Map<String, Player> playerList;
    private List<String> playerNumber; // Spieler1, Spieler2, 3, 4
    private GameState state = GameState.WAITING;
    private Instant countdownStartedAt;

    // temporäres Feld
    private Board board;
    // Figuren in Backend
    private List<Figure> figures = new ArrayList<>();

    /*
    private int currentMovementAmount = 0; // Speichert die gewuerfelte Zahl serverseitig
    private String playerWhoRolledId = null; // Speichert, wer gewuerfelt hat
    */
    private Map<String, DiceResult> diceResults;

    public Game() {
        playerList = new HashMap<>();
        this.board = new Board(); // Board direkt anlegen
        diceResults = new HashMap<String, DiceResult>();
        playerNumber = Arrays.asList(new String[numberOfPlayers]);  
    }

    public boolean addPlayer(Player player, String playerId) {
        if (playerList.containsKey(playerId)) {
            logger.info("Player " + player.getName() + "already exist!!!!!!");
            return false;
        }
        playerList.put(playerId, player);

        // Spieler an ersten freien Slot setzen
        for(int i = 0; i < playerNumber.size(); i++) {
            logger.info("Spielerslot {} : {}", i, playerNumber.get(i));
            if(playerNumber.get(i) == null) {
                playerNumber.set(i, playerId);
                break;
            }
        }
        logger.info("Spielerslot {} : {}", 1, playerNumber.get(0));
        logger.info("Spielerslot {} : {}", 2, playerNumber.get(1));
        logger.info("Spielerslot {} : {}", 3, playerNumber.get(2));
        logger.info("Spielerslot {} : {}", 4, playerNumber.get(3));

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

        // Spieler aus playerNumber rausnehmen
        for(int i = 0; i < playerNumber.size(); i++) {
            if(playerNumber.get(i).equals(playerId)) {
                playerNumber.set(i, null);
            }
        }

        return removed != null;
    }

    public GameState getState() {
        return state;
    }

    public Instant getCountdownStartedAt() {
        return countdownStartedAt;
    }

    public void startCountdown() {
        this.state = GameState.COUNTDOWN;
        this.countdownStartedAt = Instant.now();
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void setCountdownStartedAt(Instant countdownStartedAt) {
        this.countdownStartedAt = countdownStartedAt;
    }

    public boolean adminStart() {
        if (playerList.size() > 4)
            return false;
        this.state = GameState.RUNNING;
        return true;
    }
    public boolean counterStart(long requiredCountdownSeconds){
    if (this.state != GameState.COUNTDOWN || this.countdownStartedAt == null) {
        return false;
    }

    Instant now = Instant.now();
    Duration elapsed = Duration.between(this.countdownStartedAt, now);

    if (elapsed.getSeconds() >= requiredCountdownSeconds) {
        this.state = GameState.RUNNING;
        logger.info("Game started after countdown.");
        return true;
    }
    
    return false;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public List<Figure> getFigures() {
        return figures;
    }

    public void addFigure(Figure fig) {
        figures.add(fig);
        board.get(fig.getGridI(), fig.getGridJ()).addFigure(fig);   // Auf Feld setzen
    }

    /* 
    public int getCurrentMovementAmount() {
        return currentMovementAmount;
    }

    public void setCurrentMovementAmount(int currentMovementAmount) {
        this.currentMovementAmount = currentMovementAmount;
    }

    public String getPlayerWhoRolledId() {
        return playerWhoRolledId;
    }

    public void setPlayerWhoRolledId(String playerWhoRolledId) {
        this.playerWhoRolledId = playerWhoRolledId;
    }
    */

    public void updateDiceResult(String playerName, DiceResult result) {
        diceResults.put(playerName, result);
    }

    public DiceResult getDiceResultByName(String playerName) {
        return diceResults.get(playerName);
    }

    public DiceResult getDiceResultById(String playerId) {
        String playerName = getPlayerById(playerId).getName();
        return diceResults.get(playerName);
    }

    public List<String> getPlayerNumber() {
        return playerNumber;
    }
}
