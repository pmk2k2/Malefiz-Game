package de.hsrm.mi.swtpr.milefiz.service;

import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests für die Bewegungslogik
/** Hinzugefügt von Pawel
 * #48 Bewegung durch Würfel:
 * Tests zur Validierung der Abhängigkeit der Bewegung von der Würfelzahl.
 */
class MovementLogicServiceTest {

    private Game game;
    private MovementLogicService movement;
    private Figure figure;
    
    // Konstanten für IDs
    private final String PLAYER_ID = "P1";
    private final String FIGURE_ID = "F1";

    @BeforeEach
    void setup() {
        game = new Game();
        movement = new MovementLogicService();

        figure = new Figure(FIGURE_ID, PLAYER_ID, "red", 0, 0);
        game.addFigure(figure);
    }

    /**
     * Hilfsmethode: Erstellt einen Request
     */
    private FigureMoveRequest request(int toI, int toJ) {
        FigureMoveRequest r = new FigureMoveRequest();
        r.playerId = PLAYER_ID;
        r.figureId = FIGURE_ID;
        r.toI = toI;
        r.toJ = toJ;
        return r;
    }

    /**
     * Hilfsmethode: Simuliert einen passenden Würfelwurf für den Test.
     */
    private void simulateDiceRoll(int amount) {
        game.setCurrentMovementAmount(amount);
        game.setPlayerWhoRolledId(PLAYER_ID);
    }

    /**
     * #48 Bewegung durch Würfel:
     * Testet, dass eine Bewegung abgelehnt wird, wenn zuvor nicht gewürfelt wurde.
     */
    @Test
    void testMove_WithoutDiceRoll_Fails() {
        // Niemand hat gewürfelt (currentMovementAmount = 0)
        game.setCurrentMovementAmount(0);

        FigureMoveResult result = movement.moveFigure(game, request(1, 0));

        assertFalse(result.success);
        assertEquals("Du musst erst würfeln!", result.message);
    }

    /**
     * #48 Bewegung durch Würfel:
     * Testet, dass der Würfelwert nach einem erfolgreichen Zug zurückgesetzt (verbraucht) wird.
     */
    @Test
    void testMove_ResetsDice_AfterSuccess() {
        // Simuliere Würfel: 1
        simulateDiceRoll(1);

        // 1. Zug (Gültig)
        assertTrue(movement.moveFigure(game, request(1, 0)).success);

        // 2. Zug (Sofort danach nochmal versuchen -> Sollte scheitern, da Wurf verbraucht)
        assertFalse(movement.moveFigure(game, request(2, 0)).success);
        assertEquals(0, game.getCurrentMovementAmount(), "Würfel muss nach Zug auf 0 gesetzt werden");
    }

    /**
     * #48 Bewegung durch Würfel:
     * Testet, dass Bewegung fehlschlägt, wenn die Distanz größer als der Würfelwurf ist.
     */
    @Test
    void testMove_TooFar_Fails() {
        simulateDiceRoll(1);

        // Versuche 3 Felder zu gehen
        FigureMoveResult result = movement.moveFigure(game, request(3, 0));

        assertFalse(result.success);
        assertTrue(result.message.contains("genau 1 Felder"), "Sollte fehlschlagen, da Distanz nicht passt");
    }

    
    @Test
    void testMoveOutsideBoard_NotAllowed() {
        simulateDiceRoll(1); 
        FigureMoveResult result = movement.moveFigure(game, request(-1, 0));
        assertFalse(result.success);
        assertEquals("Bewegung außerhalb Spielfelds ist nicht erlaubt", result.message);
    }
    
    @Test
    void testMoveOntoBlockedField_NotAllowed() {
        simulateDiceRoll(1);

        FigureMoveResult result = movement.moveFigure(game, request(0, 1));
        
        assertFalse(result.success);
        assertEquals("Figur kann auf kein gesperrtes Feld", result.message);
    }

    @Test
    void testDiagonalMovementForbidden() {
        // Distanz nach (1,2) ist 3 (|1| + |2|). Würfel muss passen, damit Check bis Diagonal kommt.
        simulateDiceRoll(3);

        FigureMoveResult result = movement.moveFigure(game, request(1, 2));
        
        assertFalse(result.success);
        assertEquals("Diagonal verboten", result.message);
    }

    @Test
    void testJumpOverBlockedField_Forbidden() {
        // (0,0) -> (0,2) sind 2 Schritte
        // mittleres Feld = (0,1) = BLOCKED
        simulateDiceRoll(2);

        FigureMoveResult result = movement.moveFigure(game, request(0, 2));
        
        assertFalse(result.success);
        assertEquals("Überspringen von blockierten Felder ist verboten", result.message);
    }

    @Test
    void testMoveToFreeField_Succeeds() {
        // (0,0) -> (1,0) = gleiche Zeile, PATH - erlaubt
        simulateDiceRoll(1);

        FigureMoveResult result = movement.moveFigure(game, request(1, 0));
        
        assertTrue(result.success);
        assertEquals("OK", result.message);
        assertEquals(1, figure.getGridI());
        assertEquals(0, figure.getGridJ());
    }

@Test
    void testTwoFiguresAllowedOnSameField() {
        // Wir platzieren die ZWEITE Figur auf (1,0), damit sie sich bewegen muss
        Figure fig2 = new Figure("F2", PLAYER_ID, "blue", 1, 0);
        game.addFigure(fig2);
        
        // Wir simulieren, dass eine 1 gewürfelt wurde
        simulateDiceRoll(1); 
        
        // Wir bewegen fig2 (von 1,0) auf das Feld von fig1 (0,0)
        FigureMoveRequest r = request(0,0);
        r.figureId = "F2"; 
        
        FigureMoveResult result = movement.moveFigure(game, r);
        assertTrue(result.success);
    }

    @Test
    void testThirdFigureMakesFieldBlocked() {
        // zwei Figuren schon da
        Figure fig2 = new Figure("F2", PLAYER_ID, "a", 1, 0);
        Figure fig3 = new Figure("F3", PLAYER_ID, "b", 1, 0);
        game.addFigure(fig2);
        game.addFigure(fig3);

        // Unsere Hauptfigur (F1) steht auf (0,0) und will nach (1,0). Distanz 1.
        simulateDiceRoll(1);
        
        // jetzt versucht Figur 1 auf das Feld zu gehen -> Ablehnung
        FigureMoveResult result = movement.moveFigure(game, request(1, 0));
        assertFalse(result.success);
        assertEquals("Maximal 2 Figuren pro Feld", result.message);
    }

    @Test
    void testMoveFigureWrongPlayer_NotAllowed() {
        simulateDiceRoll(1);
        FigureMoveRequest r = request(1, 0);
        r.playerId = "OTHER";

        FigureMoveResult result = movement.moveFigure(game, r);
        assertFalse(result.success);
    }
}