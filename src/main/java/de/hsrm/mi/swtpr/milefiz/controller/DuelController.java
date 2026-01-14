package de.hsrm.mi.swtpr.milefiz.controller;

import java.time.Instant;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import de.hsrm.mi.swtpr.milefiz.controller.dto.DuelAnswerRequest;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.entities.player.Player;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.model.Bewegung;
import de.hsrm.mi.swtpr.milefiz.model.Direction;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.model.duel.DuelAnswer;
import de.hsrm.mi.swtpr.milefiz.model.duel.QuizQuestion;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import de.hsrm.mi.swtpr.milefiz.service.QuizService;
import de.hsrm.mi.swtpr.milefiz.model.duel.Duel;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;


@RestController
@RequestMapping("/api/duel")
public class DuelController {

    @Autowired
    private GameService gameService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private QuizService quizService;

    @PostMapping("/answer")
    public void answer(@RequestBody DuelAnswerRequest req) {

        Game game = gameService.getGame(req.gameCode());
        Duel duel = game.getActiveDuel();

        if (duel == null || duel.isFinished()) return;

        duel.getAnswers().put(
            req.playerId(),
            new DuelAnswer(
                req.playerId(),
                req.answerIndex(),
                Instant.now()
            )
        );

        evaluateDuel(game, duel, req.gameCode());
    }

    private void evaluateDuel(Game game, Duel duel, String gameCode) {

        if (duel.isFinished()) return;

        if (isTimeout(duel)) {

        if (duel.getAnswers().isEmpty()) {
            QuizQuestion newQ = quizService.getRandomQuestion();
            duel.nextQuestion(newQ);
            publishNewQuestion(gameCode, newQ);

            return;
        }

        String loser = duel.getAnswers().keySet().iterator().next();
        String winner = duel.getOpponent(loser);

        duel.finish();
        game.setActiveDuel(null);
        game.setState(GameState.RUNNING);

        resetLoserFigure(game, loser, gameCode);
        publishDuelResult(gameCode, winner, loser);
        return;
    }



        QuizQuestion q = duel.getQuestion();

        DuelAnswer first = duel.getAnswers()
            .values()
            .stream()
            .min(Comparator.comparing(DuelAnswer::getTimestamp))
            .get();

        boolean correct = first.getAnswerIndex() == q.getCorrectIndex();

        String winner = correct
            ? first.getPlayerId()
            : duel.getOpponent(first.getPlayerId());

        String loser = duel.getOpponent(winner);

        duel.finish();
        game.setActiveDuel(null);
        game.setState(GameState.RUNNING);

        resetLoserFigure(game, loser, gameCode);

        publishDuelResult(gameCode, winner, loser);
    }



    private void resetLoserFigure(Game game, String loserId, String gameCode){
        game.getFigures().stream()
            .filter(f -> f.getOwnerPlayerId().equals(loserId))
            .forEach(f -> {

                int fromI = f.getGridI();
                int fromJ = f.getGridJ();

                int baseI = 0;
                int baseJ = 0;

                f.setGridI(baseI);
                f.setGridJ(baseJ);

                Bewegung bew = new Bewegung(
                    fromI,
                    fromJ,
                    baseI,
                    baseJ,
                    Direction.SOUTH,
                    0
                );

                FrontendNachrichtEvent moveEvent =
                    new FrontendNachrichtEvent(
                        FrontendNachrichtEvent.Nachrichtentyp.INGAME,
                        FrontendNachrichtEvent.Operation.MOVE,
                        gameCode,
                        f.getId(),
                        loserId,
                        bew
                    );

                publisher.publishEvent(moveEvent);
            });
    }
    private void resetLoserFigure2(Game game, String loserId, String gameCode, Duel duel) {

    game.getFigures().stream()
        .filter(f -> f.getId().equals(loserId))
        .findFirst()
        .ifPresent(f -> {

            int fromI = f.getGridI();
            int fromJ = f.getGridJ();

            // Startfeld holen
            Player player = game.getPlayerById(f.getOwnerPlayerId());
            int startIndex = game.getPlayerNumber().indexOf(player.getId());
            Field startField = game.getBoard().getStartFieldByIndex(startIndex);

            // Board korrekt aktualisieren
            game.getBoard().get(fromI, fromJ).removeFigure(f);
            startField.addFigure(f);

            // Figur auf Startposition setzen
            f.setPosition(startField.getI(), startField.getJ());

            // Figur **ist jetzt auf dem Feld**
            f.setOnField(true);

            // Figur in Start-Richtung zurücksetzen
            f.resetOrientation();

            // Bewegung ans Frontend senden
            Bewegung bew = new Bewegung(
                fromI, fromJ,
                startField.getI(), startField.getJ(),
                Direction.valueOf(f.getOrientation().toUpperCase()),
                0
            );

            publisher.publishEvent(new FrontendNachrichtEvent(
                FrontendNachrichtEvent.Nachrichtentyp.INGAME,
                FrontendNachrichtEvent.Operation.MOVE,
                gameCode,
                f.getId(),
                loserId,
                bew
            ));
        });
}




    private void publishDuelResult(String gameCode, String winner, String loser) {
        FrontendNachrichtEvent event = new FrontendNachrichtEvent(
            FrontendNachrichtEvent.Nachrichtentyp.INGAME,
            winner,
            FrontendNachrichtEvent.Operation.DUEL_RESULT,
            gameCode,
            null
        );
        event.setOpponentId(loser);
        publisher.publishEvent(event);
    }



    private void publishNewQuestion(String gameCode, QuizQuestion q) {
        FrontendNachrichtEvent event = new FrontendNachrichtEvent(
            FrontendNachrichtEvent.Nachrichtentyp.INGAME,
            null,
            FrontendNachrichtEvent.Operation.DUEL_NEW_QUESTION,
            gameCode,
            null
        );
        event.setQuizQuestion(q); 
        publisher.publishEvent(event);
    }


    @GetMapping("/start")
    public void startDuel(@RequestParam String gameCode) {
        Game game = gameService.getGame(gameCode);
        Duel duel = game.getActiveDuel();

        if (duel == null) return;

        publishNewQuestion(gameCode, duel.getQuestion());
    }






    private boolean isTimeout(Duel duel) {
        return Duration.between(
            duel.getQuestionStartedAt(),
            Instant.now()
        ).getSeconds() >= 10;
    }




}

