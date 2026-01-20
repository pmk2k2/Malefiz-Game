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
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.model.duel.Duel;

public class Game {
    private final int numberOfPlayers = 4;

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    // @Size(min = 1, max = )
    private Map<String, Player> playerList;
    private List<String> playerNumber; // Spieler1, Spieler2, 3, 4
    private GameState state = GameState.WAITING;
    private Instant countdownStartedAt;

    private Duel activeDuel;

    // temporäres Feld
    private Board board;
    // Figuren in Backend
    private List<Figure> figures = new ArrayList<>();

    /*
     * private int currentMovementAmount = 0; // Speichert die gewuerfelte Zahl
     * serverseitig
     * private String playerWhoRolledId = null; // Speichert, wer gewuerfelt hat
     */
    private String winnerId = null; // Speichert, wer Gewinner ist
    private Map<String, DiceResult> diceResults;

    // Speichert das Würfelergebnis pro Spieler-ID
    private Map<String, Integer> playerRolls = new HashMap<>();
    // Standardwert für energie falls der Host nichts einstellt
    private int maxCollectableEnergy = 10;

    private String boardName = "DummyBoard.json";
    private int cooldown = 3;

    public Game() {
        playerList = new HashMap<>();
        this.board = new Board(); // Board direkt anlegen
        diceResults = new HashMap<String, DiceResult>();
        playerNumber = Arrays.asList(new String[numberOfPlayers]);
    }

    public Map<String, Integer> getplayerRolls() {
        return playerRolls;
    }

    public void setplayerRolls(Map<String, Integer> playerRolls) {
        this.playerRolls = playerRolls;
    }

    // Prüft, ob ein Spieler schon gewürfelt hat. Wenn ja, wird die Zahl
    // zurückgegeben.
    // Wenn nicht, wird 0 zurückgegeben
    // Um zu schauen, ob der Spieler laufen darf und wie viel er laufen darf
    public int getRollForPlayer(String playerId) {
        return playerRolls.getOrDefault(playerId, 0);
    }

    // Setzt das Ergebnis für einen Spieler
    public void setRollForPlayer(String playerId, int amount) {
        if (amount == 0) {
            playerRolls.remove(playerId); // Wurf schon genutzt, deswegen Spieler entfernen
        } else {
            playerRolls.put(playerId, amount);
        }
    }

    // Hilfsmethode, um zu schauen, ob der Spieler schon gewürfelt hat
    public boolean hasPlayerRolled(String playerId) {
        return playerRolls.containsKey(playerId);
    }

    public boolean addPlayer(Player player, String playerId) {
        if (playerList.containsKey(playerId)) {
            logger.info("Player " + player.getName() + "already exist!!!!!!");
            return false;
        }
        playerList.put(playerId, player);

        // Spieler an ersten freien Slot setzen
        for (int i = 0; i < playerNumber.size(); i++) {
            logger.info("Spielerslot {} : {}", i, playerNumber.get(i));
            if (playerNumber.get(i) == null) {
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
        for (int i = 0; i < playerNumber.size(); i++) {
            if (playerNumber.get(i) != null && playerNumber.get(i).equals(playerId)) {
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

    public boolean counterStart(long requiredCountdownSeconds) {
        if (this.state != GameState.COUNTDOWN || this.countdownStartedAt == null) {
            return false;
        }
        Duration elapsed = Duration.between(this.countdownStartedAt, Instant.now());
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
        board.get(fig.getGridI(), fig.getGridJ()).addFigure(fig); // Auf Feld setzen
    }

    /*
     * public int getCurrentMovementAmount() {
     * return currentMovementAmount;
     * }
     * 
     * public void setCurrentMovementAmount(int currentMovementAmount) {
     * this.currentMovementAmount = currentMovementAmount;
     * }
     * 
     * public String getPlayerWhoRolledId() {
     * return playerWhoRolledId;
     * }
     * 
     * public void setPlayerWhoRolledId(String playerWhoRolledId) {
     * this.playerWhoRolledId = playerWhoRolledId;
     * }
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

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    private final Map<String, Boolean> movementFinished = new HashMap<>();

    public void startMovement(String playerId) {
        movementFinished.put(playerId, false);
    }

    public void finishMovement(String playerId) {
        movementFinished.put(playerId, true);
    }

    public boolean isMovementFinished(String playerId) {
        return movementFinished.getOrDefault(playerId, false);
    }

    public int getMaxCollectableEnergy() {
        return maxCollectableEnergy;
    }

    public void setMaxCollectableEnergy(int maxCollectableEnergy) {
        this.maxCollectableEnergy = maxCollectableEnergy;
    }

    public Duel getActiveDuel() {
        return activeDuel;
    }

    public void setActiveDuel(Duel duel) {
        this.activeDuel = duel;
    }

    public void removeFigure(Figure figure) {
        figures.remove(figure);
        try {
            Board b = getBoard();
            if (b != null) {
                Field f = b.get(figure.getGridI(), figure.getGridJ());
                if (f != null) {
                    f.removeFigure(figure);
                }
            }
        } catch (Exception e) {

        }
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }    
}
