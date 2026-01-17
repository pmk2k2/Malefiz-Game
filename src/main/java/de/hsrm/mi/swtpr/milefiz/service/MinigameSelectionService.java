package de.hsrm.mi.swtpr.milefiz.service;

import java.util.List;
import java.util.Random;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.*;

@Service
public class MinigameSelectionService {

    private final ApplicationEventPublisher publisher;
    private final Random random = new Random();
    
    //DAS SIND DEMO Spiele, Hier sollen dann die Richtigen Mini-Spiele eingebunden werden
    private final List<String> games = List.of("Minispiel 1", "Minispiel 2", "Minispiel 3");

    public MinigameSelectionService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    //Fragt ab ob es ein Duell ist
    //Nimmt ein beliebiges game aus der Liste 
    //Schickt die Info an das Frontend
    @EventListener
    public void onDuelPrepare(FrontendNachrichtEvent event) {
        if (event.getOperation() != Operation.DUEL_PREPARE) return;

        String selectedGame = games.get(random.nextInt(games.size()));

        FrontendNachrichtEvent msg = new FrontendNachrichtEvent(
            Nachrichtentyp.INGAME, event.getId(), Operation.MINIGAME_SELECTED, event.getGameCode(), null
        );
        msg.setOpponentId(event.getOpponentId());
        msg.setMinigameType(selectedGame);

        publisher.publishEvent(msg);
    }
}