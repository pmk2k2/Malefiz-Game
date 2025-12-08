package de.hsrm.mi.swtpr.milefiz.service;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;

@Service
public class MovementLogicService {

    public FigureMoveResult moveFigure(Game game, FigureMoveRequest request) {

        // Hole die echte Zahl aus dem Backend-Speicher
        int allowedDistance = game.getCurrentMovementAmount();

        // Prüfungen ob gewuerfelt wurde
        if (allowedDistance <= 0) {
            return FigureMoveResult.fail("Du musst erst würfeln!");
        }
        // SpielerId prüfen und vergleichen
        if (!request.playerId.equals(game.getPlayerWhoRolledId())) {
            return FigureMoveResult.fail("Du bist nicht dran oder hast nicht gewürfelt.");
        }

        // Die Figur darf nicht außerhalb der Grenzen des Feldes begehen (nur in dem programmierten Feld)
        if (request.toI < 0 || request.toI >= game.getBoard().getWidth()
                || request.toJ < 0 || request.toJ >= game.getBoard().getHeight()) {
            return FigureMoveResult.fail("Bewegung außerhalb Spielfelds ist nicht erlaubt");
        }

        // Finden von Figur
        Figure figure = game.getFigures().stream()
                .filter(f -> f.getId().equals(request.figureId))
                .findFirst()
                .orElse(null);

        if (figure == null) {
            return FigureMoveResult.fail("Es gibt kein Figur oder sie wird nicht gefunden");
        }

        // Zuordnung vom Spieler und Figur
        if (!figure.getOwnerPlayerId().equals(request.playerId)) {
            return FigureMoveResult.fail("Die Figur gehört einem anderen Spieler");
        }

        // Zielfeld
        Field destinationField = game.getBoard().get(request.toI, request.toJ);

        // #1 und 2 Figur kann auf kein gesperrtes Feld (nur auf freie Felder)
        if (destinationField.getType() == CellType.BLOCKED) {
            return FigureMoveResult.fail("Figur kann auf kein gesperrtes Feld");
        }

        // #4 Maximal 2 Figuren pro Feld
        if (destinationField.getFigures().size() >= 2) {
            return FigureMoveResult.fail("Maximal 2 Figuren pro Feld");
        }


        int di = request.toI - figure.getGridI();
        int dj = request.toJ - figure.getGridJ();

        // Diatanz prüfen #48 Bewegung durch Würfel
        int walkedDistance = Math.abs(di) + Math.abs(dj);

        // Validierung der erlaubten Distanz
        String feldText = "Felder";
        if (walkedDistance != allowedDistance) {
            return FigureMoveResult.fail("Du musst genau " + allowedDistance + " " + feldText + " gehen.");
        }


        // Bewegung nur in einer Achse / Diagonal verboten
        if (Math.abs(di) > 0 && Math.abs(dj) > 0) {
            return FigureMoveResult.fail("Diagonal verboten");
        }

        // #3 Figur kann kein gesperrtes Feld überspringen
        if (Math.abs(di) > 1 || Math.abs(dj) > 1) {
            int midI = figure.getGridI() + Integer.signum(di);
            int midJ = figure.getGridJ() + Integer.signum(dj);

            Field midfField = game.getBoard().get(midI, midJ);

            if (midfField.getType() == CellType.BLOCKED) {
                return FigureMoveResult.fail("Überspringen von blockierten Felder ist verboten");
            }
        }

        // Ausführung der Bewegung
        Field currentField = game.getBoard().get(figure.getGridI(), figure.getGridJ());
        currentField.removeFigure(figure);

        figure.setPosition(request.toI, request.toJ);

        destinationField.addFigure(figure);


        // #4 Nur zwei Figuren können auf das gleiche Feld
        if (destinationField.getFigures().size() == 2) {
            // hier muss ein Duell gestartet werden
            return FigureMoveResult.ok();
        }

        game.setCurrentMovementAmount(0);
        game.setPlayerWhoRolledId(null);

        // Bei allen anderen Fällen
        return FigureMoveResult.ok();
    }
}
