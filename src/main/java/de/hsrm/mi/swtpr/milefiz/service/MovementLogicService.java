package de.hsrm.mi.swtpr.milefiz.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;

@Service
public class MovementLogicService {

    // alle begehbaren Nachbarfelder
    public Map<String, Field> getWalkableNeighbors(Game game, Figure figure) {

        Map<String, Field> result = new HashMap<>();

        int i = figure.getGridI();
        int j = figure.getGridJ();

        // Maske: links, rechts, oben, unten
        int[][] mask = {
            {-1, 0}, // links
            { 1, 0}, // rechts
            { 0,-1}, // oben (vorne)
            { 0, 1}  // unten (hinten)
        };

        String[] names = { "links", "rechts", "vorne", "hinten" };

        for (int k = 0; k < mask.length; k++) {
            int ni = i + mask[k][0];
            int nj = j + mask[k][1];

            // Prüfung: innerhalb des Spielfelds
            if (ni < 0 || nj < 0 || ni >= game.getBoard().getWidth() || nj >= game.getBoard().getHeight()) {
                continue;
            }

            Field f = game.getBoard().get(ni, nj);

            // BLOCKED ist nicht begehbar
            if (f.getType() == CellType.BLOCKED) {
                continue;
            }

            // Feld mit laufendem Duell (2 Figuren)
            if (f.getFigures().size() >= 2) {
                continue;
            }

            result.put(names[k], f);
        }

        return result;
    }

    // Ermittlung von Richtung, Kreuzung
    public String classifyField(Game game, Figure figure) {

        Map<String, Field> n = getWalkableNeighbors(game, figure);

        boolean left = n.containsKey("links");
        boolean right = n.containsKey("rechts");
        boolean forward = n.containsKey("vorne");
        boolean back = n.containsKey("hinten");

        int count = n.size();

        // Kreuzung wenn >= 3 Richtungen
        if (count >= 3) return "Kreuzung";

        // Gerade Strecke
        if (forward && back && !left && !right) return "Gerade";

        // Linkskurve
        if (left && back && !right && !forward) return "Linkskurve";

        // Rechtskurve
        if (right && back && !left && !forward) return "Rechtskurve";

        return "Sackgasse";
    }


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
        if (walkedDistance > allowedDistance) {
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


        Map<String, Field> newNeighbours = getWalkableNeighbors(game, figure);
        System.out.println("Begehbare: " + newNeighbours.keySet());

        String newType = classifyField(game, figure);
        System.out.println("Feldtyp: " + newType);


        // #4 Nur zwei Figuren können auf das gleiche Feld
        if (destinationField.getFigures().size() == 2) {
            // hier muss ein Duell gestartet werden
            return FigureMoveResult.ok();
        }

        // Speichert den überigen Wert des Würfels für die Sprung Energie in Zukunft 
        int remainingEnergy = allowedDistance - walkedDistance;
        game.setCurrentMovementAmount(remainingEnergy);
        if (remainingEnergy == 0) {
            game.setPlayerWhoRolledId(null);
        }

        // Bei allen anderen Fällen
        return FigureMoveResult.ok();
    }
}
