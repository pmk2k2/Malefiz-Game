package de.hsrm.mi.swtpr.milefiz.service;

import de.hsrm.mi.swtpr.milefiz.entities.board.Barrier;
import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

// Tests für die Bewegungslogik
/**
 * Hinzugefügt von Pawel
 * #48 Bewegung durch Würfel:
 * Tests zur Validierung der Abhängigkeit der Bewegung von der Würfelzahl.
 * #81 Barriere Bewegungslogik:
 * Tests zur Bewegung auf barrieren und Validierung der Stop Logik vor die
 * barriere
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
        movement = new MovementLogicService();
        game = new Game();

        Field[][] grid = new Field[][] {
                { new Field(0, 0, CellType.PATH), new Field(1, 0, CellType.PATH), new Field(2, 0, CellType.PATH) },
                { new Field(0, 1, CellType.BLOCKED), new Field(1, 1, CellType.PATH), new Field(2, 1, CellType.PATH) },
                { new Field(0, 2, CellType.PATH), new Field(1, 2, CellType.PATH), new Field(2, 2, CellType.PATH) },
                { new Field(0, 3, CellType.PATH), new Field(1, 3, CellType.PATH), new Field(2, 3, CellType.GOAL) }
        };

        game.setBoard(new Board(3, 4, grid));

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
     * Testet, dass der Würfelwert nach einem erfolgreichen Zug zurückgesetzt
     * (verbraucht) wird.
     */
    @Test
    void testMove_ResetsDice_AfterSuccess() {
        // Simuliere Würfel: 1
        simulateDiceRoll(1);

        // 1. Zug (Gültig)
        assertTrue(movement.moveFigure(game, request(1, 0)).success);

        // 2. Zug (Sofort danach nochmal versuchen -> Sollte scheitern, da Wurf
        // verbraucht)
        assertFalse(movement.moveFigure(game, request(2, 0)).success);
        assertEquals(0, game.getCurrentMovementAmount(), "Würfel muss nach Zug auf 0 gesetzt werden");
    }

    /**
     * #48 Bewegung durch Würfel:
     * Testet, dass Bewegung fehlschlägt, wenn die Distanz größer als der Würfelwurf
     * ist.
     */
    @Test
    void testMove_TooFar_Fails() {
        simulateDiceRoll(1);

        // Versuche 3 Felder zu gehen
        FigureMoveResult result = movement.moveFigure(game, request(2, 0));

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

    // Test ist fehlgeschlagen wegen Barrieren Logik #81 Barriere Bewegungslogik
    // Angepasst damit man nicht auf BLOCKED Felder laufen kann, sondern davor
    // stehen bleibt
    @Test
    void testMoveOntoBlockedField_StopsBefore() {
        simulateDiceRoll(1);

        FigureMoveResult result = movement.moveFigure(game, request(0, 1));

        assertTrue(result.success);

        assertEquals(0, figure.getGridI());
        assertEquals(0, figure.getGridJ());

        assertEquals(1, game.getCurrentMovementAmount());
    }

    @Test
    void testDiagonalMovementForbidden() {
        // Distanz nach (1,2) ist 3 (|1| + |2|). Würfel muss passen, damit Check bis
        // Diagonal kommt.
        simulateDiceRoll(3);

        FigureMoveResult result = movement.moveFigure(game, request(1, 2));

        assertFalse(result.success);
        assertEquals("Diagonal verboten", result.message);
    }

    // Test ist wegen Barrieren Logik #81 Barriere Bewegungslogik fehlgeschlagen
    // Test angepasst
    @Test
    void testJumpOverBlockedField_StopsBefore() {
        // (0,0) -> (0,2) sind 2 Schritte
        // mittleres Feld = (0,1) = BLOCKED
        simulateDiceRoll(2);

        FigureMoveResult result = movement.moveFigure(game, request(0, 2));

        // Spring über 0,1(BLOCKED)
        assertTrue(result.success);
        assertEquals(0, figure.getGridI());
        assertEquals(0, figure.getGridJ());
        assertEquals(2, game.getCurrentMovementAmount());
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
        FigureMoveRequest r = request(0, 0);
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

        game.getBoard().get(1, 0).addFigure(fig2);
        game.getBoard().get(1, 0).addFigure(fig3);

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

    // Barrieren Logik Tests #81 Barriere Bewegungslogik
    // Test mit den BLOCKED Feldern
    @Test
    void testMoveTowardsBlockedField_StopsBefore() {
        // Figur ist auf 0,0
        simulateDiceRoll(2);

        FigureMoveResult result = movement.moveFigure(game, request(0, 2));

        assertTrue(result.success);

        // Figur muss immer noch auf 0,0 stehen, weil 0,1 blockiert ist
        assertEquals(0, figure.getGridI());
        assertEquals(0, figure.getGridJ());

        // Wir haben 0 Schritte gemacht. 2 - 0 = 2 Restenergie
        assertEquals(2, game.getCurrentMovementAmount());
        // Spieler bleibt dran
        // Später kann man eine Option einbauen, dass der Spieler die laufenergie in
        // Sprungenergie umwandeln kann
        assertEquals(PLAYER_ID, game.getPlayerWhoRolledId());
    }

    // Test mit den barrieren
    @Test
    void testMoveTowardsBarrier_WithTooMuchEnergy_StopsBefore() {
        // Setup: Barriere auf (2,0) platzieren. (1,0) ist frei.
        game.getBoard().get(2, 0).setBarrier(new Barrier(2, 0));

        // Würfel: 3 (Zu viel für die Distanz 2)
        simulateDiceRoll(3);

        // Versuch: Wir klicken auf die Barriere (2,0).
        FigureMoveResult result = movement.moveFigure(game, request(2, 0));

        // Erwartung: Success, aber Stop vor der Barriere.
        assertTrue(result.success);

        // Figur muss auf (1,0) stehen (das Feld vor der Barriere).
        assertEquals(1, figure.getGridI());
        assertEquals(0, figure.getGridJ());

        // Energie: 3 gewürfelt - 1 Schritt gemacht = 2 Rest.
        assertEquals(2, game.getCurrentMovementAmount());
    }

    @Test
    void testLandOnBarrier_ExactDistance_Allowed() {
        // Setze barriere auf 2,0 im globalen Feld
        game.getBoard().get(2, 0).setBarrier(new Barrier(2, 0));

        simulateDiceRoll(2);

        // Laufen Auf die Barriere drauf
        FigureMoveResult result = movement.moveFigure(game, request(2, 0));

        assertTrue(result.success);
        assertEquals(2, figure.getGridI());

        assertEquals(0, game.getCurrentMovementAmount());
    }

    @Test
    void testMoveLongDistance_StopsAtBarrier() {
        // Setze barriere auf 2,0 im globalen Feld
        game.getBoard().get(2, 0).setBarrier(new Barrier(2, 0));

        simulateDiceRoll(6);

        // Wir laufen gegen die Barriere
        FigureMoveResult result = movement.moveFigure(game, request(2, 0));

        // Stop davor auf 1,0
        assertTrue(result.success);
        assertEquals(1, figure.getGridI());

        // Nur eine Energie sollte für das laufen verwendet werden = 5 Restenergie für
        // Springen
        assertEquals(5, game.getCurrentMovementAmount());
    }
}