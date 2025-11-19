package de.hsrm.mi.swtpr.milefiz.entities.game;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swtpr.milefiz.entities.player.Player;

public class Game {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    // @Size(min = 1, max = )
    private Map<String, Player> playerList;

    public Game() {
        playerList = new HashMap<>();
    }

    public boolean addPlayer(Player player, String playerId) {
        if (playerList.containsKey(playerId)) {
            logger.info("Player" + player.getName() + "already exist!!!!!!");
            return false;
        }
        playerList.put(playerId, player);
        logger.info("The game now has players: "
                + Arrays.toString(playerList.values().stream().map(Player::getName).toArray(String[]::new)));
        return true;
    }

    public Player getPlayer(String name) {
        for (Player p : playerList.values()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public List<Player> getPlayers() {
        return playerList.values().stream().toList();
    }

    public boolean removePlayer(String playerId) {
        Player removed = playerList.remove(playerId);
        logger.info("The game now has players: "
                + Arrays.toString(playerList.values().stream().map(Player::getName).toArray(String[]::new)));
        return removed != null;
    }

}
