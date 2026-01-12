package de.hsrm.mi.swtpr.milefiz.controller;

import java.time.Instant;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import de.hsrm.mi.swtpr.milefiz.controller.dto.DuelAnswerRequest;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.GameState;
import de.hsrm.mi.swtpr.milefiz.model.duel.DuelAnswer;
import de.hsrm.mi.swtpr.milefiz.model.duel.QuizQuestion;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import de.hsrm.mi.swtpr.milefiz.service.QuizService;
import de.hsrm.mi.swtpr.milefiz.model.duel.Duel;


@RestController
@RequestMapping("/api/duel")
public class DuelController {

    @Autowired
    private GameService gameService;

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

        evaluateDuel(game, duel);
    }

    private void evaluateDuel(Game game, Duel duel) {

        if (duel.getAnswers().isEmpty()) {
            if (isTimeout(duel)) {
                QuizQuestion newQ = quizService.getRandomQuestion();
                duel.resetForNewQuestion(newQ);
                publishNewQuestion(game, duel);
            }
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

        resetLoserFigure(game, loser);
        publishDuelResult(game, winner, loser);
    }


    private void resetLoserFigure(Game game, String loserId) {
        game.getFigures().stream()
            .filter(f -> f.getOwnerPlayerId().equals(loserId))
            .forEach(f -> f.setOnField(false));
    }

    private void publishDuelResult(Game game, String winner, String loser) {
        // zeitliche Schablone
    }

    private void publishNewQuestion(Game game, Duel duel) {
        // TODO: STOMP-Event DUEL_NEW_QUESTION
    }


    private boolean isTimeout(Duel duel) {
        return Duration.between(
            duel.getQuestionStartedAt(),
            Instant.now()
        ).getSeconds() >= 10;
    }




}

