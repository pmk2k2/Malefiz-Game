package de.hsrm.mi.swtpr.milefiz.entities.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swtpr.milefiz.entities.player.Player;

public class Game {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    // @Size(min = 1, max = )
    private List<Player> playerList;

    public Game() {
        playerList = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        playerList.add(player);
        logger.info("The game now has players: "
                + Arrays.toString(playerList.stream().map(Player::getName).toArray(String[]::new)));
    }

    public Player getPlayer(String name) {
        for (Player p : playerList) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public List<Player> getPlayers() {
        return playerList;
    }

}
