package de.hsrm.mi.swtpr.milefiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.when;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private ApplicationEventPublisher publisher; 

    @Mock
    private CodeGeneratorService codeGeneratorService;

    @InjectMocks
    private GameService gameService;


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

        boolean added = gameService.addPlayer(code, "p1", "Hoang", true, false);

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
        boolean added = gameService.addPlayer("UNKNOWN", "p1", "Ramy", false, false);

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

        boolean first = gameService.addPlayer(code, "p1", "Hoang", true, false);
        boolean second = gameService.addPlayer(code, "p1", "Ramy", false, false);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        Game game = gameService.getGame(code);
        assertThat(game.getPlayers()).hasSize(1);
        assertThat(game.getPlayers().get(0).getName()).isEqualTo("Hoang");
    }
}