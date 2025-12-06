package de.hsrm.mi.swtpr.milefiz.entities.game;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.model.GameState;

public class Game {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    // @Size(min = 1, max = )
    private Map<String, Player> playerList;
    private GameState state = GameState.WAITING;
    private Instant countdownStartedAt;

    public Game() {
        playerList = new HashMap<>();
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

}
