package de.hsrm.mi.swtpr.milefiz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameMoveEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Nachrichtentyp;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Operation;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent.Aktion;
import de.hsrm.mi.swtpr.milefiz.model.Bewegung;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.model.Direction;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;

@Service
public class MovementLogicService {
    private Logger logger = LoggerFactory.getLogger(MovementLogicService.class);

    private enum MoveType {
        STRAIGHT_NS,
        STRAIGHT_WE,
        CURVE_WS,
        CURVE_ES,
        CURVE_WN,
        CURVE_EN,
        T_CROSSING,
        CROSSING,
        DEADEND
    };

    // Damit man Zwischenstaende an alle aus dem Spiel senden kann
    private ApplicationEventPublisher publisher;

    public MovementLogicService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    // alle begehbaren Nachbarfelder
    public Map<String, Field> getWalkableNeighbors(Game game, Figure figure) {

        Map<String, Field> result = new HashMap<>();

        int i = figure.getGridI();
        int j = figure.getGridJ();

        // Maske: west, ost, norden, sueden
        int[][] mask = {
            {-1, 0}, // west
            { 1, 0}, // ost
            { 0, 1}, // norden
            { 0,-1}  // sueden 
        };

        String[] names = { "west", "east", "north", "south" };

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

            result.put(names[k], f);
        }

        return result;
    }

    // Ermittlung von Richtung, Kreuzung
    public MoveType classifyField(Game game, Figure figure) {

        Map<String, Field> n = getWalkableNeighbors(game, figure);

        boolean west = n.containsKey("west");
        boolean east = n.containsKey("east");
        boolean north = n.containsKey("north");
        boolean south = n.containsKey("south");

        int count = n.size();

        // Kreuzung wenn = 4 Richtungen, T-Kreuzung bei 3 Richtungen
        if (count >= 4) return MoveType.CROSSING;
        if (count == 3) return MoveType.T_CROSSING;

        // Gerade Strecke
        boolean geradeNordSued = north && south && !west && !east;
        if(geradeNordSued) return MoveType.STRAIGHT_NS;

        boolean geradeWestOst  = !north && !south && west && east;
        if(geradeWestOst) return MoveType.STRAIGHT_WE;

        // Kurven
        boolean curveWestSouth = !north && south && west && !east;
        if(curveWestSouth)  return MoveType.CURVE_WS;
        boolean curveWestNorth = north && !south && west && !east;
        if(curveWestNorth)  return MoveType.CURVE_WN;
        boolean curveEastNorth = north && !south && !west && east;
        if(curveEastNorth)  return MoveType.CURVE_EN;
        boolean curveEastSouth = !north && south && !west && east;
        if(curveEastSouth)  return MoveType.CURVE_ES;

        // Ansonsten -> Sackgasse
        return MoveType.DEADEND;
    }

    // Statt zu gucken, welche Richtungen man darf, einfach schauen aus welcher man kommt (also nicht darf)
    // in dem Fall ist lastDir die gelaufene Richtung, also darf man nicht in die entgegengesetzte
    public Direction getForbiddenDirection(Direction lastDir) {
        Direction dir = null;

        switch(lastDir) {
            case Direction.NORTH:
                dir = Direction.SOUTH;
                break;
            case Direction.SOUTH:
                dir = Direction.NORTH;
                break;
            case Direction.EAST:
                dir = Direction.WEST;
                break;
            case Direction.WEST:
                dir = Direction.EAST;
                break;
        }

        return dir;
    }

    // Bei Kurven die richtige Richtung erhalten
    public Direction getNewDirection(MoveType mT, Direction dir) {
        Direction newDir;
        
        // Erst gucken nach Typ von Kurve, dann aus welcher Richtung man kommt
        switch(mT) {
            case MoveType.CURVE_WS:
                newDir = (dir == Direction.EAST) ? Direction.SOUTH : Direction.WEST;
                break;
            case MoveType.CURVE_ES:
                newDir = (dir == Direction.WEST) ? Direction.SOUTH : Direction.EAST;
                break;
            case MoveType.CURVE_WN:
                newDir = (dir == Direction.EAST) ? Direction.NORTH : Direction.WEST;
                break;
            default:
            case MoveType.CURVE_EN:
                newDir = (dir == Direction.WEST) ? Direction.NORTH : Direction.EAST;
                break;
        }

        return newDir;
    }


    public FigureMoveResult moveFigure(Game game, String gameCode, FigureMoveRequest request) {
        DiceResult result = game.getDiceResultById(request.playerId);
        // SpielerId prüfen und vergleichen
        if (result == null) {
            return FigureMoveResult.fail("Du hast nicht gewürfelt oder bist kein gueltiger Spieler.");
        }

        // Hole die echte Zahl aus dem Backend-Speicher
        int allowedDistance = result.getValue();
        //int allowedDistance = game.getCurrentMovementAmount();

        // Prüfungen ob gewuerfelt wurde
        if (allowedDistance <= 0) {
            return FigureMoveResult.fail("Du musst erst würfeln!");
        }

        // Die Figur darf nicht außerhalb der Grenzen des Feldes begehen (nur in dem programmierten Feld)
        /*
        if (request.toI < 0 || request.toI >= game.getBoard().getWidth()
                || request.toJ < 0 || request.toJ >= game.getBoard().getHeight()) {
            return FigureMoveResult.fail("Bewegung außerhalb Spielfelds ist nicht erlaubt");
        } */

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

        // Wenn Figur nicht AUF dem Feld steht
        if(!figure.isOnField()) {
            Field startFeld = null, startFeldState = null;            
            List<String> playerNumber = game.getPlayerNumber();
            int playerIndex;
            // zunaechst Figur auf Startfeld setzen    
            for(int i = 0; i < playerNumber.size(); i++) {
                if(playerNumber.get(i).equals(request.playerId)) {
                    startFeld = game.getBoard().getStartFieldByIndex(i);
                    playerIndex = i;
                    logger.info("Startfeld fuer Spielerindex {} gefunden an: {} {}", playerIndex, startFeld.getI(), startFeld.getJ());
                    break;
                }
            }

            if(startFeld == null) {
                return FigureMoveResult.fail("Startfeld existiert nicht fuer Spieler!");
            }

            startFeldState = game.getBoard().get(startFeld.getI(), startFeld.getJ());
            // alle noetigen checks setzen
            if(startFeld.getType() == CellType.BLOCKED) {   // sollte eigentlich nicht passieren
                return FigureMoveResult.fail("Figur kann auf kein gesperrtes Feld");
            }
            if(startFeld.getFigures().size() >= 2) {
                List<Figure>figuresOnField = startFeld.getFigures();
                // Check ob eigener Spieler
                if(!figuresOnField.get(0).getOwnerPlayerId().equals(figuresOnField.get(1).getOwnerPlayerId())) {
                    return FigureMoveResult.fail("Maximal 2 Figuren pro Feld");
                }
            }

            // wenn alles ok -> Figur auf Startfeld setzen, und erneut nach Richtung fragen
            // Zug nicht beenden und kein Wuerfelzahl abziehen !!!

            // setzen der Figur auf neues Feld im Model
            figure.setOnField(true);
            Field currentField = game.getBoard().get(figure.getGridI(), figure.getGridJ());
            currentField.removeFigure(figure);
            figure.setPosition(startFeld.getI(), startFeld.getJ());
            startFeldState.addFigure(figure);

            // Bewegung und Request an Frontend senden
            // -1 als "startPos" der Bewegung, um startPosition ausserhalb des Feldes zu vermitteln,
            // da Felder im Model nicht < 0 werden koennen
            Bewegung bew = new Bewegung(-1, -1, startFeld.getI(), startFeld.getJ(), Direction.NORTH, 0);
            var moveEvent = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode, request.figureId, request.playerId, bew);
            publisher.publishEvent(moveEvent);
            var moveReqEvent = new IngameRequestEvent(Aktion.DIRECTION, request.playerId, request.figureId, gameCode);
            publisher.publishEvent(moveReqEvent);


            return FigureMoveResult.ok();
        }

        Direction lastDir = Direction.valueOf(request.direction.toUpperCase());
        int stepsCount = 0;
        // Festhalten der Startpositionen (wichtig fuer Frontend-Animation)
        int startI = figure.getGridI();
        int startJ = figure.getGridJ();
        do {
            /*
            FrontendNachrichtEvent moveEv = new FrontendNachrichtEvent(gameCode, figure.getOwnerPlayerId(), figure.getId(), 0, null);
            IngameRequestEvent requestEv = new IngameRequestEvent(gameCode, figure.getOwnerPlayerId(), figure.getId(), 0, null);
            */
            logger.info("Loopanfang mit stepsCount {}, lastDir {}, allowedDistance {}", stepsCount, lastDir, allowedDistance);
            int deltaI = 0, deltaJ = 0;

            // Gucken, ob Schritt moeglich ist
            switch(lastDir) {
                case Direction.NORTH:
                    deltaJ = 1;
                    break;
                case Direction.SOUTH:
                    deltaJ = -1;
                    break;
                case Direction.WEST:
                    deltaI = -1;
                    break;
                case Direction.EAST:
                    deltaI = 1;
                    break;
            }
            int destI = figure.getGridI() + deltaI;
            int destJ = figure.getGridJ() + deltaJ;
            Field destField = game.getBoard().get(destI, destJ);
            // Faelle wo Bewegung nicht moeglich ist
            if(destField.getType() == CellType.BLOCKED) {
                logger.info("Feld {} {} ist blockiert", destI, destJ);
                return FigureMoveResult.fail("Figur kann auf kein gesperrtes Feld");
            }
            if (destField.getFigures().size() >= 2) {
                return FigureMoveResult.fail("Maximal 2 Figuren pro Feld");
            }

            // Alles ok
            // setzen der Figur auf neues Feld
            logger.info("Setze Spielfigur auf neue Position {} {}", destI, destJ);
            Field currentField = game.getBoard().get(figure.getGridI(), figure.getGridJ());
            currentField.removeFigure(figure);
            figure.setPosition(destI, destJ);
            destField.addFigure(figure);

            // gelaufenen Schritt abziehen
            result.setValue(--allowedDistance);
            game.getDiceResultById(request.playerId).setValue(allowedDistance);

            // Anzahl Schritte und Richtung fuer Event anpassen
            stepsCount++;

            // Nur zwei Figuren können auf das gleiche Feld -> Kampf einleiten (in Zukunft)
            if (destField.getFigures().size() == 2) {
                // hier muss ein Duell gestartet werden
                logger.info("Zwei Spieler auf {} {}, starte Duell...", destI, destJ);
                //return FigureMoveResult.ok();
                break;
            }

            // Gucken, ob Anfrage noetig
            MoveType moveType = classifyField(game, figure);
            logger.info("Neues Feld ist {}", moveType);
            if(result.getValue() <= 0) break;
            switch(moveType) {
                case MoveType.DEADEND:
                    // restliche Energie speichern eigentlich
                    allowedDistance = 0;
                    game.getDiceResultById(request.playerId).setValue(0);
                    //return FigureMoveResult.ok();

                case MoveType.T_CROSSING:
                case MoveType.CROSSING:
                    Direction forbiddenDir = getForbiddenDirection(lastDir);
                    Bewegung bew = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir, stepsCount);
                    var moveEv = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode, request.figureId, request.playerId, bew);
                    publisher.publishEvent(moveEv);
                    var event = new IngameRequestEvent(Aktion.DIRECTION, request.playerId, request.figureId, gameCode, forbiddenDir);
                    publisher.publishEvent(event);
                    stepsCount = 0;
                    return FigureMoveResult.ok();

                case MoveType.CURVE_WS:
                case MoveType.CURVE_ES:
                case MoveType.CURVE_WN:
                case MoveType.CURVE_EN:
                    // Figur im Frontend bis zur Kurve bewegen
                    Bewegung bew2 = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir, stepsCount);
                    var moveEv2 = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode, request.figureId, request.playerId, bew2);
                    publisher.publishEvent(moveEv2);

                    // Drehen in richtige Richtung, startPos anpassen fuer naechste Animation
                    stepsCount = 0;
                    lastDir = getNewDirection(moveType, lastDir);
                    startI = figure.getGridI();
                    startJ = figure.getGridJ();
                    logger.info("Neue Richtung: {}", lastDir);
                    break;

                case MoveType.STRAIGHT_NS:
                case MoveType.STRAIGHT_WE:
                    logger.info("Laufe gerade");
                default:
                    break;
            }
            
        } while(result.getValue() > 0);

        logger.info("Alle Zuege verbraucht");

        // wenn alle Zuege verbraucht sind
        Bewegung bew = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir, stepsCount);
        var moveEv = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode, request.figureId, request.playerId, bew);
        publisher.publishEvent(moveEv);
        stepsCount = 0;

        return FigureMoveResult.ok();
    }
}
