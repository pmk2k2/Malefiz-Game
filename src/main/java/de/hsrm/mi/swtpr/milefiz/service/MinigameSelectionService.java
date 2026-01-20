package de.hsrm.mi.swtpr.milefiz.service;

import java.util.List;
import java.util.Random;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.*;
import de.hsrm.mi.swtpr.milefiz.model.duel.Duel;
import de.hsrm.mi.swtpr.milefiz.model.duel.QuizQuestion;

@Service
public class MinigameSelectionService {

    private final ApplicationEventPublisher publisher;
    private final GameService gameService; 
    private final QuizService quizService; 
    private final ArithmetikService arithmetikService;
    private final Random random = new Random();
    
    private final List<String> games = List.of("QUIZ", "BUTTON_MASHING", "ARITHMETIC");

    public MinigameSelectionService(ApplicationEventPublisher publisher, GameService gameService, QuizService quizService, ArithmetikService arithmetikService) {
        this.publisher = publisher;
        this.gameService = gameService;
        this.quizService = quizService;
        this.arithmetikService = arithmetikService;
    }

    @EventListener
    public void onDuelPrepare(FrontendNachrichtEvent event) {
        if (event.getOperation() != Operation.DUEL_PREPARE) return;

        String selectedGame = games.get(random.nextInt(games.size()));
        System.out.println("Minigame ausgewählt: " + selectedGame);

        Game game = gameService.getGame(event.getGameCode());
        Duel duel = game.getActiveDuel();

        QuizQuestion generatedQuestion = null;

        if (duel != null) {
            
            if ("QUIZ".equals(selectedGame)) {
                generatedQuestion = quizService.getRandomQuestion();
                duel.nextQuestion(generatedQuestion);
            } else if ("ARITHMETIC".equals(selectedGame)) {
                generatedQuestion = arithmetikService.generateQuestion();
                duel.nextQuestion(generatedQuestion);
            }

        }

        //  Spielauswahl
        FrontendNachrichtEvent msg = new FrontendNachrichtEvent(
            Nachrichtentyp.INGAME, event.getId(), Operation.MINIGAME_SELECTED, event.getGameCode(), null
        );
        msg.setOpponentId(event.getOpponentId());
        msg.setMinigameType(selectedGame);
        publisher.publishEvent(msg);

        if (generatedQuestion != null) {
            FrontendNachrichtEvent questionEvent = new FrontendNachrichtEvent(
                Nachrichtentyp.INGAME, 
                null, 
                Operation.DUEL_NEW_QUESTION, 
                event.getGameCode(), 
                null
            );
            questionEvent.setQuizQuestion(generatedQuestion);
            publisher.publishEvent(questionEvent);
        }
    }
}