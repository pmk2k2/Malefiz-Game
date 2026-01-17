package de.hsrm.mi.swtpr.milefiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Nachrichtentyp;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Operation;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent.Aktion;
import de.hsrm.mi.swtpr.milefiz.model.Bewegung;
import de.hsrm.mi.swtpr.milefiz.model.Step;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.model.Direction;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveRequest;
import de.hsrm.mi.swtpr.milefiz.model.FigureMoveResult;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.model.duel.Duel;
import de.hsrm.mi.swtpr.milefiz.model.duel.QuizQuestion;
import de.hsrm.mi.swtpr.milefiz.service.BoardNavigationService.MoveType;

@Service
public class MovementLogicService {
    private Logger logger = LoggerFactory.getLogger(MovementLogicService.class);
    private QuizService quizService;
    // Damit man Zwischenstaende an alle aus dem Spiel senden kann
    private ApplicationEventPublisher publisher;
    private BoardNavigationService navService;

    public MovementLogicService(ApplicationEventPublisher publisher, BoardNavigationService navService,
            QuizService quizService) {
        this.publisher = publisher;
        this.navService = navService;
        this.quizService = quizService;
    }

    private void sendStepUpdate(
            String gameCode,
            String playerId,
            int totalSteps,
            int remainingSteps) {
        publisher.publishEvent(
                new FrontendNachrichtEvent(
                        Nachrichtentyp.INGAME,
                        Operation.STEP_UPDATE,
                        gameCode,
                        playerId,
                        new Step(totalSteps, remainingSteps)));
    }

    public FigureMoveResult moveFigure(Game game, String gameCode, FigureMoveRequest request) {
        // Energie des Spielers
        Player player = game.getPlayerById(request.playerId);
        DiceResult result = game.getDiceResultById(request.playerId);
        // SpielerId prüfen und vergleichen
        if (result == null) {
            return FigureMoveResult.fail("Du hast nicht gewürfelt oder bist kein gueltiger Spieler.");
        }

        // Hole die echte Zahl aus dem Backend-Speicher
        int allowedDistance = result.getValue();
        int gegangen = 0;

        // Prüfungen ob gewuerfelt wurde
        if (allowedDistance <= 0) {
            return FigureMoveResult.fail("Du musst erst würfeln!");
        }

        // Energie sammeln Logik
        if (request.collectEnergy) {
            player = game.getPlayerById(request.playerId);
            if (player == null)
                return FigureMoveResult.fail("Spieler nicht gefunden");

            // Rest wird gespeichert
            int laufEnergieRest = allowedDistance;
            // Maximale energie für die Partie gestgelegt
            int limit = game.getMaxCollectableEnergy();

            // Die Laufenergie wird zur Sprungenergie umgewandelt und dem Spieler
            // gutgeschrieben
            player.addEnergy(laufEnergieRest, limit);

            // die Laufenergie muss jetzt auf 0 gesetzt werden damnit der Spieler nicht mehr
            // laufen kann
            result.setValue(0);
            game.setRollForPlayer(request.playerId, 0);

            // Frontend informieren
            var energyEvent = new FrontendNachrichtEvent(
                    Nachrichtentyp.INGAME,
                    request.playerId,
                    Operation.ENERGY_UPDATED,
                    gameCode,
                    player.getName(),
                    player.getEnergy());
            publisher.publishEvent(energyEvent);
            return FigureMoveResult.ok();
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

        // Wenn Figur nicht AUF dem Feld steht
        if (!figure.isOnField()) {
            Field startFeld = null, startFeldState = null;
            List<String> playerNumber = game.getPlayerNumber();
            int playerIndex;
            // zunaechst Figur auf Startfeld setzen
            for (int i = 0; i < playerNumber.size(); i++) {
                if (playerNumber.get(i).equals(request.playerId)) {
                    startFeld = game.getBoard().getStartFieldByIndex(i);
                    playerIndex = i;
                    logger.info("Startfeld fuer Spielerindex {} gefunden an: {} {}", playerIndex, startFeld.getI(),
                            startFeld.getJ());
                    break;
                }
            }

            if (startFeld == null) {
                return FigureMoveResult.fail("Startfeld existiert nicht fuer Spieler!");
            }

            startFeldState = game.getBoard().get(startFeld.getI(), startFeld.getJ());
            // alle noetigen checks setzen
            if (startFeld.getType() == CellType.BLOCKED) { // sollte eigentlich nicht passieren
                return FigureMoveResult.fail("Figur kann auf kein gesperrtes Feld");
            }
            if (startFeld.getFigures().size() >= 2) {
                List<Figure> figuresOnField = startFeld.getFigures();
                // Check ob eigener Spieler
                if (!figuresOnField.get(0).getOwnerPlayerId().equals(figuresOnField.get(1).getOwnerPlayerId())) {
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

            logger.info("Figur {} wird auf Startfeld ({}, {}) gesetzt. CellType: {}",
                    figure.getId(), startFeld.getI(), startFeld.getJ(), startFeld.getType());

            // Bewegung und Request an Frontend senden
            // -1 als "startPos" der Bewegung, um startPosition ausserhalb des Feldes zu
            // vermitteln,
            // da Felder im Model nicht < 0 werden koennen
            Bewegung bew = new Bewegung(-1, -1, startFeld.getI(), startFeld.getJ(), Direction.NORTH, 0);
            var moveEvent = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode,
                    request.figureId, request.playerId, bew);
            publisher.publishEvent(moveEvent);
            var moveReqEvent = new IngameRequestEvent(Aktion.DIRECTION, request.playerId, request.figureId, gameCode);
            publisher.publishEvent(moveReqEvent);

            // Wenn Spieler die Krone kriegt
            if (currentField.getType() == CellType.GOAL) {
                game.setWinnerId(request.playerId);
                publisher.publishEvent(new FrontendNachrichtEvent(
                        Nachrichtentyp.INGAME,
                        request.playerId,
                        Operation.GAME_OVER,
                        gameCode,
                        null));
                logger.info("ENDDDDDDDDD");
            }

            return FigureMoveResult.ok();
        }

        Direction lastDir = Direction.valueOf(request.direction.toUpperCase());
        int stepsCount = 0;
        // Festhalten der Startpositionen (wichtig fuer Frontend-Animation)
        int startI = figure.getGridI();
        int startJ = figure.getGridJ();
        boolean barrierHit = false;

        do {
            boolean moveOver = false; // Zug beendet?
            logger.info("Loopanfang mit stepsCount {}, lastDir {}, allowedDistance {}", stepsCount, lastDir,
                    allowedDistance);
            int deltaI = 0, deltaJ = 0;

            // Gucken, ob Schritt moeglich ist
            switch (lastDir) {
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

            // Die Figur darf nicht außerhalb der Grenzen des Feldes begehen (nur in dem
            // programmierten Feld)
            if (destI < 0 || destI >= game.getBoard().getWidth()
                    || destJ < 0 || destJ >= game.getBoard().getHeight()) {
                return FigureMoveResult.fail("Bewegung ausserhalb Spielfelds ist nicht erlaubt");
            }

            Field destField = game.getBoard().get(destI, destJ);

            logger.info("Figur {} bewegt sich auf Feld ({}, {}) mit CellType: {}",
                    figure.getId(), destI, destJ, destField.getType());

            // Faelle wo Bewegung nicht moeglich ist
            // potentiell wird man hier als Spieler softlocked?
            // Energie speichern oder einfach neue Richtungsanfrage?
            if (destField.getType() == CellType.BLOCKED) {
                logger.info("Feld {} {} ist blockiert", destI, destJ);
                return FigureMoveResult.fail("Figur kann auf kein gesperrtes Feld");
            }

            // Wenn Feld eine Barriere hat, schauen ob man AUF dieser landen kann
            if (destField.hasBarrier()) {
                if (allowedDistance > 1) {
                    // Figur stoppen
                    // Energie speichern (und spaeter weiterleiten)
                    int remainingEnergy = allowedDistance;
                    // Energie hinzufügen anhand der voreingestellten Menge vom Host
                    player.addEnergy(remainingEnergy, game.getMaxCollectableEnergy());

                    publisher.publishEvent(new FrontendNachrichtEvent(
                            Nachrichtentyp.INGAME,
                            request.playerId,
                            Operation.ENERGY_UPDATED,
                            gameCode,
                            player.getName(),
                            player.getEnergy()));

                    allowedDistance = 0;
                    game.getDiceResultById(request.playerId).setValue(0);
                    sendStepUpdate(gameCode, request.playerId, gegangen, 0);
                    return FigureMoveResult.ok();
                } else if (allowedDistance == 1) {
                    // Auf Barriere gelandet: Spielstatus ändern und variable auf true setzen
                    destField.setBarrier(null);
                    destField.setType(CellType.PATH);
                    game.setState(GameState.BARRIER_PLACEMENT);
                    barrierHit = true;
                }
            }

            // Nur zwei Figuren können auf das gleiche Feld -> Dritter Spieler kann nicht
            if (destField.isDuelField()) {

                player = game.getPlayerById(request.playerId);
                int energy = player.getEnergy();
                // Kosten = Maximale Kapazität der Partie, um ein Duell zu überspringen
                int moveEnergy = game.getMaxCollectableEnergy();

                boolean hasEnergy = energy >= moveEnergy;

                if (!hasEnergy || allowedDistance <= 1) {
                    // Energie hinzufügen anhand der voreingestellten Menge vom Host
                    player.addEnergy(allowedDistance, game.getMaxCollectableEnergy());

                    publisher.publishEvent(new FrontendNachrichtEvent(
                            Nachrichtentyp.INGAME,
                            request.playerId,
                            Operation.ENERGY_UPDATED,
                            gameCode,
                            player.getName(),
                            player.getEnergy()));
                    // Energie wird auf 0 gesetzt da für das überspringen nötig ist
                    allowedDistance = 0;
                    result.setValue(0);
                    game.setRollForPlayer(request.playerId, 0);
                    sendStepUpdate(gameCode, request.playerId, gegangen, 0);
                    break;
                }

                if (hasEnergy && allowedDistance > 1) {

                    int landI = destI + deltaI;
                    int landJ = destJ + deltaJ;

                    if (landI < 0 || landI >= game.getBoard().getWidth() || landJ < 0
                            || landJ >= game.getBoard().getHeight()) {
                        allowedDistance = 0;
                        break;
                    }

                    Field landField = game.getBoard().get(landI, landJ);

                    if (landField.getType() == CellType.BLOCKED || landField.getFigures().size() >= 2) {
                        allowedDistance = 0;
                        break;
                    }

                    // Energie für den Sprung verbrauchen
                    // Sprung soll noch implementiert werden, aber der abzug der Energie ist schon
                    // hier implementiert
                    player.consumeEnergy(moveEnergy);

                    publisher.publishEvent(new FrontendNachrichtEvent(
                            Nachrichtentyp.INGAME,
                            request.playerId,
                            Operation.ENERGY_UPDATED,
                            gameCode,
                            player.getName(),
                            player.getEnergy()));

                    Field currentField = game.getBoard().get(figure.getGridI(), figure.getGridJ());
                    currentField.removeFigure(figure);

                    figure.setPosition(landI, landJ);
                    landField.addFigure(figure);

                    // Schritt ausgeben
                    allowedDistance--;
                    result.setValue(allowedDistance);
                    game.getDiceResultById(request.playerId).setValue(allowedDistance);

                    stepsCount++;
                    gegangen++;
                    sendStepUpdate(gameCode, request.playerId, gegangen, allowedDistance);
                    continue;
                }
            }

            // Alles ok
            // setzen der Figur auf neues Feld
            logger.info("Setze Spielfigur auf neue Position {} {} mit CellType: {}", destI, destJ, destField.getType());
            Field currentField = game.getBoard().get(figure.getGridI(), figure.getGridJ());
            currentField.removeFigure(figure);
            figure.setPosition(destI, destJ);
            destField.addFigure(figure);

            // Prüfe ob Ziel erreicht
            if (destField.getType() == CellType.GOAL) {
                if (allowedDistance == 1) {
                    game.setWinnerId(request.playerId);
                    publisher.publishEvent(new FrontendNachrichtEvent(
                            Nachrichtentyp.INGAME,
                            request.playerId,
                            Operation.GAME_OVER,
                            gameCode,
                            null));
                    Bewegung bew = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir,
                            stepsCount);
                    var moveEv = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode,
                            request.figureId,
                            request.playerId, bew);
                    publisher.publishEvent(moveEv);
                    logger.info("ENDDDDDDDDD");
                    return FigureMoveResult.ok();
                } else {
                    return FigureMoveResult
                            .fail("Du kannst das Ziel nur mit genau der gewürfelten Schrittzahl erreichen.");
                }
            }

            // gelaufenen Schritt abziehen
            result.setValue(--allowedDistance);
            game.getDiceResultById(request.playerId).setValue(allowedDistance);

            // Anzahl Schritte und Richtung fuer Event anpassen
            stepsCount++;
            gegangen++;
            sendStepUpdate(gameCode, request.playerId, gegangen, allowedDistance);

            if (barrierHit)
                break;

            // Wenn Zug vorbei, direkt aus der Loop ausbrechen
            if (moveOver)
                break;

            // Gucken, ob Anfrage noetig
            // Ausgelagert in BoardNavigationService
            MoveType moveType = navService.classifyField(game, figure);
            logger.info("Neues Feld ist {}", moveType);
            if (result.getValue() <= 0)
                break;
            switch (moveType) {
                case DEADEND:
                    // restliche Energie speichern eigentlich
                    int remainingEnergy = allowedDistance;

                    player.addEnergy(remainingEnergy, game.getMaxCollectableEnergy());

                    publisher.publishEvent(new FrontendNachrichtEvent(
                            Nachrichtentyp.INGAME,
                            request.playerId,
                            Operation.ENERGY_UPDATED,
                            gameCode,
                            player.getName(),
                            player.getEnergy()));

                    allowedDistance = 0;
                    game.getDiceResultById(request.playerId).setValue(0);
                    sendStepUpdate(gameCode, request.playerId, gegangen, 0);

                    break;
                // return FigureMoveResult.ok();

                case T_CROSSING:
                case CROSSING:
                    Direction forbiddenDir = navService.getForbiddenDirection(lastDir);
                    Bewegung bew = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir,
                            stepsCount);
                    var moveEv = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode,
                            request.figureId, request.playerId, bew);
                    publisher.publishEvent(moveEv);
                    var event = new IngameRequestEvent(Aktion.DIRECTION, request.playerId, request.figureId, gameCode,
                            forbiddenDir);
                    publisher.publishEvent(event);
                    stepsCount = 0;
                    return FigureMoveResult.ok();

                case CURVE_WS:
                case CURVE_ES:
                case CURVE_WN:
                case CURVE_EN:
                    // Figur im Frontend bis zur Kurve bewegen
                    Bewegung bew2 = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir,
                            stepsCount);
                    var moveEv2 = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode,
                            request.figureId, request.playerId, bew2);
                    publisher.publishEvent(moveEv2);

                    // Drehen in richtige Richtung, startPos anpassen fuer naechste Animation
                    stepsCount = 0;
                    lastDir = navService.getNewDirection(moveType, lastDir);
                    startI = figure.getGridI();
                    startJ = figure.getGridJ();
                    logger.info("Neue Richtung: {}", lastDir);
                    break;

                case STRAIGHT_NS:
                case STRAIGHT_WE:
                    logger.info("Laufe gerade");
                default:
                    break;
            }

        } while (result.getValue() > 0);

        logger.info("Alle Zuege verbraucht");

        // wenn alle Zuege verbraucht sind
        Bewegung bew = new Bewegung(startI, startJ, figure.getGridI(), figure.getGridJ(), lastDir, stepsCount);
        var moveEv = new FrontendNachrichtEvent(Nachrichtentyp.INGAME, Operation.MOVE, gameCode, request.figureId,
                request.playerId, bew);
        publisher.publishEvent(moveEv);
        stepsCount = 0;

        // Spieler beendet seine Bewegung anhang vom Wuerfel
        game.finishMovement(request.playerId);

        Field field = game.getBoard().get(figure.getGridI(), figure.getGridJ());

        // Event auslösen
        if (field.isDuelField()) {

            if (game.getActiveDuel() != null) {
                return FigureMoveResult.ok();
            }

            List<Figure> figs = field.getFigures();

            if (figs.size() < 2) {
                return FigureMoveResult.ok();
            }

            String p1 = figs.get(0).getOwnerPlayerId();
            String p2 = figs.get(1).getOwnerPlayerId();

            if (game.isMovementFinished(p1) && game.isMovementFinished(p2)) {

                // Bewegung NUR für Übergabe von Koordinaten
                Bewegung duelBew = new Bewegung(
                        figure.getGridI(),
                        figure.getGridJ(),
                        figure.getGridI(),
                        figure.getGridJ(),
                        null,
                        0);

                FrontendNachrichtEvent duelEvent = new FrontendNachrichtEvent(
                        FrontendNachrichtEvent.Nachrichtentyp.INGAME,
                        FrontendNachrichtEvent.Operation.DUEL,
                        gameCode,
                        null,
                        p1, // Id des player1 im duell
                        duelBew);
                duelEvent.setOpponentId(p2);

                QuizQuestion q = quizService.getRandomQuestion();
                Duel duel = new Duel(gameCode, p1, p2, q);
                duel.resetForNewQuestion(q);

                game.setActiveDuel(duel);
                game.setState(GameState.DUEL);
                publisher.publishEvent(duelEvent);
            }
        }

        // Eine Nachricht schicken, falls eine Barriere getroffen wurde, damit der
        // Controller das automatische Öffnen des Maps triggern kann.
        return barrierHit ? FigureMoveResult.ok("BARRIER_HIT") : FigureMoveResult.ok();
    }
}