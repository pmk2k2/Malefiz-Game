package de.hsrm.mi.swtpr.milefiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;

class GameServiceTest {

    private CodeGeneratorService codeGeneratorService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        codeGeneratorService = Mockito.mock(CodeGeneratorService.class);
        gameService = new GameService(codeGeneratorService, null);
    }

    /**
     * #21 Raum/Spiel erstellen:
     * createGame() muss neuen Game-Eintrag mit generiertem Code anlegen.
     */
    @Test
    void createGame_createsGameAndReturnsCode() {
        when(codeGeneratorService.generateCode()).thenReturn("ABCD");

        String code = gameService.createGame();

        assertThat(code).isEqualTo("ABCD");
        Game game = gameService.getGame(code);
        assertThat(game).isNotNull();
        assertThat(game.getPlayers()).isEmpty();
    }

    /**
     * #21 Raum/Spiel erstellen:
     * addPlayer() mit gültigem Code fügt Spieler hinzu.
     */
    @Test
    void addPlayer_validGameCode_addsPlayer() {
        when(codeGeneratorService.generateCode()).thenReturn("ABCD");
        String code = gameService.createGame();

        boolean added = gameService.addPlayer(code, "p1", "Hoang", true);

        assertThat(added).isTrue();
        Game game = gameService.getGame(code);
        assertThat(game.getPlayers()).hasSize(1);
        Player p = game.getPlayers().get(0);
        assertThat(p.getName()).isEqualTo("Hoang");
    }

    /**
     * #21 Raum/Spiel beitreten:
     * addPlayer() mit ungültigem Game-Code schlägt fehl.
     */
    @Test
    void addPlayer_invalidGameCode_returnsFalse() {
        boolean added = gameService.addPlayer("UNKNOWN", "p1", "Ramy", false);

        assertThat(added).isFalse();
    }

    /**
     * #21 / #20:
     * Derselbe playerId darf in einem Spiel nicht doppelt vorkommen.
     */
    @Test
    void addPlayer_samePlayerIdInSameGame_returnsFalseForSecond() {
        when(codeGeneratorService.generateCode()).thenReturn("ABCD");
        String code = gameService.createGame();

        boolean first = gameService.addPlayer(code, "p1", "Hoang", true);
        boolean second = gameService.addPlayer(code, "p1", "Ramy", false);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        Game game = gameService.getGame(code);
        assertThat(game.getPlayers()).hasSize(1);
        assertThat(game.getPlayers().get(0).getName()).isEqualTo("Hoang");
    }
}