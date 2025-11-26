package de.hsrm.mi.swtpr.milefiz.service;

import de.hsrm.mi.swtpr.milefiz.entities.board.Field;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;

@Service
public class MovementLogicService {
    
    public FigureMoveResult moveFigure(Game game, FigureMoveRequest request) {
        
        // Finden von Figur
        Figure figure = game.getFigures().stream()
            .filter(f -> f.getId().equals(request.figureId))
            .findFirst()
            .orElse(null);

        if (figure == null) {
            return FigureMoveResult.fail("Es gibt kein Figur oder sie wird nicht gefunden");
        }

        // Zuordnung vom Spieler und Figur
        if (!figure.getPlayerId().equals(request.playerId)) {
            return FigureMoveResult.fail("Die Figur gehört einem anderen Spieler");
        }

        // Zielfeld
        Field destinationField = game.getBoard().get(request.toI, request.toJ);



        // #1 und 2 Figur kann auf kein gesperrtes Feld (nur auf freie Felder)
        if (destinationField.getType() == CellType.BLOCKED) {
            return FigureMoveResult.fail("Figur kann auf kein gesperrtes Feld");
        }

        // if (request.diceValue <= 0) {
        //     return FigureMoveResult.fail("Ungültiger Würfelwert");
        // }

        // #4 Nur zwei Figuren können auf das gleiche Feld
        if (destinationField.getFigures().size() >= 2) {
            return FigureMoveResult.fail("Nur zwei Figuren können auf das gleiche Feld");
        }


        // #3 Figur kann kein gesperrtes Feld überspringen
        int di = request.toI - figure.getI();
        int dj = request.toJ - figure.getJ();

        if (Math.abs(di) > 1 || Math.abs(dj) > 1) {
            int midI = figure.getI() + Integer.signum(di);
            int midJ = figure.getJ() + Integer.signum(dj);

            Field midfField = game.getBoard().get(midI, midJ);

            if (midfField.getType() == CellType.BLOCKED) {
                return FigureMoveResult.fail("Überspringen von blockierten Felder ist verboten");
            }
        }


        // Ausführung der Bewegung
        Field currentField = game.getBoard().get(figure.getI(), figure.getJ());
        currentField.removeFigure(figure);

        figure.setPosition(request.toI, request.toJ);

        destinationField.addFigure(figure);


        // #5 Bei der dritten Figur wird das Feld wie ein gesperrtes Feld gewertet
        if (destinationField.getFigures().size() == 3) {
            destinationField.setType(CellType.BLOCKED);
            return FigureMoveResult.fail("Dritte Figur blockiert das Feld");
        }

        // Bewegung nur in einer Achse
        if (Math.abs(di) > 0 && Math.abs(dj) > 0) {
            return FigureMoveResult.fail("Diagnol nicht erlaubt");
        }


        // Bei allen anderen Fällen
        return FigureMoveResult.ok();


    }

}
